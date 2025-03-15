package com.bright.timetable.controllers;

import com.bright.timetable.models.Schedule;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class generalController {

    @GetMapping("/")
    public ModelAndView landingPage() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("landing");
        return mav;
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("login");
        return mav;
    }

    @PostMapping("/home")
    public ModelAndView showHomePage(@RequestParam("username") String username,
                                     @RequestParam("password") String password) {
        ModelAndView mav = new ModelAndView();

        if ("admin".equals(username) && "admin123".equals(password)) {
            return new ModelAndView("redirect:/admin/schedules");
        }
        else if (username.length() == 10 && username.equals(password)) {
            return new ModelAndView("redirect:/student/course");
        }
        else {
            mav.setViewName("login"); // Show login page again
            mav.addObject("error", "Invalid username or password!");
        }
        return mav;
    }
}
