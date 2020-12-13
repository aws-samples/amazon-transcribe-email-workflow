package transcribe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Properties;

// JavaMail libraries. Download the JavaMail API 
// from https://javaee.github.io/javamail/
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
// AWS SDK libraries. Download the AWS SDK for Java 
// from https://aws.amazon.com/sdk-for-java
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

public class EmailService {

	private String bucketName = Constants.bucketName;
	private String SENDER = Constants.senderEmail;
	private Regions regions = Constants.awsSeerviceRegion;

	// The email body for recipients with non-HTML email clients.
	private static String BODY_TEXT = "Hello,\r\n"
			+ "Please see the attached transcribed file.";

	// The HTML body of the email.
	private static String BODY_HTML = "<html>"
			+ "<head></head>"
			+ "<body>"
			+ "<h1>Hello!</h1>"
			+ "<p>Please see the attached transcribed file.</p>"
			+ "</body>"
			+ "</html>";

	public void send(String To, String SUBJECT, File fileName) throws AddressException, MessagingException, IOException {

		System.out.println("In EmailService -> Send function()...");

		Session session = Session.getDefaultInstance(new Properties());

		MimeMessage message = new MimeMessage(session);

		message.setSubject(SUBJECT, "UTF-8");
		message.setFrom(new InternetAddress(SENDER));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(To));
		//message.setContent(BODY, "text/plain; charset=UTF-8");
		
		MimeMultipart msg_body = new MimeMultipart("alternative");
        
        // Create a wrapper for the HTML and text parts.        
        MimeBodyPart wrap = new MimeBodyPart();
        
        // Define the text part.
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(BODY_TEXT, "text/plain; charset=UTF-8");
                
        // Define the HTML part.
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(BODY_HTML,"text/html; charset=UTF-8");
                
        // Add the text and HTML parts to the child container.
        msg_body.addBodyPart(textPart);
        msg_body.addBodyPart(htmlPart);
        
        // Add the child container to the wrapper object.
        wrap.setContent(msg_body);
        
        // Create a multipart/mixed parent container.
        MimeMultipart msg = new MimeMultipart("mixed");
        
        // Add the parent container to the message.
        message.setContent(msg);
        
        // Add the multipart/alternative part to the message.
        msg.addBodyPart(wrap);
        
        // Define the attachment
        MimeBodyPart att = new MimeBodyPart();
        DataSource fds = new FileDataSource(fileName);
        att.setDataHandler(new DataHandler(fds));
        att.setFileName(fds.getName());
        
        // Add the attachment to the message.
        msg.addBodyPart(att);
		
		try {
			System.out.println("Attempting to send an email through Amazon SES "
					+"using the AWS SDK for Java...");

			AmazonSimpleEmailService client = 
					AmazonSimpleEmailServiceClientBuilder
					.standard()
					.withRegion(regions)
					.build();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			message.writeTo(outputStream);
			RawMessage rawMessage = 
					new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = 
					new SendRawEmailRequest(rawMessage);

			client.sendRawEmail(rawEmailRequest);
			System.out.println("Email sent!");
		} catch (Exception ex) {
			System.out.println("Email Failed");
			System.err.println("Error message: " + ex.getMessage());
			//processFailedEmail(To, SUBJECT, BODY);
			ex.printStackTrace();
		}
	}

	public void processFailedEmail(String TO,String SUBJECT,String BODY) {
		try {
			String[] values = TO.split(",");
			for(int cnt = 0; cnt < values.length; cnt++) {
				if(!(values[cnt].trim().equals(""))) {
					try {
						//sendRetry(values[cnt], SUBJECT, BODY);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}


}
