package com.spring.jdbctraining.DAO;


import com.github.pgasync.Db;
import com.spring.jdbctraining.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.function.Supplier;

@Service
public class StudentDAOImpl implements StudentDAO {
    private final Db db;

    @Autowired
    public StudentDAOImpl(Supplier<Db> dbProvider) {
        this.db = dbProvider.get();
    }

    @Override
    public Observable<Student> getAllStudents() {
        return db
                .queryRows("select id,name,password,email,mobno,dob from student")
                .map(empRow -> {
                    Student emp = new Student();
                    int idx = 0;
                    emp.setId(empRow.getInt(idx++));
                    emp.setName(empRow.getString(idx++));
                    emp.setPassword(String.valueOf(empRow.getString(idx++)));
                    emp.setEmail(String.valueOf(empRow.getString(idx++)));
//                        emp.setMobno(Long.parseLong(empRow._5()));
                    emp.setDob(empRow.getDate(idx++));
                    return emp;
                });
    }

    @Override
    public Student getStudent(int Studentid) {
        return getAllStudents().first().single().toBlocking().single();
    }

    @Override
    public Observable<Boolean> saveStudent(Student student) {
        String query = "insert into student (id, name, password ,email,mobno ,dob) values (?,?,?,?,?,?)";
        return dml(query, student);
    }

    @Override
    public Observable<Boolean> updateStudent(Student student) {
        String query = "update student set name=?, password=? , email=? , mobno=?,dob=?  where id=?";
        return dml(query, student);
    }

    @Override
    public Observable<Boolean> deleteStudent(int studentid) {
        return dml("delete from student where id=?");
    }

    private Observable<Boolean> dml(String query, Student student) {
        Object[] params = {student.getId(), student.getName(), student.getPassword(), student.getEmail(), student.getMobno(), student.getDob()};
        return dml(query, params);
    }

    private Observable<Boolean> dml(String query, Object... params) {
        return db.begin().flatMap(transaction ->
                transaction.querySet(query, params)
                        .flatMap(resultSet -> transaction.commit())
                        .map(voidd -> true)
        ).onErrorResumeNext(Observable.just(false));
    }

}
