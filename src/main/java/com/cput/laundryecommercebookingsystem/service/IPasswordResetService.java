package com.cput.laundryecommercebookingsystem.service;

public interface IPasswordResetService {

    /** Emails a reset link. Does nothing (and says nothing) when the email has no account. */
    void requestReset(String email);

    /** Sets a new password using the token from the emailed link. 400 when the link is invalid or expired. */
    void resetPassword(String rawToken, String newPassword);
}
