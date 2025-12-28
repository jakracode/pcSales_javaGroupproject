-- SQL script to add image column to products table if it doesn't exist
-- Run this in your MySQL database (e.g., via phpMyAdmin or MySQL Workbench)

ALTER TABLE products ADD COLUMN IF NOT EXISTS image VARCHAR(255) AFTER unit;

-- If your MySQL version doesn't support ADD COLUMN IF NOT EXISTS, use:
-- ALTER TABLE products ADD COLUMN image VARCHAR(255) AFTER unit;
