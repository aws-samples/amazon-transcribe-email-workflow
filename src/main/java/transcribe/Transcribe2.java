package transcribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClient;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;
import com.amazonaws.services.transcribe.model.DeleteTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.LanguageCode;
import com.amazonaws.services.transcribe.model.Media;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.TranscriptionJob;
import com.amazonaws.services.transcribe.model.TranscriptionJobStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Transcribe2{

	//@Autowired
	//private Environment env;

	private String bucketName = Constants.bucketName;
	private Regions regions = Constants.awsSeerviceRegion;

	public AmazonTranscribe transcribeClient() {
		System.out.println("Intialize Transcribe Client");
		AmazonTranscribe amazonTranscribe = AmazonTranscribeClientBuilder
				.standard()
				.withRegion(regions)
				.build();

		return amazonTranscribe;
	}


	public AmazonS3 s3Client() {
		System.out.println("Intialize AWS S3 Client");
		AmazonS3 s3client = AmazonS3ClientBuilder
				.standard()
				.withRegion(regions)
				.build();

		return s3client;
	}

	public void deleteFileFromAwsBucket(String fileName) {
		System.out.println("Delete File from AWS Bucket "+fileName);
		String key = fileName.replaceAll(" ", "_").toLowerCase();
		//String newKey = key + "-" + System.currentTimeMillis();
		//s3Client().copyObject(bucketName, key, bucketName, newKey);
		try {
			Thread.sleep(1000);
			s3Client().deleteObject(bucketName, key);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void testFileFromAwsBucket(String fileName) {
		System.out.println("Delete File from AWS Bucket "+fileName);
		String key = fileName.replaceAll(" ", "_").toLowerCase();
		//String newKey = key + "-" + System.currentTimeMillis();
		//s3Client().copyObject(bucketName, key, bucketName, newKey);
		S3Object s3object = null;
		S3ObjectInputStream inputStream = null;
		try {
			Thread.sleep(1000);
			s3object = s3Client().getObject(bucketName, key);
			inputStream = s3object.getObjectContent();
			File file = new File("/tmp/test2.wav");
			if (file.createNewFile()) {
				System.out.println("File created: " + file.getName());
			}
			//FileUtils.copyInputStreamToFile(inputStream, file);
			//System.out.println("Object Size : "+inputStream.read());
			PutObjectRequest put1 = new PutObjectRequest("sanjay-textract",
					"test.wav",
					file);
			s3Client().putObject(put1.withCannedAcl(CannedAccessControlList.BucketOwnerFullControl));

			CopyObjectRequest copy1 = new CopyObjectRequest("sanjay-transcribe","test.wav","sanjay-textract","test2.wav");
			s3Client().copyObject(copy1.withCannedAccessControlList(CannedAccessControlList.BucketOwnerFullControl));


		}catch(Exception e) {
			e.printStackTrace();
		}finally{
/*
			try{
				if(s3object != null){
					s3object.close();
				}
				if(inputStream != null){
					inputStream.close();
				}
			}catch(Exception resExec){
				resExec.printStackTrace();
			}
*/
		}
	}

	public StartTranscriptionJobResult startTranscriptionJob(String key) {
		System.out.println("Start Transcription Job By Key Before : "+key);
		Media media = new Media().withMediaFileUri(s3Client().getUrl(bucketName, key).toExternalForm());
		media.setMediaFileUri("s3://"+bucketName+"/"+key);
		System.out.println("startTranscriptionJob :: media created... "+media.getMediaFileUri());

		key = key.substring(key.indexOf("/")+1);
		System.out.println("Start Transcription Job By Key After : "+key);

		int length = 10;
		boolean useLetters = true;
		boolean useNumbers = false;
		String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
		System.out.println("startTranscriptionJob :: bucketName "+bucketName);
		String jobName = key.concat(generatedString);
		System.out.println("jobName : "+jobName);

		StartTranscriptionJobRequest startTranscriptionJobRequest = new StartTranscriptionJobRequest()
				.withLanguageCode(LanguageCode.EnUS).withTranscriptionJobName(jobName).withMedia(media);
		System.out.println("Job request started...");
		StartTranscriptionJobResult startTranscriptionJobResult = transcribeClient()
				.startTranscriptionJob(startTranscriptionJobRequest);
		System.out.println("Job result...");

		return startTranscriptionJobResult;
	}


	public GetTranscriptionJobResult getTranscriptionJobResult(String jobName) {
		System.out.println("Get Transcription Job Result By Job Name : "+jobName);
		GetTranscriptionJobRequest getTranscriptionJobRequest = new GetTranscriptionJobRequest()
				.withTranscriptionJobName(jobName);
		Boolean resultFound = false;
		TranscriptionJob transcriptionJob = new TranscriptionJob();
		GetTranscriptionJobResult getTranscriptionJobResult = new GetTranscriptionJobResult();
		while (resultFound == false) {
			getTranscriptionJobResult = transcribeClient().getTranscriptionJob(getTranscriptionJobRequest);
			transcriptionJob = getTranscriptionJobResult.getTranscriptionJob();
			if (transcriptionJob.getTranscriptionJobStatus()
					.equalsIgnoreCase(TranscriptionJobStatus.COMPLETED.name())) {
				return getTranscriptionJobResult;
			} else if (transcriptionJob.getTranscriptionJobStatus()
					.equalsIgnoreCase(TranscriptionJobStatus.FAILED.name())) {
				return null;
			} else if (transcriptionJob.getTranscriptionJobStatus()
					.equalsIgnoreCase(TranscriptionJobStatus.IN_PROGRESS.name())) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("Interrupted Exception {}"+e.getMessage());
				}
			}
		}
		return getTranscriptionJobResult;
	}

	public TranscriptionResponseDTO downloadTranscriptionResponse(String uri){
		System.out.println("Download Transcription Result from Transcribe URi {}"+uri);
		OkHttpClient okHttpClient = new OkHttpClient()
				.newBuilder()
				.connectTimeout(60, TimeUnit.SECONDS)
				.writeTimeout(60, TimeUnit.SECONDS)
				.readTimeout(60, TimeUnit.SECONDS)
				.build();
		Request request = new Request.Builder().url(uri).build();
		Response response;
		try {
			response = okHttpClient.newCall(request).execute();
			String body = response.body().string();
			ObjectMapper objectMapper = new ObjectMapper();
			response.close();

			return objectMapper.readValue(body, TranscriptionResponseDTO.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void deleteTranscriptionJob(String jobName) {
		System.out.println("Delete Transcription Job from amazon Transcribe : "+jobName);
		DeleteTranscriptionJobRequest deleteTranscriptionJobRequest = new DeleteTranscriptionJobRequest()
				.withTranscriptionJobName(jobName);
		transcribeClient().deleteTranscriptionJob(deleteTranscriptionJobRequest);
	}

	public TranscriptionResponseDTO extractSpeechTextFromAudio(String fileName) {
		System.out.println("Request to extract Speech Text from Audio : "+fileName);

		// Create a key that is like name for file and will be used for creating unique name based id for transcription job
		String key = fileName.replaceAll(" ", "_").toLowerCase();

		// Start Transcription Job and get result
		//System.out.println("1...");
		StartTranscriptionJobResult startTranscriptionJobResult = startTranscriptionJob(key);


		// Get name of job started for the file
		//System.out.println("2...");
		String transcriptionJobName = startTranscriptionJobResult.getTranscriptionJob().getTranscriptionJobName();

		// Get result after the procesiing is complete
		//System.out.println("3...");
		GetTranscriptionJobResult getTranscriptionJobResult = getTranscriptionJobResult(transcriptionJobName);

		//delete file as processing is done
		//System.out.println("4...");
		deleteFileFromAwsBucket(key);

		// Url of result file for transcription
		//System.out.println("5...");
		String transcriptFileUriString = getTranscriptionJobResult.getTranscriptionJob().getTranscript().getTranscriptFileUri();

		// Get the transcription response by downloading the file
		//System.out.println("6...");
		TranscriptionResponseDTO transcriptionResponseDTO = downloadTranscriptionResponse(transcriptFileUriString);

		//Delete the transcription job after finishing or it will get deleted after 90 days automatically if you do not call
		deleteTranscriptionJob(transcriptionJobName);

		return transcriptionResponseDTO;
	}

	public void uploadResponseFileToAwsBucket(String newKey, String data) {
		System.out.println("uploadResponseFileToAwsBucket -> key : "+newKey);

		try {
			s3Client().putObject(bucketName, newKey, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
