package com.fu.cinema.service;

import com.fu.cinema.dto.ChangePasswordForm;
import com.fu.cinema.dto.CustomerForm;
import com.fu.cinema.dto.ProfileForm;
import com.fu.cinema.entity.Account;
import com.fu.cinema.entity.Customer;
import com.fu.cinema.enums.AccountStatus;
import com.fu.cinema.enums.Role;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.repository.AccountRepository;
import com.fu.cinema.repository.BookingRepository;
import com.fu.cinema.repository.CustomerRepository;
import com.fu.cinema.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer sampleCustomer;
    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = new Account("an@gmail.com", "encoded_pwd", Role.CUSTOMER, AccountStatus.ACTIVE);
        sampleAccount.setAccountId(1L);

        sampleCustomer = new Customer();
        sampleCustomer.setCustomerId(1L);
        sampleCustomer.setFullName("Nguyen Van An");
        sampleCustomer.setEmail("an@gmail.com");
        sampleCustomer.setMobile("0901234567");
        sampleCustomer.setBirthday(LocalDate.now().minusYears(20));
        sampleCustomer.setIdentityCard("012345678901");
        sampleCustomer.setAccount(sampleAccount);
    }

    @Test
    void search_shouldReturnPage() {
        when(customerRepository.search(eq("an"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleCustomer)));

        Page<Customer> result = customerService.search("an", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(customerRepository).search(eq("an"), any(Pageable.class));
    }

    @Test
    void getForm_shouldReturnMappedForm() {
        when(customerRepository.findWithAccountById(1L)).thenReturn(Optional.of(sampleCustomer));

        CustomerForm form = customerService.getForm(1L);

        assertNotNull(form);
        assertEquals("Nguyen Van An", form.getFullName());
        assertEquals("an@gmail.com", form.getEmail());
        assertEquals(AccountStatus.ACTIVE, form.getStatus());
    }

    @Test
    void create_success() {
        CustomerForm form = new CustomerForm();
        form.setFullName("Tran Van B");
        form.setEmail("b@gmail.com");
        form.setMobile("0909876543");
        form.setBirthday(LocalDate.now().minusYears(18));
        form.setIdentityCard("123456789012");
        form.setPassword("Secret123");
        form.setStatus(AccountStatus.ACTIVE);

        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(customerRepository.existsByMobile(anyString())).thenReturn(false);
        when(customerRepository.existsByIdentityCard(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("encoded");
        when(accountRepository.save(any(Account.class))).thenReturn(sampleAccount);

        assertDoesNotThrow(() -> customerService.create(form));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void create_duplicateEmail_throwsException() {
        CustomerForm form = new CustomerForm();
        form.setEmail("an@gmail.com");
        form.setPassword("123456");
        form.setBirthday(LocalDate.now().minusYears(18));

        when(customerRepository.existsByEmail("an@gmail.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> customerService.create(form));
        assertTrue(ex.getMessage().contains("Email"));
    }

    @Test
    void create_under13_throwsException() {
        CustomerForm form = new CustomerForm();
        form.setEmail("kid@gmail.com");
        form.setPassword("123456");
        form.setBirthday(LocalDate.now().minusYears(10));

        BusinessException ex = assertThrows(BusinessException.class, () -> customerService.create(form));
        assertTrue(ex.getMessage().contains("13 tuổi"));
    }

    @Test
    void delete_withoutBookings_shouldDeleteBoth() {
        when(customerRepository.findWithAccountById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(bookingRepository.existsByCustomer_CustomerId(1L)).thenReturn(false);

        boolean deleted = customerService.delete(1L);

        assertTrue(deleted);
        verify(customerRepository).delete(sampleCustomer);
        verify(accountRepository).delete(sampleAccount);
    }

    @Test
    void delete_withBookings_shouldLockAccountOnly_BR04() {
        when(customerRepository.findWithAccountById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(bookingRepository.existsByCustomer_CustomerId(1L)).thenReturn(true);

        boolean deleted = customerService.delete(1L);

        assertFalse(deleted);
        assertEquals(AccountStatus.LOCKED, sampleAccount.getStatus());
        verify(customerRepository, never()).delete(any());
        verify(accountRepository, never()).delete(any());
    }

    @Test
    void toggleLock_shouldSwitchStatus() {
        when(customerRepository.findWithAccountById(1L)).thenReturn(Optional.of(sampleCustomer));

        customerService.toggleLock(1L);
        assertEquals(AccountStatus.LOCKED, sampleAccount.getStatus());

        customerService.toggleLock(1L);
        assertEquals(AccountStatus.ACTIVE, sampleAccount.getStatus());
    }

    @Test
    void changePassword_success() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("OldPass123");
        form.setNewPassword("NewPass123");
        form.setConfirmPassword("NewPass123");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
        when(passwordEncoder.matches("OldPass123", "encoded_pwd")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123")).thenReturn("new_encoded_pwd");

        assertDoesNotThrow(() -> customerService.changePassword(1L, form));
        assertEquals("new_encoded_pwd", sampleAccount.getPassword());
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsException() {
        ChangePasswordForm form = new ChangePasswordForm();
        form.setCurrentPassword("WrongPass");
        form.setNewPassword("NewPass123");
        form.setConfirmPassword("NewPass123");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
        when(passwordEncoder.matches("WrongPass", "encoded_pwd")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> customerService.changePassword(1L, form));
        assertTrue(ex.getMessage().contains("Mật khẩu hiện tại không đúng"));
    }
}