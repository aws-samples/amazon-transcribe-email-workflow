package transcribe;

import com.amazonaws.regions.Regions;

public final class Constants {
	
	public static final Regions awsSeerviceRegion = Regions.US_EAST_1;
	public static final String bucketName = "";
	public static final String dynamoDBTable = "Transcribe";
	public static final String senderEmail = "email_of_SES_account_sending_emails_back_to_users";

}
