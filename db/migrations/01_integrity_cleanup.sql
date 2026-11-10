USE laundrybooking_ecommerce;


UPDATE payments SET status = UPPER(TRIM(status)) WHERE status IS NOT NULL;
UPDATE payments SET payment_method = UPPER(TRIM(payment_method)) WHERE payment_method IS NOT NULL;

UPDATE student SET role = 'USER' WHERE role IS NULL;

SET @guard := (SELECT COUNT(*) FROM payments p LEFT JOIN bookings b ON b.booking_id=p.booking_id WHERE p.booking_id IS NOT NULL AND b.booking_id IS NULL);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='payments' AND constraint_name='fk_payments_booking') > 0), 'SELECT ''fk_payments_booking: already done''',
            IF(@guard = 0, 'ALTER TABLE payments ADD CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (booking_id)',
               CONCAT('SELECT ''fk_payments_booking: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM payments p LEFT JOIN orders o ON o.order_id=p.order_id WHERE p.order_id IS NOT NULL AND o.order_id IS NULL);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='payments' AND constraint_name='fk_payments_order') > 0), 'SELECT ''fk_payments_order: already done''',
            IF(@guard = 0, 'ALTER TABLE payments ADD CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (order_id)',
               CONCAT('SELECT ''fk_payments_order: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM payments WHERE status IS NULL);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='payments' AND column_name='status' AND is_nullable='NO') > 0), 'SELECT ''payments.status NOT NULL: already done''',
            IF(@guard = 0, 'ALTER TABLE payments MODIFY status varchar(255) NOT NULL',
               CONCAT('SELECT ''payments.status NOT NULL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM payments WHERE payment_method IS NULL);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='payments' AND column_name='payment_method' AND is_nullable='NO') > 0), 'SELECT ''payments.payment_method NOT NULL: already done''',
            IF(@guard = 0, 'ALTER TABLE payments MODIFY payment_method varchar(255) NOT NULL',
               CONCAT('SELECT ''payments.payment_method NOT NULL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT COUNT(*) FROM time_slots WHERE date IS NULL);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='time_slots' AND column_name='date' AND is_nullable='NO') > 0), 'SELECT ''time_slots.date NOT NULL: already done''',
            IF(@guard = 0, 'ALTER TABLE time_slots MODIFY date date NOT NULL',
               CONCAT('SELECT ''time_slots.date NOT NULL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- time_slots.start_time NOT NULL
SET @guard := (SELECT COUNT(*) FROM time_slots WHERE start_time IS NULL);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='time_slots' AND column_name='start_time' AND is_nullable='NO') > 0), 'SELECT ''time_slots.start_time NOT NULL: already done''',
            IF(@guard = 0, 'ALTER TABLE time_slots MODIFY start_time time(6) NOT NULL',
               CONCAT('SELECT ''time_slots.start_time NOT NULL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM time_slots WHERE end_time IS NULL);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='time_slots' AND column_name='end_time' AND is_nullable='NO') > 0), 'SELECT ''time_slots.end_time NOT NULL: already done''',
            IF(@guard = 0, 'ALTER TABLE time_slots MODIFY end_time time(6) NOT NULL',
               CONCAT('SELECT ''time_slots.end_time NOT NULL: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;



SET @guard := (SELECT COUNT(*) FROM payments WHERE (booking_id IS NULL) = (order_id IS NULL));
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='payments' AND constraint_name='chk_payments_target') > 0), 'SELECT ''chk_payments_target: already done''',
            IF(@guard = 0, 'ALTER TABLE payments ADD CONSTRAINT chk_payments_target CHECK ((booking_id IS NULL) <> (order_id IS NULL))',
               CONCAT('SELECT ''chk_payments_target: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM payments WHERE status NOT IN ('PENDING','COMPLETED','FAILED'));
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='payments' AND constraint_name='chk_payments_status') > 0), 'SELECT ''chk_payments_status: already done''',
            IF(@guard = 0, 'ALTER TABLE payments ADD CONSTRAINT chk_payments_status CHECK (status IN (''PENDING'',''COMPLETED'',''FAILED''))',
               CONCAT('SELECT ''chk_payments_status: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT COUNT(*) FROM payments WHERE payment_method NOT IN ('CARD','EFT'));
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='payments' AND constraint_name='chk_payments_method') > 0), 'SELECT ''chk_payments_method: already done''',
            IF(@guard = 0, 'ALTER TABLE payments ADD CONSTRAINT chk_payments_method CHECK (payment_method IN (''CARD'',''EFT''))',
               CONCAT('SELECT ''chk_payments_method: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM payments WHERE amount < 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='payments' AND constraint_name='chk_payments_amount') > 0), 'SELECT ''chk_payments_amount: already done''',
            IF(@guard = 0, 'ALTER TABLE payments ADD CONSTRAINT chk_payments_amount CHECK (amount >= 0)',
               CONCAT('SELECT ''chk_payments_amount: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM bookings WHERE total_amount < 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='bookings' AND constraint_name='chk_bookings_total') > 0), 'SELECT ''chk_bookings_total: already done''',
            IF(@guard = 0, 'ALTER TABLE bookings ADD CONSTRAINT chk_bookings_total CHECK (total_amount >= 0)',
               CONCAT('SELECT ''chk_bookings_total: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM orders WHERE total_amount < 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='orders' AND constraint_name='chk_orders_total') > 0), 'SELECT ''chk_orders_total: already done''',
            IF(@guard = 0, 'ALTER TABLE orders ADD CONSTRAINT chk_orders_total CHECK (total_amount >= 0)',
               CONCAT('SELECT ''chk_orders_total: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT COUNT(*) FROM order_item WHERE quantity <= 0 OR unit_price < 0 OR subtotal < 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='order_item' AND constraint_name='chk_order_item_values') > 0), 'SELECT ''chk_order_item_values: already done''',
            IF(@guard = 0, 'ALTER TABLE order_item ADD CONSTRAINT chk_order_item_values CHECK (quantity > 0 AND unit_price >= 0 AND subtotal >= 0)',
               CONCAT('SELECT ''chk_order_item_values: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM products WHERE price < 0 OR inventory_quantity < 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='products' AND constraint_name='chk_products_values') > 0), 'SELECT ''chk_products_values: already done''',
            IF(@guard = 0, 'ALTER TABLE products ADD CONSTRAINT chk_products_values CHECK (price >= 0 AND inventory_quantity >= 0)',
               CONCAT('SELECT ''chk_products_values: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT COUNT(*) FROM laundry_services WHERE price < 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='laundry_services' AND constraint_name='chk_services_price') > 0), 'SELECT ''chk_services_price: already done''',
            IF(@guard = 0, 'ALTER TABLE laundry_services ADD CONSTRAINT chk_services_price CHECK (price >= 0)',
               CONCAT('SELECT ''chk_services_price: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM laundry_rooms WHERE capacity < 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='laundry_rooms' AND constraint_name='chk_rooms_capacity') > 0), 'SELECT ''chk_rooms_capacity: already done''',
            IF(@guard = 0, 'ALTER TABLE laundry_rooms ADD CONSTRAINT chk_rooms_capacity CHECK (capacity >= 0)',
               CONCAT('SELECT ''chk_rooms_capacity: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT COUNT(*) FROM reviews WHERE rating NOT BETWEEN 1 AND 5);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='reviews' AND constraint_name='chk_reviews_rating') > 0), 'SELECT ''chk_reviews_rating: already done''',
            IF(@guard = 0, 'ALTER TABLE reviews ADD CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)',
               CONCAT('SELECT ''chk_reviews_rating: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT COUNT(*) FROM time_slots WHERE end_time <= start_time);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema=DATABASE() AND table_name='time_slots' AND constraint_name='chk_time_slots_range') > 0), 'SELECT ''chk_time_slots_range: already done''',
            IF(@guard = 0, 'ALTER TABLE time_slots ADD CONSTRAINT chk_time_slots_range CHECK (end_time > start_time)',
               CONCAT('SELECT ''chk_time_slots_range: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT COUNT(*) FROM (SELECT machine_number FROM laundry_machine WHERE machine_number IS NOT NULL GROUP BY machine_number HAVING COUNT(*)>1) d);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='laundry_machine' AND index_name='uq_laundry_machine_number') > 0), 'SELECT ''uq_laundry_machine_number: already done''',
            IF(@guard = 0, 'ALTER TABLE laundry_machine ADD CONSTRAINT uq_laundry_machine_number UNIQUE (machine_number)',
               CONCAT('SELECT ''uq_laundry_machine_number: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT COUNT(*) FROM (SELECT date FROM time_slots GROUP BY date, start_time, end_time HAVING COUNT(*)>1) d);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='time_slots' AND index_name='uq_time_slots_window') > 0), 'SELECT ''uq_time_slots_window: already done''',
            IF(@guard = 0, 'ALTER TABLE time_slots ADD CONSTRAINT uq_time_slots_window UNIQUE (date, start_time, end_time)',
               CONCAT('SELECT ''uq_time_slots_window: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @guard := (SELECT 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='bookings' AND index_name='idx_bookings_machine_slot') > 0), 'SELECT ''idx_bookings_machine_slot: already done''',
            IF(@guard = 0, 'CREATE INDEX idx_bookings_machine_slot ON bookings (machine_id, time_slot_id)',
               CONCAT('SELECT ''idx_bookings_machine_slot: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='time_slots' AND index_name='idx_time_slots_date') > 0), 'SELECT ''idx_time_slots_date: already done''',
            IF(@guard = 0, 'CREATE INDEX idx_time_slots_date ON time_slots (date)',
               CONCAT('SELECT ''idx_time_slots_date: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='notifications' AND index_name='idx_notifications_student_read') > 0), 'SELECT ''idx_notifications_student_read: already done''',
            IF(@guard = 0, 'CREATE INDEX idx_notifications_student_read ON notifications (student_id, is_read)',
               CONCAT('SELECT ''idx_notifications_student_read: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


SET @guard := (SELECT 0);
SET @sql := IF(((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='payments' AND index_name='idx_payments_status') > 0), 'SELECT ''idx_payments_status: already done''',
            IF(@guard = 0, 'CREATE INDEX idx_payments_status ON payments (status)',
               CONCAT('SELECT ''idx_payments_status: SKIPPED - ', @guard, ' row(s) break this rule; fix them (see 00_audit_checks.sql) and run again''')));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;


