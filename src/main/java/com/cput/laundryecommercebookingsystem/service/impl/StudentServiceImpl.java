package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.service.IStudentService;
import com.cput.laundryecommercebookingsystem.factory.StudentFactory;
import com.cput.laundryecommercebookingsystem.util.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/*StudentServiceImpl.java
 * Author: Sabotseng Ndaba (230235875)
 * Date: 28 July 2026
 */

@Service
public class StudentServiceImpl implements IStudentService {

    private final IStudentRepository IStudentRepository;

    public StudentServiceImpl(IStudentRepository IStudentRepository) {
        this.IStudentRepository = IStudentRepository;
    }

    @Override
    @Transactional
    public Student createStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student details are required.");
        }

        // The factory checks the required fields and sets the creation date.
        Student validated = StudentFactory.createStudent(
                trim(student.getFirstName()),
                trim(student.getLastName()),
                normaliseEmail(student.getEmail()),
                trim(student.getPhoneNumber()),
                student.getPassword());

        if (validated == null) {
            throw new IllegalArgumentException(
                    "First name, last name, email, phone number and password are all required.");
        }
        if (validated.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (IStudentRepository.findByEmail(validated.getEmail()).isPresent()) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        Student studentToSave = new Student.Builder()
                .setFirstName(validated.getFirstName())
                .setLastName(validated.getLastName())
                .setEmail(validated.getEmail())
                .setPhoneNumber(validated.getPhoneNumber())
                .setPassword(PasswordHasher.hash(validated.getPassword()))
                .setCreatedAt(validated.getCreatedAt())
                .build();

        return IStudentRepository.save(studentToSave);
    }

    @Override
    @Transactional
    public Student updateStudent(Student student) {
        if (student == null || student.getStudentId() == null) {
            throw new IllegalArgumentException("Student ID is required.");
        }

        Student existing = IStudentRepository.findById(student.getStudentId())
                .orElseThrow(() -> new NoSuchElementException("Student not found."));

        String firstName = valueOrExisting(student.getFirstName(), existing.getFirstName());
        String lastName = valueOrExisting(student.getLastName(), existing.getLastName());
        String phoneNumber = valueOrExisting(student.getPhoneNumber(), existing.getPhoneNumber());
        String email = student.getEmail() == null || student.getEmail().isBlank()
                ? existing.getEmail()
                : normaliseEmail(student.getEmail());

        if (!email.equals(existing.getEmail()) && IStudentRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        // The password only changes when a new one is sent; the creation date never changes.
        String password = student.getPassword() == null || student.getPassword().isBlank()
                ? existing.getPassword()
                : PasswordHasher.hash(student.getPassword());

        Student updated = new Student.Builder()
                .setStudentId(existing.getStudentId())
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setPassword(password)
                .setCreatedAt(existing.getCreatedAt())
                .build();

        return IStudentRepository.save(updated);
    }

    @Override
    @Transactional
    public Optional<Student> login(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }

        Optional<Student> found = IStudentRepository.findByEmail(normaliseEmail(email));
        if (found.isEmpty() || !PasswordHasher.matches(password, found.get().getPassword())) {
            return Optional.empty();
        }

        Student student = found.get();
        if (!PasswordHasher.isHashed(student.getPassword())) {
            // Upgrade an account that still stores the plain password.
            student = IStudentRepository.save(new Student.Builder()
                    .setStudentId(student.getStudentId())
                    .setFirstName(student.getFirstName())
                    .setLastName(student.getLastName())
                    .setEmail(student.getEmail())
                    .setPhoneNumber(student.getPhoneNumber())
                    .setPassword(PasswordHasher.hash(password))
                    .setCreatedAt(student.getCreatedAt())
                    .build());
        }
        return Optional.of(student);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String normaliseEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private static String valueOrExisting(String value, String existing) {
        return value == null || value.isBlank() ? existing : value.trim();
    }

    @Override
    @Transactional
    public void deleteStudent(Long studentId) {
        IStudentRepository.deleteById(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> getStudentById(Long studentId) {
        return IStudentRepository.findById(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> getStudentByEmail(String email) {
        return IStudentRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> getStudentByPhoneNumber(String phoneNumber) {
        return IStudentRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return IStudentRepository.findAll();
    }
}
