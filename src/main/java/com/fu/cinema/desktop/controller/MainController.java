package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.navigation.SceneManager;
import com.fu.cinema.desktop.navigation.View;
import com.fu.cinema.desktop.session.DesktopSession;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
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

    private static final DateTimeFormatter CLOCK =
            DateTimeFormatter.ofPattern("HH:mm:ss  dd/MM/yyyy");

    private final SceneManager sceneManager;
    private final DesktopSession session;

    @FXML
    private BorderPane root;

    @FXML
    private Label lblUser;

    @FXML
    private Label lblClock;

    private Timeline clock;

    @FXML
    private void initialize() {
        session.requireAdmin();

        lblUser.textProperty().bind(
                Bindings.concat(
                        "Nhân viên: ",
                        session.displayNameProperty()
                )
        );

        onDashboard();

        updateClock();

        clock = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event -> updateClock()
                )
        );

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Dừng đồng hồ khi MainController bị tháo khỏi Scene.
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && clock != null) {
                clock.stop();
            }
        });
    }

    @FXML
    private void onDashboard() {
        show(View.DASHBOARD);
    }

    @FXML
    private void onCounterSale() {
        show(View.COUNTER_SALE);
    }

    @FXML
    private void onBooking() {
        show(View.BOOKING);
    }

    @FXML
    private void onGenre() {
        show(View.GENRE);
    }

    @FXML
    private void onMovie() {
        show(View.MOVIE);
    }

    @FXML
    private void onSchedule() {
        show(View.SCHEDULE);
    }

    @FXML
    private void onReport() {
        show(View.REPORT);
    }

    @FXML
    private void onLogout() {
        session.logout();

        if (clock != null) {
            clock.stop();
        }

        sceneManager.switchTo(View.LOGIN);
    }

    public void dispose() {
        if (clock != null) {
            clock.stop();
            clock = null;
        }

        if (lblUser != null) {
            lblUser.textProperty().unbind();
        }
    }

    private void updateClock() {
        lblClock.setText(LocalDateTime.now().format(CLOCK));
    }

    /**
     * Load child screen into center.
     * Display placeholder if the FXML has not been implemented.
     */
    private void show(View view) {
        if (getClass().getResource(view.path()) == null) {
            root.setCenter(placeholder(view));
            return;
        }

        sceneManager.showIn(root, view);
    }

    private VBox placeholder(View view) {
        Label title = new Label(view.title());
        title.getStyleClass().add("page-title");

        Label note = new Label(
                "Màn hình này chưa được triển khai."
        );

        VBox box = new VBox(title, note);
        box.getStyleClass().add("page");

        return box;
    }
}