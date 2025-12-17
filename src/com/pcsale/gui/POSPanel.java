package com.pcsale.gui;

import com.pcsale.dao.ProductDAO;
import com.pcsale.dao.SaleDAO;
import com.pcsale.model.Product;
import com.pcsale.model.Sale;
import com.pcsale.model.SaleItem;
import com.pcsale.util.SessionManager;
import com.pcsale.util.Formatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * POSPanel - Point of Sale interface for making sales
 */
public class POSPanel extends JPanel {
    
    private JTextField txtSearch;
    private JTextField txtQuantity;
    private JTable productTable;
    private JTable cartTable;
    private DefaultTableModel productModel;
    private DefaultTableModel cartModel;
    private JLabel lblSubtotal;
    private JLabel lblTax;
    private JLabel lblDiscount;
    private JLabel lblTotal;
    private JTextField txtAmountPaid;
    private JLabel lblChange;
    private JComboBox<String> cboPaymentMethod;
    
    private ProductDAO productDAO;
    private SaleDAO saleDAO;
    private List<SaleItem> cartItems;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    
    public POSPanel() {
        productDAO = new ProductDAO();
        saleDAO = new SaleDAO();
        cartItems = new ArrayList<>();
        initComponents();
        loadProducts();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel lblTitle = new JLabel("Point of Sale");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);
        
