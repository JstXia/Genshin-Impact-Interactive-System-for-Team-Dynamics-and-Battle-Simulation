package ui;

import db.DBManager;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.security.MessageDigest;

public class LoginUI extends JFrame {
    private JTextField tfEmail;
    private JPasswordField pfPassword;
    private JButton btnLogin;       
    private JButton btnRegister;
    private JCheckBox chkShowPassword;
    private JLabel lblStatus;
    
    public LoginUI() {
        super("ISTDBS - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 450);
        setLocationRelativeTo(null);
        
        initializeUI();
    }
    
    private void initializeUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("ISTDBS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 200));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // Email field
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Email:"), gbc);
        tfEmail = new JTextField(20);
        gbc.gridx = 1;
        mainPanel.add(tfEmail, gbc);
        
        // Password field
        gbc.gridy = 2;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Password:"), gbc);
        pfPassword = new JPasswordField(20);
        gbc.gridx = 1;
        mainPanel.add(pfPassword, gbc);
        
        // Show password checkbox
        gbc.gridy = 3;
        gbc.gridx = 1;
        chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                pfPassword.setEchoChar((char) 0);
            } else {
                pfPassword.setEchoChar('*');
            }
        });
        mainPanel.add(chkShowPassword, gbc);
        
        // Login Button
        btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(70, 130, 200));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(150, 35));
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        mainPanel.add(btnLogin, gbc);
        
        // Register Button
        btnRegister = new JButton("Create New Account");
        btnRegister.setBackground(new Color(100, 150, 100));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new Dimension(150, 35));
        gbc.gridy = 5;
        mainPanel.add(btnRegister, gbc);
        
        // Status label
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
        gbc.gridy = 6;
        mainPanel.add(lblStatus, gbc);
        
        // Test connection button
        JButton btnTest = new JButton("Test Database Connection");
        btnTest.setFont(new Font("Arial", Font.PLAIN, 10));
        btnTest.setPreferredSize(new Dimension(150, 25));
        gbc.gridy = 7;
        mainPanel.add(btnTest, gbc);
        
        add(mainPanel);
        
        // Add action listeners
        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> showRegistrationDialog());
        btnTest.addActionListener(e -> testDatabaseConnection());
        
        // Test database connection on startup
        testDatabaseConnection();
    }
    
    private void testDatabaseConnection() {
        if (DBManager.testConnection()) {
            lblStatus.setText("✓ Database connected!");
            lblStatus.setForeground(new Color(0, 150, 0));
        } else {
            lblStatus.setText("✗ Database connection failed! Check MySQL");
            lblStatus.setForeground(Color.RED);
        }
    }
    
    private void login() {
        String email = tfEmail.getText().trim();
        String password = new String(pfPassword.getPassword());
        
        if (email.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter email and password");
            lblStatus.setForeground(Color.RED);
            return;
        }
        
        try {
            List<Map<String, Object>> users = DBManager.fetchAll(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                email, hashPassword(password)
            );
            
            if (!users.isEmpty()) {
                String userName = (String) users.get(0).get("name");
                lblStatus.setText("Login successful! Welcome " + userName);
                lblStatus.setForeground(new Color(0, 150, 0));
                openMainUI(email, userName);
            } else {
                lblStatus.setText("Invalid email or password");
                lblStatus.setForeground(Color.RED);
            }
        } catch (SQLException ex) {
            lblStatus.setText("Database error: " + ex.getMessage());
            lblStatus.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }
    
    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(this, "Register New Account", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JTextField tfRegEmail = new JTextField(20);
        JTextField tfName = new JTextField(20);
        JPasswordField pfRegPassword = new JPasswordField(20);
        JPasswordField pfConfirmPassword = new JPasswordField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        panel.add(tfName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(tfRegEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(pfRegPassword, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        panel.add(pfConfirmPassword, gbc);
        
        JButton btnSubmit = new JButton("Register");
        JButton btnCancel = new JButton("Cancel");
        
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(btnSubmit, gbc);
        gbc.gridx = 1;
        panel.add(btnCancel, gbc);
        
        JLabel lblRegStatus = new JLabel(" ");
        lblRegStatus.setForeground(Color.RED);
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(lblRegStatus, gbc);
        
        dialog.add(panel);
        
        btnSubmit.addActionListener(e -> {
            String name = tfName.getText().trim();
            String email = tfRegEmail.getText().trim();
            String password = new String(pfRegPassword.getPassword());
            String confirm = new String(pfConfirmPassword.getPassword());
            
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                lblRegStatus.setText("All fields are required");
                return;
            }
            
            if (!password.equals(confirm)) {
                lblRegStatus.setText("Passwords do not match");
                return;
            }
            
            if (password.length() < 6) {
                lblRegStatus.setText("Password must be at least 6 characters");
                return;
            }
            
            try {
                // Check if email exists
                List<Map<String, Object>> existing = DBManager.fetchAll(
                    "SELECT * FROM users WHERE email = ?", email
                );
                
                if (!existing.isEmpty()) {
                    lblRegStatus.setText("Email already registered");
                    return;
                }
                
                DBManager.execute(
                    "INSERT INTO users (email, name, password, auth_provider) VALUES (?, ?, ?, ?)",
                    email, name, hashPassword(password), "EMAIL"
                );
                
                JOptionPane.showMessageDialog(dialog, 
                    "Registration successful! Please login.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                
            } catch (SQLException ex) {
                lblRegStatus.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        btnCancel.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }
    
    private void openMainUI(String email, String name) {
        SwingUtilities.invokeLater(() -> {
            ui.MainUI mainUI = new ui.MainUI();
            mainUI.setUserEmail(email);
            mainUI.setUserName(name);
            mainUI.setVisible(true);
            dispose();
        });
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            return password;
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginUI().setVisible(true);
        });
    }
}
