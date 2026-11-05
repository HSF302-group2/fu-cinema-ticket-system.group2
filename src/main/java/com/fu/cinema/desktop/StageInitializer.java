package com.fu.cinema.desktop;

import com.fu.cinema.desktop.navigation.SceneManager;
import com.fu.cinema.desktop.navigation.View;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final SceneManager sceneManager;                 // TODO 34

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        // init() tạo Scene, nạp desktop.css và tự gọi stage.show()
        sceneManager.init(event.getStage());

        // TODO 36: đổi thành View.LOGIN khi đã có login.fxml.
        // Tạm vào thẳng khung chính để kiểm tra điều hướng của TODO 34.
        sceneManager.switchTo(View.MAIN);
    }
}
