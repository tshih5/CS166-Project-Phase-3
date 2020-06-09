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
import java.util.Arrays;
import java.util.Random;

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

    private static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 64) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

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

        //System.out.print("Please enter user password: ");
        //pwd = ReadUserInput().trim();
        pwd = getSaltString();
        System.out.println("Password is: " + pwd);

        query = "SELECT * FROM Users WHERE email = \'" + email + "\'";

        try { //check to see if input email matches any on record.
            number_rows_returned = esql.executeQueryAndPrintResult(query);
        }catch (SQLException e) {
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        if(number_rows_returned > 0){
            System.out.println("A user with email " + email + " has already been registered. Please try again");
            return;
        }else{
            //no user with same email found, proceed to insert.
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
        String[] ssid = new String[10];
        String[] queries = new String[3];
        String insert_query  = "";
        int number_rows_returned = 0;
        int errors = 0;

        System.out.print("Please enter the booking ID: ");
        bid = ReadUserInput().trim();
        System.out.println("bid is: " + bid);

        System.out.print("Please enter status: (Paid, Cancelled, Pending): ");
        status = ReadUserInput().trim();
        System.out.println("Status is: " + status);

        System.out.print("Please enter date and time in mm/dd/yy hh:mm:ss AM/PM format: ");
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
                        System.out.println("\nError: Booking id " + bid + " already exists!\n");
                        errors++;
                    }
                    break;
                }
                case 1:	{
                    if(number_rows_returned == 0){
                        System.out.println("\nError: Show with sid " + sid + " does not exist!\n");
                        errors++;
                    }
                    break;
                }
                case 2:{
                    if(number_rows_returned == 0){
                        System.out.println("\nError: User with email " + email + " does not exist!\n");
                        errors++;
                    }
                    break;
                }
            }
        }

        //checks if any errors have occured.
        if(errors > 0){
            System.out.println("Please fix all errors and try again");
            return;
        }

        //create empty booking
        insert_query = "INSERT INTO Bookings (bid, status, bdatetime, seats, sid, email) VALUES (\'" + bid + "\', \'" + status + "\', \'" + bdatetime + "\', \'" + seats + "\', \'" + sid + "\', \'" + email +"\')";
        try {
            esql.executeUpdate(insert_query);
            System.out.println("Booking " + bid + " has been successfully created.");
        }catch (SQLException e) {
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        //display seats available for show
        String display_seats = "SELECT ssid FROM ShowSeats WHERE bid IS NULL AND sid = " + sid;
        try {
            System.out.println("Here are the available seats for sid " + sid);
            esql.executeQueryAndPrintResult(display_seats);
        }catch (SQLException e) {
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        //Prompt user for seat to be booked.
        for(int i = 0; i < Integer.parseInt(seats); ++i){
            System.out.print("Please enter seat to be booked: ");
            ssid[i] = ReadUserInput().trim();
            System.out.println("Adding seat " + ssid[i] + " to booking " + bid);
            String update_showseat_booking = "UPDATE ShowSeats SET bid = " + bid + " WHERE ssid = " + ssid[i];
            try {
                esql.executeUpdate(update_showseat_booking);
                System.out.println("SUCCESS!");
            }catch (SQLException e) {
                System.out.println("We did an oopsie on our end. Please try again later. " + e);
            }
        }
    }
    
    public static void AddMovieShowingToTheater(Ticketmaster esql){//3
        //movie
        String mvid = "";
        String title = "";
        String rdate = "";
        String country = "";
        String description = "";
        String duration = "";
        String lang = "";
        String genre = "";

        //show
        String sid = "";
        //String mvid = ""; 
        String sdate = "";
        String sttime = "";
        String edtime = "";

        //play
        //String sid "";
        String tid = ""; //theater id

        String insert_query = "";
        String insert_query2 = "";
        String insert_query3 = "";
        String query = "";
        int number_rows_returned = 0;

        
        System.out.print("Please enter the theater ID: ");
        tid = ReadUserInput().trim();
        System.out.println("tid is: " + tid);

        query = "SELECT * FROM Theaters WHERE tid = " + tid;

        try { //check if theater exists
            number_rows_returned = esql.executeQueryAndPrintResult(query);
        }catch (SQLException e) {
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        if (number_rows_returned > 0){
            System.out.println("Theater id " + tid + " exists.");
        }
        else { //(number_rows_returned == 0)
            System.out.println("Error: Theater id " + tid + " does not exist.");
            return;
        }


        //inputs for movie
        System.out.print("Please enter the movie ID: ");
        mvid = ReadUserInput().trim();
        System.out.println("mvid is: " + mvid);

        System.out.print("Please enter the movie title: ");
        title = ReadUserInput().trim();
        System.out.println("movie title is: " + title);

        System.out.print("Please enter the release date in MM/DD/YYYY format: ");
        rdate = ReadUserInput().trim();
        System.out.println("rdate is: " + rdate);

        System.out.print("Please enter the country: ");
        country = ReadUserInput().trim();
        System.out.println("country is: " + country);

        System.out.print("Please enter the description: ");
        description = ReadUserInput().trim();
        System.out.println("description is: " + description);

        System.out.print("Please enter the duration: ");
        duration = ReadUserInput().trim();
        System.out.println("duration is: " + duration);

        System.out.print("Please enter the language code (2 letter abbreviations, i.e. English = en): ");
        lang = ReadUserInput().trim();
        System.out.println("language is: " + lang);

        System.out.print("Please enter the genre: ");
        genre = ReadUserInput().trim();
        System.out.println("genre is: " + genre);


        //insert new movie into movies table
        insert_query = "INSERT INTO Movies (mvid, title, rdate, country, description, duration, lang, genre) VALUES (\'" + mvid + "\', \'" + title + "\', \'" + rdate + "\', \'" + country + "\', \'" + description + "\', \'" + duration +"\', \'" + lang +"\', \'" + genre +"\')";
        try {
            esql.executeUpdate(insert_query);
            System.out.println("Movie " + mvid + " has been successfully added. Have a nice day :)");
        }catch (SQLException e) {
            System.out.println("We did an oopsie on our end. Please try again later.");
        }


        //show inputs
        System.out.print("Please enter the show ID: ");
        sid = ReadUserInput().trim();
        System.out.println("sid is: " + sid);

        System.out.print("Please enter the show date: ");
        sdate = ReadUserInput().trim();
        System.out.println("show date is: " + sdate);

        System.out.print("Please enter the show start time (in HH:MM format): ");
        sttime = ReadUserInput().trim();
        System.out.println("start time is: " + sttime);

        System.out.print("Please enter the show end time (in HH:MM format): ");
        edtime = ReadUserInput().trim();
        System.out.println("end time is: " + edtime);

        //insert into shows
        insert_query2 = "INSERT INTO Shows (sid, mvid, sdate, sttime, edtime) VALUES (\'" + sid + "\', \'" + mvid + "\', \'" + sdate + "\', \'" + sttime + "\', \'" + edtime + "\')";
        try {
            esql.executeUpdate(insert_query2);
            System.out.println("Show " + sid + " has been successfully added. Have a nice day :)");
        }catch (SQLException e) {
            System.out.println("We did an oopsie on our end. Please try again later.");
        }

        //insert into plays
        insert_query3 = "INSERT INTO Plays (sid, tid) VALUES (\'" + sid + "\', \'" + tid + "\')";
        try {
            esql.executeUpdate(insert_query3);
            System.out.println("Play with Show " + sid + " and Theater " + tid + " has been successfully added.Have a nice day :)");
        } catch (SQLException e) {
            System.out.println("We did an oopsie on our end. Please try again later.");
        }
    }
    
    public static void CancelPendingBookings(Ticketmaster esql){//4
        int bid = -1;
        List<List<String>> result = new ArrayList<List<String>>(); 

        String get_status_query = "Select bid FROM Bookings WHERE status = \'Pending\'";
        //get list of bookings that have pending status
        try{
             result = esql.executeQueryAndReturnResult(get_status_query);
             System.out.println(Arrays.deepToString(result.toArray()));
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        //then loop through the result and update those entries with the same bid
        for(int i = 0; i < result.size(); ++i){
            //parse bid string to int
            bid = Integer.parseInt(result.get(i).get(0));
            String update_query = "UPDATE Bookings SET status = \'Cancelled\' WHERE bid = " + bid;

            try{
                esql.executeUpdate(update_query);
            }catch (SQLException e){
                System.out.println("Error updating Booking entry with bid " + bid + ". Please try again later.");
                return;
            }
        }
        System.out.println("Successfully cancelled all pending payments.");

        //  PRINT OUT STATUS OF ALL BOOKINGS
        get_status_query = "Select status FROM Bookings";
        try{
            esql.executeQueryAndPrintResult(get_status_query);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }
        
    }
    
    public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
        String bid = "";
        String ssid = "";
        String new_ssid = "";
        List<List<String>> result = new ArrayList<List<String>>();

        //get booking to be edited.
        System.out.print("Please input booking ID to be changed: ");
        bid = ReadUserInput().trim();
        System.out.println("bid is: " + bid + "\n");

        //output the seats that are associated with the booking ID
        String seats_query = "SELECT ssid FROM ShowSeats WHERE bid = " + bid;
        System.out.println("Seats available to be changed are: ");
        try{
            esql.executeQueryAndPrintResult(seats_query);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        //gets seat to be changed from
        System.out.print("\nWhich seat would you like to be changed?: ");
        ssid = ReadUserInput().trim();
        System.out.println("The seat to be changed is: " + ssid + "\n");

        //show available seats that are the same price
        String show_available_query = "SELECT ssid FROM ShowSeats WHERE bid IS NULL" +
                            " INTERSECT SELECT s1.ssid FROM ShowSeats s1 WHERE s1.price = (SELECT s2.price FROM ShowSeats s2 WHERE s2.ssid = " + ssid + ")" +
                            " INTERSECT SELECT s1.ssid FROM ShowSeats s1, Plays p1 WHERE s1.sid = p1.sid AND p1.tid = (SELECT p2.tid FROM ShowSeats s2, Plays p2 WHERE s2.sid = p2.sid AND s2.ssid = " + ssid + ")";
        try{
            result = esql.executeQueryAndReturnResult(show_available_query);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        //print seats or error if no seats are available
        if(result.size() == 0){
            System.out.println("Sorry, there are no seats available to be changed to.");
            return;
        }else{
            System.out.print("Here are the seats that are still available at the same price: ");
            for(int i = 0; i < result.size(); ++i){
                System.out.print(result.get(i).get(0) + " ");
            }
        }

        //get seat to be changed to
        System.out.print("\nWhich seat would you like to change Seat " + ssid + " to?: ");
        new_ssid = ReadUserInput().trim();
        System.out.println("New seat is: " + new_ssid + "\n");

        //update chosen seats and set old seat booking to null
        String remove_old_booking = "UPDATE ShowSeats SET bid = NULL WHERE ssid = " + ssid;
        String add_new_booking = "UPDATE ShowSeats SET bid = " + bid + " WHERE ssid = " + new_ssid;
        try{
            esql.executeUpdate(remove_old_booking);
            esql.executeUpdate(add_new_booking);
            System.out.println("Booking has been successfully updated! :)");
        }catch (SQLException e){
            System.out.println("Error updating Booking entry with bid " + bid + ". Please try again later.");
            return;
        }
    }
    
    public static void RemovePayment(Ticketmaster esql){//6
        String pid = "";
        String bid = "";
        List<List<String>> result = new ArrayList<List<String>>();

        //get pid of payment to be cancelled
        System.out.print("Please enter the pid of the payment to be cancelled: ");
        pid = ReadUserInput().trim();
        System.out.println("Payment ID is: " + pid + "\n");

        //get bid of booking corresponding to payment
        String booking_query = "SELECT bid FROM Payments WHERE pid = " + pid;
        try{
            result = esql.executeQueryAndReturnResult(booking_query);
            bid = result.get(0).get(0);
            System.out.println("Booking corresponding to pid " + pid + " found: " + bid);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        //update booking and delete payment
        String update_booking = "UPDATE Bookings SET status = \'Cancelled\' WHERE bid = " + bid;
        String delete_payment = "DELETE FROM Payments WHERE pid = " + pid;
        try{
            esql.executeUpdate(update_booking);
            esql.executeUpdate(delete_payment);
            //esql.executeQueryAndPrintResult("SELECT * FROM Bookings WHERE status = \'Cancelled\'");
        }catch (SQLException e){
            System.out.println("Error updating Booking entry with bid " + bid + ". Please try again later.");
            return;
        }
        System.out.println("Successfully deleted payment " + pid + ". Have a nice day!");
    }
    
    public static void ClearCancelledBookings(Ticketmaster esql){//7
        String delete_cancelled_query = "DELETE FROM Bookings WHERE status = \'Cancelled\'";
        try{
            esql.executeUpdate(delete_cancelled_query);
            esql.executeQueryAndPrintResult("SELECT * FROM Bookings WHERE Status = \'Cancelled\'");
        }catch(SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }
        System.out.println("Sucessfully cleared all cancelled bookings.");
    }
    
    public static void RemoveShowsOnDate(Ticketmaster esql){//8
        String query = "";
        String delete_query = "";
        String date = "";
        String cid = "";

        System.out.print("Please enter the date in MM/DD/YYYY format (you can leave out the preceding 0's for month and day, for example: 1/1/2001 instead of 01/01/2001): ");
        date = ReadUserInput().trim();
        System.out.println("date is: " + date);

        System.out.print("Please enter the cinema id: ");
        cid = ReadUserInput().trim();
        System.out.println("cid is: " + cid);

        //display what is about to be deleted
        query = "SELECT * FROM Shows Where sdate = \'" + date + "\' AND sid IN (SELECT p.sid FROM Plays p, Theaters t WHERE p.tid = t.tid AND t.cid = " + cid + ")";
        System.out.println("Delete all Shows on the date " + date + " in Cinema " + cid + ": ");
        try{
            esql.executeQueryAndPrintResult(query);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        //delete
        delete_query = "DELETE FROM Shows Where sdate = \'" + date + "\' AND sid IN (SELECT p.sid FROM Plays p, Theaters t WHERE p.tid = t.tid AND t.cid = " + cid + ")";
        System.out.println("Deleting...");
        try{
            esql.executeUpdate(delete_query);
            System.out.println("Deleted.");
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            //System.out.println(e);
            return;
        }
    }
    
    public static void ListTheatersPlayingShow(Ticketmaster esql){//9
        //
        String query = "";
        String cid = "";
        String sid = "";

        System.out.print("Please enter the cinema id: ");
        cid = ReadUserInput().trim();
        System.out.println("cid is: " + cid);

        System.out.print("Please enter the show id: ");
        sid = ReadUserInput().trim();
        System.out.println("sid is: " + sid);


        query = "SELECT t FROM Theaters t, Plays p WHERE p.sid = " + sid + " AND t.cid = " + cid + " AND p.tid = t.tid";
        System.out.println("All Theaters in Cinema " + cid + " playing the show " + sid + ": ");
        try{
            esql.executeQueryAndPrintResult(query);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }
    }
    
    public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
        //
        String query = "";
        String date = "";
        String time = "";


        System.out.print("Please enter the date in MM/DD/YYYY format (you can leave out the preceding 0's for month and day, for example: 1/1/2001 instead of 01/01/2001): ");
        date = ReadUserInput().trim();
        System.out.println("date is: " + date);

        System.out.print("Please enter the time in HH:MM format (you can leave out the preceding 0 for hours, for example: 1:00 instead of 01:00): ");
        time = ReadUserInput().trim();
        System.out.println("time is: " + time);


        query = "SELECT * FROM Shows WHERE sdate = '" + date + "' AND sttime = '" + time + "'";
        System.out.println("All Shows that start on " + date + " at " + time + ": ");
        try{
            esql.executeQueryAndPrintResult(query);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }
    }

    public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
        //
        String query = "SELECT title FROM Movies WHERE title ~* 'love' AND (SELECT EXTRACT(YEAR FROM rdate) > 2010)";
        System.out.println("Movies with titles containing 'love' released after 2010: ");
        try{
            esql.executeQueryAndPrintResult(query);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }
    }

    public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
        //
        String query = "SELECT u.fname, u.lname, u.email FROM Users u, Bookings b WHERE b.status = \'Pending\' AND b.email = u.email";
        
        System.out.println("User(s) with pending bookings: ");
        try{
            esql.executeQueryAndPrintResult(query);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }
    }

    public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
        String date_range_low = "";
        String date_range_hi = "";
        String cid = "";
        String mvid = "";
        List<List<String>> results = new ArrayList<List<String>>();
        

        //get start and end date
        System.out.print("Please enter a start date of the form mm/dd/yy: ");
        date_range_low = ReadUserInput().trim();
        System.out.println("Start date: " + date_range_low);

        System.out.print("Please enter a end date of the form mm/dd/yy: ");
        date_range_hi = ReadUserInput().trim();
        System.out.println("End date: " + date_range_hi);

        System.out.print("Please enter the requested cinema by ID: ");
        cid = ReadUserInput().trim();
        System.out.println("Cinema ID is: " + cid);

        System.out.print("Please enter the requested movie by ID: ");
        mvid = ReadUserInput().trim();
        System.out.println("Movie ID is: " + mvid);

        //Need shows, plays, cinema, theater, movies
        String get_shows = "SELECT m.title as Title, round((m.duration + 0.0)/3600, 2) as Duration, s.sdate as Showdate, s.sttime FROM Plays p, Shows s, Cinemas c, Theaters t, Movies m WHERE c.cid = t.cid AND t.tid = p.tid AND p.sid = s.sid AND s.mvid = m.mvid " + 
            "AND m.mvid = " + mvid + " AND c.cid = " + cid + " AND s.sdate > \'" + date_range_low + "\' AND s.sdate < \'" + date_range_hi + "\'";
        
        try { //display result
            results = esql.executeQueryAndReturnResult(get_shows);
        }catch (SQLException e) {
            System.out.println("We did an oopsie on our end. Please try again later. " + e);
            return;
        }

        System.out.println("Displaying shows at Cinema " + cid + " with mvid " + mvid + " between " + date_range_low + " and " + date_range_hi);
        System.out.println("+-----------------------------+----------+---------------+------------+");
        System.out.printf("|%30s %10s %15s %12s", "TITLE            |", "DURATION|", "SHOWDATE|", "START TIME|");
        System.out.println();
        System.out.println("+-----------------------------+----------+---------------+------------+");
        for(List<String> dat: results){
            System.out.printf("|%30s %10s %15s %12s",
                dat.get(0) + " ", dat.get(1) + " ", dat.get(2) + " ", dat.get(3) + " |");
            System.out.println();
        }
        System.out.println("+-----------------------------+----------+---------------+------------+");
    }

    public static void ListBookingInfoForUser(Ticketmaster esql){//14
        //
        String email = "";
        String query = "";
        int number_rows_returned = 0;
        String list_query = "";
        

        System.out.print("Please enter user email: ");
        email = ReadUserInput().trim();
        System.out.println("email is: " + email);

        
        query = "SELECT * FROM Bookings WHERE email = \'" + email + "\'";

        try { //check if user exists
            number_rows_returned = esql.executeQueryAndPrintResult(query);
        }catch (SQLException e) {
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }

        if (number_rows_returned > 0){
            System.out.println("User with the email " + email + " exists.");
        }
        else { //(number_rows_returned == 0)
            System.out.println("Error: User with the email " + email + " does not exist.");
            return;
        }



        list_query = "SELECT m.title, s.sdate, s.sttime, t.tname, cs.sno FROM Movies m, Shows s, Bookings b, ShowSeats ss, Theaters t, CinemaSeats cs WHERE b.email = \'" + email + "\' AND s.sid = b.sid AND m.mvid = s.mvid AND b.bid = ss.bid AND cs.csid = ss.csid AND cs.tid = t.tid";
        System.out.println(email + "\'s bookings information: ");
        try{
            esql.executeQueryAndPrintResult(list_query);
        }catch (SQLException e){
            System.out.println("We did an oopsie on our end. Please try again later.");
            return;
        }
    }
}