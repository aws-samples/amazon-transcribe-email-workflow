package transcribe;

import com.amazonaws.regions.Regions;

public final class Constants {
	
	public static final Regions awsSeerviceRegion = Regions.US_EAST_1;
	public static final String bucketName = "transcribe-email";
	public static final String dynamoDBTable = "Transcribe";
	public static final String senderEmail = "a verified email in SES that will be used to send the response back to the users";

}
