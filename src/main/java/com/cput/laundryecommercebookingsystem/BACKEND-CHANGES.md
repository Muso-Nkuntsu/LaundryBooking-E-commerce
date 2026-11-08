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
- No real payment gateway: card payments are simulated (approved straight away, nothing charged, card details never sent).
- A time slot's `available` flag is not changed by bookings. Availability is worked out per machine instead.
- The database password is still in `application.properties`.

## Update: payment, cart and booking flow fixes

### What changed in behaviour

| Before | Now |
|---|---|
| Every booking was CONFIRMED straight away, even before paying | A booking that costs money starts as **PENDING** (machine held). It becomes CONFIRMED once paid. A free booking (no service) is still confirmed straight away. |
| "Add to cart" did nothing | Real cart in the frontend, and a new checkout endpoint that turns the cart into an order (PENDING until paid) |
| EFT was treated as paid at once, with no bank details | EFT shows the account details and a reference, asks who paid and from which bank, and is saved as a PENDING payment. The booking or order stays PENDING until the money is confirmed. |

### Payment rules (`PaymentServiceImpl`)
- The amount is always the booking's or order's own total, worked out on the server. An amount sent by the browser is ignored.
- **CARD** is simulated: saved as `COMPLETED`, which confirms the booking or marks the order `PAID`, and sends a notification.
- **EFT** is saved as `PENDING` with `transactionRef`, `payerName` and `payerBank`. To confirm it once the money is in the bank: `PUT /payment/{id}/complete`. This confirms the booking, or marks the order PAID, and notifies the student. `PUT /payment/{id}/fail` marks it failed, and the student can pay again or cancel.
- Paying for something that is cancelled, already paid, or already has an EFT waiting to be verified is refused with 409.

### Endpoints
| Method and path | Purpose |
|---|---|
| POST `/api/orders/checkout` | Body `{ "studentId": 1, "items": [ { "productId": 3, "quantity": 2 } ] }`. Prices come from the database, stock is checked and held, and the order starts PENDING. |
| PATCH `/api/orders/{id}/cancel` | Now also puts the held stock back. |
| POST `/payment/create` | Params: `paymentMethod` (CARD or EFT), `bookingId` or `orderId`; for EFT also `transactionRef`, `payerName`, `payerBank`. `amount` and `status` are no longer needed. |
| GET `/payment/order/{orderId}` | New: payments for an order. |

### Files changed
- `service/impl/BookingServiceImpl.java`, `service/IBookingService.java`: paid bookings start PENDING; new `confirmBooking`.
- `service/impl/PaymentServiceImpl.java`, `service/PaymentService.java`, `controller/PaymentController.java`: rules above.
- `domain/Payment.java`, `factory/PaymentFactory.java`: `payerName` and `payerBank` added.
- `service/impl/OrderServiceImpl.java`, `service/IOrderService.java`, `controller/OrderController.java`: checkout, and stock put back on cancel.
- New: `dto/CheckoutRequest.java`.

### Database
Nothing to run by hand: Hibernate adds the two new `payments` columns (`payer_name`, `payer_bank`) on the next start. Existing bookings keep their current status.

### Tests
`PaymentServiceImpl` and `OrderServiceImpl` have new constructor arguments:
- `PaymentServiceImpl(paymentRepository, laundryService, bookingService, orderService, notificationService)`
- `OrderServiceImpl(orderRepository, studentRepository, productRepository)`

## Email verification

New accounts must confirm their email address before they can log in.

How it works:
1. `POST /student/create` saves the student as unverified and emails a link: `<app.frontend-url>/verify-email?token=...`.
2. The React page `/verify-email` sends the token to `POST /student/verify-email`.
3. A valid token marks the student verified (`email_verified`, `email_verified_at`). Invalid, used and expired (24 hours) tokens are rejected with 400.
4. `POST /student/login` returns 403 with a message while the email is unverified (only after the password is correct).
5. `POST /student/resend-verification` sends a new link and cancels the old one. Asking again within 60 seconds gives 429; a mail failure gives 503. The reply is the same for unknown and already-verified emails.
6. Changing the email in `POST /student/update` marks the new address unverified and sends a new link.

Only a SHA-256 hash of each token is stored (new table `email_verification_token`).

To send real emails, fill in the `spring.mail.*` lines in `application.properties` (they are commented out). Until `spring.mail.host` is set, the verification link is printed in the backend console instead.

Existing accounts: the new `email_verified` column defaults to false, so students registered before this change must verify once. On the login page they get a "Send a new verification email" button. To skip that for existing test accounts, run:

    UPDATE student SET email_verified = 1, email_verified_at = NOW();

New dependency: `spring-boot-starter-mail` (run a Maven reload in IntelliJ).

New and changed endpoints:

| Method and path | Purpose |
|---|---|
| POST `/student/verify-email` | Body `{ "token" }`. 200, or 400 when the token is invalid or expired. |
| POST `/student/resend-verification` | Body `{ "email" }`. 200, 429 or 503. |
| POST `/student/login` | Now also returns 403 while the email is unverified. |
