package com.spring.jdbctraining.DAO;

import com.spring.jdbctraining.model.*;
import rx.Observable;
import rx.Single;

public interface StudentDAO
{
	Observable<Student> getAllStudents();

	Observable<Student> getStudent(int studentId);

	Observable<Long> saveStudent(Student student);

	Observable<Long> updateStudent(Student student);
	
	Observable<Long> deleteStudent(int studentId);
}
