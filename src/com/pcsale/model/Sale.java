package com.pcsale.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sale Model Class
 * Represents a sales transaction
 */
public class Sale {
    private int id;
    private String invoiceNo;
    private Integer customerId;
    private String customerName;
    private int userId;
    private String userName;
    private LocalDateTime saleDate;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal changeDue;
    private String paymentMethod; // cash, card, mobile, credit
    private String notes;
    private List<SaleItem> items;
    
    // Constructors
    public Sale() {
        this.saleDate = LocalDateTime.now();
        this.tax = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.paymentMethod = "cash";
        this.items = new ArrayList<>();
    }
    
    public Sale(String invoiceNo, int userId) {
        this();
        this.invoiceNo = invoiceNo;
        this.userId = userId;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getInvoiceNo() {
        return invoiceNo;
    }
    
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
    
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public LocalDateTime getSaleDate() {
        return saleDate;
    }
    
    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTax() {
        return tax;
    }
    
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getAmountPaid() {
        return amountPaid;
    }
    
    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }
    
    public BigDecimal getChangeDue() {
        if (changeDue == null && amountPaid != null && totalAmount != null) {
            return amountPaid.subtract(totalAmount);
        }
        return changeDue;
    }
    
    public void setChangeDue(BigDecimal changeDue) {
        this.changeDue = changeDue;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<SaleItem> getItems() {
        return items;
    }
    
    public void setItems(List<SaleItem> items) {
        this.items = items;
    }
    
    // Utility methods
    public void addItem(SaleItem item) {
        this.items.add(item);
    }
    
    public void calculateTotals() {
        // Calculate subtotal from items
        subtotal = items.stream()
                .map(SaleItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total: subtotal + tax - discount
        totalAmount = subtotal.add(tax).subtract(discount);
        
        // Calculate change
        if (amountPaid != null) {
            changeDue = amountPaid.subtract(totalAmount);
        }
    }
    
    @Override
    public String toString() {
        return "Invoice: " + invoiceNo;
    }
}
