import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {

	private Session session;
	private Folder inbox;

	public void createSession(String user, String pass) {
		System.out.println("POP3 Session erstellen");

		final Properties props = new Properties();

		props.setProperty( "mail.pop3.host", "pop.gmail.com" );
		props.setProperty( "mail.pop3.user", user );
		props.setProperty( "mail.pop3.password", pass );
		props.setProperty( "mail.pop3.port", "995" );
		props.setProperty( "mail.pop3.auth", "true" );
		props.setProperty( "mail.pop3.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory" );

		props.setProperty( "mail.smtp.host", "smtp.gmail.com" );
		props.setProperty( "mail.smtp.auth", "true" );
		props.setProperty( "mail.smtp.port", "465" );
		props.setProperty( "mail.smtp.socketFactory.port", "465" );
		props.setProperty( "mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory" );
		props.setProperty( "mail.smtp.socketFactory.fallback", "false" );

		session = Session.getInstance( props, new javax.mail.Authenticator() {
			@Override protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication( props.getProperty( "mail.pop3.user" ),
						props.getProperty( "mail.pop3.password" ) );
			}
		} );
		System.out.println("POP3 Session erstellt");
	}

	public String[] getMessage() throws MessagingException, IOException {

		System.out.println("Nachrichten abrufen");

		String [] message = new String[4];
		message[0] = "false";

		if(inbox.getMessageCount() >= 1) {
			
			System.out.println("Neue Nachrichten vorhanden");

			for ( Message m : inbox.getMessages() ) {

				if ( m.isMimeType( "text/plain" ) ) {
					/*System.out.println( "Nachricht ist text/plain" );

				System.out.println( "\nNachricht:" );
				System.out.println( "Von: " + Arrays.toString(m.getFrom()) );
				System.out.println( "Betreff: " + m.getSubject() );
				System.out.println( "Gesendet am: " + m.getSentDate() );
				System.out.println( "Content-Type: " + new ContentType( m.getContentType() ) );*/

					System.out.println("Ung�ltige Nachricht, Mail l�schen");
					m.setFlag(Flags.Flag.DELETED, true);

				} else if ( m.isMimeType( "multipart/*" ) ) {

					Multipart mp = (Multipart) m.getContent();
					String subject = m.getSubject();
					String[] arr = subject.split(" ");

					if(arr[0].equalsIgnoreCase("Sitzungseinladung")) {
						System.out.println("Neue Sitzungseinladung");

						message[0] = "true";
						message[2] = m.getSubject();

						String date = arr[1];
						Date dt = new java.util.Date();
						SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

						try {
							dt = sdf.parse(arr[1]);
						} catch (ParseException e) {
							System.out.println("Parse Exception");
							e.printStackTrace();
						}
						sdf = new SimpleDateFormat("yyyy-MM-dd");
						String fromatedDate = sdf.format(dt);

						int sitzungsID = Datenbank.createNewSitzung(fromatedDate);
						message[3] = Integer.toString(sitzungsID);

						for ( int j = 1; j < mp.getCount(); j++ ) {
							Part part = mp.getBodyPart( j );
							String disp = part.getDisposition();

							if ( disp == null || disp.equalsIgnoreCase( Part.ATTACHMENT ) ) {
								MimeBodyPart mimePart = (MimeBodyPart) part;

								//System.out.println("MIME-Typ ist " + mimePart.getContentType());

								if (mimePart.isMimeType("text/html")); {
									message[1] = mimePart.getContent().toString();
									//System.out.println(message[1]);
								}
							}
						} 
					} else {
						System.out.println("Keine Sitzungseinladung, Mail l�schen");
						m.setFlag(Flags.Flag.DELETED, true);
					}
				}
			}
			if(message[0] == "true") {
				System.out.println("Neue Sitzungseinladung");
			} else {
				System.out.println("Keine neue Sitzungseinladung");
			}
		} else {
			System.out.println("Keine neuen Nachrichten");
		}
		return message;
	}

	public void openInbox() throws MessagingException {
		System.out.println("Inbox �ffnen");
		Store store = session.getStore( "pop3" );
		store.connect();

		Folder folder = store.getFolder( "INBOX" );
		folder.open( Folder.READ_WRITE);

		inbox = folder;
		System.out.println("Inbox ge�ffnet");
	}

	public void closeInbox() throws MessagingException {
		System.out.println("POP3 Verbindung schlie�en");
		inbox.close(true);
		inbox.getStore().close();
		System.out.println("POP3 Verbindung geschlossen");
	}

	public void sendEmail(String recipient, String subject, String txtMsg, String htmlMsg ) throws MessagingException {

		MimeMultipart content = new MimeMultipart( "alternative" );

		MimeBodyPart text = new MimeBodyPart();
		text.setContent( txtMsg, "text/text" );
		content.addBodyPart( text );

		MimeBodyPart html = new MimeBodyPart();
		html.setContent( htmlMsg, "text/html" );
		content.addBodyPart( html );

		Message msg = new MimeMessage( session );

		InternetAddress addressTo = new InternetAddress( recipient );
		msg.setRecipient( Message.RecipientType.TO, addressTo );

		msg.setSubject( subject );
		msg.setContent( content );
		Transport.send( msg );
	}

	public void sendEmailHtml(String recipient, String subject, String htmlMsg ) throws MessagingException {

		MimeMultipart content = new MimeMultipart( "alternative" );

		MimeBodyPart html = new MimeBodyPart();
		html.setContent( htmlMsg, "text/html" );
		content.addBodyPart( html );

		Message msg = new MimeMessage( session );

		InternetAddress addressTo = new InternetAddress( recipient );
		msg.setRecipient( Message.RecipientType.TO, addressTo );

		msg.setSubject( subject );
		msg.setContent( content );
		Transport.send( msg );
	}
}
