
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.swing.*;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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
        String username = "f0223277";//Replace e1234567 to your username
        String password = "f0223277";//Replace e1234567 to your password

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
                //bookRenew();
            } else if (options[choice - 1].equals("reserve a book")) {
                //bookReserve();
            } else if (options[choice - 1].equals("exit")) {
                break;
            }
        }
    }
    
      //bookSearch();

   


}


