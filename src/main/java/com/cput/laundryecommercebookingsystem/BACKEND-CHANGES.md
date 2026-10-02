# Backend fixes: what changed and what to do

## How to install

1. Back up your current `src/main/java/com/cput/laundryecommercebookingsystem` folder (or commit it to Git).
2. Replace it with the `laundryecommercebookingsystem` folder from this zip.
3. Run the two database statements below (or use the "fresh database" option).
4. Start the backend, then the frontend.

`pom.xml` and `application.properties` are unchanged. No new dependency is needed.

## Database changes Hibernate will not make for you

`ddl-auto=update` adds new columns and tables but does not change existing columns. Two existing columns need a manual change.

Option A, keep your data. Run in MySQL:

    ALTER TABLE payments MODIFY service_id BIGINT NULL;
    ALTER TABLE products MODIFY product_id BIGINT NOT NULL AUTO_INCREMENT;

If the second statement is refused because `order_item` references `products`, use option B, or drop that foreign key, run the statement, and add the key back.

Option B, fresh database (deletes all test data):

    DROP DATABASE laundrybooking_ecommerce;
    CREATE DATABASE laundrybooking_ecommerce;

Hibernate recreates every table on the next start.

## Existing student accounts

Passwords are now stored hashed. Accounts created before this change still hold the plain password; they keep working, and each one is converted to a hash the first time that student logs in.

## Tests

If the project has tests that build these classes by hand, they need the new constructor arguments:
- `BookingServiceImpl(bookingRepository, notificationService)`
- `LaundryMachineServiceImpl(machineRepository, roomRepository, timeSlotRepository, bookingRepository)`

## Endpoints

New:

| Method and path | Purpose |
|---|---|
| POST `/student/login` | Body `{ "email", "password" }`. Returns the student, or 401. |
| GET `/laundrymachine/available?timeSlotId=3` | Machines not out of order and not booked for that slot |
| GET `/timeslot/upcoming?days=7` | Slots from today onward |
| GET `/timeslot/date/{date}` | Slots on one date (2026-10-05) |
| GET `/timeslot/available` | Slots flagged available |
| GET, POST, DELETE `/product/...` | `getall`, `read/{id}`, `create`, `update`, `delete/{id}` |
| GET `/review/getall`, `/review/service/{id}`, `/review/student/{id}`, `/review/summary` | Read reviews |
| POST `/review/create` | Body `{ "studentId", "serviceId", "rating", "comment" }` |
| DELETE `/review/delete/{id}` | Delete a review |
| GET `/notification/student/{id}` and `/unread` | A student's notifications |
| PUT `/notification/{id}/read`, `/notification/student/{id}/read-all` | Mark as read |
| GET `/order-item/student/{id}`, `/order-item/order/{id}` | Order items for a student or an order |

Changed:

| Method and path | Change |
|---|---|
| POST `/api/bookings` | Takes query parameters `studentId`, `machineId`, `timeSlotId`, optional `serviceId`. The total is calculated on the server. |
| PUT `/api/bookings/{id}/cancel` | Now cancels the booking and frees the slot |
| DELETE `/api/bookings/{id}` | Now deletes the booking |
| POST `/laundrymachine/create` | Takes one JSON body: `{ "machineNumber", "type", "status", "laundryRoomId" }` |
| POST `/student/create` | Validates, rejects a duplicate email with 409, hashes the password |
| POST `/student/update` | Changes name, email and phone; the password only when a new one is sent |
| POST `/payment/create` | `status` is optional and defaults to PENDING; `serviceId` is optional |

Errors now come back as a status code with a plain-text message: 400 invalid request, 404 not found, 409 conflict (double booking, duplicate email).

## Changes by file

