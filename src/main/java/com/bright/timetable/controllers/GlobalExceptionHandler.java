package com.bright.timetable.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception ex, HttpServletRequest request, Model model) {
        model.addAttribute("error", "Something went wrong!");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return new ModelAndView("error/error");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleScheduleNotFoundException(IllegalArgumentException ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("error", "Resource Not Found");
        mav.addObject("message", ex.getMessage());
        return mav;
    }
}
