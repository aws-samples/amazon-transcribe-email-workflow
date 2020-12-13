package transcribe;


import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;

public class DataService {

	private String dynamoDBTable = Constants.dynamoDBTable;

	public EmailItem getById(String key) {
		System.out.println("DataService Get function called...");
		EmailItem emailItem = new EmailItem();
		try {
			Util util = new Util();
			DynamoDB dynamoDB = util.getDynamoDB();
			Table table = dynamoDB.getTable(dynamoDBTable);
			PrimaryKey pk = new PrimaryKey();
			pk.addComponent("MSG_ID", key);
			Item item = table.getItem(pk);
			emailItem.setEmail((String)item.get("EMAIL"));
			emailItem.setMsgId((String)item.get("MSG_ID"));
			emailItem.setFileName((String)item.get("FILE_NAME"));
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("DataService Get function completed...");
		return emailItem;
	}

}
