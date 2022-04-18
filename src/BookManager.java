import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import static java.time.temporal.ChronoUnit.DAYS;

public class BookManager {
    Scanner in = null;
    Connection conn = null;
    // Database Host
    final String databaseHost = "orasrv1.comp.hkbu.edu.hk";
    // Database Port
    final int databasePort = 1521;
    // Database name
    final String database = "pdborcl.orasrv1.comp.hkbu.edu.hk";
    final String proxyHost = "faith.comp.hkbu.edu.hk";
    final int proxyPort = 22;
    final String forwardHost = "localhost";
    int forwardPort;
    Session proxySession = null;
    boolean noException = true;

    // JDBC connecting host
    String jdbcHost;
    // JDBC connecting port
    int jdbcPort;

    String[] options = { // if you want to add an option, append to the end of
            // this array
            "search a book", "borrow a book", "return a book",
            "renew a book", "reserve a book",
            "exit" };

    /**
     * Get YES or NO. Do not change this function.
     *
     * @return boolean
     */
    boolean getYESorNO(String message) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(message));
        JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dialog = pane.createDialog(null, "Question");
        dialog.setVisible(true);
        boolean result = JOptionPane.YES_OPTION == (int) pane.getValue();
        dialog.dispose();
        return result;
    }

    /**
     * Get username & password. Do not change this function.
     *
     * @return username & password
     */
    String[] getUsernamePassword(String title) {
        JPanel panel = new JPanel();
        final TextField usernameField = new TextField();
        final JPasswordField passwordField = new JPasswordField();
        panel.setLayout(new GridLayout(2, 2));
        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);
        JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
            private static final long serialVersionUID = 1L;

            @Override
            public void selectInitialValue() {
                usernameField.requestFocusInWindow();
            }
        };
        JDialog dialog = pane.createDialog(null, title);
        dialog.setVisible(true);
        dialog.dispose();
        return new String[] { usernameField.getText(), new String(passwordField.getPassword()) };
    }

    /**
     * Login the proxy. Do not change this function.
     *
     * @return boolean
     */
    public boolean loginProxy() {
        if (getYESorNO("Using ssh tunnel or not?")) { // if using ssh tunnel
            String[] namePwd = getUsernamePassword("Login cs lab computer");
            String sshUser = namePwd[0];
            String sshPwd = namePwd[1];
            try {
                proxySession = new JSch().getSession(sshUser, proxyHost, proxyPort);
                proxySession.setPassword(sshPwd);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                proxySession.setConfig(config);
                proxySession.connect();
                proxySession.setPortForwardingL(forwardHost, 0, databaseHost, databasePort);
                forwardPort = Integer.parseInt(proxySession.getPortForwardingL()[0].split(":")[0]);
            } catch (JSchException e) {
                e.printStackTrace();
                return false;
            }
            jdbcHost = forwardHost;
            jdbcPort = forwardPort;
        } else {
            jdbcHost = databaseHost;
            jdbcPort = databasePort;
        }
        return true;
    }

    /**
     * Login the oracle system. Change this function under instruction.
     *
     * @return boolean
     */
    public boolean loginDB() {
        String username = "f0230974";//Replace e1234567 to your username
        String password = "f0230974";//Replace e1234567 to your password

        /* Do not change the code below */
        if(username.equalsIgnoreCase("e1234567") || password.equalsIgnoreCase("e1234567")) {
            String[] namePwd = getUsernamePassword("Login sqlplus");
            username = namePwd[0];
            password = namePwd[1];
        }
        String URL = "jdbc:oracle:thin:@" + jdbcHost + ":" + jdbcPort + "/" + database;

        try {
            System.out.println("Logging " + URL + " ...");
            conn = DriverManager.getConnection(URL, username, password);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Close the manager. Do not change this function.
     */
    public void close() {
        System.out.println("Thanks for using this manager! Bye...");
        try {
            if (conn != null)
                conn.close();
            if (proxySession != null) {
                proxySession.disconnect();
            }
            in.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show the options. If you want to add one more option, put into the
     * options array above.
     */
    public void showOptions() {
        System.out.println("Please choose following option:");
        for (int i = 0; i < options.length; ++i) {
            System.out.println("(" + (i + 1) + ") " + options[i]);
        }
    }

    public void run() {
        while (noException) {
            showOptions();
            String line = in.nextLine();
            if (line.equalsIgnoreCase("exit"))
                return;
            int choice = -1;
            try {
                choice = Integer.parseInt(line);
            } catch (Exception e) {
                System.out.println("This option is not available");
                continue;
            }
            if (!(choice >= 1 && choice <= options.length)) {
                System.out.println("This option is not available");
                continue;
            }
            if (options[choice - 1].equals("search a book")) {
                //bookSearch();
            } else if (options[choice - 1].equals("borrow a book")) {
                //bookBorrow();
            } else if (options[choice - 1].equals("return a book")) {
                //bookReturn();
            } else if (options[choice - 1].equals("renew a book")) {
                bookRenew();
            } else if (options[choice - 1].equals("reserve a book")) {
                bookReserve();
            } else if (options[choice - 1].equals("exit")) {
                break;
            }
        }
    }

/**
    * Check whether the student number is existed in the database 
    * if the data is not existed,
    * the input is invalid
    */
    private boolean checkHaveStudent(String sno) {
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT sno FROM STUDENTS WHERE sno = '" + sno + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next()) {
                System.out.println("Your student number is invalid!");
                return false;
            }
            else {
                return true;
            }

            } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
            return false;
        }
    }

/**
    * Check whether the book's call number is existed in the database
    * if the data is not existed,
    * the input is invalid 
    */
    private boolean checkHaveBook(String call_no) {
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT call_no FROM BOOKS WHERE call_no = '" + call_no + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next()) {
                System.out.println("The call_no is invalid!");
                return false;
            }
            else {
                return true;
            }

        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
            return false;
        }
    }
    
