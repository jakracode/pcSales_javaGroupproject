package com.pcsale.dao;

import com.pcsale.model.Sale;
import com.pcsale.model.SaleItem;
import com.pcsale.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SaleDAO - Data Access Object for Sale operations
 */
public class SaleDAO {
    
    /**
     * Create a new sale with items (transaction)
     */
    public boolean createSale(Sale sale) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert sale
            String saleSql = "INSERT INTO sales (invoice_no, customer_id, user_id, sale_date, subtotal, " +
                           "tax, discount, total_amount, amount_paid, payment_method, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            saleStmt.setString(1, sale.getInvoiceNo());
            setIntegerOrNull(saleStmt, 2, sale.getCustomerId());
            saleStmt.setInt(3, sale.getUserId());
            saleStmt.setTimestamp(4, Timestamp.valueOf(sale.getSaleDate()));
            saleStmt.setBigDecimal(5, sale.getSubtotal());
            saleStmt.setBigDecimal(6, sale.getTax());
            saleStmt.setBigDecimal(7, sale.getDiscount());
            saleStmt.setBigDecimal(8, sale.getTotalAmount());
            saleStmt.setBigDecimal(9, sale.getAmountPaid());
            saleStmt.setString(10, sale.getPaymentMethod());
            saleStmt.setString(11, sale.getNotes());
            
            saleStmt.executeUpdate();
            
            // Get generated sale ID
            ResultSet rs = saleStmt.getGeneratedKeys();
            int saleId = 0;
            if (rs.next()) {
                saleId = rs.getInt(1);
            }
            
            // Insert sale items
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            
            for (SaleItem item : sale.getItems()) {
                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setBigDecimal(4, item.getUnitPrice());
                itemStmt.addBatch();
                
                // Update product stock
                String stockSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?";
                PreparedStatement stockStmt = conn.prepareStatement(stockSql);
                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.executeUpdate();
                stockStmt.close();
            }
            
            itemStmt.executeBatch();
            
