package com.whackamole.models;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Represents an empty hole.
 * This is now a "marker" class. It returns an empty StackPane to signal
 * to the GUI that this hole should be empty.
 */
public class Empty extends HoleOccupant {

    public Empty() {
        super(Integer.MAX_VALUE);
        this.visible = true;
    }

    @Override
    public int whack() {
        return 0; // Whacking empty hole does nothing
    }

    @Override
    public Node getDisplayNode() {
        return new StackPane();
    }

    @Override
    public boolean tick() {
        return false; // Never expires
    }
}