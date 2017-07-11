package com.spring.jdbctraining;

import com.spring.jdbctraining.model.Student;
import com.spring.jdbctraining.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;
import rx.Observable;


@Controller
public class AccountController {
    @Autowired
    public StudentService studentService;

    @RequestMapping(value = "/student.html", method = RequestMethod.GET)
    public DeferredResult<ModelAndView> showStudents(Model model) {
        Observable<ModelAndView> observable = studentService.getAllStudents()
                .toList()
                .map(students -> {
                    ModelAndView modelAndView = new ModelAndView("home");
                    modelAndView.addObject("students", students);
                    return modelAndView;
                });
        DeferredResult<ModelAndView> deferredResult = new DeferredResult<>();
        observable.subscribe(result -> deferredResult.setResult(result), e -> deferredResult.setErrorResult(e));
        return deferredResult;
    }

    @RequestMapping(value = "/account.html", method = RequestMethod.GET)
    public DeferredResult<ModelAndView> manage_account(Model model) {
        Observable<ModelAndView> observable = studentService.getAllStudents()
                .toList()
                .map(students -> {
                    ModelAndView modelAndView = new ModelAndView("account");
                    modelAndView.addObject("students", students);
                    return modelAndView;
                });
        DeferredResult<ModelAndView> deferredResult = new DeferredResult<>();
        observable.subscribe(result -> deferredResult.setResult(result), e -> deferredResult.setErrorResult(e));
        return deferredResult;
    }

    @RequestMapping(value = "/remove/{id}.html", method = RequestMethod.GET)
    public DeferredResult<String> manage_account_remove(@PathVariable("id") Integer id, Model model) {
        Observable<String> observable = studentService.deleteStudent(id)
                .take(1)
                .map(signal -> "redirect:/account.html?success=true");
        DeferredResult<String> deferredResult = new DeferredResult<>();
        observable.subscribe(result -> deferredResult.setResult(result), e -> deferredResult.setErrorResult(e));
        return deferredResult;
    }

    @RequestMapping(value = "/update/{id}.html", method = RequestMethod.GET)
    public DeferredResult<ModelAndView> manage_account_update(@PathVariable("id") Integer id, Model model) {
        Observable<ModelAndView> observable = studentService.getStudent(id)
                .map(student -> {
                    ModelAndView modelAndView = new ModelAndView("account");
                    modelAndView.addObject("studentdata", student);
                    return modelAndView;
                });
        DeferredResult<ModelAndView> deferredResult = new DeferredResult<>();
        observable.subscribe(result -> deferredResult.setResult(result), e -> deferredResult.setErrorResult(e));
        return deferredResult;
    }

    @RequestMapping(value = "/submitupdateddata.html", method = RequestMethod.POST)
    public DeferredResult<String> submit_updateddata(@Validated @ModelAttribute("student1") Student student, BindingResult result) {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        studentService.updateStudent(student)
                .subscribe(completed -> deferredResult.setResult("redirect:/account.html?success=true"),
                        error -> deferredResult.setErrorResult(error));
        return deferredResult;
    }


}
