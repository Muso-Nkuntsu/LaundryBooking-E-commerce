package com.cput.laundryecommercebookingsystem.repository;
/*
* MUso Nkuntsu
* 231223722
* 27 July 2026*/
import com.cput.laundryecommercebookingsystem.domain.Notification;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.NotificationType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface iNotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStudent(Student student);

    List<Notification> findByStudentAndIsReadFalse(Student student);
    List<Notification> findByStudentAndType(Student student, NotificationType notificationType);
}
