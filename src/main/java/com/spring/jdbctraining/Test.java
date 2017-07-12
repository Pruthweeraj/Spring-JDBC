package com.spring.jdbctraining;

import com.spring.jdbctraining.service.StudentService;
import com.spring.jdbctraining.service.StudentServiceImpl;
import com.spring.jdbctraining.spring.SpringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Test {
    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(SpringConfig.class)
                .getBean(StudentService.class)
                .createStudentsAndGetAllStudents()
                .count()
                .doOnNext(c -> LOGGER.info("Got " + c + " students"))
                .doOnCompleted(()->exit())
                .subscribe();
    }

    private static void exit() {
        LOGGER.info("Closing the JVM, there are hooks, there are no in-progress modifications and we have transactions anyway, so it should be safe");
        System.exit(0);
    }
}
