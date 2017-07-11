package com.spring.jdbctraining.DAO;

import com.github.pgasync.Transaction;
import com.spring.jdbctraining.model.*;
import rx.Observable;
import rx.Single;

public interface StudentDAO
{
	Observable<Student> getAllStudents(Transaction transaction);

	Observable<Student> getStudent(Transaction transaction, int studentId);

	Observable<Void> saveStudent(Transaction transaction, Student student);

	Observable<Void> updateStudent(Transaction transaction, Student student);
	
	Observable<Void> deleteStudent(Transaction transaction, int studentId);
}
