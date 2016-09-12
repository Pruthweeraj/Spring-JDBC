package com.spring.jdbctraining.DAO;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

@Component
public class Database implements Provider<Db> {
    @Override
    public Db get() {
        return new ConnectionPoolBuilder()
                .hostname("localhost")
                .port(5432)
                .database("db")
                .username("postgres")
                .password("mysecretpassword")
                .poolSize(20)
                .build();
    }
}
