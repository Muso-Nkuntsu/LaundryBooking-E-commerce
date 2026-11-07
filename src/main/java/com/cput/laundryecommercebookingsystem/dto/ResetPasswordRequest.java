package com.cput.laundryecommercebookingsystem.dto;

public record ResetPasswordRequest(String token, String newPassword) {
}
