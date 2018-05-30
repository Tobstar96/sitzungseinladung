import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.mail.MessagingException;

public class Datenbank {


	private static Connection conn = null;

	// Hostname
	private static String dbHost = "192.168.0.2";

	// Port -- Standard: 3306
	private static String dbPort = "3306";

	// Datenbankname
	private static String database = "ivsitzungen";

	// Datenbankuser
	private static String dbUser = "tobi";

	// Datenbankpasswort
	private static String dbPassword = "!iha2.8.g11";

	private Datenbank() {
		try {
			System.out.println("Datenbank Treiber registrieren");
			
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Treiber registriert, DB verbinden");			
			conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&"+ "password=" + dbPassword + "&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
			System.out.println("DB verbunden");
		} catch (SQLException e) {
			System.out.println("Connect nicht moeglich");
			System.out.println(e.getErrorCode());
			System.out.println(e.getMessage());
			System.exit(0);
		} catch (ClassNotFoundException e) {
			System.out.println("Treiber nicht geladen");
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Connection getInstance()
	{
		if(conn == null)
			new Datenbank();
		return conn;
	}

	/**
	 * Schreibt die Namensliste in die Konsole
	 */
	public static ArrayList<ArrayList> getAddressAndCode()
	{
		System.out.println("Emailadressen und Codes aus DB abrufen");
		conn = getInstance();

		if(conn != null)
		{
			// Anfrage-Statement erzeugen.
			Statement query;
			try {
				query = conn.createStatement();

				String sql = "SELECT Email, Code FROM user";
				ResultSet result = query.executeQuery(sql);
				result.getFetchSize();

				ArrayList<String> addresses = new ArrayList<String>();
				ArrayList<Integer> codes = new ArrayList<Integer>();
				
				while (result.next()) {
					addresses.add(result.getString("Email"));  // Alternativ: result.getString(1);
					codes.add(result.getInt("Code")); // Alternativ: result.getString(2);
				}
				
				ArrayList<ArrayList> list = new ArrayList<ArrayList>();
				list.add(addresses);
				list.add(codes);

				System.out.println("Emailadressen und Codes abgerufen");
								
				return list;
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	public static int createNewSitzung(String date) {
		System.out.println("Neue Sitzung eintragen, mit DB verbinden");
		
		conn = getInstance();

		if(conn != null)
		{
			try {
				System.out.println("DB verbunden");
				// Insert-Statement erzeugen (Fragezeichen werden später ersetzt).
				String sql = "INSERT INTO sitzungen(Datum) " +
						"VALUES(?)";
				PreparedStatement preparedStatement = conn.prepareStatement(sql);
				// Erstes Fragezeichen durch "firstName" Parameter ersetzen
				// Zweites Fragezeichen durch "lastName" Parameter ersetzen
				preparedStatement.setString(1, date);
				// SQL ausführen.
				if(preparedStatement.executeUpdate() == 1) {
					System.out.println("Sitzung erfolgreich eingetragen");
				} else {
					System.out.println("Fehler beim Eintragen der Sitzung");
				}
				
				Statement query = conn.createStatement();

				sql = "SELECT ID FROM sitzungen";
				ResultSet result = query.executeQuery(sql);
				result.getFetchSize();
				
				int sitzungsID = 0;
				
				while (result.next()) {
					sitzungsID = result.getInt("ID");
				}
				
				System.out.println("SitzungsID: " + sitzungsID);
				
				return sitzungsID;

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	
	public static void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

