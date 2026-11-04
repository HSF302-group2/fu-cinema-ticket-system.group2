package com.fu.cinema.desktop.session;

import com.fu.cinema.dto.SessionUser;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.springframework.stereotype.Component;

@Component
public class DesktopSession {

    private final ReadOnlyStringWrapper displayName =
            new ReadOnlyStringWrapper("Chưa đăng nhập");

    private SessionUser currentUser;

    public void login(SessionUser user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "Thông tin người dùng không được null"
            );
        }

        currentUser = user;
        displayName.set(user.getDisplayName());
    }

    public void logout() {
        currentUser = null;
        displayName.set("Chưa đăng nhập");
    }

    public SessionUser getCurrentUser() {
        return currentUser;
    }

    public ReadOnlyStringProperty displayNameProperty() {
        return displayName.getReadOnlyProperty();
    }
}