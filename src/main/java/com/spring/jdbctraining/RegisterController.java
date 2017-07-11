package com.spring.jdbctraining;

import java.util.Locale;

import com.spring.jdbctraining.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;

import com.spring.jdbctraining.DAO.StudentDAOImpl;
import com.spring.jdbctraining.model.Student;
import com.spring.jdbctraining.model.StudentNameEditor;
import rx.Observable;

@Controller
public class RegisterController {
    @Autowired
    private StudentService studentImpl;

    /**
     * Simply selects the home view to render by returning its name.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home(Locale locale, Model model) {
        return new ModelAndView("home");
    }


    @RequestMapping(value = "/register.html", method = RequestMethod.GET)
    public ModelAndView Register(Model model) {
        return new ModelAndView("register");
    }

    @RequestMapping(value = "/submitregister.html", method = RequestMethod.POST)
    public DeferredResult<ModelAndView> submitregister(@Validated @ModelAttribute("student1") Student student, BindingResult result) {
        DeferredResult<ModelAndView> deferredResult = new DeferredResult<>();
        if (result.hasErrors()) {
            deferredResult.setResult(new ModelAndView("register"));
        } else {
            studentImpl.saveStudent(student)
                    .doOnCompleted(() -> deferredResult.setResult(new ModelAndView("success")))
                    .subscribe();
        }
        return deferredResult;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        //	binder.setDisallowedFields("mobno");
        /*SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        binder.registerCustomEditor(Date.class,"dob", new CustomDateEditor(dateFormat, false));*/
        binder.registerCustomEditor(String.class, "name", new StudentNameEditor());
    }

    @ModelAttribute
    public void commonmessage(Model model) {
        model.addAttribute("commonmessage", "i am a spring developer");
    }

}
