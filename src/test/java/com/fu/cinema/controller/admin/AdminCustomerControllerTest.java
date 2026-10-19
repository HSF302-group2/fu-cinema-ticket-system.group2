package com.fu.cinema.controller.admin;

import com.fu.cinema.dto.CustomerForm;
import com.fu.cinema.service.CustomerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private AdminCustomerController controller;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/customers");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void list_shouldReturnListView() {
        when(customerService.search(any(), anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of()));
        Model model = new ConcurrentModel();

        String view = controller.list("an", 0, model);

        assertEquals("admin/customers/list", view);
        verify(customerService).search(eq("an"), eq(0), anyInt());
    }

    @Test
    void create_withErrors_shouldReturnFormView() {
        CustomerForm form = new CustomerForm();
        when(bindingResult.hasErrors()).thenReturn(true);
        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.create(form, bindingResult, model, ra);

        assertEquals("admin/customers/form", view);
        verify(customerService, never()).create(any());
    }

    @Test
    void create_success_shouldRedirect() {
        CustomerForm form = new CustomerForm();
        form.setFullName("Nguyen Van An");
        when(bindingResult.hasErrors()).thenReturn(false);
        Model model = new ConcurrentModel();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.create(form, bindingResult, model, ra);

        assertEquals("redirect:/admin/customers", view);
        verify(customerService).create(form);
    }

    @Test
    void toggleLock_shouldCallServiceAndRedirect() {
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.toggleLock(1L, ra);

        assertEquals("redirect:/admin/customers", view);
        verify(customerService).toggleLock(1L);
    }

    @Test
    void delete_shouldCallServiceAndRedirect() {
        when(customerService.delete(1L)).thenReturn(true);
        RedirectAttributes ra = new RedirectAttributesModelMap();

        String view = controller.delete(1L, ra);

        assertEquals("redirect:/admin/customers", view);
        verify(customerService).delete(1L);
    }
}