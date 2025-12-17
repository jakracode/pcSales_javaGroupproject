package com.pcsale.gui;

import com.pcsale.dao.SaleDAO;
import com.pcsale.model.Sale;
import com.pcsale.model.SaleItem;
import com.pcsale.util.Formatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * SalesHistoryPanel - Display and manage sales history
 */
public class SalesHistoryPanel extends JPanel {
    
    private SaleDAO saleDAO;
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbPeriod;
    private JLabel lblTotalSales;
    private JLabel lblTotalAmount;
    private List<Sale> currentSales;
    
    public SalesHistoryPanel() {
        saleDAO = new SaleDAO();
        initComponents();
        loadSales();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel lblTitle = new JLabel("Sales History");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(lblTitle, BorderLayout.WEST);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setBackground(Color.WHITE);
        
        lblTotalSales = new JLabel("Total Sales: 0");
        lblTotalSales.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalSales.setForeground(new Color(52, 73, 94));
        statsPanel.add(lblTotalSales);
        
        statsPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        lblTotalAmount = new JLabel("Total Amount: $0.00");
        lblTotalAmount.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalAmount.setForeground(new Color(46, 213, 115));
        statsPanel.add(lblTotalAmount);
        
        topPanel.add(statsPanel, BorderLayout.EAST);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        JLabel lblPeriod = new JLabel("Period:");
        lblPeriod.setFont(new Font("Arial", Font.PLAIN, 14));
        filterPanel.add(lblPeriod);
        
        cmbPeriod = new JComboBox<>(new String[]{"All Time", "Today", "This Week", "This Month", "Last 30 Days"});
        cmbPeriod.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbPeriod.setPreferredSize(new Dimension(150, 30));
        cmbPeriod.addActionListener(e -> loadSales());
        filterPanel.add(cmbPeriod);
        
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        filterPanel.add(lblSearch);
        
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(300, 30));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable();
            }
        });
        filterPanel.add(txtSearch);
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadSales());
        filterPanel.add(btnRefresh);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        String[] columns = {"Invoice No", "Date", "Customer", "Cashier", "Items", "Subtotal", 
                           "Tax", "Discount", "Total", "Payment"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        salesTable = new JTable(tableModel);
        salesTable.setFont(new Font("Arial", Font.PLAIN, 13));
        salesTable.setRowHeight(30);
        salesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        salesTable.getTableHeader().setBackground(new Color(52, 73, 94));
        salesTable.getTableHeader().setForeground(Color.BLACK);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesTable.setSelectionBackground(new Color(52, 152, 219, 100));
        
        // Set column widths
        salesTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Invoice
        salesTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Date
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Customer
        salesTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Cashier
        salesTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Items
        salesTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Subtotal
        salesTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Tax
        salesTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Discount
        salesTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Total
        salesTable.getColumnModel().getColumn(9).setPreferredWidth(100); // Payment
        
        // Add double-click listener to view sale details
        salesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = salesTable.getSelectedRow();
                    if (row != -1) {
                        viewSaleDetails(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JButton btnViewDetails = new JButton("View Details");
        btnViewDetails.setFont(new Font("Arial", Font.PLAIN, 14));
        btnViewDetails.setBackground(new Color(52, 152, 219));
        btnViewDetails.setForeground(Color.WHITE);
        btnViewDetails.setFocusPainted(false);
        btnViewDetails.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewDetails.addActionListener(e -> {
            int row = salesTable.getSelectedRow();
            if (row != -1) {
                viewSaleDetails(row);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a sale to view details", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(btnViewDetails);
        
        // Combine panels
        JPanel topCombined = new JPanel(new BorderLayout());
        topCombined.setBackground(Color.WHITE);
        topCombined.add(topPanel, BorderLayout.NORTH);
        topCombined.add(filterPanel, BorderLayout.CENTER);
        
        add(topCombined, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadSales() {
        tableModel.setRowCount(0);
        
        LocalDateTime startDate = null;
        LocalDateTime endDate = LocalDateTime.now();
        
        String period = (String) cmbPeriod.getSelectedItem();
        switch (period) {
            case "Today":
                startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                break;
            case "This Week":
                startDate = LocalDateTime.now().minusDays(7);
                break;
            case "This Month":
                startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "Last 30 Days":
                startDate = LocalDateTime.now().minusDays(30);
                break;
            default:
                startDate = null;
                endDate = null;
                break;
        }
        
        currentSales = saleDAO.getSalesByDateRange(startDate, endDate);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        double totalAmount = 0;
        
        for (Sale sale : currentSales) {
            Object[] row = {
                sale.getInvoiceNo(),
                sale.getSaleDate().format(formatter),
                sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in",
                sale.getUserName(),
                sale.getItems() != null ? sale.getItems().size() : 0,
                Formatter.formatCurrency(sale.getSubtotal().doubleValue()),
                Formatter.formatCurrency(sale.getTax().doubleValue()),
                Formatter.formatCurrency(sale.getDiscount().doubleValue()),
                Formatter.formatCurrency(sale.getTotalAmount().doubleValue()),
                sale.getPaymentMethod().toUpperCase()
            };
            tableModel.addRow(row);
            totalAmount += sale.getTotalAmount().doubleValue();
        }
        
        lblTotalSales.setText("Total Sales: " + currentSales.size());
        lblTotalAmount.setText("Total Amount: " + Formatter.formatCurrency(totalAmount));
    }
    
    private void filterTable() {
        String searchText = txtSearch.getText().toLowerCase();
        
        if (searchText.trim().isEmpty()) {
            salesTable.setRowSorter(null);
            return;
        }
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        salesTable.setRowSorter(sorter);
        
        RowFilter<DefaultTableModel, Object> filter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    if (entry.getStringValue(i).toLowerCase().contains(searchText)) {
                        return true;
                    }
                }
                return false;
            }
        };
        
        sorter.setRowFilter(filter);
    }
    
    private void viewSaleDetails(int row) {
        int modelRow = salesTable.convertRowIndexToModel(row);
        Sale sale = currentSales.get(modelRow);
        
        // Fetch full sale details with items
        Sale fullSale = saleDAO.getSaleById(sale.getId());
        
        if (fullSale == null) {
            JOptionPane.showMessageDialog(this, 
                "Error loading sale details", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        showSaleDetailsDialog(fullSale);
    }
    
    private void showSaleDetailsDialog(Sale sale) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                     "Sale Details - " + sale.getInvoiceNo(), 
                                     true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Header panel
        JPanel headerPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createTitledBorder("Sale Information"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        addInfoField(headerPanel, "Invoice No:", sale.getInvoiceNo());
        addInfoField(headerPanel, "Date:", sale.getSaleDate().format(formatter));
        addInfoField(headerPanel, "Customer:", sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in");
        addInfoField(headerPanel, "Cashier:", sale.getUserName());
        addInfoField(headerPanel, "Payment Method:", sale.getPaymentMethod().toUpperCase());
        
        // Items table
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Items"));
        
        String[] columns = {"Product", "Barcode", "Quantity", "Unit Price", "Subtotal"};
        DefaultTableModel itemsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable itemsTable = new JTable(itemsModel);
        itemsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        itemsTable.setRowHeight(25);
        itemsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        itemsTable.getTableHeader().setBackground(new Color(52, 73, 94));
        itemsTable.getTableHeader().setForeground(Color.WHITE);
        
        for (SaleItem item : sale.getItems()) {
            Object[] row = {
                item.getProductName(),
                item.getBarcode(),
                item.getQuantity(),
                Formatter.formatCurrency(item.getUnitPrice().doubleValue()),
                Formatter.formatCurrency(item.getSubtotal().doubleValue())
            };
            itemsModel.addRow(row);
        }
        
        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsPanel.add(itemsScroll, BorderLayout.CENTER);
        
        // Totals panel
        JPanel totalsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        totalsPanel.setBackground(Color.WHITE);
        totalsPanel.setBorder(BorderFactory.createTitledBorder("Totals"));
        
        addInfoField(totalsPanel, "Subtotal:", Formatter.formatCurrency(sale.getSubtotal().doubleValue()));
        addInfoField(totalsPanel, "Tax:", Formatter.formatCurrency(sale.getTax().doubleValue()));
        addInfoField(totalsPanel, "Discount:", Formatter.formatCurrency(sale.getDiscount().doubleValue()));
        
        JLabel lblTotalLabel = new JLabel("Total Amount:");
        lblTotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel lblTotalValue = new JLabel(Formatter.formatCurrency(sale.getTotalAmount().doubleValue()));
        lblTotalValue.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalValue.setForeground(new Color(46, 213, 115));
        totalsPanel.add(lblTotalLabel);
        totalsPanel.add(lblTotalValue);
        
        addInfoField(totalsPanel, "Amount Paid:", Formatter.formatCurrency(sale.getAmountPaid().doubleValue()));
        addInfoField(totalsPanel, "Change:", Formatter.formatCurrency(sale.getChangeDue().doubleValue()));
        
        // Notes
        if (sale.getNotes() != null && !sale.getNotes().trim().isEmpty()) {
            JPanel notesPanel = new JPanel(new BorderLayout());
            notesPanel.setBackground(Color.WHITE);
            notesPanel.setBorder(BorderFactory.createTitledBorder("Notes"));
            
            JTextArea txtNotes = new JTextArea(sale.getNotes());
            txtNotes.setFont(new Font("Arial", Font.PLAIN, 13));
            txtNotes.setEditable(false);
            txtNotes.setLineWrap(true);
            txtNotes.setWrapStyleWord(true);
            notesPanel.add(new JScrollPane(txtNotes), BorderLayout.CENTER);
            
            mainPanel.add(notesPanel, BorderLayout.SOUTH);
        }
        
        // Close button
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.PLAIN, 14));
        btnClose.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnClose);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(headerPanel, BorderLayout.NORTH);
        centerPanel.add(itemsPanel, BorderLayout.CENTER);
        centerPanel.add(totalsPanel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void addInfoField(JPanel panel, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 13));
        
        panel.add(lblLabel);
        panel.add(lblValue);
    }
}