            conn.commit(); // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    /**
     * Get sale by ID with items
     */
    public Sale getSaleById(int id) {
        String sql = "SELECT s.*, c.name as customer_name, u.full_name as user_name " +
                     "FROM sales s " +
                     "LEFT JOIN customers c ON s.customer_id = c.id " +
                     "JOIN users u ON s.user_id = u.id " +
                     "WHERE s.id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Sale sale = extractSaleFromResultSet(rs);
                sale.setItems(getSaleItems(id));
                return sale;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get sale by invoice number
     */
    public Sale getSaleByInvoice(String invoiceNo) {
        String sql = "SELECT s.*, c.name as customer_name, u.full_name as user_name " +
                     "FROM sales s " +
                     "LEFT JOIN customers c ON s.customer_id = c.id " +
                     "JOIN users u ON s.user_id = u.id " +
                     "WHERE s.invoice_no = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, invoiceNo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Sale sale = extractSaleFromResultSet(rs);
                sale.setItems(getSaleItems(sale.getId()));
                return sale;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all sales
     */
    public List<Sale> getAllSales() {
        return getSalesByDateRange(null, null);
    }
    
    /**
     * Get sales by date range
     */
    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT s.*, c.name as customer_name, u.full_name as user_name " +
            "FROM sales s " +
            "LEFT JOIN customers c ON s.customer_id = c.id " +
            "JOIN users u ON s.user_id = u.id ");
        
        if (startDate != null && endDate != null) {
            sql.append("WHERE s.sale_date BETWEEN ? AND ? ");
        }
        sql.append("ORDER BY s.sale_date DESC");
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            if (startDate != null && endDate != null) {
                stmt.setTimestamp(1, Timestamp.valueOf(startDate));
                stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sales.add(extractSaleFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }
    
    /**
     * Get sale items for a sale
     */
    public List<SaleItem> getSaleItems(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT si.*, p.name as product_name, p.barcode " +
                     "FROM sale_items si " +
                     "JOIN products p ON si.product_id = p.id " +
                     "WHERE si.sale_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                items.add(extractSaleItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    /**
     * Generate unique invoice number
     */
    public String generateInvoiceNumber() {
        String prefix = "INV";
        String sql = "SELECT invoice_no FROM sales ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastInvoice = rs.getString("invoice_no");
                int number = Integer.parseInt(lastInvoice.substring(3)) + 1;
                return prefix + String.format("%06d", number);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prefix + "000001";
    }
    
    /**
     * Get today's sales total
     */
    public double getTodaySalesTotal() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM sales " +
                     "WHERE DATE(sale_date) = CURDATE()";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Get sales count for a period
     */
    public int getSalesCount(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT COUNT(*) as count FROM sales WHERE sale_date BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Extract Sale object from ResultSet
     */
    private Sale extractSaleFromResultSet(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getInt("id"));
        sale.setInvoiceNo(rs.getString("invoice_no"));
        sale.setCustomerId((Integer) rs.getObject("customer_id"));
        sale.setCustomerName(rs.getString("customer_name"));
        sale.setUserId(rs.getInt("user_id"));
        sale.setUserName(rs.getString("user_name"));
        sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
        sale.setSubtotal(rs.getBigDecimal("subtotal"));
        sale.setTax(rs.getBigDecimal("tax"));
        sale.setDiscount(rs.getBigDecimal("discount"));
        sale.setTotalAmount(rs.getBigDecimal("total_amount"));
        sale.setAmountPaid(rs.getBigDecimal("amount_paid"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setNotes(rs.getString("notes"));
        return sale;
    }
    
    /**
     * Extract SaleItem object from ResultSet
     */
    private SaleItem extractSaleItemFromResultSet(ResultSet rs) throws SQLException {
        SaleItem item = new SaleItem();
        item.setId(rs.getInt("id"));
        item.setSaleId(rs.getInt("sale_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setBarcode(rs.getString("barcode"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setSubtotal(rs.getBigDecimal("subtotal"));
        return item;
    }
    
    /**
     * Get daily sales totals for the last N days
     */
    public List<Object[]> getDailySalesData(int days) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT DATE(sale_date) as sale_day, COUNT(*) as count, SUM(total_amount) as total " +
                     "FROM sales " +
                     "WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "GROUP BY DATE(sale_date) " +
                     "ORDER BY sale_day";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, days);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getDate("sale_day"),
                    rs.getInt("count"),
                    rs.getDouble("total")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    /**
     * Get weekly sales totals for the last N weeks
     */
    public List<Object[]> getWeeklySalesData(int weeks) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT YEARWEEK(sale_date) as sale_week, " +
                     "MIN(DATE(sale_date)) as week_start, " +
                     "COUNT(*) as count, " +
                     "SUM(total_amount) as total " +
                     "FROM sales " +
                     "WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL ? WEEK) " +
                     "GROUP BY YEARWEEK(sale_date) " +
                     "ORDER BY sale_week";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, weeks);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getDate("week_start"),
                    rs.getInt("count"),
                    rs.getDouble("total")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    /**
     * Get monthly sales totals for the last N months
     */
    public List<Object[]> getMonthlySalesData(int months) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(sale_date, '%Y-%m') as sale_month, " +
                     "MIN(DATE(sale_date)) as month_start, " +
                     "COUNT(*) as count, " +
                     "SUM(total_amount) as total " +
                     "FROM sales " +
                     "WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                     "GROUP BY DATE_FORMAT(sale_date, '%Y-%m') " +
                     "ORDER BY sale_month";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, months);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("sale_month"),
                    rs.getInt("count"),
                    rs.getDouble("total")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    /**
     * Get hourly sales distribution for today
     */
    public List<Object[]> getHourlySalesData() {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT HOUR(sale_date) as sale_hour, COUNT(*) as count, SUM(total_amount) as total " +
                     "FROM sales " +
                     "WHERE DATE(sale_date) = CURDATE() " +
                     "GROUP BY HOUR(sale_date) " +
                     "ORDER BY sale_hour";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("sale_hour"),
                    rs.getInt("count"),
                    rs.getDouble("total")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    /**
     * Helper method to set Integer or NULL
     */
    private void setIntegerOrNull(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }
}
