package com.spring.jdbctraining.DAO;


import com.github.pgasync.Transaction;
import com.spring.jdbctraining.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rx.Completable;
import rx.Observable;

import java.util.Arrays;

@Service
public class StudentDAOImpl implements StudentDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudentDAOImpl.class);

    @Override
    public Observable<Student> getAllStudents(Transaction transaction) {
        return transaction
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
    public Observable<Student> getStudent(Transaction transaction, int studentId) {
        return getAllStudents(transaction)
                .filter(student -> student.getId().equals(new Long(studentId)))
                .first();
    }

    @Override
    public Completable saveStudent(Transaction transaction, Student student) {
        return dml(transaction, "insert into student (id, name, password, email, mobno) values ($1,$2,$3,$4,$5)", student);
    }

    @Override
    public Completable updateStudent(Transaction transaction, Student student) {
        return dml(transaction, "update student set name=$2, password=$3, email=$4, mobno=$5 where id=$1", student);
    }

    @Override
    public Completable deleteStudent(Transaction transaction, int studentId) {
        return dml(transaction, "delete from student where id=$1");
    }

    private Completable dml(Transaction transaction, String query, Student student) {
        return dml(transaction, query, toParams(student));
    }

    private Completable dml(Transaction transaction, String query, Object... params) {
        LOGGER.info("Sending query={} with params={}", query, Arrays.asList(params));
        return transaction.querySet(query, params).toCompletable();
    }

    private Object[] toParams(Student student) {
        return new Object[]{student.getId(), student.getName(), student.getPassword(), student.getEmail(), student.getMobno()/*, student.getDob()*/};
    }

}
