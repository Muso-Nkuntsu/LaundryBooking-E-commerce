USE laundrybooking_ecommerce;


SET @guard := (SELECT COUNT(*) FROM bookings WHERE ABS(total_amount - ROUND(total_amount,2)) > 0.000001 OR ABS(total_amount) >= 99999999);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='bookings' AND column_name='total_amount' AND data_type='decimal') > 0), 'SELECT ''bookings.total_amount DECIMAL: already done''',
            IF(@guard = 0, 'ALTER TABLE bookings MODIFY total_amount DECIMAL(10,2) NOT NULL',
               CONCAT('SELECT ''bookings.total_amount DECIMAL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT COUNT(*) FROM orders WHERE ABS(total_amount - ROUND(total_amount,2)) > 0.000001 OR ABS(total_amount) >= 99999999);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='orders' AND column_name='total_amount' AND data_type='decimal') > 0), 'SELECT ''orders.total_amount DECIMAL: already done''',
            IF(@guard = 0, 'ALTER TABLE orders MODIFY total_amount DECIMAL(10,2) NOT NULL',
               CONCAT('SELECT ''orders.total_amount DECIMAL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM order_item WHERE ABS(unit_price - ROUND(unit_price,2)) > 0.000001 OR ABS(unit_price) >= 99999999);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='order_item' AND column_name='unit_price' AND data_type='decimal') > 0), 'SELECT ''order_item.unit_price DECIMAL: already done''',
            IF(@guard = 0, 'ALTER TABLE order_item MODIFY unit_price DECIMAL(10,2) NOT NULL',
               CONCAT('SELECT ''order_item.unit_price DECIMAL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM order_item WHERE ABS(subtotal - ROUND(subtotal,2)) > 0.000001 OR ABS(subtotal) >= 99999999);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='order_item' AND column_name='subtotal' AND data_type='decimal') > 0), 'SELECT ''order_item.subtotal DECIMAL: already done''',
            IF(@guard = 0, 'ALTER TABLE order_item MODIFY subtotal DECIMAL(10,2) NOT NULL',
               CONCAT('SELECT ''order_item.subtotal DECIMAL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM products WHERE ABS(price - ROUND(price,2)) > 0.000001 OR ABS(price) >= 99999999);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='products' AND column_name='price' AND data_type='decimal') > 0), 'SELECT ''products.price DECIMAL: already done''',
            IF(@guard = 0, 'ALTER TABLE products MODIFY price DECIMAL(10,2) NOT NULL',
               CONCAT('SELECT ''products.price DECIMAL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM laundry_services WHERE ABS(price - ROUND(price,2)) > 0.000001 OR ABS(price) >= 99999999);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='laundry_services' AND column_name='price' AND data_type='decimal') > 0), 'SELECT ''laundry_services.price DECIMAL: already done''',
            IF(@guard = 0, 'ALTER TABLE laundry_services MODIFY price DECIMAL(10,2) NOT NULL',
               CONCAT('SELECT ''laundry_services.price DECIMAL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT COUNT(*) FROM payments WHERE ABS(amount - ROUND(amount,2)) > 0.000001 OR ABS(amount) >= 99999999);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='payments' AND column_name='amount' AND data_type='decimal') > 0), 'SELECT ''payments.amount DECIMAL: already done''',
            IF(@guard = 0, 'ALTER TABLE payments MODIFY amount DECIMAL(10,2) NOT NULL',
               CONCAT('SELECT ''payments.amount DECIMAL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


