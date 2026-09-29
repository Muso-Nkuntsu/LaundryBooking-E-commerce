package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.service.IStudentService;
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
        return IStudentRepository.save(student);
    }

    @Override
    @Transactional
    public Student updateStudent(Student student) {
        if (!IStudentRepository.existsById(student.getStudentId())) {
            throw new NoSuchElementException("Student not found.");
        }
        return IStudentRepository.save(student);
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
