package com.fu.cinema.desktop.session;

import com.fu.cinema.dto.SessionUser;
import com.fu.cinema.exception.BusinessException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.springframework.stereotype.Component;

@Component
public class DesktopSession {

    private volatile SessionUser user;

    private final ReadOnlyStringWrapper displayName =
            new ReadOnlyStringWrapper("");

    public void login(SessionUser user) {
        requireFxThread();

        if (user == null || !user.isAdmin()) {
            throw new BusinessException(
                    "Ứng dụng quầy vé chỉ dành cho tài khoản ADMIN"
            );
        }

        this.user = user;
        displayName.set(user.getDisplayName());
    }

    public void logout() {
        requireFxThread();

        user = null;
        displayName.set("");
    }

    public SessionUser getUser() {
        return user;
    }

    public ReadOnlyStringProperty displayNameProperty() {
        return displayName.getReadOnlyProperty();
    }

    public void requireAdmin() {
        SessionUser currentUser = user;

        if (currentUser == null || !currentUser.isAdmin()) {
            throw new BusinessException(
                    "Bạn không có quyền thực hiện thao tác này"
            );
        }
    }

    private void requireFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Cập nhật session phải chạy trên JavaFX thread"
            );
        }
    }
}