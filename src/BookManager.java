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
                bookSearch();
            } else if (options[choice - 1].equals("borrow a book")) {
                bookBorrow();
            } else if (options[choice - 1].equals("return a book")) {
                bookReturn();
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
     * to check if the sno is valid or not when there is input of sno
     * if not, return false
     */
    private boolean checkHaveStudent(String sno) {
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT sno FROM STUDENTS WHERE sno = '" + sno + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next()) {
                System.out.println("Your student number is invalid!");
                System.out.println("=============================================");
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
     * to check if the call_no is valid or not when there is input of call_no
     * if not, return false
     */
    private boolean checkHaveBook(String call_no) {
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT call_no FROM BOOKS WHERE call_no = '" + call_no + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next()) {
                System.out.println("The call_no is invalid!");
                System.out.println("=============================================");
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
            Statement stm = conn.createStatement();
            String sql = "SELECT title,author,amount,location FROM Books WHERE ISBN = '" + ISBN + "'";
            ResultSet rs = stm.executeQuery(sql);

            if(!rs.next()) {
                System.out.println("This ISBN" + ISBN + "is not valid!");
                System.out.println("=============================================");
                return;
            }

            String[] heads = { "Title", "Author", "Amount", "Location" };
            for (int i = 0; i < 4; ++i) {
                try {
                    System.out.println(heads[i] + " : " + rs.getString(i + 1));
                    System.out.println("=============================================");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
        }
    }










    /**
     * checker of bookBorrow
     * Check whether the book has sufficient amount for user to borrow
     * only the book's amount is more than 0
     * the borrow will succeed
     */
    private boolean checkBookAvailableForBorrow(String call_no){
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
                System.out.println(amount);
                System.out.println("The book is not available at present!");
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
     * checker of bookBorrow
     * this method is to check the book amount borrowed by the student
     * if amount = 0
     * he/she cannot borrow that book
     */
    private boolean checkBookAmount(String sno){
        try{
            Statement stm = conn.createStatement();
            String sql = "SELECT COUNT(*) as book_amount FROM Borrow WHERE borrower = '" + sno + "'";
            ResultSet rs = stm.executeQuery(sql);
            if(!rs.next())
                return true;
            int amount = rs.getInt("book_amount");
            if(amount < 5){
                return true;
            }
            else {
                System.out.println("You have already borrowed 5 books!");
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
     * checker of bookBorrow and bookRenew
     * Check whether the students have any overdue book
     * if yes, he/she will fail in borrowing and renewing books
     */
    private boolean checkOverdue(String sno) {
        try {
            Statement stm = conn.createStatement();
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            String currentDateString = currentDate.format(formatter);
            String sql = "SELECT * FROM BORROW WHERE borrower = '" + sno + "' AND d_date < '" + currentDateString + "'";


            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next()) {
                return true;
            } else {
                System.out.println("You have at least one overdue book, you cannot borrow or renew!");
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
     * checker of bookBorrow
     * Check whether the students who want to borrow the book is the first one who reserve
     * if yes, he/she can borrow the book
     * if no, he/she cannot borrow the book because it is reserved by the other student
     */
    private boolean checkBorrowerReserve(String sno,String call_no) {
        try{
            Statement stm = conn.createStatement();
            String sql = "SELECT sno FROM RESERVE WHERE book = '" + call_no + "' ORDER BY reserveDate ASC";
            ResultSet rs = stm.executeQuery(sql);

            if(rs.next()){
                String found_sno = rs.getString("sno").trim();
                if(!found_sno.equals(sno)){
                    System.out.println("Someone has reserved this book already!");
                    return false;
                } else{
                    deleteReserve(sno,call_no);
                    System.out.println("Your reserve has been delete!");
                    return true;
                }
            }else {
                return true;
            }


        }catch (SQLException e1){
            e1.printStackTrace();
            noException = false;
            return false;
        }
    }



    /**
     * delete a reserve record
     */
    private void deleteReserve(String sno, String call_no) {
        try {
            Statement stm = conn.createStatement();
            String sql = "DELETE FROM RESERVE WHERE sno = '" + sno + "' AND book = '" + call_no +"'";
            stm.executeUpdate(sql);
            stm.close();

        } catch (SQLException e) {
            e.printStackTrace();
            noException = false;
        }
    }

    /**
     * add a borrow record
     */
    private void addBorrow(String sno, String call_no, String b_date ,String d_date) {
    	 try {
             Statement stm = conn.createStatement();

             String sql = "INSERT INTO Borrow VALUES(" + "'" + sno + "'," + // this is student no
                     "'" + call_no + "'," + // this is call_no
                     "'" + b_date + "'," + "'" + d_date + "'"+ //this is reserve date
                     ")";

             stm.executeUpdate(sql);
             stm.close();
             System.out.println("You have borrowed the book successfully!");
             System.out.println("=============================================");

         } catch (SQLException e) {
             e.printStackTrace();
             System.out.println("Fail to borrow book " + call_no + "!");
             System.out.println("=============================================");
             noException = false;
         }
     }

    /**
     * ask the student to input
     * run all checkers of bookBorrow
     * and run addBorrow if all checkers ok
     */
    private void bookBorrow() {
    	System.out.println("Please input your student number, call_no: ");
    	String line = in.nextLine();

    	if (line.equalsIgnoreCase("exit"))
			return;

		String[] values = line.trim().split(",");

		if (values.length < 2) {
			System.out.println("The value number is expected to be 2!");
            System.out.println("=============================================");
			return;
		}

		String sno = values[0];
        String call_no = values[1];

        if (!checkHaveStudent(sno)) {
            return;
        }

        if (!checkHaveBook(call_no)) {
            return;
        }

        if(!checkBookAvailableForBorrow (call_no)) {
            return;
        }

        if(!checkBookAmount(sno)) {
        	return;
        }

        if (!checkOverdue(sno)) {
            return;
        }

        if(!checkBorrowerReserve(sno,call_no)) {
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        LocalDate currentDate = LocalDate.now();
        LocalDate returnDate = currentDate.plusDays(28);
        String b_date = currentDate.format(formatter);
        String d_date = returnDate.format(formatter);

        addBorrow(sno,call_no,b_date,d_date);
    }










    /**
     * checker of bookRenew
     * renewal is only allowed during the second half borrowing period
     * check whether the current date is within the second half
     * if no, the renewal will be rejected
     */
    private boolean checkSecondHalf(String sno, String call_no){
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT b_date, d_date FROM BORROW WHERE borrower = '" + sno + "' AND book = '" + call_no + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next()) {
                return false;
            }
            else {
                // 2022-03-24 00:00:00 (format of the output) so need to turn to string if i want the date only
                String[] b_date_string = rs.getString("b_date").split(" ");
                String[] d_date_string = rs.getString("d_date").split(" ");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                //changing the format of the date
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
                }
                else {
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
     *  checker of bookRenew
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
     * checker of bookRenew
     * Check whether the book is reserved by other student
     * if yes, the renew will be failed
     */
    private boolean checkReserved(String call_no){
        try{
            Statement stm = conn.createStatement();
            String sql = "SELECT reserveDate FROM RESERVE WHERE book = '" + call_no + "'";
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
      * add a renewal record
      */
    private void addRenew(String sno, String book) {

        try {
            Statement stm = conn.createStatement();
            String sql = "INSERT INTO RENEW VALUES(" + "'" + sno + "', " + // this is student no
                    "'" + book + "'" + // this is call_no
                    ")";
            stm.executeUpdate(sql);
            stm.close();
            System.out.println("succeed to renew book!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("fail to renew book " + book + "!");
            noException = false;
        }
    }
	
    /**
      * update the due date in borrow table
     */
    private void updateBorrow(String sno, String call_no) {
    	try {
    		Statement stm = conn.createStatement();
    		String sql = "SELECT d_date FROM BORROW WHERE sno = '" + sno + "' AND call_no = '" + call_no + "'";
    		ResultSet rs = stm.executeQuery(sql);
    		String[] d_date_string = rs.getString("d_date").split(" ");
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    		LocalDate d_date = LocalDate.parse(d_date_string[0],formatter);
    		d_date = d_date.plusDays(14);

    	}catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
        }
    }

    /**
     * ask the student to input
     * run all checkers of bookRenew
     * and run addRenew if all checkers ok
     */
    private void bookRenew() {
        System.out.println("Please input your student number, call_no: ");
        String line = in.nextLine();

        if (line.equalsIgnoreCase("exit"))
            return;
        String[] values = line.trim().split(",");

        if (values.length < 2) {
            System.out.println("The value number is expected to be 2");
            return;
        }
        String sno = values[0];
        String call_no = values[1];

        if (!checkHaveStudent(sno)) {
            return;
        }

        if (!checkHaveBook(call_no)) {
            return;
        }

        if (!checkOverdue(sno)){
            return;
        }

        if (!checkSecondHalf(sno, call_no)) {

            return;
        }

        if (!checkBookCanRenew(sno, call_no)) {

            return;
        }

        if(!checkReserved(call_no)){
            return;
        }

        addRenew(sno, call_no);
	    
	    updateBorrow(sno, call_no);
    }












    /**
     * checker of bookReserve
     * Check book amount
     * only the book's amount is equal to 0
     * the reserve will succeed
     */
    private boolean checkBookAvailableForReserve(String call_no){
        try {
            Statement stm = conn.createStatement();
            String sql = "SELECT amount FROM BOOKS WHERE call_no = '" + call_no + "'";
            ResultSet rs = stm.executeQuery(sql);
            if (!rs.next())
                return false;
            int amount = rs.getInt("amount");
            if (amount == 0) {
                return true;
            }
            else {
                System.out.println("The book is available, you cannot reserve!");
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
 * checker of bookReserve
 * Check whether the student has reserved any book
 * if yes, he/she cannot reserve
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
                System.out.println("You have already reserved for a book! You cannot reserve anymore!");
                System.out.println("=============================================");
                return true;
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
            return true;
        }
    }

/**
 * checker of bookReserve
 * If student has already borrowed that book
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
                System.out.println("You have borrowed this book!");
                System.out.println("=============================================");
                return true;
            }

        } catch (SQLException e1) {
            e1.printStackTrace();
            noException = false;
            return true;
        }
    }

    /**
     * add a reserve record
     */
    private void addReserve(String sno, String call_no, String reservationDate) {
        try {
            Statement stm = conn.createStatement();
            String sql = "INSERT INTO Reserve VALUES(" + "'" + sno + "'," + // this is student no
                    "'" + call_no + "'," + // this is call_no
                    "'" + reservationDate +
                    "')";
            System.out.println(sql);
            stm.executeUpdate(sql);
            stm.close();
            System.out.println("Succeed to reserve book!");
            System.out.println("=============================================");
            //
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Fail to reserve book " + call_no + "!");
            System.out.println("=============================================");
            noException = false;
        }
    }

    /**
     * ask the student to input
     * run all checkers of bookReserve
     * and run addReserve if all checkers ok
     */
    private void bookReserve() {
        System.out.println("Please input your student number, call_no, date:");
        String line = in.nextLine();

        if (line.equalsIgnoreCase("exit"))
            return;
        String[] values = line.trim().split(",");

        if (values.length < 3) {
            System.out.println("The value number is expected to be 3");
            System.out.println("=============================================");
            return;
        }
        String sno = values[0];
        String call_no = values[1];
        String date = values[2];

        //checking
        if (!checkHaveStudent(sno)) {
            System.out.println("lol");
            return;
        }

        if (!checkHaveBook(call_no)) {
            System.out.println("loll");
            return;
        }

        if (!checkBookAvailableForReserve(call_no)) {
            System.out.println("lolll");
            return;
        }

        if (checkStudentReserved(sno)) {
            System.out.println("lollll" );
            return;
        }
        if (checkStudentBorrowed(sno, call_no)){
            System.out.println("lolllll");
            return;
        }

        addReserve(sno, call_no, date);
    }









    /**
     * delete a borrow record
     * equal to return a book
     */
    private void deleteBorrow(String sno, String call_no) {
        try {
            Statement stm = conn.createStatement();
            String sql = "DELETE FROM BORROW WHERE borrower = '" + sno + "' AND book = '" + call_no +"'";
            stm.executeUpdate(sql);
            stm.close();
            System.out.println("Succeed to return book!");
            System.out.println("=============================================");
            //
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Fail to return book " + call_no + "!");
            System.out.println("=============================================");
            noException = false;
        }
    }

    /**
     * ask the student to input
     * run all checkers of bookReturn
     * and run addReturn if all checkers ok
     */
    private void bookReturn() {
        System.out.println("Please input your student number, call_no: ");
        String line = in.nextLine();

        if (line.equalsIgnoreCase("exit"))
            return;
        String[] values = line.trim().split(",");

        if (values.length < 2) {
            System.out.println("The value number is expected to be 2");
            System.out.println("=============================================");
            return;
        }
        String sno = values[0];
        String call_no = values[1];

        if (!checkHaveStudent(sno)) {
            return;
        }

        if (!checkHaveBook(call_no)) {
            return;
        }

        deleteBorrow(sno, call_no);
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