/**
     * Asking user input ISBN of the book
     * if the book is available
     * display the title, author, amount, and location
     */
     private void bookSearch() {
        System.out.println("Please input ISBN of the book: ");

        String ISBN = in.nextLine();
        ISBN = ISBN.trim();


        if (ISBN.equalsIgnoreCase("exit"))
            return;

        try {
            /**
             * Create the statement and sql
             */

            Statement stm = conn.createStatement();

            String sql = "SELECT title,author,amount,location FROM Books WHERE ISBN = '"+ ISBN +"'";
            
            System.out.println(sql);

            ResultSet rs = stm.executeQuery(sql);

            if(!rs.next()) {
                System.out.println("Does not have this book with" + ISBN);
                return;
            }

            String[] heads = { "Title", "Author", "Amount", "Location" };
            for (int i = 0; i < 4; ++i) {
                try {
                    System.out.println(heads[i] + " : " + rs.getString(i + 1));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
        }
    }
	
	 private boolean checkBookAmount(String sno){
        try{
            Statement stm = conn.createStatement();
            String sql = "SELECT COUNT(*) as sbamount FROM Borrow WHERE borrower = '" + sno + "'";
            ResultSet rs = stm.executeQuery(sql);
            if(!rs.next())
                return true;
            int amount = rs.getInt("sbamount");
            if(amount < 5){
                return true;
            }
            else {
                return false;
            }

        }catch (SQLException e1){
            e1.printStackTrace();
            noException = false;
            return false;
        }
    }
    
    private void addBorrow(String sno, String call_no, String b_date ,String d_date) {
    	 try {
             Statement stm = conn.createStatement();
             
             String sql = "INSERT INTO Borrow VALUES(" + "'" + sno + "'," + // this is student no
                     "'" + call_no + "'," + // this is call_no
                     "'" + b_date + "'," + "'" + d_date + "'"+ //this is reserve date
                     ")";
             System.out.println(sql);
             stm.executeUpdate(sql);
             stm.close();
             System.out.println("The borrowing succeeded");
             //
         } catch (SQLException e) {
             e.printStackTrace();
             System.out.println("fail to borrow book " + call_no + "!");
             noException = false;
         }
     }
    	
    
    
    private void bookBorrow() {
    	System.out.println("Please input your student number, call_no, b_date: ");
    	String SNO_call = in.nextLine();
    	SNO_call = SNO_call.trim();
    	
    	if (SNO_call.equalsIgnoreCase("exit"))
			return;
		String[] values = SNO_call.split(",");

		if (values.length < 4) {
			System.out.println("The value number is expected to be 4");
			return;
		}
		
		String sno = values[0];
        String call_no = values[1];
        String b_date = values[2];
        String d_date = values[3];
        
        if (!checkHaveStudent(sno)) {
            System.out.println("=============================================");
            return;
        }
        System.out.println("lol");
        if (!checkHaveBook(call_no)) {
            System.out.println("=============================================");
            return;
        }
        
        if(!checkBookAvailable (call_no)) {
        	System.out.println("The book is not available at present");
        		return;}
        
        if(!checkBookAmount(sno)) {
        	System.out.println("You have already borrowed 5 books");
        	return;}
        
        if (!checkOverdue(sno))
        		return;
        System.out.println("loll");
        if(!checkReserved(sno))
        	return;
        
        addBorrow(sno,call_no,b_date,d_date);
     
    	
    	
    }
    
/**
    * Asking user to input student number and book's call number
    * if the user and book are available, renewal is successful and due date will postpone for two weeks
    * the statement of borrow and renew will be updated when there is a successful renewal
    */
    private void bookRenew() {
        System.out.println("Please input your student number, call_no: ");
        String line = in.nextLine();

        if (line.equalsIgnoreCase("exit"))
            return;
        String[] values = line.split(",");

        if (values.length < 2) {
            System.out.println("The value number is expected to be 2");
            return;
        }
        String sno = values[0];
        String call_no = values[1];

        if (!checkHaveStudent(sno)) {
            System.out.println("=============================================");
            return;
        }
        System.out.println("lol");
        if (!checkHaveBook(call_no)) {
            System.out.println("=============================================");
            return;
        }
        System.out.println("loll");
        if(!checkOverdue(sno)){
            return;
        }
        System.out.println("lollll");
        if (!checkSecondHalf(sno, call_no)) {
            return;
        }
        System.out.println("lolll");
        if (!checkBookCanRenew(sno, call_no)) {
            return;
        }
        System.out.println("lolllll");
        if(!checkReserved(call_no)){
            return;
        }
        System.out.println("lollllll");
        addRenew(sno, call_no);
    }


/**
    * After renewing book successfully,
    * the information about the book will be inserted into renew table 
    */
    private void addRenew(String sno, String call_no) {
        /**
         * A sample input is:
         * INSERT INTO Renew VALUES('22222222', 'B0000');
         */

        try {
            Statement stm = conn.createStatement();
            String sql = "INSERT INTO RENEW VALUES(" + "'" + sno + "', " + // this is student no
                    "'" + call_no + "'" + // this is call_no
                    ")";
            stm.executeUpdate(sql);
            stm.close();
            System.out.println("succeed to renew book!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("fail to renew book " + call_no + "!");
            noException = false;
        }
    }
    
/**
    * Check whether the students have any overdue book
    * if yes, he/she will fail in borrowing and renewing books
    */
    private boolean checkOverdue(String sno) {
            try {
                Statement stm = conn.createStatement();
                LocalDate currentDate = LocalDate.now();
                String sql = "SELECT * FROM BORROW WHERE borrower = '" + sno + "' AND d_date > '" + currentDate + "'";
                ResultSet rs = stm.executeQuery(sql);
                if (!rs.next()) {
                    System.out.println("yeah");
                    return true;
                } else {
                    System.out.println("You have at least one overdue book, you cannot make a new borrowing");
                    return false;
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
                noException = false;
                return false;
            }
        }

/**
    * Renewal is only allowed during second half borrowing period
    * Check whether the current date is within the second half
    * if no, the renewal will be rejected 
    */
    private boolean checkSecondHalf(String sno, String call_no){
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT b_date, d_date FROM BORROW WHERE borrower = '" + sno + "' AND book = '" + call_no + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next()) {
                System.out.println("You have not borrowed this book!");
                System.out.println("=============================================");
                return false;
            }
            else {
                // 2022-03-24 00:00:00
                String[] b_date_string = rs.getString("b_date").split(" ");
                String[] d_date_string = rs.getString("d_date").split(" ");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate b_date = LocalDate.parse(b_date_string[0],formatter);
                LocalDate d_date = LocalDate.parse(d_date_string[0],formatter);
                long daysBetween = DAYS.between(b_date, d_date);
                LocalDate mid_date = b_date.plusDays(daysBetween/2);
                LocalDate currentDate = LocalDate.now();
                if (mid_date.isAfter(currentDate)){
                    System.out.println("Mid date: " + mid_date);
                    System.out.println("Current date: " + currentDate);
                    System.out.println("You can only renew after second half of borrow period!");
                    System.out.println("=============================================");
                    return false;
                }else {
                    return true;
                }
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
            return false;
        }
    }

/**
    *  Check whether the book has renewed before
    *  if yes, the renewal will be rejected
    *  Each book can only be renewed for once
    */
    private boolean checkBookCanRenew(String sno, String call_no){
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT * FROM RENEW WHERE sno = '" + sno + "' AND book = '" + call_no + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next())
                return true;
            else {
                System.out.println("You cannot renew this book again!");
                System.out.println("=============================================");
                return false;
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
            return false;
        }
    }
    
/**
    * Check whether the book is reserved by other student
    * if yes, the reservation will be failed
    */
    private boolean checkReserved(String call_no){
        try{
            Statement stm = conn.createStatement();
            String sql = "SELECT d_date FROM RESERVE WHERE book = '" + call_no + "'";
            ResultSet rs = stm.executeQuery(sql);
            if(!rs.next()){
                return true;
            }
            else {
                System.out.println("The book is reserved by another student.");
                System.out.println("=============================================");
                return false;
            }
        }catch (SQLException e1){
            e1.printStackTrace();
            noException = false;
            return false;
        }
    }
	
/**
    * Check whether the book has sufficient amount for user to borrow or reserve
    * only the book's amount more than 0 is allowed
    * with more than 0 amount, the renewal and reservation will be succeed
    */
    private boolean checkBookAvailable(String call_no){
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT amount FROM BOOKS WHERE call_no = '" + call_no + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next())
                return false;
            int amount = rs.getInt("amount");
            if (amount > 0) {
                return true;
            }
            else {
                return false;
            }

        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
            return false;
        }
    }

/**
    * Check whether the student has reserved any book
    * Students are only allowed to reserve one book
    */
    private boolean checkStudentReserved(String sno){
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT * FROM RESERVE WHERE sno = '" + sno + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next())
                return false;
            else {
                return true;
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
            return true;
        }
    }

/**
    * If student has already borrowed certain book
    * he/she is not allowed to reserve it
    */
    private boolean checkStudentBorrowed(String sno, String call_no){
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT * FROM BORROW WHERE book = '" + call_no + "' AND borrower = '" + sno +"'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next())
                return false;
            else {
                return true;
            }

        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
            return true;
        }
    }

