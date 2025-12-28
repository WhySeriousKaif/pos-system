-- Fix product table: Make price column nullable or set default
-- Option 1: Make price nullable (if price is not required)
ALTER TABLE product MODIFY COLUMN price DECIMAL(10,2) NULL;

-- Option 2: Set default value to 0 (if price should always have a value)
-- ALTER TABLE product MODIFY COLUMN price DECIMAL(10,2) NOT NULL DEFAULT 0;

-- Option 3: Remove price column if it's redundant (since we have mrp and selling_price)
-- ALTER TABLE product DROP COLUMN price;
