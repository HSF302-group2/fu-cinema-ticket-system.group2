
package com.fu.cinema.controller;

import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.util.SessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addCurrentUser(
            HttpServletRequest request,
            Model model) {

        SessionUser currentUser = SessionUtils.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
    }
}