/**
    * Asking the user to input student number, book's call number and request date
    * if the user and input meet the following conditions
    * Book will be reserved successfully
    */
    private void bookReserve() {
        System.out.println("Please input your student number, call_no, date:");
        String line = in.nextLine();

        if (line.equalsIgnoreCase("exit"))
            return;
        String[] values = line.split(",");

        if (values.length < 3) {
            System.out.println("The value number is expected to be 3");
            return;
        }
        String sno = values[0];
        String call_no = values[1];
        String date = values[2];

        //checking
        if (!checkHaveStudent(sno))
            return;

        if (!checkHaveBook(call_no))
            return;

        if (checkBookAvailable(call_no)) {
            System.out.println("The book is available, you cannot reserve!");
            System.out.println("=============================================");
            return;
        }
        if (checkStudentReserved(sno)) {
            System.out.println("You have already reserved for a book! You cannot reserve anymore!");
            System.out.println("=============================================");
            return;
        }
        if (checkStudentBorrowed(sno, call_no)){
            System.out.println("You have borrowed this book!");
            System.out.println("=============================================");
            return;
        }
        addReserve(sno, call_no, date);
    }

/**
    * if the reservation is successful,
    * student number, book's call number and request date will be inserted into reserve
    */
    private void addReserve(String sno, String call_no, String date) {
        /**
         * A sample input is:
         * INSERT INTO Reserve VALUES('12345678', 'A0000', '20-Apr-2022');
         */


        try {
            Statement stm = conn.createStatement();
            String sql = "INSERT INTO RESERVE VALUES(" + "'" + sno + "', " + // this is student no
                    "'" + call_no + "', " + // this is call_no
                    "'" + date + "'" + //this is reserve date
                    ")";
            stm.executeUpdate(sql);
            stm.close();
            System.out.println("succeed to reserve book!");
            //
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("fail to reserve book " + call_no + "!");
            noException = false;
        }
    }




    public BookManager() {
        System.out.println("Welcome to use this manager!");
        in = new Scanner(System.in);
    }

    public static void main(String[] args) {
        BookManager manager = new BookManager();
        if (!manager.loginProxy()) {
            System.out.println("Login proxy failed, please re-examine your username and password!");
            return;
        }
        if (!manager.loginDB()) {
            System.out.println("Login database failed, please re-examine your username and password!");
            return;
        }
        System.out.println("Login succeed!");
        try {
            manager.run();
        } finally {
            manager.close();
        }
    }


}


