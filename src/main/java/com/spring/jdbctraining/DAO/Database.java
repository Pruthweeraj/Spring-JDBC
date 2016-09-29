package com.spring.jdbctraining.DAO;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.function.Supplier;

@Component
public class Database implements Supplier<Db> {
    @Override
    public Db get() {
        return new ConnectionPoolBuilder()
                .hostname("localhost")
                .port(5432)
                .database("postgres")
                .username("postgres")
                .password("mysecretpassword")
                .poolSize(2)
                .build();
    }
}
