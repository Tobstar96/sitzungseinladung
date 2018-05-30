import java.io.IOException;
import java.util.ArrayList;

import javax.mail.MessagingException;

public class Sitzungseinladung {
	
	static String [] message = new String[2];
	static Email email = new Email();
	static String link = "http://localhost/confirmation.php?code=";
	
	public static void main( String[] args ) throws MessagingException, IOException {
		System.out.println("POP3 Verbinden");
		openConnection();
		System.out.println("POP3 Verbunden, Nachrichten abrufen");
				
		boolean newInvitation = getMessages();
		
		if(newInvitation) {
			System.out.println("Neue Einladung erhalten, Email verarbeiten");
			processEmail();
			System.out.println("Verarbeitung abgeschlossen, alle Emails versendet, POP3 Verbindung schlieﬂen");
			closeConnection();
		} else {
			System.out.println("Keine neue Einladung, POP3 Verbindung schlieﬂen");
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
		Datenbank.closeConnection();
		System.out.println("POP3 und DB Verbindung geschlossen");
	}
	
	@SuppressWarnings("unchecked")
	public static void processEmail() throws MessagingException {
		String htmlTemplate = message[1];
		int sitzungsID = Integer.parseInt(message[3]);
		
		ArrayList<ArrayList> list = Datenbank.getAddressAndCode();
		ArrayList<String> addresses = list.get(0);
		ArrayList<Integer> codes = list.get(1);
		
		for(int i = 0; i < addresses.size(); i++) {
			String html = htmlTemplate;
			int code = codes.get(i);
	
			html = html.replaceAll("--//--", "<a href=" + link + code + "?confirmation=1?sitzungsid=" + sitzungsID + "><div style='background-color:green'>Ich komme</div></a>" 
									+ "<a href=" + link + code + "?confirmation=0?sitzungsid=" + sitzungsID + "><div style='background-color:red'>Ich komme nicht</div></a>");
			
			email.sendEmailHtml(addresses.get(i), message[2], html);

			System.out.println("Email " + (int)(i + 1) + " von " + addresses.size() + " gesendet");
		}
		
		
	}
}
