USE laundrybooking_ecommerce;

SELECT 'A. unexpected table' AS check_name, t.table_name
FROM information_schema.tables t
WHERE t.table_schema = DATABASE() AND t.table_type = 'BASE TABLE'
  AND t.table_name NOT IN (SELECT t FROM (SELECT 'student' AS t UNION ALL SELECT 'laundry_rooms' UNION ALL SELECT 'laundry_machine' UNION ALL SELECT 'time_slots' UNION ALL SELECT 'laundry_services' UNION ALL SELECT 'bookings' UNION ALL SELECT 'products' UNION ALL SELECT 'orders' UNION ALL SELECT 'order_item' UNION ALL SELECT 'payments' UNION ALL SELECT 'reviews' UNION ALL SELECT 'notifications' UNION ALL SELECT 'email_verification_token' UNION ALL SELECT 'password_reset_token') x);

SELECT 'B. unexpected column' AS check_name, c.table_name, c.column_name, c.column_type
FROM information_schema.columns c
WHERE c.table_schema = DATABASE()
  AND c.table_name IN (SELECT t FROM (SELECT 'student' AS t UNION ALL SELECT 'laundry_rooms' UNION ALL SELECT 'laundry_machine' UNION ALL SELECT 'time_slots' UNION ALL SELECT 'laundry_services' UNION ALL SELECT 'bookings' UNION ALL SELECT 'products' UNION ALL SELECT 'orders' UNION ALL SELECT 'order_item' UNION ALL SELECT 'payments' UNION ALL SELECT 'reviews' UNION ALL SELECT 'notifications' UNION ALL SELECT 'email_verification_token' UNION ALL SELECT 'password_reset_token') x)
  AND NOT EXISTS (SELECT 1 FROM (
    SELECT 'student' AS t, 'student_id' AS c
    UNION ALL SELECT 'student','first_name'
    UNION ALL SELECT 'student','last_name'
    UNION ALL SELECT 'student','email'
    UNION ALL SELECT 'student','phone_number'
    UNION ALL SELECT 'student','password'
    UNION ALL SELECT 'student','created_at'
    UNION ALL SELECT 'student','email_verified'
    UNION ALL SELECT 'student','email_verified_at'
    UNION ALL SELECT 'student','role'
    UNION ALL SELECT 'student','disabled'
    UNION ALL SELECT 'laundry_rooms','room_id'
    UNION ALL SELECT 'laundry_rooms','room_number'
    UNION ALL SELECT 'laundry_rooms','location'
    UNION ALL SELECT 'laundry_rooms','capacity'
    UNION ALL SELECT 'laundry_rooms','description'
    UNION ALL SELECT 'laundry_rooms','is_active'
    UNION ALL SELECT 'laundry_machine','machine_id'
    UNION ALL SELECT 'laundry_machine','machine_number'
    UNION ALL SELECT 'laundry_machine','type'
    UNION ALL SELECT 'laundry_machine','status'
    UNION ALL SELECT 'laundry_machine','room_id'
    UNION ALL SELECT 'time_slots','time_slot_id'
    UNION ALL SELECT 'time_slots','date'
    UNION ALL SELECT 'time_slots','start_time'
    UNION ALL SELECT 'time_slots','end_time'
    UNION ALL SELECT 'time_slots','is_available'
    UNION ALL SELECT 'laundry_services','service_id'
    UNION ALL SELECT 'laundry_services','service_name'
    UNION ALL SELECT 'laundry_services','description'
    UNION ALL SELECT 'laundry_services','price'
    UNION ALL SELECT 'bookings','booking_id'
    UNION ALL SELECT 'bookings','booking_date'
    UNION ALL SELECT 'bookings','status'
    UNION ALL SELECT 'bookings','total_amount'
    UNION ALL SELECT 'bookings','student_id'
    UNION ALL SELECT 'bookings','machine_id'
    UNION ALL SELECT 'bookings','time_slot_id'
    UNION ALL SELECT 'bookings','service_id'
    UNION ALL SELECT 'products','product_id'
    UNION ALL SELECT 'products','name'
    UNION ALL SELECT 'products','description'
    UNION ALL SELECT 'products','price'
    UNION ALL SELECT 'products','category'
    UNION ALL SELECT 'products','inventory_quantity'
    UNION ALL SELECT 'products','image_url'
    UNION ALL SELECT 'products','active'
    UNION ALL SELECT 'orders','order_id'
    UNION ALL SELECT 'orders','order_date'
    UNION ALL SELECT 'orders','total_amount'
    UNION ALL SELECT 'orders','status'
    UNION ALL SELECT 'orders','student_id'
    UNION ALL SELECT 'order_item','order_item_id'
    UNION ALL SELECT 'order_item','order_id'
    UNION ALL SELECT 'order_item','product_id'
    UNION ALL SELECT 'order_item','quantity'
    UNION ALL SELECT 'order_item','unit_price'
    UNION ALL SELECT 'order_item','subtotal'
    UNION ALL SELECT 'payments','payment_id'
    UNION ALL SELECT 'payments','amount'
    UNION ALL SELECT 'payments','payment_date'
    UNION ALL SELECT 'payments','payment_method'
    UNION ALL SELECT 'payments','status'
    UNION ALL SELECT 'payments','transaction_ref'
    UNION ALL SELECT 'payments','booking_id'
    UNION ALL SELECT 'payments','order_id'
    UNION ALL SELECT 'payments','payer_name'
    UNION ALL SELECT 'payments','payer_bank'
    UNION ALL SELECT 'payments','service_id'
    UNION ALL SELECT 'reviews','id'
    UNION ALL SELECT 'reviews','student_id'
    UNION ALL SELECT 'reviews','service_id'
    UNION ALL SELECT 'reviews','rating'
    UNION ALL SELECT 'reviews','comment'
    UNION ALL SELECT 'reviews','date'
    UNION ALL SELECT 'notifications','notification_id'
    UNION ALL SELECT 'notifications','student_id'
    UNION ALL SELECT 'notifications','message'
    UNION ALL SELECT 'notifications','type'
    UNION ALL SELECT 'notifications','date_sent'
    UNION ALL SELECT 'notifications','is_read'
    UNION ALL SELECT 'email_verification_token','id'
    UNION ALL SELECT 'email_verification_token','token_hash'
    UNION ALL SELECT 'email_verification_token','student_id'
    UNION ALL SELECT 'email_verification_token','created_at'
    UNION ALL SELECT 'email_verification_token','expires_at'
    UNION ALL SELECT 'email_verification_token','used_at'
    UNION ALL SELECT 'password_reset_token','id'
    UNION ALL SELECT 'password_reset_token','token_hash'
    UNION ALL SELECT 'password_reset_token','student_id'
    UNION ALL SELECT 'password_reset_token','created_at'
    UNION ALL SELECT 'password_reset_token','expires_at'
  ) e WHERE e.t = c.table_name AND e.c = c.column_name);


