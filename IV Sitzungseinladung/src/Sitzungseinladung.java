import java.io.IOException;
import java.util.ArrayList;

import javax.mail.MessagingException;

public class Sitzungseinladung {
	
	static String [] message = new String[2];
	static Email email = new Email();
	static String link = "http://192.168.0.2/confirmation.php?userID=";
	
	public static void main( String[] args ) throws MessagingException, IOException {
		openConnection();
				
		boolean newInvitation = getMessages();
		
		if(newInvitation) {
			processEmail();
			closeConnection();
		} else {
			closeConnection();
		}
		System.out.println("Beendet");
		System.exit(0);
	}
	
	public static void openConnection() throws MessagingException, IOException {
		email.createSession("ivcsitzungseinladung@gmail.com", "DPwiNe!G2.8.1996!" );
		email.openInbox();
	}
	
	public static boolean getMessages() throws MessagingException, IOException {
		message = email.getMessage();
		
		if(message[0] == "true") {
			return true;
		} else {
			return false;
		}
	}
	
	public static void closeConnection() throws MessagingException {
		email.closeInbox();
		Datenbank.closeConnection();
	}
	
	@SuppressWarnings("unchecked")
	public static void processEmail() throws MessagingException {
		System.out.println("Sitzungseinladung verarbeiten");
		
		String htmlTemplate = message[1];
		int sitzungsID = Integer.parseInt(message[3]);
		
		ArrayList<ArrayList> list = Datenbank.getAddressAndCode();
		ArrayList<String> addresses = list.get(0);
		ArrayList<Integer> codes = list.get(1);
		
		for(int i = 0; i < addresses.size(); i++) {
			String html = htmlTemplate;
			int code = codes.get(i);
	
			html = html.replaceAll("/--/", "<a href=" + link + code + "?confirmation=1?sitzungsID=" + sitzungsID + "><div style='background-color:green'>Ich komme</div></a>" 
									+ "<a href=" + link + code + "?confirmation=0?sitzungsID=" + sitzungsID + "><div style='background-color:red'>Ich komme nicht</div></a>");
			
			email.sendEmailHtml(addresses.get(i), message[2], html);

			System.out.println("Email " + (int)(i + 1) + " von " + addresses.size() + " gesendet");
		}
		System.out.println("Senden abgeschlossen");
	}
}
