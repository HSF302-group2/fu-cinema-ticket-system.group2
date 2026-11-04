package com.fu.cinema.desktop.navigation;

import com.fu.cinema.desktop.controller.MainController;
import com.fu.cinema.desktop.session.DesktopSession;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

@Component
public class SceneManager {

    private static final String CSS = "/desktop/css/desktop.css";

    private final ApplicationContext context;
    private final DesktopSession session;

    private Stage stage;
    private Scene scene;
    private MainController activeMainController;

    public SceneManager(
            ApplicationContext context,
            DesktopSession session
    ) {
        this.context = context;
        this.session = session;
    }

    public void init(Stage stage) {
        requireFxThread();

        this.stage = stage;
        this.scene = new Scene(new StackPane(), 1200, 720);

        scene.getStylesheets().add(
                requireResource(CSS).toExternalForm()
        );

        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(640);
        stage.setOnHidden(event -> disposeMainController());
        stage.show();
    }

    public void switchTo(View view) {
        requireFxThread();
        requireInitialized();

        Loaded<Object> loaded = load(view);

        disposeMainController();

        scene.setRoot(loaded.root());
        stage.setTitle(view.title());

        if (loaded.controller() instanceof MainController controller) {
            activeMainController = controller;
        }
    }

    public void showIn(BorderPane host, View view) {
        requireFxThread();

        host.setCenter(load(view).root());
    }

    public <C> Loaded<C> load(View view) {
        requireFxThread();

        if (view != View.LOGIN) {
            session.requireAdmin();
        }

        FXMLLoader loader = new FXMLLoader(
                requireResource(view.path())
        );

        loader.setControllerFactory(context::getBean);

        try {
            Parent root = loader.load();

            return new Loaded<>(root, loader.getController());
        } catch (IOException ex) {
            throw new UncheckedIOException(
                    "Không nạp được " + view.path(), ex
            );
        }
    }

    public Stage getStage() {
        return stage;
    }

    private void disposeMainController() {
        if (activeMainController != null) {
            activeMainController.dispose();
            activeMainController = null;
        }
    }

    private URL requireResource(String path) {
        URL resource = getClass().getResource(path);

        if (resource == null) {
            throw new IllegalStateException(
                    "Không tìm thấy tài nguyên: " + path
            );
        }

        return resource;
    }

    private void requireInitialized() {
        if (stage == null || scene == null) {
            throw new IllegalStateException(
                    "Cần khởi tạo SceneManager trước"
            );
        }
    }

    private void requireFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "Thao tác giao diện phải chạy trên JavaFX thread"
            );
        }
    }

    public record Loaded<C>(Parent root, C controller) {
    }
}