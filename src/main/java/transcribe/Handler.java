package transcribe;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handler implements RequestHandler<S3Event, String> {
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final Logger logger = LoggerFactory.getLogger(Handler.class);


	@Override
	public String handleRequest(S3Event s3event, Context context) {
		try {
			System.out.println("EVENT: " + gson.toJson(s3event));
			S3EventNotificationRecord record = s3event.getRecords().get(0);

			String srcBucket = record.getS3().getBucket().getName();

			// Object key may have spaces or unicode non-ASCII characters.
			String srcKey = record.getS3().getObject().getUrlDecodedKey();

			//String dstBucket = srcBucket;
			//String dstKey = "resized-" + srcKey;
			System.out.println("FileName : " + srcKey);

			// Download the image from S3 into a stream
			/*
			AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
			S3Object s3Object = s3Client.getObject(new GetObjectRequest(
					srcBucket, srcKey));
			InputStream objectData = s3Object.getObjectContent();
			 */

			invokeTranscribeService(srcKey);

			// Uploading to S3 destination bucket
			/*
			System.out.println("Writing to: " + dstBucket + "/" + dstKey);
			try {
				s3Client.putObject(dstBucket, dstKey, is, meta);
			}
			catch(AmazonServiceException e)
			{
				logger.error(e.getErrorMessage());
				System.exit(1);
			}
			 */
			System.out.println("Successfully processed...");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return "OK";
	}

	private void invokeTranscribeService(String fileName) {
		String response = "";
		TranscriptionTextDTO transcriptionTextDTO = new TranscriptionTextDTO();
		System.out.println("invokeTranscribeService()...");
		System.out.println("File Name"+fileName);
		Transcribe transcribeObj = new Transcribe();
		TranscriptionResponseDTO transcribeDTO = transcribeObj.extractSpeechTextFromAudio(fileName);
		System.out.println("Job Name :"+transcribeDTO.getJobName());
		List<TranscriptionTextDTO> list = transcribeDTO.getResults().getTranscripts();
		String outputFile = fileName.substring(0, fileName.lastIndexOf("."))+".txt";
		for(int cnt = 0; cnt < list.size(); cnt++) {
			TranscriptionTextDTO dto = list.get(cnt);
			response = dto.getTranscript();
			System.out.println("Text :"+dto.getTranscript());
			transcribeObj.uploadResponseFileToAwsBucket(outputFile,dto.getTranscript());
		}
		transcriptionTextDTO.setTranscript(response);

		sendEmail(fileName, response);
	}

	private void sendEmail(String fileName, String response) {
		System.out.println("sendEmail() called :: fileName : "+fileName);
		DataService dataService = new DataService();
		EmailItem item = dataService.getById(fileName);
		System.out.println("sendEmail() :: email : "+item.getEmail());
		String origFileName = item.getFileName();
		System.out.println("origFileName : "+origFileName);

		String newFileName = "/tmp/"+origFileName.substring(0,origFileName.lastIndexOf("."))+".txt";
		System.out.println("newFileName : "+newFileName);
		File file = new File(newFileName);
		try {
			FileUtils.writeStringToFile(file, response);
		}catch(Exception e) {
			e.printStackTrace();
		}

		String subject = "Transcription of ["+origFileName+"]";

		EmailService emailService = new EmailService();
		try {
			emailService.send(item.getEmail(), subject, file);
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				Thread.sleep(1000);
				file.delete();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

	}
}