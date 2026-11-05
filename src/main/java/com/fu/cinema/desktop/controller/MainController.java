package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.navigation.SceneManager;
import com.fu.cinema.desktop.navigation.View;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MainController {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss  dd/MM/yyyy");

    private final SceneManager sceneManager;
    // TODO 36: thêm "private final DesktopSession session;" rồi bind lblUser với session.displayNameProperty()

    @FXML private BorderPane root;
    @FXML private Label lblUser;
    @FXML private Label lblClock;

    private Timeline clock;

    @FXML
    private void initialize() {
        // TODO 36: lblUser.textProperty().bind(Bindings.concat("Nhân viên: ", session.displayNameProperty()));
        lblUser.setText("Nhân viên: (chưa đăng nhập)");

        lblClock.setText(LocalDateTime.now().format(CLOCK));
        clock = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> lblClock.setText(LocalDateTime.now().format(CLOCK))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Dừng đồng hồ khi khung chính bị gỡ khỏi Scene (đăng xuất / đổi màn hình)
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                clock.stop();
            }
        });

        onDashboard();
    }

    @FXML private void onDashboard()   { show(View.DASHBOARD); }
    @FXML private void onCounterSale() { show(View.COUNTER_SALE); }
    @FXML private void onBooking()     { show(View.BOOKING); }
    @FXML private void onGenre()       { show(View.GENRE); }
    @FXML private void onMovie()       { show(View.MOVIE); }
    @FXML private void onSchedule()    { show(View.SCHEDULE); }
    @FXML private void onReport()      { show(View.REPORT); }

    @FXML
    private void onLogout() {
        if (!exists(View.LOGIN)) {
            // TODO 36: bỏ nhánh này khi đã có login.fxml
            new Alert(Alert.AlertType.INFORMATION,
                    "Chưa có màn hình đăng nhập (TODO 36).").showAndWait();
            return;
        }
        clock.stop();
        // TODO 36: session.logout();
        sceneManager.switchTo(View.LOGIN);
    }

    /** Nạp màn hình con vào vùng center; nếu FXML chưa có thì hiện trang tạm. */
    private void show(View view) {
        if (!exists(view)) {
            root.setCenter(placeholder(view));
            return;
        }
        sceneManager.showIn(root, view);
    }

    private boolean exists(View view) {
        return getClass().getResource(view.path()) != null;
    }

    private VBox placeholder(View view) {
        Label title = new Label(view.title());
        title.getStyleClass().add("page-title");
        Label note = new Label("Màn hình này chưa được triển khai.");
        VBox box = new VBox(title, note);
        box.getStyleClass().add("page");
        return box;
    }
}
