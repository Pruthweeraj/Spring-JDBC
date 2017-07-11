package com.spring.jdbctraining.service;

import com.spring.jdbctraining.model.Student;
import rx.Completable;
import rx.Observable;

public interface StudentService {
    Observable<Student> getAllStudents();

    Observable<Student> getStudent(int studentId);

    Completable saveStudent(Student student);

    Completable updateStudent(Student student);

    Completable deleteStudent(int studentId);

    Observable<Student> createStudentsAndGetAllStudents();
}
