package com.spring.jdbctraining.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;


import javax.sql.DataSource;


import com.github.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.spring.jdbctraining.model.Student;
import rx.Observable;

@Service
public class StudentDAOImpl implements StudentDAO {

    @Autowired
    private DataSource dataSource;


    @Override
    public Observable<Student> getAllStudents() {
        Database db = Database.from("jdbc:mysql://localhost:3306/shopping","root","petclinic");

        Class<String> stringClass = String.class;

        return db
                .select("select id,name,password,email,mobno,dob from student")
                .getAs(Integer.class, stringClass, stringClass, stringClass, stringClass, Date.class)
                .map(empRow->{
                        Student emp = new Student();
                        emp.setId(empRow._1());
                        emp.setName(String.valueOf(empRow._2()));
                        emp.setPassword(String.valueOf(empRow._3()));
                        emp.setEmail(String.valueOf(empRow._4()));
//                        emp.setMobno(Long.parseLong(empRow._5()));
                        emp.setDob(empRow._6());
                        return emp;
                    });
    }

    @Override
    public Student getOnestudents(int Studentid) {
        String query = "select id, name, password,email,dob,mobno from student where id = ?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        //using RowMapper anonymous class, we can create a separate RowMapper for reuse
        Student emp = jdbcTemplate.queryForObject(query, new Object[]{Studentid}, new RowMapper<Student>() {

            @Override
            public Student mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                Student emp = new Student();
                emp.setId((rs.getInt("id")));
                emp.setName(rs.getString("name"));
                emp.setPassword(rs.getString("password"));
                emp.setEmail(rs.getString("email"));
                emp.setMobno(rs.getLong("mobno"));
                emp.setDob(rs.getDate("dob"));
                return emp;

            }
        });

        return emp;
    }

    @Override
    public boolean saveStudents(Student student) {

        String query = "insert into student (id, name, password ,email,mobno ,dob) values (?,?,?,?,?,?)";

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        Object[] args = new Object[]{student.getId(), student.getName(), student.getPassword(), student.getEmail(), student.getMobno(), student.getDob()};

        int out = jdbcTemplate.update(query, args);

        if (out != 0) {
            System.out.println("Employee saved with id=" + student.getId());
            return true;
        } else System.out.println("Employee save failed with id=" + student.getId());
        return false;


    }

    @Override
    public boolean Updatestudents(Student student) {

        String query = "update student set name=?, password=? , email=? , mobno=?,dob=?  where id=?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Object[] args = new Object[]{student.getName(), student.getPassword(), student.getEmail(), student.getMobno(), student.getDob(), student.getId()};

        int out = jdbcTemplate.update(query, args);
        if (out != 0) {
            System.out.println("student updated with id=" + student.getId());
            return true;
        } else System.out.println("No Employee found with id=" + student.getId());
        return false;
    }

    @Override
    public boolean deletestudents(int studentid) {


        String query = "delete from student where id=?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        int out = jdbcTemplate.update(query, studentid);
        if (out != 0) {

            System.out.println("Employee deleted with id=" + studentid);
            return true;
        }
        return false;

    }

}
