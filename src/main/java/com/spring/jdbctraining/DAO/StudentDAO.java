package com.spring.jdbctraining.DAO;

import com.spring.jdbctraining.model.*;
import rx.Observable;
import rx.Single;

public interface StudentDAO
{
	Observable<Student> getAllStudents();

	Observable<Student> getStudent(int studentId);

	Observable<Void> saveStudent(Student student);

	Observable<Void> updateStudent(Student student);
	
	Observable<Void> deleteStudent(int studentId);
}