SELECT 'C. missing column' AS check_name, e.t AS table_name, e.c AS column_name
FROM (
    SELECT 'student' AS t, 'student_id' AS c
    UNION ALL SELECT 'student','first_name'
    UNION ALL SELECT 'student','last_name'
    UNION ALL SELECT 'student','email'
    UNION ALL SELECT 'student','phone_number'
    UNION ALL SELECT 'student','password'
    UNION ALL SELECT 'student','created_at'
    UNION ALL SELECT 'student','email_verified'
    UNION ALL SELECT 'student','email_verified_at'
    UNION ALL SELECT 'student','role'
    UNION ALL SELECT 'student','disabled'
    UNION ALL SELECT 'laundry_rooms','room_id'
    UNION ALL SELECT 'laundry_rooms','room_number'
    UNION ALL SELECT 'laundry_rooms','location'
    UNION ALL SELECT 'laundry_rooms','capacity'
    UNION ALL SELECT 'laundry_rooms','description'
    UNION ALL SELECT 'laundry_rooms','is_active'
    UNION ALL SELECT 'laundry_machine','machine_id'
    UNION ALL SELECT 'laundry_machine','machine_number'
    UNION ALL SELECT 'laundry_machine','type'
    UNION ALL SELECT 'laundry_machine','status'
    UNION ALL SELECT 'laundry_machine','room_id'
    UNION ALL SELECT 'time_slots','time_slot_id'
    UNION ALL SELECT 'time_slots','date'
    UNION ALL SELECT 'time_slots','start_time'
    UNION ALL SELECT 'time_slots','end_time'
    UNION ALL SELECT 'time_slots','is_available'
    UNION ALL SELECT 'laundry_services','service_id'
    UNION ALL SELECT 'laundry_services','service_name'
    UNION ALL SELECT 'laundry_services','description'
    UNION ALL SELECT 'laundry_services','price'
    UNION ALL SELECT 'bookings','booking_id'
    UNION ALL SELECT 'bookings','booking_date'
    UNION ALL SELECT 'bookings','status'
    UNION ALL SELECT 'bookings','total_amount'
    UNION ALL SELECT 'bookings','student_id'
    UNION ALL SELECT 'bookings','machine_id'
    UNION ALL SELECT 'bookings','time_slot_id'
    UNION ALL SELECT 'bookings','service_id'
    UNION ALL SELECT 'products','product_id'
    UNION ALL SELECT 'products','name'
    UNION ALL SELECT 'products','description'
    UNION ALL SELECT 'products','price'
    UNION ALL SELECT 'products','category'
    UNION ALL SELECT 'products','inventory_quantity'
    UNION ALL SELECT 'products','image_url'
    UNION ALL SELECT 'products','active'
    UNION ALL SELECT 'orders','order_id'
    UNION ALL SELECT 'orders','order_date'
    UNION ALL SELECT 'orders','total_amount'
    UNION ALL SELECT 'orders','status'
    UNION ALL SELECT 'orders','student_id'
    UNION ALL SELECT 'order_item','order_item_id'
    UNION ALL SELECT 'order_item','order_id'
    UNION ALL SELECT 'order_item','product_id'
    UNION ALL SELECT 'order_item','quantity'
    UNION ALL SELECT 'order_item','unit_price'
    UNION ALL SELECT 'order_item','subtotal'
    UNION ALL SELECT 'payments','payment_id'
    UNION ALL SELECT 'payments','amount'
    UNION ALL SELECT 'payments','payment_date'
    UNION ALL SELECT 'payments','payment_method'
    UNION ALL SELECT 'payments','status'
    UNION ALL SELECT 'payments','transaction_ref'
    UNION ALL SELECT 'payments','booking_id'
    UNION ALL SELECT 'payments','order_id'
    UNION ALL SELECT 'payments','payer_name'
    UNION ALL SELECT 'payments','payer_bank'
    UNION ALL SELECT 'payments','service_id'
    UNION ALL SELECT 'reviews','id'
    UNION ALL SELECT 'reviews','student_id'
    UNION ALL SELECT 'reviews','service_id'
    UNION ALL SELECT 'reviews','rating'
    UNION ALL SELECT 'reviews','comment'
    UNION ALL SELECT 'reviews','date'
    UNION ALL SELECT 'notifications','notification_id'
    UNION ALL SELECT 'notifications','student_id'
    UNION ALL SELECT 'notifications','message'
    UNION ALL SELECT 'notifications','type'
    UNION ALL SELECT 'notifications','date_sent'
    UNION ALL SELECT 'notifications','is_read'
    UNION ALL SELECT 'email_verification_token','id'
    UNION ALL SELECT 'email_verification_token','token_hash'
    UNION ALL SELECT 'email_verification_token','student_id'
    UNION ALL SELECT 'email_verification_token','created_at'
    UNION ALL SELECT 'email_verification_token','expires_at'
    UNION ALL SELECT 'email_verification_token','used_at'
    UNION ALL SELECT 'password_reset_token','id'
    UNION ALL SELECT 'password_reset_token','token_hash'
    UNION ALL SELECT 'password_reset_token','student_id'
    UNION ALL SELECT 'password_reset_token','created_at'
    UNION ALL SELECT 'password_reset_token','expires_at'
) e
LEFT JOIN information_schema.columns c
  ON c.table_schema = DATABASE() AND c.table_name = e.t AND c.column_name = e.c
