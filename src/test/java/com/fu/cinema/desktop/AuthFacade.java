package com.fu.cinema.desktop;

import com.fu.cinema.desktop.facade.AuthFacade;
import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DesktopFacadeTest {

    @Autowired
    private AuthFacade authFacade;

    @Test
    void adminWithCorrectPassword_canLoginDesktop() {
        SessionUser user = authFacade.loginAdmin(
                "admin@fucinema.vn",
                "Admin@123"
        );

        assertNotNull(user);
        assertTrue(user.isAdmin());
    }

    @Test
    void adminWithWrongPassword_cannotLoginDesktop() {
        assertThrows(
                BusinessException.class,
                () -> authFacade.loginAdmin(
                        "admin@fucinema.vn",
                        "WrongPassword123"
                )
        );
    }

    @Test
    void customerAccount_cannotLoginDesktop() {
        assertThrows(
                BusinessException.class,
                () -> authFacade.loginAdmin(
                        "an.nv@gmail.com",
                        "Customer@123"
                )
        );
    }
}