### Muso Nkuntsu
- `config/GlobalCORS.java`: the CORS settings were defined but never applied. Now registered through `WebMvcConfigurer`, with headers and PATCH allowed.
- `controller/BookingController.java`: five `@RequestBody` parameters replaced with `@RequestParam`; total calculated from the service price; errors handled centrally.
- `service/impl/BookingServiceImpl.java`: `cancelBooking` and `deleteBooking` implemented (they returned null); cancelled bookings no longer block a slot; out-of-order machines rejected; a notification is sent on booking and on cancelling.
- `factory/BookingFactory.java`: a total of R0 is allowed (it was rejected, so bookings without a service failed); fixed `createBookingWithStatus`, which only set the service when it was null.
- `repository/iBookingRepository.java`: two queries added.
- `domain/Booking.java`: linked records load with the booking so JSON works.
- `service/impl/NotificationServiceImpl.java`: `@Service` was missing, so Spring never created it. Added, plus `markAllAsRead`.
- `service/INotificationService.java`, `domain/Notification.java`: small supporting changes.
- New: `controller/NotificationController.java`.

### Sabotseng Ndaba
- `service/impl/StudentServiceImpl.java`: registration failed because `createdAt` was empty. Now uses `StudentFactory`, checks for a duplicate email, hashes the password. `updateStudent` no longer overwrites the password or creation date. `login` added.
- `controller/StudentController.java`: `/login` added.
- `domain/Student.java`: the password is accepted but never included in responses.
- `service/IStudentService.java`: `login` added.
- `controller/OrderItemController.java`, `service/IOrderItemService.java`, `service/impl/OrderItemServiceImpl.java`, `repository/IOrderItemRepository.java`: order items by student and by order.
- `domain/OrderItem.java`: linked records load with the item so JSON works.

### Talent Nocuze
- `domain/LaundryRoom.java`: the machine list is left out of JSON (room and machine pointed at each other endlessly); `equals` compared IDs by reference.
- `repository/ILaundryRoomRepository.java`, `service/ILaundryRoomService.java`, `service/impl/LaundryRoomServiceImpl.java`, `controller/LaundryRoomController.java`: the room ID was `int`/`Integer` but the entity uses `Long`.
- `domain/Order.java`: the item list is left out of JSON for the same loop reason.

### Lindokuhle Nanto
- `controller/LaundryMachineController.java`: four `@RequestBody` parameters replaced with one request body; `/available` added.
- `service/impl/LaundryMachineServiceImpl.java`: `createMachine` implemented (it threw "not supported"); `getAvailableMachines` added.
- `service/ILaundryMachineService.java`: `getAvailableMachines` added.
- `repository/IReviewRepository.java`: `findByLaundryService(Long)` compared a service with a number; now `findByLaundryServiceId`.
- `service/impl/ReviewServiceImpl.java`: uses the corrected query; leftover stub removed.
- New: `controller/ReviewController.java`.

### Libolwetu Nokenke
- `controller/TimeSlotController.java`, `service/TimeSlotService.java`, `service/impl/TimeSlotServiceImpl.java`, `repository/TimeSlotRepository.java`: upcoming, by-date and available endpoints.
- `domain/Payment.java`: the service link is optional (it was required, so paying for a booking failed).
- `controller/PaymentController.java`: `status` defaults to PENDING.

### Snalo
- `domain/Product.java`: the ID is now generated automatically.
- `repository/IProductRepository.java`: ID type corrected from `String` to `Long`.
- New: `service/IProductService.java`, `service/impl/ProductServiceImpl.java`, `controller/ProductController.java`.

### New shared files
- `config/GlobalExceptionHandler.java`: one place that turns errors into status codes and messages.
- `util/PasswordHasher.java`: password hashing using Java's built-in PBKDF2.
- `dto/`: `LoginRequest`, `CreateMachineRequest`, `CreateReviewRequest`, `ReviewSummary`.

## Not done

- No tokens or sessions: endpoints still trust the student ID the caller sends. Fine for a demo; say so in the report.
- No cart or checkout: there is no endpoint to create an order from the shop.
- Payments are only recorded as PENDING; nothing is charged.
- A time slot's `available` flag is not changed by bookings. Availability is worked out per machine instead.
- The database password is still in `application.properties`.
