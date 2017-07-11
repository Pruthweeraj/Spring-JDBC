package com.spring.jdbctraining.DAO;


import com.github.pgasync.Db;
import com.spring.jdbctraining.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.function.Supplier;

@Service
public class StudentDAOImpl implements StudentDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentDAOImpl.class);
    private final Db database;

    @Autowired
    public StudentDAOImpl(Supplier<Db> dbProvider) {
        this.database = dbProvider.get();
    }

    @Override
    public Observable<Student> getAllStudents() {
        return database
                .queryRows("select id,name,password,email,mobno,dob from student")
                .map(row -> {
                    Student student = new Student();
                    int idx = 0;
                    student.setId(row.getLong(idx++));
                    student.setName(row.getString(idx));
                    return student;
                });
    }

    @Override
    public Observable<Student> getStudent(int studentId) {
        return getAllStudents().first();
    }

    @Override
    public Observable<Void> saveStudent(Student student) {
        String query = "insert into student (id, name, password, email, mobno) values ($1,$2,$3,$4,$5)";
        return dml(query, student);
    }

    @Override
    public Observable<Void> updateStudent(Student student) {
        String query = "update student set name=$2, password=$3, email=$4, mobno=$5 where id=$1";
        return database.querySet(query, toParams(student)).cast(Void.class);
    }
//    @Override
    public Observable<Void> updateStudentInExplicitTransaction(Student student) {
        String query = "update student set name=$2, password=$3, email=$4, mobno=$5 where id=$1";
        return dml(query, student);
    }

    @Override
    public Observable<Void> deleteStudent(int studentId) {
        return dml("delete from student where id=$1").cast(Void.class);
    }

    private Observable<Void> dml(String query, Student student) {
        return dml(query, toParams(student));
    }

    private Object[] toParams(Student student) {
        return new Object[]{student.getId(), student.getName(), student.getPassword(), student.getEmail(), student.getMobno()/*, student.getDob()*/};
    }

    private Observable<Void> dml(String query, Object... params) {
        return database.begin()
                .flatMap(transaction ->
                        transaction.querySet(query, params)
                                .flatMap(resultSet -> transaction.commit())
                                .doOnError(e -> {
                                    LOGGER.warn("Error during transaction (rolling back)", e);
                                    transaction.rollback();
                                }));
    }

}
