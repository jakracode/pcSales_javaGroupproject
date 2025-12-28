package com.pcsale.gui;

import com.pcsale.dao.CategoryDAO;
import com.pcsale.model.Category;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * CategoryPanel - Category management interface
 */
public class CategoryPanel extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private CategoryDAO categoryDAO;
    
    public CategoryPanel() {
        categoryDAO = new CategoryDAO();
        initComponents();
        loadCategories();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitle = new JLabel("Category Management");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Name", "Description"};
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
        
        JButton btnAdd = new JButton("Add Category");
        btnAdd.setBackground(new Color(46, 213, 115));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> showAddDialog());
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadCategories());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadCategories() {
        tableModel.setRowCount(0);
        List<Category> categories = categoryDAO.getAllCategories();
        for (Category cat : categories) {
            tableModel.addRow(new Object[]{
                cat.getId(),
                cat.getName(),
                cat.getDescription()
            });
        }
    }
    
    private void showAddDialog() {
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        Object[] message = {
            "Name:", nameField,
            "Description:", descField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Add Category", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Category cat = new Category();
            cat.setName(nameField.getText());
            cat.setDescription(descField.getText());
            if (categoryDAO.addCategory(cat)) {
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this, "Error adding category");
            }
        }
    }
}
