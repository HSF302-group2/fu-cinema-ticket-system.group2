
package com.fu.cinema.service.impl;
import org.springframework.transaction.annotation.Transactional;
import com.fu.cinema.dto.RegisterForm;
import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.entity.Account;
import com.fu.cinema.entity.Customer;
import com.fu.cinema.enums.AccountStatus;
import com.fu.cinema.enums.Role;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.repository.AccountRepository;
import com.fu.cinema.repository.CustomerRepository;
import com.fu.cinema.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String INVALID_CREDENTIALS =
            "Tên đăng nhập hoặc mật khẩu không đúng";

    @Override
    public SessionUser login(String username, String password) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        Account account = accountRepository.findByUsername(username.trim())
                .orElseThrow(() ->
                        new BusinessException(INVALID_CREDENTIALS));

        // Dùng BCrypt để so sánh mật khẩu nhập vào với mật khẩu đã mã hóa.
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        // BR-03: Không cho phép tài khoản bị khóa đăng nhập.
        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new BusinessException("Tài khoản đã bị khóa");
        }

        Long customerId = null;
        String displayName = account.getUsername();

        if (account.getRole() == Role.CUSTOMER) {
            Customer customer = customerRepository
                    .findByAccount_AccountId(account.getAccountId())
                    .orElseThrow(() ->
                            new BusinessException(INVALID_CREDENTIALS));

            customerId = customer.getCustomerId();
            displayName = customer.getFullName();
        }

        return new SessionUser(
                account.getAccountId(),
                customerId,
                displayName,
                account.getRole()
        );
    }


    @Override
    @Transactional
    public SessionUser register(RegisterForm form) {
        if (form == null) {
            throw new BusinessException("Dữ liệu đăng ký không hợp lệ");
        }

        String email = form.getEmail() == null
                ? "" : form.getEmail().trim();
        String mobile = form.getMobile() == null
                ? "" : form.getMobile().trim();
        String identityCard = form.getIdentityCard() == null
                ? "" : form.getIdentityCard().trim();
        String fullName = form.getFullName() == null
                ? "" : form.getFullName().trim();

        // Kiểm tra mật khẩu xác nhận
        if (!java.util.Objects.equals(
                form.getPassword(), form.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp");
        }

        // Kiểm tra tuổi tối thiểu 13
        if (form.getBirthday() == null
                || java.time.Period.between(
                form.getBirthday(), java.time.LocalDate.now()
        ).getYears() < 13) {
            throw new BusinessException(
                    "Bạn phải từ đủ 13 tuổi trở lên để đăng ký"
            );
        }

        // BR-01: Email/username, số điện thoại và CCCD phải duy nhất
        if (accountRepository.existsByUsername(email)
                || customerRepository.existsByEmail(email)) {
            throw new BusinessException("Email đã được sử dụng");
        }

        if (customerRepository.existsByMobile(mobile)) {
            throw new BusinessException("Số điện thoại đã được sử dụng");
        }

        if (customerRepository.existsByIdentityCard(identityCard)) {
            throw new BusinessException("CCCD đã được sử dụng");
        }

        // Tạo Account với mật khẩu BCrypt
        Account account = new Account(
                email,
                passwordEncoder.encode(form.getPassword()),
                Role.CUSTOMER,
                AccountStatus.ACTIVE
        );
        account = accountRepository.save(account);

        // Tạo Customer liên kết với Account trong cùng transaction
        Customer customer = new Customer();
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setMobile(mobile);
        customer.setBirthday(form.getBirthday());
        customer.setIdentityCard(identityCard);
        customer.setAccount(account);

        customer = customerRepository.save(customer);

        return new SessionUser(
                account.getAccountId(),
                customer.getCustomerId(),
                customer.getFullName(),
                Role.CUSTOMER
        );
    }

}
