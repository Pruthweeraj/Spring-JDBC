package com.spring.jdbctraining.DAO;

import com.spring.jdbctraining.model.*;
import rx.Observable;

public interface StudentDAO
{
	Observable<Student> getAllStudents();

	//todo Observable<Student>
	Student getStudent(int studentId);

	Observable<Boolean> saveStudent(Student student);

	Observable<Boolean> updateStudent(Student student);
	
	Observable<Boolean> deleteStudent(int studentId);
}
