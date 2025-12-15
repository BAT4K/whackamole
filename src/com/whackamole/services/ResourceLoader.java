package com.whackamole.services;

import javafx.scene.image.Image;
import java.io.InputStream;

/**
 * A utility class to robustly load resources (like images)
 * from the classpath (i.e., from *inside* the JAR file).
 */
public class ResourceLoader {

    /**
     * Loads an image from the root of the classpath.
     * @param fileName The name of the file (e.g., "Mole.png")
     * @return The loaded Image object.
     */
    public static Image load(String fileName) {
        // Look for the file at the root of the classpath
        String path = "/" + fileName;

        InputStream stream = ResourceLoader.class.getResourceAsStream(path);

        if (stream == null) {
            System.err.println("CRITICAL ERROR: Could not load resource: " + path);
            throw new RuntimeException("Failed to load resource: " + path);
        }

        return new Image(stream);
    }
}