import boto3
import email
import os
from botocore.exceptions import ClientError

S3BucketName='transcribe-email'
DynamoRegion='us-east-1'
DynamoTableName='Transcribe'

def lambda_handler(event, context):
	
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
                extension = fileName[fileName.rindex("."):]
                print("extension : " + extension)
                if (extension.lower() == '.wav' or extension.lower() == '.m4a' or extension.lower() == '.mp3' or extension.lower() == '.mp4'):
                	newKey = "audio/"+msg_id+""+extension
                	s3 = boto3.client('s3')
                	s3.create_bucket(Bucket=S3BucketName)
                	s3.put_object(Body=part.get_payload(decode=True), Bucket=S3BucketName, Key=newKey)
   
                	put_item(fileName, fromAdd, newKey)
 
def put_item(fileName, email, msgId):
    dynamodb = boto3.resource('dynamodb', region_name=DynamoRegion)
    table = dynamodb.Table(DynamoTableName)
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
    table = dynamodb.Table(DynamoTableName)
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
