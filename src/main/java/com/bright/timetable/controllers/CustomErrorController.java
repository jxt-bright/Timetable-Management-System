package com.bright.timetable.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("error", "Page Not Found");
                model.addAttribute("message", "The requested page could not be found");
                return "error/404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("error", "Server Error");
                model.addAttribute("message", "Something went wrong on our side");
                return "error/500";
            }
        }
        return "error/error";
    }
}
