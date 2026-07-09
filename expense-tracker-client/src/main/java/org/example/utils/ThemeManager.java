package org.example.utils;

import javafx.scene.Scene;
import java.util.Objects;

public final class ThemeManager {

    private static final String LIGHT = "/style-light.css";

    private ThemeManager() {}

    public static void apply(Scene scene) {
        // Disabled loading of style-light.css to allow style.css (which contains premium squircle/gradient cards) to work.
    }
}