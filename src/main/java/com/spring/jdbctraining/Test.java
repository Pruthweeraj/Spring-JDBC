package com.spring.jdbctraining;

import com.spring.jdbctraining.service.StudentService;
import com.spring.jdbctraining.spring.SpringConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Test {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(SpringConfig.class)
                .getBean(StudentService.class)
                .createStudentsAndGetAllStudents()
                .subscribe();
    }
}
