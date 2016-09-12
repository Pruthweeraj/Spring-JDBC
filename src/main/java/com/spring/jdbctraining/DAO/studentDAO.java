package com.spring.jdbctraining.DAO;

import com.spring.jdbctraining.model.*;
import rx.Observable;

public interface studentDAO 
{

	public Observable<Student> getAllStudents();

	public Student getOnestudents(int studentid);

	public Observable<Boolean> saveStudents(Student student);

	public Observable<Boolean> Updatestudents(Student student);
	
	public Observable<Boolean> deletestudents(int studentid);
}
