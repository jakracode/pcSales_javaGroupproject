package com.pcsale.model;

import java.math.BigDecimal;

/**
 * SaleItem Model Class
 * Represents an item in a sale transaction
 */
public class SaleItem {
    private int id;
    private int saleId;
    private int productId;
    private String productName;
    private String barcode;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    
    // Constructors
    public SaleItem() {
    }
    
    public SaleItem(int productId, String productName, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(new BigDecimal(quantity));
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getSaleId() {
        return saleId;
    }
    
    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (unitPrice != null) {
            this.subtotal = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        if (unitPrice != null) {
            this.subtotal = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
    
    public BigDecimal getSubtotal() {
        if (subtotal == null && unitPrice != null) {
            subtotal = unitPrice.multiply(new BigDecimal(quantity));
        }
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    @Override
    public String toString() {
        return productName + " x " + quantity;
    }
}
