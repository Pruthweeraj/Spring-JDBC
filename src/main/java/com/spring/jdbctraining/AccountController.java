package com.spring.jdbctraining;

import com.spring.jdbctraining.DAO.StudentDAO;
import com.spring.jdbctraining.model.Student;
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


@Controller
public class AccountController {


    @Autowired
    public StudentDAO studentImpl;


    @RequestMapping(value = "/student.html", method = RequestMethod.GET)
    public DeferredResult<ModelAndView> show_users(Model model) {
        DeferredResult<ModelAndView> modelAndViewDeferredResult = new DeferredResult<>();
        studentImpl.getAllStudents()
                .toList()
                .map(students -> {
                    ModelAndView modelAndView = new ModelAndView("home");
                    modelAndView.addObject("students", students);
                    return modelAndView;
                }).subscribe(modelAndView -> modelAndViewDeferredResult.setResult(modelAndView), error -> error.printStackTrace());
        return modelAndViewDeferredResult;
    }


    @RequestMapping(value = "/account.html", method = RequestMethod.GET)
    public ModelAndView manage_account(Model model) {
        java.util.List<Student> students = studentImpl.getAllStudents().toList().toBlocking().single();


        ModelAndView modelAndView = new ModelAndView("account");
        modelAndView.addObject("students", students);
        return modelAndView;
    }

    @RequestMapping(value = "/remove/{id}.html", method = RequestMethod.GET)
    public String manage_account_remove(@PathVariable("id") Integer id, Model model) {
        studentImpl.deleteStudent(id);


        return "redirect:/account.html?success=true";
    }

    @RequestMapping(value = "/update/{id}.html", method = RequestMethod.GET)
    public ModelAndView manage_account_update(@PathVariable("id") Integer id, Model model) {

        Student student = studentImpl.getStudent(id);

        ModelAndView modelAndView = new ModelAndView("account");
        modelAndView.addObject("studentdata", student);
        return modelAndView;
    }


    @RequestMapping(value = "/submitupdateddata.html", method = RequestMethod.POST)
    public DeferredResult<String> submit_updateddata(@Validated @ModelAttribute("student1") Student student, BindingResult result) {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        studentImpl.updateStudent(student)
                .subscribe(completed->deferredResult.setResult("redirect:/account.html?success=true"),
                        error -> deferredResult.setErrorResult(error));
        return deferredResult;
    }

}
