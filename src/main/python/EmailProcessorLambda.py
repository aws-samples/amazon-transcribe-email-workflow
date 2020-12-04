import boto3
import email
import os
from botocore.exceptions import ClientError

def lambda_handler(event, context):

	S3BucketName='bucket_name'
	DynamoRegion='region'#us-east-1
	
    transcribe_client = boto3.client('transcribe')
    
    workmail = boto3.client('workmailmessageflow')
    msg_id = event['messageId']
    raw_msg = workmail.get_raw_message_content(messageId=msg_id)
    mail = email.message_from_bytes(raw_msg['messageContent'].read())
    fromAdd = mail['from']
    #print('fromAdd : '+fromAdd)

    for part in mail.walk():
        if part.get_content_maintype() == 'multipart':
            #print part.as_string()
            continue
        if part.get('Content-Disposition') is None:
            #print part.as_string()
            continue
        fileName = part.get_filename()
        if bool(fileName):
            filePath = os.path.join(".", 'attachments', fileName)
            if not os.path.isfile(filePath) :
                print("file : " + fileName)
                #fp = open(filePath, 'wb')
                #fp.write(part.get_payload(decode=True))
                #fp.close()
                extension = fileName[fileName.rindex("."):]
                print("extension : " + extension)
                newKey = msg_id+""+extension
                s3 = boto3.client('s3')
                s3.create_bucket(Bucket=S3BucketName)
                s3.put_object(Body=part.get_payload(decode=True), Bucket=S3BucketName, Key=newKey)
   
                put_item(fileName, fromAdd, newKey)
                
                #get_item(newKey)
   
                #s3.put_object_acl(Bucket="sanjay-transcribe", Key=fileName, ACL='public-read')
                #url = s3.generate_presigned_url('get_object',
                #                                    Params={'Bucket': 'sanjay-transcribe-new',
                #                                            'Key': fileName},
                #                                    ExpiresIn=120)
                #                                    
                #print("pre-signed URL : " + url)          
                #url = "s3://sanjay-transcribe-new/2020-10-23T00_56_55.642Z.wav"
                #transcribe_file('email-job', url, transcribe_client)
    #parsed_msg = email.message_from_bytes(raw_msg['messageContent'].read())
    #print(parsed_msg)

def put_item(fileName, email, msgId):
    dynamodb = boto3.resource('dynamodb', region_name=DynamoRegion)
    table = dynamodb.Table('Transcribes')
    response = table.put_item(
       Item={
            'FILE_NAME': fileName,
            'EMAIL': email,
            'MSG_ID': msgId
        }
    )
    return response
 
def get_item(msgId):
    dynamodb = boto3.resource('dynamodb', region_name=DynamoRegion)
    table = dynamodb.Table('Transcribes')
    try:
        response = table.get_item(Key={'MSG_ID': msgId})
    except ClientError as e:
        print(e.response['Error']['Message'])
    else:
        print(response['Item'])
        return response['Item']    
    
def transcribe_file(job_name, file_uri, transcribe_client):
    transcribe_client.start_transcription_job(
        TranscriptionJobName=job_name,
        Media={'MediaFileUri': file_uri},
        MediaFormat='wav',
        LanguageCode='en-US',
        OutputBucketName=S3BucketName
    )

    max_tries = 60
    while max_tries > 0:
        max_tries -= 1
        job = transcribe_client.get_transcription_job(TranscriptionJobName=job_name)
        job_status = job['TranscriptionJob']['TranscriptionJobStatus']
        if job_status in ['COMPLETED', 'FAILED']:
            print(f"Job {job_name} is {job_status}.")
            if job_status == 'COMPLETED':
                print(
                    f"Download the transcript from\n"
                    f"\t{job['TranscriptionJob']['Transcript']['TranscriptFileUri']}.")
            break
        else:
            print(f"Waiting for {job_name}. Current status is {job_status}.")
        time.sleep(10)    
