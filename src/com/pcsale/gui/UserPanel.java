package com.pcsale.gui;

import com.pcsale.dao.UserDAO;
import com.pcsale.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * UserPanel - User management interface
 */
public class UserPanel extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private UserDAO userDAO;
    
    public UserPanel() {
        userDAO = new UserDAO();
        initComponents();
        loadUsers();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitle = new JLabel("User Management");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Username", "Full Name", "Role", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = new JButton("Add User");
        btnAdd.setBackground(new Color(46, 213, 115));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> showAddDialog());
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadUsers());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User user : users) {
            tableModel.addRow(new Object[]{
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStatus()
            });
        }
    }
    
    private void showAddDialog() {
        JTextField userField = new JTextField();
        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"admin", "cashier"});
        
        Object[] message = {
            "Username:", userField,
            "Full Name:", nameField,
            "Password:", passField,
            "Role:", roleBox
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Add User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            User user = new User();
            user.setUsername(userField.getText());
            user.setFullName(nameField.getText());
            user.setPassword(new String(passField.getPassword()));
            user.setRole(roleBox.getSelectedItem().toString());
            user.setStatus("active");
            if (userDAO.addUser(user)) {
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Error adding user");
            }
        }
    }
}
