package com.spring.jdbctraining.DAO;


import com.github.pgasync.Db;
import com.spring.jdbctraining.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

import javax.inject.Provider;

@Service
public class StudentDAOImpl implements studentDAO {

    @Autowired
    private Provider<Db> dbProvider;

    @Override
    public Observable<Student> getAllStudents() {
        return db()
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
    public Student getOnestudents(int Studentid) {
        return getAllStudents().first().single().toBlocking().single();
    }

    @Override
    public Observable<Boolean> saveStudents(Student student) {
        String query = "insert into student (id, name, password ,email,mobno ,dob) values (?,?,?,?,?,?)";
        return dml(query, student);
    }

    @Override
    public Observable<Boolean> Updatestudents(Student student) {
        String query = "update student set name=?, password=? , email=? , mobno=?,dob=?  where id=?";
        return dml(query, student);
    }

    @Override
    public Observable<Boolean> deletestudents(int studentid) {
        return dml("delete from student where id=?");
    }

    private Observable<Boolean> dml(String query, Student student) {
        Object[] params = {student.getId(), student.getName(), student.getPassword(), student.getEmail(), student.getMobno(), student.getDob()};
        return dml(query, params);
    }

    private Observable<Boolean> dml(String query, Object... params) {
        return db().begin().flatMap(transaction ->
                transaction.querySet(query, params)
                        .flatMap(resultSet -> transaction.commit())
                        .map(voidd -> true)
        ).onErrorResumeNext(Observable.just(false));
    }

    private Db db() {
        return dbProvider.get();
    }

}