        // Main content - split into left (products) and right (cart)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        
        // LEFT PANEL - Product Search and List
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBackground(Color.WHITE);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        
        JLabel lblSearch = new JLabel("Search Product:");
        txtSearch = new JTextField(25);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchProducts());
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(149, 165, 166));
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadProducts());
        
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        
        // Product table
        String[] productColumns = {"ID", "Barcode", "Name", "Price", "Stock"};
        productModel = new DefaultTableModel(productColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productModel);
        productTable.setFont(new Font("Arial", Font.PLAIN, 13));
        productTable.setRowHeight(25);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        
        JScrollPane productScroll = new JScrollPane(productTable);
        
        // Add to cart panel
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.setBackground(Color.WHITE);
        
        JLabel lblQty = new JLabel("Quantity:");
        txtQuantity = new JTextField("1", 5);
        txtQuantity.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton btnAddToCart = new JButton("Add to Cart");
        btnAddToCart.setBackground(new Color(46, 213, 115));
        btnAddToCart.setForeground(Color.BLACK);
        btnAddToCart.setFont(new Font("Arial", Font.BOLD, 14));
        btnAddToCart.setFocusPainted(false);
        btnAddToCart.addActionListener(e -> addToCart());
        
        addPanel.add(lblQty);
        addPanel.add(txtQuantity);
        addPanel.add(btnAddToCart);
        
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(productScroll, BorderLayout.CENTER);
        leftPanel.add(addPanel, BorderLayout.SOUTH);
        
        // RIGHT PANEL - Shopping Cart and Checkout
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBackground(Color.WHITE);
        
        JLabel lblCart = new JLabel("Shopping Cart");
        lblCart.setFont(new Font("Arial", Font.BOLD, 16));
        lblCart.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        
        // Cart table
        String[] cartColumns = {"Product", "Price", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setFont(new Font("Arial", Font.PLAIN, 13));
        cartTable.setRowHeight(25);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setPreferredSize(new Dimension(400, 250));
        
        // Cart buttons
        JPanel cartBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cartBtnPanel.setBackground(Color.WHITE);
        
        JButton btnRemove = new JButton("Remove Item");
        btnRemove.setBackground(new Color(231, 76, 60));
        btnRemove.setForeground(Color.BLACK);
        btnRemove.setFocusPainted(false);
        btnRemove.addActionListener(e -> removeFromCart());
        
        JButton btnClear = new JButton("Clear Cart");
        btnClear.setBackground(new Color(149, 165, 166));
        btnClear.setForeground(Color.BLACK);
        btnClear.setFocusPainted(false);
        btnClear.addActionListener(e -> clearCart());
        
        cartBtnPanel.add(btnRemove);
        cartBtnPanel.add(btnClear);
        
        // Totals panel
        JPanel totalsPanel = new JPanel(null);
        totalsPanel.setBackground(Color.WHITE);
        totalsPanel.setPreferredSize(new Dimension(400, 200));
        totalsPanel.setBorder(BorderFactory.createTitledBorder("Payment"));
        
        int yPos = 25;
        
        addTotalLabel(totalsPanel, "Subtotal:", 20, yPos);
        lblSubtotal = addTotalValue(totalsPanel, "$0.00", 250, yPos);
        yPos += 25;
        
        addTotalLabel(totalsPanel, "Tax (0%):", 20, yPos);
        lblTax = addTotalValue(totalsPanel, "$0.00", 250, yPos);
        yPos += 25;
        
        addTotalLabel(totalsPanel, "Discount:", 20, yPos);
        lblDiscount = addTotalValue(totalsPanel, "$0.00", 250, yPos);
        yPos += 30;
        
        addTotalLabel(totalsPanel, "TOTAL:", 20, yPos).setFont(new Font("Arial", Font.BOLD, 18));
        lblTotal = addTotalValue(totalsPanel, "$0.00", 250, yPos);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 20));
        lblTotal.setForeground(new Color(46, 213, 115));
        yPos += 35;
        
        addTotalLabel(totalsPanel, "Payment Method:", 20, yPos);
        cboPaymentMethod = new JComboBox<>(new String[]{"Cash", "Card", "Mobile", "Credit"});
        cboPaymentMethod.setBounds(250, yPos, 130, 25);
        totalsPanel.add(cboPaymentMethod);
        yPos += 30;
        
        addTotalLabel(totalsPanel, "Amount Paid:", 20, yPos);
        txtAmountPaid = new JTextField();
        txtAmountPaid.setBounds(250, yPos, 130, 25);
        txtAmountPaid.setFont(new Font("Arial", Font.PLAIN, 14));
        txtAmountPaid.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateChange();
            }
        });
        totalsPanel.add(txtAmountPaid);
        yPos += 30;
        
        addTotalLabel(totalsPanel, "Change:", 20, yPos);
        lblChange = addTotalValue(totalsPanel, "$0.00", 250, yPos);
        lblChange.setForeground(new Color(52, 152, 219));
        
        // Checkout button
        JPanel checkoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        checkoutPanel.setBackground(Color.WHITE);
        
        JButton btnCheckout = new JButton("COMPLETE SALE");
        btnCheckout.setPreferredSize(new Dimension(350, 50));
        btnCheckout.setBackground(new Color(46, 213, 115));
        btnCheckout.setForeground(Color.BLACK);
        btnCheckout.setFont(new Font("Arial", Font.BOLD, 18));
        btnCheckout.setFocusPainted(false);
        btnCheckout.addActionListener(e -> completeSale());
        checkoutPanel.add(btnCheckout);
        
        // Add to right panel
        JPanel cartTopPanel = new JPanel(new BorderLayout());
        cartTopPanel.setBackground(Color.WHITE);
        cartTopPanel.add(lblCart, BorderLayout.NORTH);
        cartTopPanel.add(cartScroll, BorderLayout.CENTER);
        cartTopPanel.add(cartBtnPanel, BorderLayout.SOUTH);
        
        rightPanel.add(cartTopPanel, BorderLayout.NORTH);
        rightPanel.add(totalsPanel, BorderLayout.CENTER);
        rightPanel.add(checkoutPanel, BorderLayout.SOUTH);
        
        // Add to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Enter key on search
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchProducts();
                }
            }
        });
        
        // Double-click to add to cart
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addToCart();
                }
            }
        });
    }
    
    private JLabel addTotalLabel(JPanel panel, String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(x, y, 200, 20);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(lbl);
        return lbl;
    }
    
    private JLabel addTotalValue(JPanel panel, String text, int x, int y) {
        JLabel lbl = new JLabel(text);
        lbl.setBounds(x, y, 130, 20);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lbl);
        return lbl;
    }
    
    private void loadProducts() {
        productModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            if (product.getStatus().equals("active") && product.getStockQuantity() > 0) {
                productModel.addRow(new Object[]{
                    product.getId(),
                    product.getBarcode(),
                    product.getName(),
                    Formatter.formatCurrency(product.getSellingPrice().doubleValue()),
                    product.getStockQuantity()
                });
            }
        }
    }
    
    private void searchProducts() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadProducts();
            return;
        }
        
        productModel.setRowCount(0);
        List<Product> products = productDAO.searchProducts(keyword);
        for (Product product : products) {
            if (product.getStockQuantity() > 0) {
                productModel.addRow(new Object[]{
                    product.getId(),
                    product.getBarcode(),
                    product.getName(),
                    Formatter.formatCurrency(product.getSellingPrice().doubleValue()),
                    product.getStockQuantity()
                });
            }
        }
    }
    
    private void addToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product!");
            return;
        }
        
        try {
            int quantity = Integer.parseInt(txtQuantity.getText());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!");
                return;
            }
            
            int productId = (int) productModel.getValueAt(selectedRow, 0);
            String productName = (String) productModel.getValueAt(selectedRow, 2);
            int stock = (int) productModel.getValueAt(selectedRow, 4);
            
            if (quantity > stock) {
                JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + stock);
                return;
            }
            
            Product product = productDAO.getProductById(productId);
            
            // Check if product already in cart
            boolean found = false;
            for (SaleItem item : cartItems) {
                if (item.getProductId() == productId) {
                    item.setQuantity(item.getQuantity() + quantity);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                SaleItem item = new SaleItem(productId, productName, quantity, product.getSellingPrice());
                cartItems.add(item);
            }
            
            updateCartTable();
            txtQuantity.setText("1");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity!");
        }
    }
    
    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove!");
            return;
        }
        
        cartItems.remove(selectedRow);
        updateCartTable();
    }
    
    private void clearCart() {
        if (cartItems.isEmpty()) return;
        
        int option = JOptionPane.showConfirmDialog(this,
            "Clear all items from cart?",
            "Confirm",
            JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            cartItems.clear();
            updateCartTable();
        }
    }
    
    private void updateCartTable() {
        cartModel.setRowCount(0);
        subtotal = BigDecimal.ZERO;
        
        for (SaleItem item : cartItems) {
            cartModel.addRow(new Object[]{
                item.getProductName(),
                Formatter.formatCurrency(item.getUnitPrice().doubleValue()),
                item.getQuantity(),
                Formatter.formatCurrency(item.getSubtotal().doubleValue())
            });
            subtotal = subtotal.add(item.getSubtotal());
        }
        
        updateTotals();
    }
    
    private void updateTotals() {
        tax = BigDecimal.ZERO; // Can be calculated based on tax rate
        discount = BigDecimal.ZERO;
        total = subtotal.add(tax).subtract(discount);
        
        lblSubtotal.setText(Formatter.formatCurrency(subtotal.doubleValue()));
        lblTax.setText(Formatter.formatCurrency(tax.doubleValue()));
        lblDiscount.setText(Formatter.formatCurrency(discount.doubleValue()));
        lblTotal.setText(Formatter.formatCurrency(total.doubleValue()));
        
        calculateChange();
    }
    
    private void calculateChange() {
        try {
            String amountText = txtAmountPaid.getText().trim();
            if (!amountText.isEmpty()) {
                BigDecimal amountPaid = new BigDecimal(amountText);
                BigDecimal change = amountPaid.subtract(total);
                lblChange.setText(Formatter.formatCurrency(change.doubleValue()));
            } else {
                lblChange.setText("$0.00");
            }
        } catch (NumberFormatException e) {
            lblChange.setText("$0.00");
        }
    }
    
    private void completeSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }
        
        String amountText = txtAmountPaid.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter amount paid!");
            return;
        }
        
        try {
            BigDecimal amountPaid = new BigDecimal(amountText);
            if (amountPaid.compareTo(total) < 0) {
                JOptionPane.showMessageDialog(this, "Amount paid is less than total!");
                return;
            }
            
            // Create sale
            Sale sale = new Sale();
            sale.setInvoiceNo(saleDAO.generateInvoiceNumber());
            sale.setUserId(SessionManager.getCurrentUser().getId());
            sale.setSubtotal(subtotal);
            sale.setTax(tax);
            sale.setDiscount(discount);
            sale.setTotalAmount(total);
            sale.setAmountPaid(amountPaid);
            sale.setPaymentMethod(cboPaymentMethod.getSelectedItem().toString().toLowerCase());
            sale.setItems(cartItems);
            
            boolean success = saleDAO.createSale(sale);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Sale completed successfully!\nInvoice: " + sale.getInvoiceNo() + 
                    "\nChange: " + Formatter.formatCurrency(amountPaid.subtract(total).doubleValue()),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reset form
                cartItems.clear();
                updateCartTable();
                txtAmountPaid.setText("");
                cboPaymentMethod.setSelectedIndex(0);
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to complete sale!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount!");
        }
    }
}
