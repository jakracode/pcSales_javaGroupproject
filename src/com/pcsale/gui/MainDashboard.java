package com.pcsale.gui;

import com.pcsale.dao.ProductDAO;
import com.pcsale.dao.SaleDAO;
import com.pcsale.dao.CustomerDAO;
import com.pcsale.util.SessionManager;
import com.pcsale.util.Formatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainDashboard - Main application window with navigation
 */
public class MainDashboard extends JFrame {
    
    private JPanel contentPanel;
    private JLabel lblWelcome;
    private ProductDAO productDAO;
    private SaleDAO saleDAO;
    private CustomerDAO customerDAO;
    
    public MainDashboard() {
        productDAO = new ProductDAO();
        saleDAO = new SaleDAO();
        customerDAO = new CustomerDAO();
        initComponents();
        showDashboardPanel();
    }
    
    private void initComponents() {
        setTitle("PC Sale POS System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(52, 73, 94));
        topPanel.setPreferredSize(new Dimension(1200, 60));
        
        JLabel lblTitle = new JLabel("  PC SALE POS SYSTEM");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        topPanel.add(lblTitle, BorderLayout.WEST);
        
        lblWelcome = new JLabel("Welcome, " + SessionManager.getCurrentUser().getFullName() + "  ");
        lblWelcome.setFont(new Font("Arial", Font.PLAIN, 14));
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(lblWelcome, BorderLayout.EAST);
        
        // Left menu panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(44, 62, 80));
        menuPanel.setPreferredSize(new Dimension(200, 640));
        
        // Menu buttons
        addMenuButton(menuPanel, "Dashboard", e -> showDashboardPanel());
        addMenuButton(menuPanel, "POS / Sales", e -> showPOSPanel());
        addMenuButton(menuPanel, "Products", e -> showProductsPanel());
        addMenuButton(menuPanel, "Customers", e -> showCustomersPanel());
        addMenuButton(menuPanel, "Sales History", e -> showSalesHistoryPanel());
        addMenuButton(menuPanel, "Reports", e -> showReportsPanel());
        
        if (SessionManager.hasManagementAccess()) {
            addMenuButton(menuPanel, "Users", e -> showUsersPanel());
            addMenuButton(menuPanel, "Categories", e -> showCategoriesPanel());
        }
        
        menuPanel.add(Box.createVerticalGlue());
        
        JButton btnLogout = createMenuButton("Logout");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.addActionListener(e -> logout());
        menuPanel.add(btnLogout);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Add to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBackground(new Color(44, 62, 80));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(0, 15, 0, 0));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(52, 73, 94));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.getText().equals("Logout")) {
                    btn.setBackground(new Color(44, 62, 80));
                }
            }
        });
        
        return btn;
    }
    
    private void addMenuButton(JPanel panel, String text, ActionListener listener) {
        JButton btn = createMenuButton(text);
        btn.addActionListener(listener);
        panel.add(btn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    private void showDashboardPanel() {
        contentPanel.removeAll();
        
        JPanel dashPanel = new JPanel(null);
        dashPanel.setBackground(new Color(236, 240, 241));
        
        JLabel lblDashboard = new JLabel("Dashboard Overview");
        lblDashboard.setFont(new Font("Arial", Font.BOLD, 24));
        lblDashboard.setBounds(30, 20, 300, 30);
        dashPanel.add(lblDashboard);
        
        // Stats cards
        int yPos = 70;
        
        // Today's Sales
        double todaySales = saleDAO.getTodaySalesTotal();
        addStatCard(dashPanel, "Today's Sales", Formatter.formatCurrency(todaySales), 
                    new Color(46, 213, 115), 30, yPos);
        
        // Total Products
        int totalProducts = productDAO.getAllProducts().size();
        addStatCard(dashPanel, "Total Products", String.valueOf(totalProducts), 
                    new Color(52, 152, 219), 280, yPos);
        
        // Total Customers
        int totalCustomers = customerDAO.getCustomerCount();
        addStatCard(dashPanel, "Total Customers", String.valueOf(totalCustomers), 
                    new Color(155, 89, 182), 530, yPos);
        
        // Low Stock Alert
        int lowStockCount = productDAO.getLowStockProducts().size();
        addStatCard(dashPanel, "Low Stock Items", String.valueOf(lowStockCount), 
                    new Color(231, 76, 60), 780, yPos);
        
        // Sales Chart
        JLabel lblChart = new JLabel("Last 7 Days Sales");
        lblChart.setFont(new Font("Arial", Font.BOLD, 18));
        lblChart.setBounds(30, 210, 300, 30);
        dashPanel.add(lblChart);
        
        // Get last 7 days sales data
        java.util.List<Object[]> salesData = saleDAO.getDailySalesData(7);
        SalesChartPanel chartPanel = new SalesChartPanel(
            "Daily Sales", 
            salesData, 
            "Date", 
            "Sales Amount ($)", 
            SalesChartPanel.ChartType.BAR
        );
        chartPanel.setBounds(30, 250, 950, 280);
        chartPanel.setChartColor(new Color(46, 213, 115));
        dashPanel.add(chartPanel);
        
        // Quick actions
        JLabel lblQuick = new JLabel("Quick Actions");
        lblQuick.setFont(new Font("Arial", Font.BOLD, 18));
        lblQuick.setBounds(30, 550, 200, 30);
        dashPanel.add(lblQuick);
        
        JButton btnNewSale = new JButton("New Sale");
        btnNewSale.setBounds(30, 590, 200, 50);
        btnNewSale.setBackground(new Color(46, 213, 115));
        btnNewSale.setForeground(Color.BLACK);
        btnNewSale.setFont(new Font("Arial", Font.BOLD, 16));
        btnNewSale.setFocusPainted(false);
        btnNewSale.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNewSale.addActionListener(e -> showPOSPanel());
        dashPanel.add(btnNewSale);
        
        JButton btnAddProduct = new JButton("Add Product");
        btnAddProduct.setBounds(250, 590, 200, 50);
        btnAddProduct.setBackground(new Color(52, 152, 219));
        btnAddProduct.setForeground(Color.BLACK);
        btnAddProduct.setFont(new Font("Arial", Font.BOLD, 16));
        btnAddProduct.setFocusPainted(false);
        btnAddProduct.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddProduct.addActionListener(e -> showProductsPanel());
        dashPanel.add(btnAddProduct);
        
        JButton btnViewReports = new JButton("View Reports");
        btnViewReports.setBounds(470, 590, 200, 50);
        btnViewReports.setBackground(new Color(155, 89, 182));
        btnViewReports.setForeground(Color.BLACK);
        btnViewReports.setFont(new Font("Arial", Font.BOLD, 16));
        btnViewReports.setFocusPainted(false);
        btnViewReports.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewReports.addActionListener(e -> showReportsPanel());
        dashPanel.add(btnViewReports);
        
        contentPanel.add(dashPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint(); 
    }
    
    private void addStatCard(JPanel panel, String title, String value, Color color, int x, int y) {
        JPanel card = new JPanel(null);
        card.setBounds(x, y, 220, 120);
        card.setBackground(color);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 2));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setBounds(10, 10, 200, 25);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        card.add(lblTitle);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setBounds(10, 45, 200, 40);
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Arial", Font.BOLD, 28));
        card.add(lblValue);
        
        panel.add(card);
    }
    
    private void showPOSPanel() {
        contentPanel.removeAll();
        contentPanel.add(new POSPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showProductsPanel() {
        contentPanel.removeAll();
        contentPanel.add(new ProductPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showCustomersPanel() {
        contentPanel.removeAll();
        contentPanel.add(new CustomerPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showSalesHistoryPanel() {
        contentPanel.removeAll();
        contentPanel.add(new SalesHistoryPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showReportsPanel() {
        contentPanel.removeAll();
        JLabel lbl = new JLabel("Reports - Coming Soon", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        contentPanel.add(lbl, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showUsersPanel() {
        contentPanel.removeAll();
        JLabel lbl = new JLabel("User Management - Coming Soon", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        contentPanel.add(lbl, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showCategoriesPanel() {
        contentPanel.removeAll();
        JLabel lbl = new JLabel("Category Management - Coming Soon", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        contentPanel.add(lbl, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void logout() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        }
    }
}
