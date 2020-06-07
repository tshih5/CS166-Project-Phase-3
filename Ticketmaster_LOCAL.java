/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static String ReadUserInput(){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String userInput = "";
		try { // read the string, parse it and break.
			userInput = reader.readLine();
		}catch (Exception e) {
			System.out.println("Your input is invalid!");
		}//end try
		return userInput;
	}

	public static void AddUser(Ticketmaster esql){//1
		
		String fname = "";
		String lname = "";
		String email = "";
		String phone = "";
		String pwd = "";
		String query = "";
		int number_rows_returned = 0;

		System.out.print("Please enter first name: ");
		fname = ReadUserInput().trim();
		System.out.println("First name is: " + fname);

		System.out.print("Please enter last name: ");
		lname = ReadUserInput().trim();
		System.out.println("Last name is: " + lname);

		System.out.print("Please enter user email: ");
		email = ReadUserInput().trim();
		System.out.println("email is: " + email);

		System.out.print("Please enter phone number: ");
		phone = ReadUserInput().trim();
		System.out.println("phone number is: " + phone);

		System.out.print("Please enter user password: ");
		pwd = ReadUserInput().trim();
		System.out.println("Password is: " + pwd);

		query = "SELECT * FROM Users WHERE email = \'" + email + "\'";

		try { // read the string, parse it and break.
			number_rows_returned = esql.executeQueryAndPrintResult(query);
		}catch (SQLException e) {
			System.out.println("We did an oopsie on our end. Please try again later.");
			return;
		}

		if(number_rows_returned > 0){
			System.out.println("A user with email " + email + " has already been registered. Please try again");
			return;
		}else{
			//no user with email found proceed to insert.
			query = "INSERT INTO Users (email, lname, fname, phone, pwd) VALUES (\'" + email + "\', \'" + lname + "\', \'" + fname + "\', \'" + phone + "\', \'" + pwd + "\')";
			try {
				esql.executeUpdate(query);
				System.out.println(fname + " " + lname + " has been successfully added. Have a nice day :)");
			}catch (SQLException e) {
				System.out.println("We did an oopsie on our end. Please try again later.");
			}
		}
	}
	
	public static void AddBooking(Ticketmaster esql){//2
		String bid = "";
		String status = "";
		String bdatetime = "";
    	String seats = "";
    	String sid = "";
    	String email = "";
		String[] queries = new String[3];
		String insert_query  = "";
		int number_rows_returned = 0;
		int errors = 0;

		/*
		query = "SELECT * FROM Bookings";
		try { // read the string, parse it and break.
			bid = Integer.toString(1 + esql.executeQueryAndPrintResult(query));
		}catch (SQLException e) {
			System.out.println("We did an oopsie on our end. Please try again later.");
		}
		*/

		System.out.print("Please enter the booking ID: ");
		bid = ReadUserInput().trim();
		System.out.println("bid is: " + bid);

		System.out.print("Please enter status: (Paid, Cancelled, Pending): ");
		status = ReadUserInput().trim();
		System.out.println("Status is: " + status);

		System.out.print("Please enter date/ and time in mm/dd/yy hh:mm:ss AM/PM format: ");
		bdatetime = ReadUserInput().trim();

		System.out.print("Please enter number of seats booked: ");
		seats = ReadUserInput().trim();
		System.out.println("# of seats: " + seats);

		System.out.print("Please enter show ID: ");
		sid = ReadUserInput().trim();
		System.out.println("Show ID is : " + sid);

		System.out.print("Please enter booker email: ");
		email = ReadUserInput().trim();
		System.out.println("Email is: " + email);

		
		queries[0] = "SELECT * FROM Bookings WHERE bid = " + bid;
		queries[1] = "SELECT * FROM Shows WHERE sid = " + sid;
		queries[2] = "SELECT * FROM Users WHERE email = \'" + email + "\'";

		for(int i = 0; i < 3; ++i){
			try { //check if bid, sid, or email exists
				number_rows_returned = esql.executeQueryAndPrintResult(queries[i]);
			}catch (SQLException e) {
				System.out.println("We did an oopsie on our end. Please try again later.");
				return;
			}

			switch(i){
				case 0:{
					if(number_rows_returned > 0){
						System.out.println("Error: Booking id " + bid + " already exists!");
						errors++;
					}
					break;
				}
				case 1:	{
					if(number_rows_returned == 0){
						System.out.println("Error: Show with sid " + sid + " does not exist!");
						errors++;
					}
					break;
				}
				case 2:{
					if(number_rows_returned == 0){
						System.out.println("Error: User with email " + email + " does not exist!");
						errors++;
					}
					break;
				}
			}
		}

		if(errors > 0){
			System.out.println("Please fix all errors and try again");
			return;
		}else{
			insert_query = "INSERT INTO Bookings (bid, status, bdatetime, seats, sid, email) VALUES (\'" + bid + "\', \'" + status + "\', \'" + bdatetime + "\', \'" + seats + "\', \'" + sid + "\', \'" + email +"\')";
			try {
				esql.executeUpdate(insert_query);
				System.out.println("Booking " + bid + " has been successfully added. Have a nice day :)");
			}catch (SQLException e) {
				System.out.println("We did an oopsie on our end. Please try again later.");
			}
		}
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3

	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
		
	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		//
		
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
		
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//
		
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//
		
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		
	}
	
}