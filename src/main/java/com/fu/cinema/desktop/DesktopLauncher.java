package com.fu.cinema.desktop;

import javafx.application.Application;

public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "false");
        Application.launch(CinemaFxApp.class, args);
    }
}