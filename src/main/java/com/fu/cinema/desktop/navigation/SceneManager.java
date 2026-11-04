package com.fu.cinema.desktop.navigation;

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

    private Stage stage;
    private Scene scene;

    public SceneManager(ApplicationContext context) {
        this.context = context;
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
        stage.show();
    }

    /**
     * Đổi toàn bộ nội dung cửa sổ, ví dụ Login ↔ Main.
     */
    public void switchTo(View view) {
        requireFxThread();
        requireInitialized();

        scene.setRoot(load(view).root());
        stage.setTitle(view.title());
    }

    /**
     * Nạp màn hình con vào vùng center của khung chính.
     */
    public void showIn(BorderPane host, View view) {
        requireFxThread();

        host.setCenter(load(view).root());
    }

    public <C> Loaded<C> load(View view) {
        requireFxThread();

        FXMLLoader loader = new FXMLLoader(
                requireResource(view.path())
        );

        // Lấy controller từ Spring để hỗ trợ dependency injection.
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
        if (scene == null || stage == null) {
            throw new IllegalStateException(
                    "Cần gọi SceneManager.init() trước khi chuyển màn hình"
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