WHERE c.column_name IS NULL;


SELECT 'D. no primary key' AS check_name, t.table_name
FROM information_schema.tables t
LEFT JOIN information_schema.table_constraints tc
  ON tc.table_schema = t.table_schema AND tc.table_name = t.table_name AND tc.constraint_type = 'PRIMARY KEY'
WHERE t.table_schema = DATABASE() AND t.table_type = 'BASE TABLE' AND tc.constraint_name IS NULL;


SELECT 'E. orphan bookings.student'  AS check_name, COUNT(*) AS n FROM bookings b LEFT JOIN student s ON s.student_id=b.student_id WHERE s.student_id IS NULL
UNION ALL SELECT 'E. orphan bookings.machine', COUNT(*) FROM bookings b LEFT JOIN laundry_machine m ON m.machine_id=b.machine_id WHERE m.machine_id IS NULL
UNION ALL SELECT 'E. orphan bookings.time_slot', COUNT(*) FROM bookings b LEFT JOIN time_slots ts ON ts.time_slot_id=b.time_slot_id WHERE ts.time_slot_id IS NULL
UNION ALL SELECT 'E. orphan bookings.service', COUNT(*) FROM bookings b LEFT JOIN laundry_services s ON s.service_id=b.service_id WHERE b.service_id IS NOT NULL AND s.service_id IS NULL
UNION ALL SELECT 'E. orphan payments.booking', COUNT(*) FROM payments p LEFT JOIN bookings b ON b.booking_id=p.booking_id WHERE p.booking_id IS NOT NULL AND b.booking_id IS NULL
UNION ALL SELECT 'E. orphan payments.order', COUNT(*) FROM payments p LEFT JOIN orders o ON o.order_id=p.order_id WHERE p.order_id IS NOT NULL AND o.order_id IS NULL
UNION ALL SELECT 'E. orphan payments.service', COUNT(*) FROM payments p LEFT JOIN laundry_services s ON s.service_id=p.service_id WHERE p.service_id IS NOT NULL AND s.service_id IS NULL
UNION ALL SELECT 'E. orphan orders.student', COUNT(*) FROM orders o LEFT JOIN student s ON s.student_id=o.student_id WHERE s.student_id IS NULL
UNION ALL SELECT 'E. orphan order_item.order', COUNT(*) FROM order_item i LEFT JOIN orders o ON o.order_id=i.order_id WHERE o.order_id IS NULL
UNION ALL SELECT 'E. orphan order_item.product', COUNT(*) FROM order_item i LEFT JOIN products p ON p.product_id=i.product_id WHERE p.product_id IS NULL
UNION ALL SELECT 'E. orphan reviews', COUNT(*) FROM reviews r LEFT JOIN student s ON s.student_id=r.student_id LEFT JOIN laundry_services v ON v.service_id=r.service_id WHERE s.student_id IS NULL OR v.service_id IS NULL
UNION ALL SELECT 'E. orphan notifications', COUNT(*) FROM notifications n LEFT JOIN student s ON s.student_id=n.student_id WHERE s.student_id IS NULL
UNION ALL SELECT 'E. orphan machines.room', COUNT(*) FROM laundry_machine m LEFT JOIN laundry_rooms r ON r.room_id=m.room_id WHERE r.room_id IS NULL;

