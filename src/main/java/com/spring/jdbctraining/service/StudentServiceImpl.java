package com.spring.jdbctraining.service;

import com.github.pgasync.Db;
import com.github.pgasync.Transaction;
import com.spring.jdbctraining.DAO.StudentDAO;
import com.spring.jdbctraining.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Func1;

import java.util.Date;
import java.util.Random;
import java.util.function.Supplier;

@Service
public class StudentServiceImpl implements StudentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentServiceImpl.class);
    private final Db database;
    private final StudentDAO studentDAO;

    @Autowired
    public StudentServiceImpl(Supplier<Db> dbProvider, StudentDAO studentDAO) {
        this.database = dbProvider.get();
        this.studentDAO = studentDAO;
    }

    @Override
    public Observable<Student> getAllStudents() {
        return transactionalWork(transaction -> studentDAO.getAllStudents(transaction));
    }


    @Override
    public Observable<Student> getStudent(int studentId) {
        return transactionalWork(transaction -> studentDAO.getStudent(transaction, studentId));
    }

    @Override
    public Observable<Void> saveStudent(Student student) {
        return transactionalWork(transaction -> studentDAO.saveStudent(transaction, student));
    }

    @Override
    public Observable<Void> updateStudent(Student student) {
        return transactionalWork(transaction -> studentDAO.updateStudent(transaction, student));
    }

    @Override
    public Observable<Void> deleteStudent(int studentId) {
        return transactionalWork(transaction -> studentDAO.deleteStudent(transaction, studentId));
    }

    @Override
    public Observable<Student> createStudentsAndGetAllStudents() {
        return getStudentObservable()
                .toList().map(list->list.size())
                .doOnNext(c->LOGGER.info("Got "+c+" students"))
                .ignoreElements().cast(Student.class)
                .concatWith(Observable.defer(()->getStudentObservable()));
    }

    private Observable<Student> getStudentObservable() {
        return transactionalWork(transaction -> {
            Observable<Student> firstStudent =
                    studentDAO.saveStudent(transaction, newStudent())
                            .cast(Student.class)
                            .concatWith(Observable.defer(() -> studentDAO.getAllStudents(transaction)));
           return firstStudent;
        });
    }

    private Student newStudent() {
        Student student = new Student();
        student.setDob(new Date());
        student.setEmail("nicu.mara@gma.com");
        student.setId(21L);
        student.setMobno(12341223L);
        student.setName("nicu marasiou");
//        student.set
        return student;
    }

    @FunctionalInterface
    interface TransactionalActivity<T> extends Func1<Transaction, Observable<T>> {
//        Observable<T> action(Transaction transaction);
    }

    private <T> Observable<T> transactionalWork(TransactionalActivity<T> activity) {
        return database.begin()
                .flatMap(transaction ->
                        activity.call(transaction)
                                .compose(commitOrRollback(transaction)));
    }

    private <T> Observable.Transformer<T, T> commitOrRollback(Transaction transaction) {
        return origObservable ->
                origObservable
                        .compose(obs -> commit(transaction, obs))
                        .compose(obs -> rollback(transaction, obs));
    }

    private <T> Observable<T> commit(Transaction transaction, Observable<T> decoratedObservable) {
        return decoratedObservable
                .doOnCompleted(() -> LOGGER.info("Committing"))
                .concatWith(Observable.defer(() ->
                        transaction.commit()
                                .ignoreElements()
                                .map(__ -> (T) null)))
                .doOnCompleted(() -> LOGGER.info("Committed"))
                .doOnError(e -> LOGGER.warn("Error during transaction (rolling back)", e));
    }

    private <T> Observable<T> rollback(Transaction transaction, Observable<T> decoratedObservable) {
        return decoratedObservable
                .doOnError(e -> LOGGER.warn("Error during transaction (rolling back)", e))
                .onErrorResumeNext(e -> Observable.defer(() ->
                        transaction.rollback()
                                .ignoreElements()
                                .doOnCompleted(() -> LOGGER.info("Rolled back"))
                                .concatWith(Observable.error(e))
                                .map(__ -> (T) null)));
    }

}
