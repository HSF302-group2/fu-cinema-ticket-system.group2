
package com.fu.cinema.controller;
import com.fu.cinema.dto.RegisterForm;
import com.fu.cinema.dto.LoginForm;
import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.service.AuthService;
import com.fu.cinema.util.SessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new LoginForm());
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("form") LoginForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            SessionUser user = authService.login(
                    form.getUsername(),
                    form.getPassword()
            );

            startSession(request, user);

            if (user.isAdmin()) {
                return "redirect:/admin";
            }

            return "redirect:/";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") RegisterForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra validation của RegisterForm
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(form);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đăng ký thành công. Vui lòng đăng nhập."
            );

            return "redirect:/login";

        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        }
    }

    @PostMapping("/logout")
    public String logout(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        SessionUtils.clearSession(request);
        redirectAttributes.addFlashAttribute(
                "success", "Đã đăng xuất thành công"
        );

        return "redirect:/login";
    }

    private void startSession(
            HttpServletRequest request,
            SessionUser user) {

        HttpSession oldSession = request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        SessionUtils.setCurrentUser(newSession, user);
    }
}