SELECT 'F. foreign key' AS check_name, k.table_name, k.column_name, k.referenced_table_name, k.referenced_column_name, r.delete_rule
FROM information_schema.key_column_usage k
JOIN information_schema.referential_constraints r
  ON r.constraint_schema = k.constraint_schema AND r.constraint_name = k.constraint_name
WHERE k.table_schema = DATABASE() AND k.referenced_table_name IS NOT NULL
ORDER BY k.table_name, k.column_name;


SELECT 'G. duplicate machine_number' AS check_name, machine_number, COUNT(*) AS n FROM laundry_machine WHERE machine_number IS NOT NULL GROUP BY machine_number HAVING COUNT(*) > 1;
SELECT 'G. duplicate time slot' AS check_name, date, start_time, end_time, COUNT(*) AS n FROM time_slots GROUP BY date, start_time, end_time HAVING COUNT(*) > 1;
SELECT 'G. payment with both or neither target' AS check_name, payment_id, booking_id, order_id FROM payments WHERE (booking_id IS NULL) = (order_id IS NULL);


SELECT 'H. payment status' AS check_name, status, COUNT(*) AS n FROM payments GROUP BY status;
SELECT 'H. payment method' AS check_name, payment_method, COUNT(*) AS n FROM payments GROUP BY payment_method;
SELECT 'H. student role' AS check_name, role, COUNT(*) AS n FROM student GROUP BY role;
SELECT 'H. negative or zero amounts' AS check_name, 'payments' AS tbl, COUNT(*) AS n FROM payments WHERE amount < 0
UNION ALL SELECT 'H. negative or zero amounts','bookings', COUNT(*) FROM bookings WHERE total_amount < 0
UNION ALL SELECT 'H. negative or zero amounts','orders', COUNT(*) FROM orders WHERE total_amount < 0
UNION ALL SELECT 'H. negative or zero amounts','order_item', COUNT(*) FROM order_item WHERE quantity <= 0 OR unit_price < 0 OR subtotal < 0
UNION ALL SELECT 'H. negative or zero amounts','products', COUNT(*) FROM products WHERE price < 0 OR inventory_quantity < 0
UNION ALL SELECT 'H. negative or zero amounts','laundry_services', COUNT(*) FROM laundry_services WHERE price < 0
UNION ALL SELECT 'H. negative or zero amounts','laundry_rooms', COUNT(*) FROM laundry_rooms WHERE capacity < 0;
SELECT 'H. rating outside 1..5' AS check_name, id, rating FROM reviews WHERE rating NOT BETWEEN 1 AND 5;
SELECT 'H. slot end not after start' AS check_name, time_slot_id FROM time_slots WHERE end_time <= start_time;
SELECT 'H. nulls in columns the code always fills' AS check_name,
       SUM(date IS NULL) AS time_slot_date, SUM(start_time IS NULL) AS time_slot_start, SUM(end_time IS NULL) AS time_slot_end
