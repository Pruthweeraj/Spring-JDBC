package com.spring.jdbctraining.service;

import com.github.pgasync.Db;
import com.github.pgasync.Transaction;
import com.spring.jdbctraining.DAO.StudentDAO;
import com.spring.jdbctraining.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Completable;
import rx.Observable;

import java.util.Date;
import java.util.function.Function;
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
        return transactionalObservable(transaction -> studentDAO.getAllStudents(transaction));
    }


    @Override
    public Observable<Student> getStudent(int studentId) {
        return transactionalObservable(transaction -> studentDAO.getStudent(transaction, studentId));
    }

    @Override
    public Completable saveStudent(Student student) {
        return transactionalCompletable(transaction -> studentDAO.saveStudent(transaction, student));
    }

    @Override
    public Completable updateStudent(Student student) {
        return transactionalCompletable(transaction -> studentDAO.updateStudent(transaction, student));
    }

    @Override
    public Completable deleteStudent(int studentId) {
        return transactionalCompletable(transaction -> studentDAO.deleteStudent(transaction, studentId));
    }

    private Completable transactionalCompletable(Function<Transaction, Completable> actionToCompletable) {
        Function<Transaction, Observable<Void>> actionToObservable = transaction -> actionToCompletable.apply(transaction).toObservable();
        return transactionalObservable(actionToObservable).toCompletable();
    }

    @Override
    public Observable<Student> createStudentsAndGetAllStudents() {
        return addStudentAndGetAllStudents()
                .count()
                .doOnNext(c -> LOGGER.info("Got " + c + " students"))
                .toCompletable().<Student>toObservable()
                .concatWith(addStudentAndGetAllStudents());
    }

    private Observable<Student> addStudentAndGetAllStudents() {
        return transactionalObservable(transaction ->
                studentDAO.saveStudent(transaction, newStudent())
                        .<Student>toObservable()
                        .concatWith(studentDAO.getAllStudents(transaction)));
    }

    private Student newStudent() {
        Student student = new Student();
        student.setDob(new Date());
        student.setEmail("nicu.mara@gma.com");
        student.setId(21L);
        student.setMobno(12341223L);
        student.setName("nicu marasiou");
        return student;
    }

    private <T> Observable<T> transactionalObservable(Function<Transaction, Observable<T>> activity) {
        return database.begin()
                .flatMap(transaction -> {
                    Observable<T> decoratedObservable = activity.apply(transaction);
                    Observable<T> commitCompletable = commit(transaction, decoratedObservable);
                    Observable<T> rollbackCompletable = rollback(transaction, commitCompletable);
                    return rollbackCompletable;
                });
    }

    private <T> Observable<T> commit(Transaction transaction, Observable<T> decoratedObservable) {
        return decoratedObservable
                .doOnCompleted(() -> LOGGER.info("Committing"))
                .concatWith(Observable.defer(() -> transaction.commit().toCompletable().toObservable()))
                .doOnCompleted(() -> LOGGER.info("Committed"))
                .doOnError(e -> LOGGER.warn("Error during transaction (rolling back)", e));
    }

    private <T> Observable<T> rollback(Transaction transaction, Observable<T> decoratedObservable) {
        return decoratedObservable
                .doOnError(e -> LOGGER.warn("Error during transaction (rolling back)", e))
                .onErrorResumeNext(e -> Observable.defer(() ->
                        transaction.rollback()
                                .toCompletable().<T>toObservable()
                                .doOnCompleted(() -> LOGGER.info("Rolled back"))
                                .concatWith(Observable.error(e))));
    }

}
