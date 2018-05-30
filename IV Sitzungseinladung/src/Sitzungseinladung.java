import java.io.IOException;
import java.util.ArrayList;

import javax.mail.MessagingException;

public class Sitzungseinladung {
	
	static String [] message = new String[2];
	static Email email = new Email();
	static String link = "http://localhost/confirmation.php?code=";
	
	public static void main( String[] args ) throws MessagingException, IOException {
		openConnection();
		
		boolean newInvitation = getMessages();
		if(newInvitation) {
			processEmail();
		} else {
			closeConnection();
		}
	}
	
	public static void openConnection() throws MessagingException, IOException {
		email.createSession("ivcsitzungseinladung@gmail.com", "DPwiNe!G2.8.1996!" );
		email.openInbox();
	}
	
	public static boolean getMessages() throws MessagingException, IOException {
		message = email.getMessage();
		
		if(message[0] != null || message [1] != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void closeConnection() throws MessagingException {
		email.closeInbox();
	}
	
	public static void processEmail() throws MessagingException {
		String plainTemplate = message[0];
		String htmlTemplate = message[1];
		
		ArrayList<ArrayList> list = Datenbank.getAddressAndCode();
		ArrayList<String> addresses = list.get(0);
		ArrayList<Integer> codes = list.get(1);
		
		for(int i = 0; i < addresses.size(); i++) {
			String plain = plainTemplate;
			String html = htmlTemplate;
			int code = codes.get(i);
			html.replace("--//--", "<a href=" + link + code + "?confirmation=1>Ich komme</a></br>" 
									+ "<a href=" + link + code + "?confirmation=0>Ich komme nicht</a>");
			
			email.sendEmailHtml(addresses.get(i), message[2], html);
		}
		
		
	}
}
