package com.spring.jdbctraining.DAO;

import com.spring.jdbctraining.model.*;
import rx.Observable;

public interface StudentDAO
{

	public Observable<Student> getAllStudents();

	public Student getOnestudents(int studentid);

	public boolean saveStudents(Student student);

	public boolean Updatestudents(Student student);
	
	public boolean deletestudents(int studentid);
}
