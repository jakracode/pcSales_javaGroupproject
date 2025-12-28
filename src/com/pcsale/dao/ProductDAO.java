package com.pcsale.dao;

import com.pcsale.model.Product;
import com.pcsale.util.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO - Data Access Object for Product operations
 */
public class ProductDAO {
    
    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                     "ORDER BY p.name";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    /**
     * Get product by ID
     */
    public Product getProductById(int id) {
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                     "WHERE p.id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get product by barcode
     */
    public Product getProductByBarcode(String barcode) {
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                     "WHERE p.barcode = ? AND p.status = 'active'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Search products by name or barcode
     */
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                     "WHERE (p.name LIKE ? OR p.barcode LIKE ?) AND p.status = 'active' " +
                     "ORDER BY p.name LIMIT 50";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String search = "%" + keyword + "%";
            stmt.setString(1, search);
            stmt.setString(2, search);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    /**
     * Get low stock products
     */
    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.id " +
                     "WHERE p.stock_quantity <= p.reorder_level AND p.status = 'active' " +
                     "ORDER BY p.stock_quantity";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    
    /**
     * Add new product
     */
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (barcode, name, category_id, supplier_id, cost_price, " +
                     "selling_price, stock_quantity, reorder_level, unit, image, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getName());
            setIntegerOrNull(stmt, 3, product.getCategoryId());
            setIntegerOrNull(stmt, 4, product.getSupplierId());
            stmt.setBigDecimal(5, product.getCostPrice());
            stmt.setBigDecimal(6, product.getSellingPrice());
            stmt.setInt(7, product.getStockQuantity());
            stmt.setInt(8, product.getReorderLevel());
            stmt.setString(9, product.getUnit());
            stmt.setString(10, product.getImage());
            stmt.setString(11, product.getStatus());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update product
     */
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET barcode = ?, name = ?, category_id = ?, supplier_id = ?, " +
                     "cost_price = ?, selling_price = ?, stock_quantity = ?, reorder_level = ?, " +
                     "unit = ?, image = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getName());
            setIntegerOrNull(stmt, 3, product.getCategoryId());
            setIntegerOrNull(stmt, 4, product.getSupplierId());
            stmt.setBigDecimal(5, product.getCostPrice());
            stmt.setBigDecimal(6, product.getSellingPrice());
            stmt.setInt(7, product.getStockQuantity());
            stmt.setInt(8, product.getReorderLevel());
            stmt.setString(9, product.getUnit());
            stmt.setString(10, product.getImage());
            stmt.setString(11, product.getStatus());
            stmt.setInt(12, product.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update product stock
     */
    public boolean updateStock(int productId, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete product
     */
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if barcode exists
     */
    public boolean barcodeExists(String barcode, int excludeId) {
        String sql = "SELECT COUNT(*) FROM products WHERE barcode = ? AND id != ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, barcode);
            stmt.setInt(2, excludeId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Extract Product object from ResultSet
     */
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setBarcode(rs.getString("barcode"));
        product.setName(rs.getString("name"));
        product.setCategoryId((Integer) rs.getObject("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setSupplierId((Integer) rs.getObject("supplier_id"));
        product.setSupplierName(rs.getString("supplier_name"));
        product.setCostPrice(rs.getBigDecimal("cost_price"));
        product.setSellingPrice(rs.getBigDecimal("selling_price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setReorderLevel(rs.getInt("reorder_level"));
        product.setUnit(rs.getString("unit"));
        product.setImage(rs.getString("image"));
        product.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            product.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return product;
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
