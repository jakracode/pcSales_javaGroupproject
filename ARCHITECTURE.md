# PC SALE POS SYSTEM - ARCHITECTURE OVERVIEW

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER (GUI)                 │
├─────────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ LoginFrame   │  │ MainDashboard│  │  POSPanel    │         │
│  │              │→ │              │→ │              │         │
│  │ - Username   │  │ - Statistics │  │ - Product    │         │
│  │ - Password   │  │ - Navigation │  │   Search     │         │ 
│  │ - Login Btn  │  │ - Menu       │  │ - Cart       │         │
│  └──────────────┘  └──────────────┘  │ - Checkout   │         │
│                                      └──────────────┘         │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ ProductPanel │  │CustomerPanel │  │ Other Panels │         │
│  │              │  │              │  │              │         │
│  │ - List View  │  │ - List View  │  │ - Reports    │         │
│  │ - Add/Edit   │  │ - Add/Edit   │  │ - Settings   │         │
│  │ - Delete     │  │ - Search     │  │ - Users      │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
                        Uses / Updates
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER (DAO)                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  UserDAO     │  │ ProductDAO   │  │  SaleDAO     │         │
│  │              │  │              │  │              │         │
│  │ authenticate()│  │ getAllProducts() │ createSale() │       │
│  │ getUserById()│  │ searchProducts() │ getSaleById()│       │
│  │ addUser()    │  │ addProduct() │  │ getSales()   │         │
│  │ updateUser() │  │ updateProduct() │ generateInvoice()│     │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐                           │
│  │CustomerDAO   │  │ CategoryDAO  │                           │
│  │              │  │              │                           │
│  │ getAllCustomers() │ getAllCategories()                     │
│  │ addCustomer()│  │ addCategory()│                           │
│  │ searchCustomers() │ updateCategory()                       │
│  └──────────────┘  └──────────────┘                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
                        JDBC Calls
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE CONNECTION LAYER                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │            DatabaseConfig (Connection Manager)            │  │
│  │                                                           │  │
│  │  - getConnection()  : Returns MySQL Connection           │  │
│  │  - closeConnection(): Closes connection                  │  │
│  │  - testConnection() : Tests database connectivity        │  │
│  │                                                           │  │
│  │  Connection Pool: MySQL JDBC Driver (Connector/J)        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
                        SQL Queries
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER (MySQL)                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Database: pc_sale_db (MySQL 8.0)                               │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   users      │  │  products    │  │   sales      │         │
│  │              │  │              │  │              │         │
│  │ - id         │  │ - id         │  │ - id         │         │
│  │ - username   │  │ - barcode    │  │ - invoice_no │         │
│  │ - password   │  │ - name       │  │ - user_id    │         │
│  │ - role       │  │ - price      │  │ - total      │         │
│  │ - status     │  │ - stock      │  │ - date       │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ customers    │  │ categories   │  │ sale_items   │         │
│  │              │  │              │  │              │         │
│  │ - id         │  │ - id         │  │ - id         │         │
│  │ - code       │  │ - name       │  │ - sale_id    │         │
│  │ - name       │  │ - description│  │ - product_id │         │
│  │ - phone      │  └──────────────┘  │ - quantity   │         │
│  │ - points     │                     │ - price      │         │
│  └──────────────┘                     └──────────────┘         │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ suppliers    │  │ expenses     │  │stock_movements│        │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
                        Runs on
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                      SERVER LAYER (WAMP)                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  WAMP Server Stack:                                             │
│  - Windows OS                                                   │
│  - Apache Web Server (for phpMyAdmin)                           │
│  - MySQL Server 8.0 (Database Engine)                           │
│  - PHP (for phpMyAdmin interface)                               │
│                                                                  │
│  Services:                                                      │
│  - MySQL Service (Port 3306)                                    │
│  - Apache Service (Port 80)                                     │
│  - phpMyAdmin (http://localhost/phpmyadmin)                     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow Example: Making a Sale

```
1. USER INTERACTION
   └─> User clicks product in POSPanel
   └─> User enters quantity
   └─> User clicks "Add to Cart"

2. GUI LAYER (POSPanel.java)
   └─> Creates SaleItem object
   └─> Adds to cart ArrayList
   └─> Updates cart table display
   └─> User clicks "Complete Sale"

3. DAO LAYER (SaleDAO.java)
   └─> createSale(Sale sale)
       ├─> Begins database transaction
       ├─> Generates invoice number
       ├─> Inserts into sales table
       ├─> Inserts sale_items records
       ├─> Updates product stock
       └─> Commits transaction

4. DATABASE LAYER
   └─> MySQL executes queries
   └─> Updates tables atomically
   └─> Returns success/failure

5. RESPONSE FLOW
   └─> DAO returns boolean result
   └─> GUI shows success message
   └─> Clears cart
   └─> Updates product list
```

## Session Management Flow

```
┌─────────────┐
│ LoginFrame  │
└──────┬──────┘
       │ authenticate()
       ↓
┌──────────────────┐
│    UserDAO       │
│ authenticate()   │
└──────┬───────────┘
       │ SQL Query
       ↓
┌──────────────────┐
│   Database       │
│   users table    │
└──────┬───────────┘
       │ User object
       ↓
┌──────────────────┐
│ SessionManager   │
│ setCurrentUser() │
└──────┬───────────┘
       │
       ↓
┌──────────────────┐
│ MainDashboard    │
│ (User logged in) │
└──────────────────┘
       │
       ├─> All panels check: SessionManager.getCurrentUser()
       └─> Role-based access: SessionManager.isAdmin()
```

## Model Relationships

```
User (1) ─────creates─────→ (N) Sale
                               │
                               │ has
                               ↓
                           SaleItem (N)
                               │
                               │ references
                               ↓
Product (1) ←─────────────── (N) SaleItem
    │
    │ belongs to
    ↓
Category (1) ─────has─────→ (N) Product

Customer (1) ─────makes─────→ (N) Sale

Supplier (1) ─────supplies─────→ (N) Product

Product (1) ─────tracks─────→ (N) StockMovement
```

## Security & Validation Flow

```
User Input
    │
    ↓
┌─────────────────┐
│ GUI Validation  │  ← Empty field checks
│                 │  ← Format validation
└────────┬────────┘
         │ Valid
         ↓
┌─────────────────┐
│  DAO Layer      │  ← Business logic
│                 │  ← Duplicate checks
└────────┬────────┘
         │ Valid
         ↓
┌─────────────────┐
│ PreparedStmt    │  ← SQL injection prevention
│                 │  ← Parameter binding
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│ Database        │  ← Constraints
│                 │  ← Foreign keys
│                 │  ← Triggers
└─────────────────┘
```

## Utility Classes Support

```
┌──────────────────────────────────────┐
│        Utility Classes               │
├──────────────────────────────────────┤
│                                      │
│  SessionManager                      │
│  ├─> Manages logged-in user          │
│  ├─> Role checking                   │
│  └─> Session lifecycle               │
│                                      │
│  Formatter                           │
│  ├─> Currency formatting ($1,234.56) │
│  ├─> Date formatting (MMM DD, YYYY)  │
│  └─> Number formatting (1,234)       │
│                                      │
│  DatabaseConfig                      │
│  ├─> Connection pooling              │
│  ├─> Configuration management        │
│  └─> Error handling                  │
│                                      │
└──────────────────────────────────────┘
         ↑
         │ Used by
         │
    All Layers
```

## Build & Deployment Process

```
Source Files (.java)
    │
    ↓
┌─────────────────┐
│ javac compiler  │  ← build.ps1 / build.bat
└────────┬────────┘
         │ Compiles
         ↓
Class Files (.class)
    │
    ↓ Stored in
┌─────────────────┐
│   bin/          │  ← Compiled bytecode
└────────┬────────┘
         │
         ↓ Combined with
┌─────────────────┐
│   lib/          │  ← MySQL JDBC Driver
└────────┬────────┘
         │
         ↓ Runtime
┌─────────────────┐
│ Java Runtime    │  ← java -cp "bin;lib/*"
│ JVM executes    │
└─────────────────┘
         │
         ↓ Connects to
┌─────────────────┐
│ MySQL Database  │  ← WAMP Server
│ pc_sale_db      │
└─────────────────┘
```

---

## Key Design Principles

1. **Separation of Concerns**

   - GUI handles presentation
   - DAO handles data access
   - Models hold data
   - Utils provide common functions

2. **Single Responsibility**

   - Each class has one clear purpose
   - DAO classes focus on database operations
   - GUI classes focus on user interaction

3. **DRY (Don't Repeat Yourself)**

   - Common code in utility classes
   - Reusable components
   - Centralized configuration

4. **Error Handling**

   - Try-catch blocks in all database operations
   - User-friendly error messages
   - Transaction rollback on failures

5. **Security**
   - PreparedStatements prevent SQL injection
   - Password validation
   - Role-based access control
   - Session management

---

This architecture ensures:
✅ Maintainability - Easy to update and fix
✅ Scalability - Can add new features easily
✅ Security - Multiple layers of protection
✅ Performance - Efficient database operations
✅ Usability - Intuitive user interface
