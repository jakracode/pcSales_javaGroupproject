package com.pcsale.gui;

import com.pcsale.dao.UserDAO;
import com.pcsale.model.User;
import com.pcsale.util.DatabaseConfig;
import com.pcsale.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginFrame - User login window
 */
public class LoginFrame extends JFrame {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private UserDAO userDAO;
    
    public LoginFrame() {
        userDAO = new UserDAO();
        initComponents();
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        setTitle("PC Sale POS - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setResizable(false);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(45, 52, 54));
        
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(99, 110, 114));
        headerPanel.setPreferredSize(new Dimension(450, 80));
        JLabel lblTitle = new JLabel("PC SALE POS SYSTEM");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        
        // Login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(null);
        loginPanel.setBackground(new Color(45, 52, 54));
        loginPanel.setPreferredSize(new Dimension(450, 200));
        
        // Username
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setBounds(60, 30, 100, 25);
        lblUsername.setForeground(Color.WHITE);
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(lblUsername);
        
        txtUsername = new JTextField();
        txtUsername.setBounds(160, 30, 220, 30);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(txtUsername);
        
        // Password
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(60, 70, 100, 25);
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(lblPassword);
        
        txtPassword = new JPasswordField();
        txtPassword.setBounds(160, 70, 220, 30);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(txtPassword);
        
        // Buttons
        btnLogin = new JButton("Login");
        btnLogin.setBounds(160, 120, 100, 35);
        btnLogin.setBackground(new Color(46, 213, 115));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> login());
        loginPanel.add(btnLogin);
        
        btnExit = new JButton("Exit");
        btnExit.setBounds(280, 120, 100, 35);
        btnExit.setBackground(new Color(235, 77, 75));
        btnExit.setForeground(Color.BLACK);
        btnExit.setFont(new Font("Arial", Font.BOLD, 14));
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> System.exit(0));
        loginPanel.add(btnExit);
        
        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Enter key listener
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });
        
        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPassword.requestFocus();
                }
            }
        });
    }
    
    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter username and password!", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Test database connection
        if (!DatabaseConfig.testConnection()) {
            JOptionPane.showMessageDialog(this,
                "Cannot connect to database!\nPlease make sure WAMP server is running.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Authenticate user
        User user = userDAO.authenticate(username, password);
        
        if (user != null) {
            SessionManager.setCurrentUser(user);
            JOptionPane.showMessageDialog(this,
                "Welcome, " + user.getFullName() + "!",
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Open main dashboard
            SwingUtilities.invokeLater(() -> {
                new MainDashboard().setVisible(true);
            });
            
            // Close login window
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password!",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtUsername.requestFocus();
        }
    }
    
    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
