package com.cput.laundryecommercebookingsystem.security;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.Payment;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.Role;
import com.cput.laundryecommercebookingsystem.repository.IOrderRepository;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.repository.PaymentRepository;
import com.cput.laundryecommercebookingsystem.support.SecurityTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Talent Nocuze
 * 230405886
 */
@SpringBootTest(properties = "app.security.enforce-account-status=true")
class AdminDashboardSecurityTest {

    private static final String PW = "Secret123";

    @Autowired private WebApplicationContext context;
    @Autowired private IStudentRepository students;
    @Autowired private IOrderRepository orders;
    @Autowired private PaymentRepository payments;
    @Autowired private PasswordEncoder encoder;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = SecurityTestSupport.mockMvc(context);
    }

    // ---------- helpers ----------

    private Student create(String prefix, Role role) {
        return students.save(new Student.Builder().setFirstName("First").setLastName(prefix)
                .setEmail(prefix + UUID.randomUUID() + "@test.com").setPhoneNumber("0821234567")
                .setPassword(encoder.encode(PW)).setRole(role).setCreatedAt(LocalDateTime.now()).build());
    }

    private String bearer(Student s) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"" + s.getEmail() + "\",\"password\":\"" + PW + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.<String>read(body, "$.token");
    }

    private static final String[] ADMIN_GETS = {
            "/api/admin/overview", "/api/admin/reports", "/api/admin/users", "/api/admin/bookings",
            "/api/admin/products", "/api/admin/orders", "/api/admin/payments"};

    // ---------- authorisation ----------

    @Test
    void anonymousCallersGet401_normalUsersGet403_adminsGet200() throws Exception {
        String user = bearer(create("user", Role.USER));
        String admin = bearer(create("admin", Role.ADMIN));
        for (String path : ADMIN_GETS) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).header("Authorization", user)).andExpect(status().isForbidden());
            mvc.perform(get(path).header("Authorization", admin)).andExpect(status().isOk());
        }
    }

    @Test
    void normalUserCannotUseAnyAdminWriteEndpoint() throws Exception {
        Student victim = create("victim", Role.USER);
        String user = bearer(create("user", Role.USER));
        String json = "{}";
        mvc.perform(put("/api/admin/users/" + victim.getStudentId() + "/role").header("Authorization", user)
                .contentType("application/json").content("{\"role\":\"ADMIN\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/users/" + victim.getStudentId() + "/active").header("Authorization", user)
                .contentType("application/json").content("{\"active\":false}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/products").header("Authorization", user)
                .contentType("application/json").content(json)).andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/orders/1/status").header("Authorization", user)
                .contentType("application/json").content("{\"status\":\"PAID\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/payments/1/verify").header("Authorization", user)).andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/bookings/1/status").header("Authorization", user)
                .contentType("application/json").content("{\"status\":\"COMPLETED\"}")).andExpect(status().isForbidden());
        assertEquals(Role.USER, students.findById(victim.getStudentId()).orElseThrow().getRole());
    }

    @Test
    void overviewComesFromTheDatabase_andContainsNoPasswords() throws Exception {
        long before = students.count();
        create("counted", Role.USER);
        String admin = bearer(create("admin", Role.ADMIN));
        String body = mvc.perform(get("/api/admin/overview").header("Authorization", admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(before + 2, JsonPath.<Number>read(body, "$.totalUsers").longValue());
        assertFalse(body.toLowerCase().contains("password"));
    }

    // ---------- users ----------

    @Test
    void adminCanEditAUser_butNotWithAnEmailThatBelongsToSomeoneElse() throws Exception {
        Student target = create("target", Role.USER);
        Student other = create("other", Role.USER);
        String admin = bearer(create("admin", Role.ADMIN));

        mvc.perform(put("/api/admin/users/" + target.getStudentId()).header("Authorization", admin)
                        .contentType("application/json")
                        .content("{\"firstName\":\"New\",\"lastName\":\"Name\",\"email\":\"" + target.getEmail()
                                + "\",\"phoneNumber\":\"0712345678\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("New"));
        assertEquals("New", students.findById(target.getStudentId()).orElseThrow().getFirstName());
        // the password hash must survive an edit
        assertTrue(encoder.matches(PW, students.findById(target.getStudentId()).orElseThrow().getPassword()));

        mvc.perform(put("/api/admin/users/" + target.getStudentId()).header("Authorization", admin)
                        .contentType("application/json")
                        .content("{\"firstName\":\"New\",\"lastName\":\"Name\",\"email\":\"" + other.getEmail()
                                + "\",\"phoneNumber\":\"0712345678\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deactivatedUserCannotLogIn_andTheirExistingTokenStopsWorkingAtOnce() throws Exception {
        Student target = create("target", Role.USER);
        String targetToken = bearer(target);
        mvc.perform(get("/api/auth/me").header("Authorization", targetToken)).andExpect(status().isOk());

        String admin = bearer(create("admin", Role.ADMIN));
        mvc.perform(put("/api/admin/users/" + target.getStudentId() + "/active").header("Authorization", admin)
                        .contentType("application/json").content("{\"active\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));

        mvc.perform(get("/api/auth/me").header("Authorization", targetToken)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"" + target.getEmail() + "\",\"password\":\"" + PW + "\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(put("/api/admin/users/" + target.getStudentId() + "/active").header("Authorization", admin)
                .contentType("application/json").content("{\"active\":true}")).andExpect(status().isOk());
        bearer(target); // can sign in again
    }

    @Test
    void promotingAndDemotingChangesPermissionsImmediately() throws Exception {
        Student target = create("target", Role.USER);
        String oldToken = bearer(target);
        String admin = bearer(create("admin", Role.ADMIN));

        mvc.perform(put("/api/admin/users/" + target.getStudentId() + "/role").header("Authorization", admin)
                        .contentType("application/json").content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
        // the old USER token no longer matches the account's role -> must sign in again
        mvc.perform(get("/api/admin/overview").header("Authorization", oldToken)).andExpect(status().isUnauthorized());
        String newToken = bearer(target);
        mvc.perform(get("/api/admin/overview").header("Authorization", newToken)).andExpect(status().isOk());

        mvc.perform(put("/api/admin/users/" + target.getStudentId() + "/role").header("Authorization", admin)
                .contentType("application/json").content("{\"role\":\"USER\"}")).andExpect(status().isOk());
        mvc.perform(get("/api/admin/overview").header("Authorization", newToken)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/overview").header("Authorization", bearer(target))).andExpect(status().isForbidden());
    }

    @Test
    void roleMustBeAKnownValue() throws Exception {
        Student target = create("target", Role.USER);
        String admin = bearer(create("admin", Role.ADMIN));
        mvc.perform(put("/api/admin/users/" + target.getStudentId() + "/role").header("Authorization", admin)
                .contentType("application/json").content("{\"role\":\"SUPERUSER\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void adminCannotLockThemselvesOut_orRemoveTheLastAdmin() throws Exception {
        Student admin = create("admin", Role.ADMIN);
        String token = bearer(admin);
        mvc.perform(put("/api/admin/users/" + admin.getStudentId() + "/active").header("Authorization", token)
                .contentType("application/json").content("{\"active\":false}")).andExpect(status().isConflict());
        mvc.perform(put("/api/admin/users/" + admin.getStudentId() + "/role").header("Authorization", token)
                .contentType("application/json").content("{\"role\":\"USER\"}")).andExpect(status().isConflict());
        assertTrue(students.findById(admin.getStudentId()).orElseThrow().isActive());
    }

    // ---------- products ----------

    private long createProduct(String admin, String name, int stock) throws Exception {
        String body = mvc.perform(post("/api/admin/products").header("Authorization", admin)
                        .contentType("application/json")
                        .content("{\"name\":\"" + name + "\",\"description\":\"Nice\",\"price\":59.5,"
                                + "\"category\":\"Soap\",\"inventoryQuantity\":" + stock + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.<Number>read(body, "$.productId").longValue();
    }

    @Test
    void productCrud_priceStockActive_andCustomersOnlySeeActiveProducts() throws Exception {
        String admin = bearer(create("admin", Role.ADMIN));
        String user = bearer(create("user", Role.USER));
        String name = "Soap-" + UUID.randomUUID();
        long id = createProduct(admin, name, 10);

        mvc.perform(put("/api/admin/products/" + id).header("Authorization", admin).contentType("application/json")
                        .content("{\"name\":\"" + name + "\",\"description\":\"Better\",\"price\":75.255,"
                                + "\"category\":\"Soap\",\"inventoryQuantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(75.26))
                .andExpect(jsonPath("$.inventoryQuantity").value(4));
        mvc.perform(put("/api/admin/products/" + id + "/stock").header("Authorization", admin)
                        .contentType("application/json").content("{\"inventoryQuantity\":42}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.inventoryQuantity").value(42));

        mvc.perform(get("/product/read/" + id).header("Authorization", user)).andExpect(status().isOk());
        mvc.perform(put("/api/admin/products/" + id + "/active").header("Authorization", admin)
                .contentType("application/json").content("{\"active\":false}")).andExpect(status().isOk());
        mvc.perform(get("/product/read/" + id).header("Authorization", user)).andExpect(status().isNotFound());
        mvc.perform(get("/api/admin/products/" + id).header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));

        mvc.perform(delete("/api/admin/products/" + id).header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.deleted").value(true));
        mvc.perform(get("/api/admin/products/" + id).header("Authorization", admin)).andExpect(status().isNotFound());
    }

    @Test
    void productInputIsValidated() throws Exception {
        String admin = bearer(create("admin", Role.ADMIN));
        mvc.perform(post("/api/admin/products").header("Authorization", admin).contentType("application/json")
                        .content("{\"name\":\"\",\"price\":-5,\"inventoryQuantity\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/products").header("Authorization", admin).contentType("application/json")
                        .content("{\"name\":\"X\",\"price\":5,\"inventoryQuantity\":1,\"imageUrl\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void imageUpload_acceptsRealImages_rejectsDisguisedFiles_andServesOnlyOurOwnNames() throws Exception {
        String admin = bearer(create("admin", Role.ADMIN));
        long id = createProduct(admin, "Pic-" + UUID.randomUUID(), 1);

        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        String body = mvc.perform(multipart("/api/admin/products/" + id + "/image")
                        .file(new MockMultipartFile("file", "x.png", "image/png", png)).header("Authorization", admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String url = JsonPath.read(body, "$.imageUrl");
        assertTrue(url.startsWith("/api/public/product-images/"));
        mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));

        // a script renamed to .png is refused because its CONTENT is not an image
        mvc.perform(multipart("/api/admin/products/" + id + "/image")
                        .file(new MockMultipartFile("file", "evil.png", "image/png", "<script>alert(1)</script>".getBytes()))
                        .header("Authorization", admin))
                .andExpect(status().isBadRequest());
        // names we did not generate are never served
        mvc.perform(get("/api/public/product-images/not-a-real-file.png")).andExpect(status().isNotFound());
    }

    // ---------- orders & payments ----------

    @Test
    void verifyingAnEftPayment_marksItCompleted_andTheOrderPaid() throws Exception {
        Student customer = create("customer", Role.USER);
        String admin = bearer(create("admin", Role.ADMIN));
        String customerToken = bearer(customer);
        Order order = orders.save(new Order.Builder().studentId(customer).totalAmount(120.0).build());

        String created = mvc.perform(post("/payment/create").header("Authorization", customerToken)
                        .contentType("application/json")
                        .content("{\"orderId\":" + order.getOrderId() + ",\"paymentMethod\":\"EFT\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long paymentId = JsonPath.<Number>read(created, "$.paymentId").longValue();

        mvc.perform(get("/api/admin/orders/" + order.getOrderId()).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.paymentStatus").value("Pending"))
                .andExpect(jsonPath("$.payments.length()").value(1));

        mvc.perform(put("/api/admin/payments/" + paymentId + "/verify").header("Authorization", customerToken))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/payments/" + paymentId + "/verify").header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Completed"));

        assertEquals("PAID", orders.findById(order.getOrderId()).orElseThrow().getStatus().name());
        mvc.perform(get("/api/admin/orders/" + order.getOrderId()).header("Authorization", admin))
                .andExpect(jsonPath("$.order.paymentStatus").value("Completed"));
        // a verified payment is final
        mvc.perform(put("/api/admin/payments/" + paymentId + "/reject").header("Authorization", admin))
                .andExpect(status().isConflict());
    }

    @Test
    void adminCanUpdateOrderStatus_andInvalidStatusIsRejected() throws Exception {
        Student customer = create("customer", Role.USER);
        String admin = bearer(create("admin", Role.ADMIN));
        Order order = orders.save(new Order.Builder().studentId(customer).totalAmount(10.0).build());

        mvc.perform(put("/api/admin/orders/" + order.getOrderId() + "/status").header("Authorization", admin)
                        .contentType("application/json").content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        assertEquals("IN_PROGRESS", orders.findById(order.getOrderId()).orElseThrow().getStatus().name());

        mvc.perform(put("/api/admin/orders/" + order.getOrderId() + "/status").header("Authorization", admin)
                        .contentType("application/json").content("{\"status\":\"BOGUS\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectingAPayment_marksItFailed() throws Exception {
        Student customer = create("customer", Role.USER);
        String admin = bearer(create("admin", Role.ADMIN));
        Order order = orders.save(new Order.Builder().studentId(customer).totalAmount(10.0).build());
        Payment pending = payments.save(Payment.builder().setAmount(10.0).setPaymentDate(LocalDateTime.now())
                .setPaymentMethod("EFT").setStatus("Pending").setTransactionRef("LR-TEST" + UUID.randomUUID().toString().substring(0, 6))
                .setOrderId(order.getOrderId()).build());

        mvc.perform(put("/api/admin/payments/" + pending.getPaymentId() + "/reject").header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Failed"));
    }

    @Test
    void bookingStatusRejectsUnknownValues_andMissingBookingsAre404() throws Exception {
        String admin = bearer(create("admin", Role.ADMIN));
        mvc.perform(put("/api/admin/bookings/999999/status").header("Authorization", admin)
                        .contentType("application/json").content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/admin/bookings/1/status").header("Authorization", admin)
                        .contentType("application/json").content("{\"status\":\"WHATEVER\"}"))
                .andExpect(status().isBadRequest());
    }
}
