package com.fu.cinema.controller.customer;

import com.fu.cinema.dto.ChangePasswordForm;
import com.fu.cinema.dto.ProfileForm;
import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.enums.Role;
import com.fu.cinema.service.CustomerService;
import com.fu.cinema.util.SessionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerProfileControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private CustomerProfileController controller;

    @Test
    void profile_notLoggedIn_shouldRedirectLogin() {
        MockHttpSession session = new MockHttpSession();
        Model model = new ConcurrentModel();

        String view = controller.profile(session, model);

        assertEquals("redirect:/login", view);
    }

    @Test
    void profile_loggedIn_shouldReturnProfileView() {
        MockHttpSession session = new MockHttpSession();
        SessionUser user = new SessionUser(1L, 2L, "Nguyen Van An", Role.CUSTOMER);
        session.setAttribute(SessionUtils.CURRENT_USER, user);
        when(customerService.getProfile(2L)).thenReturn(new ProfileForm());
        Model model = new ConcurrentModel();

        String view = controller.profile(session, model);

        assertEquals("customer/profile", view);
        verify(customerService).getProfile(2L);
    }

    @Test
    void updateProfile_success_shouldUpdateSessionAndRedirect() {
        MockHttpSession session = new MockHttpSession();
        SessionUser user = new SessionUser(1L, 2L, "Nguyen Van An", Role.CUSTOMER);
        session.setAttribute(SessionUtils.CURRENT_USER, user);

        ProfileForm oldProfile = new ProfileForm();
        oldProfile.setUsername("an@gmail.com");
        when(customerService.getProfile(2L)).thenReturn(oldProfile);

        ProfileForm form = new ProfileForm();
        form.setFullName("Nguyen Van B");
        when(bindingResult.hasErrors()).thenReturn(false);

        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.updateProfile(form, bindingResult, session, model, ra);

        assertEquals("redirect:/customer/profile", view);
        verify(customerService).updateProfile(eq(2L), any(ProfileForm.class));
        SessionUser updatedUser = (SessionUser) session.getAttribute(SessionUtils.CURRENT_USER);
        assertEquals("Nguyen Van B", updatedUser.getDisplayName());
    }

    @Test
    void changePassword_success_shouldCallServiceAndRedirect() {
        MockHttpSession session = new MockHttpSession();
        SessionUser user = new SessionUser(1L, 2L, "Nguyen Van An", Role.CUSTOMER);
        session.setAttribute(SessionUtils.CURRENT_USER, user);

        ChangePasswordForm form = new ChangePasswordForm();
        when(bindingResult.hasErrors()).thenReturn(false);
        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.changePassword(form, bindingResult, session, model, ra);

        assertEquals("redirect:/customer/profile", view);
        verify(customerService).changePassword(eq(1L), eq(form));
    }
}