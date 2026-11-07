package com.cput.laundryecommercebookingsystem.domain;

/*Student.java
Domain entity for the Laundry Booking E-commerce System
Author: Sabotseng Ndaba(230235875)
Date: 25 July 2026
 */

import com.cput.laundryecommercebookingsystem.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    // Accepted when registering or logging in, never sent back in a response.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Set by the verification flow only; a client cannot send these in a request body.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false)
    private boolean emailVerified;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime emailVerifiedAt;

    // Set by an administrator only; a client cannot send these in a request body.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Role role;

    // A deactivated account cannot log in, and its token stops working straight away.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean disabled;

    protected Student(){
    }

    private Student(Builder builder){
        this.studentId = builder.studentId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.password = builder.password;
        this.createdAt = builder.createdAt;
        this.emailVerified = builder.emailVerified;
        this.emailVerifiedAt = builder.emailVerifiedAt;
        this.role = builder.role;
        this.disabled = builder.disabled;
    }

    //Getters
    public Long getStudentId() {
        return studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    /** Accounts created before roles were added have no role yet, so they count as USER. */
    public Role getRole() {
        return role == null ? Role.USER : role;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void markEmailVerified(LocalDateTime verifiedAt) {
        this.emailVerified = true;
        this.emailVerifiedAt = verifiedAt;
    }

    public void changePasswordHash(String passwordHash) {
        this.password = passwordHash;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    //Builder Pattern
    public static class Builder{
        private Long studentId;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String password;
        private LocalDateTime createdAt;
        private boolean emailVerified;
        private LocalDateTime emailVerifiedAt;
        private Role role;
        private boolean disabled;

        public Builder setStudentId(Long studentId){
            this.studentId = studentId;
            return this;
        }
        public Builder setFirstName(String firstName){
            this.firstName = firstName;
            return this;
        }
        public Builder setLastName(String lastName){
            this.lastName = lastName;
            return this;
        }
        public Builder setEmail(String email){
            this.email = email;
            return this;
        }
        public Builder setPhoneNumber(String phoneNumber){
            this.phoneNumber = phoneNumber;
            return this;
        }
        public Builder setPassword(String password){
            this.password = password;
            return this;
        }
        public Builder setCreatedAt(LocalDateTime createdAt){
            this.createdAt = createdAt;
            return this;
        }
        public Builder setEmailVerified(boolean emailVerified){
            this.emailVerified = emailVerified;
            return this;
        }
        public Builder setEmailVerifiedAt(LocalDateTime emailVerifiedAt){
            this.emailVerifiedAt = emailVerifiedAt;
            return this;
        }
        public Builder setRole(Role role){
            this.role = role;
            return this;
        }
        public Builder setDisabled(boolean disabled){
            this.disabled = disabled;
            return this;
        }

        public Student build(){
            return new Student(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student student)) return false;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    @Override
    public String toString() {
        // No password hash or personal details, so nothing sensitive ends up in a log.
        return "Student{studentId=" + studentId + ", role=" + getRole() + "}";
    }
}