FROM time_slots;
SELECT 'H. payments with null status/method' AS check_name, SUM(status IS NULL) AS null_status, SUM(payment_method IS NULL) AS null_method FROM payments;


SELECT 'I. order_item.subtotal <> quantity*unit_price' AS check_name, order_item_id FROM order_item WHERE ABS(subtotal - quantity*unit_price) > 0.005;
SELECT 'I. orders.total_amount <> sum of items' AS check_name, o.order_id, o.total_amount, SUM(i.subtotal) AS items_total
FROM orders o JOIN order_item i ON i.order_id = o.order_id GROUP BY o.order_id, o.total_amount HAVING ABS(o.total_amount - SUM(i.subtotal)) > 0.005;


SELECT 'J. overlapping machine bookings' AS check_name, b1.booking_id AS booking_a, b2.booking_id AS booking_b, b1.machine_id
FROM bookings b1 JOIN time_slots s1 ON s1.time_slot_id = b1.time_slot_id
JOIN bookings b2 ON b2.machine_id = b1.machine_id AND b2.booking_id > b1.booking_id
JOIN time_slots s2 ON s2.time_slot_id = b2.time_slot_id
WHERE b1.status <> 'CANCELLED' AND b2.status <> 'CANCELLED'
  AND s1.date = s2.date AND s1.start_time < s2.end_time AND s1.end_time > s2.start_time;

SELECT table_name, table_rows AS approx_rows FROM information_schema.tables WHERE table_schema = DATABASE() ORDER BY table_name;
