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
	private static String dbHost = "127.0.0.1";

	// Port -- Standard: 3306
	private static String dbPort = "3306";

	// Datenbankname
	private static String database = "ivsitzungen";

	// Datenbankuser
	private static String dbUser = "root";

	// Datenbankpasswort
	private static String dbPassword = "test";

	private Datenbank() {
		try {
			// Datenbanktreiber für ODBC Schnittstellen laden.
			// Für verschiedene ODBC-Datenbanken muss dieser Treiber
			// nur einmal geladen werden.
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("MySQL JDBC Driver Registered!");

			// Verbindung zur ODBC-Datenbank 'sakila' herstellen.
			// Es wird die JDBC-ODBC-Brücke verwendet.
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "tobias", "test");
			//conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
			//		+ dbPort + "/" + database + "?" + "user=" + dbUser + "&"
			//		+ "password=" + dbPassword);
		} catch (ClassNotFoundException e) {
			System.out.println("Treiber nicht gefunden");
			try {
				Sitzungseinladung.closeConnection();
			} catch (MessagingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.exit(0);
		} catch (SQLException e) {
			System.out.println("Connect nicht moeglich");
			try {
				Sitzungseinladung.closeConnection();
			} catch (MessagingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.exit(0);
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
		conn = getInstance();

		if(conn != null)
		{
			// Anfrage-Statement erzeugen.
			Statement query;
			try {
				query = conn.createStatement();

				// Ergebnistabelle erzeugen und abholen.
				String sql = "SELECT email, code FROM user";
				ResultSet result = query.executeQuery(sql);
				result.getFetchSize();

				// Ergebnissätze durchfahren.
				ArrayList<String> addresses = new ArrayList<String>();
				ArrayList<Integer> codes = new ArrayList<Integer>();
				
				while (result.next()) {
					addresses.add(result.getString("email"));  // Alternativ: result.getString(1);
					codes.add(result.getInt("code")); // Alternativ: result.getString(2);
					System.out.println(addresses.get(addresses.size()-1) + " " + codes.get(codes.size()-1));
				}
				
				ArrayList<ArrayList> list = new ArrayList<ArrayList>();
				list.add(addresses);
				list.add(codes);
								
				return list;
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	/**
	 * Fügt einen neuen Datensatz hinzu 
	 */
	public static void insertName(String firstName, String lastName)
	{
		conn = getInstance();

		if(conn != null)
		{
			try {

				// Insert-Statement erzeugen (Fragezeichen werden später ersetzt).
				String sql = "INSERT INTO actor(first_name, last_name) " +
						"VALUES(?, ?)";
				PreparedStatement preparedStatement = conn.prepareStatement(sql);
				// Erstes Fragezeichen durch "firstName" Parameter ersetzen
				preparedStatement.setString(1, firstName);
				// Zweites Fragezeichen durch "lastName" Parameter ersetzen
				preparedStatement.setString(2, lastName);
				// SQL ausführen.
				preparedStatement.executeUpdate();

				// Es wird der letzte Datensatz abgefragt
				String lastActor = "SELECT actor_id, first_name, last_name " +
						"FROM actor " +
						"ORDER BY actor_id DESC LIMIT 1";
				ResultSet result = preparedStatement.executeQuery(lastActor);

				// Wenn ein Datensatz gefunden wurde, wird auf diesen zugegriffen 
				if(result.next())
				{
					System.out.println("(" + result.getInt(1) + ")" + 
							result.getString(2) + " " + 
							result.getString(3));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Aktualisiert den Datensatz mit der übergebenen actorId 
	 */
	public static void updateName(String firstName, String lastName, int actorId)
	{
		conn = getInstance();

		if(conn != null)
		{
			try {

				String querySql = "SELECT actor_id, first_name, last_name " +
						"FROM actor " +
						"WHERE actor_id = ?";

				// PreparedStatement erzeugen.
				PreparedStatement preparedQueryStatement = conn.prepareStatement(querySql);
				preparedQueryStatement.setInt(1, actorId);
				ResultSet result = preparedQueryStatement.executeQuery();

				if(result.next())
				{
					// Vorher
					System.out.println("VORHER: (" + result.getInt(1) + ")" + 
							result.getString(2) + " " + 
							result.getString(3));
				}

				// Ergebnistabelle erzeugen und abholen.
				String updateSql = "UPDATE actor " +
						"SET first_name = ?, last_name = ? " +
						"WHERE actor_id = ?";
				PreparedStatement preparedUpdateStatement = conn.prepareStatement(updateSql);
				// Erstes Fragezeichen durch "firstName" Parameter ersetzen
				preparedUpdateStatement.setString(1, firstName);
				// Zweites Fragezeichen durch "lastName" Parameter ersetzen
				preparedUpdateStatement.setString(2, lastName);
				// Drittes Fragezeichen durch "actorId" Parameter ersetzen
				preparedUpdateStatement.setInt(3, actorId);
				// SQL ausführen
				preparedUpdateStatement.executeUpdate();

				// Es wird der letzte Datensatz abgefragt
				result = preparedQueryStatement.executeQuery();

				if(result.next())
				{
					System.out.println("NACHHER: (" + result.getInt(1) + ")" + 
							result.getString(2) + " " + 
							result.getString(3));
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}

