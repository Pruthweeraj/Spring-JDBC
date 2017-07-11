package com.spring.jdbctraining.DAO;

import com.github.pgasync.Transaction;
import com.spring.jdbctraining.model.*;
import rx.Completable;
import rx.Observable;
import rx.Single;

public interface StudentDAO
{
	Observable<Student> getAllStudents(Transaction transaction);

	Observable<Student> getStudent(Transaction transaction, int studentId);

	Completable saveStudent(Transaction transaction, Student student);

	Completable updateStudent(Transaction transaction, Student student);
	
	Completable deleteStudent(Transaction transaction, int studentId);
}
