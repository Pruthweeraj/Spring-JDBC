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
                    emp.setId(empRow.getLong(idx++));
                    emp.setName(empRow.getString(idx++));
//                    emp.setPassword(empRow.getString(idx++));
//                    idx++;
//                    emp.setEmail(empRow.getString(idx++));
//                    emp.setMobno(empRow.getLong(idx++));
//                    emp.setDob(empRow.getDate(idx++));
                    return emp;
                });
    }

    @Override
    public Observable<Student> getStudent(int studentId) {
        return getAllStudents().first();
    }

    @Override
    public Observable<Long> saveStudent(Student student) {
        String query = "insert into student (id, name, password ,email,mobno ) values (?,?,?,?,?)";
        return dml(query, student);
    }

    @Override
    public Observable<Long> updateStudent(Student student) {
        String query = "update student set name=?, password=? , email=? , mobno=?  where id=?";
        return dml(query, student);
    }

    @Override
    public Observable<Long> deleteStudent(int studentid) {
        return dml("delete from student where id=?");
    }

    private Observable<Long> dml(String query, Student student) {
        Object[] params = {student.getId(), student.getName(), student.getPassword(), student.getEmail(), student.getMobno()/*, student.getDob()*/};
        return dml(query, params);
    }

    private Observable<Long> dml(String query, Object... params) {
        return db.begin().flatMap(transaction ->
                transaction.querySet(query, params)
                        .flatMap(resultSet -> transaction.commit().map(__ -> resultSet.iterator().next().getLong(0))
                                .doOnError(e-> transaction.rollback()))
        );
    }

}
