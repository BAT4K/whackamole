package com.whackamole.models;

import com.whackamole.services.ResourceLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Module 1: Concrete implementation for a standard Mole.
 */
public class Mole extends HoleOccupant {

    private static Image MOLE_IMG;
    private static final int HIDE_Y_POSITION = -5;
    private boolean isHit = false;

    public Mole(int timeRemaining) {
        super(timeRemaining);
    }

    @Override
    public int whack() {
        if (isHit || !visible) return 0;
        isHit = true;
        this.hide();
        return 100;
    }

    @Override
    public Node getDisplayNode() {
        return createMoleNode();
    }

    /**
     * Creates a node containing ONLY the mole sprite.
     */
    private Node createMoleNode() {
        if (MOLE_IMG == null) {
            MOLE_IMG = ResourceLoader.load("Mole.png");
        }

        StackPane stack = new StackPane();
        stack.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);

        ImageView moleView = new ImageView(MOLE_IMG);
        moleView.setFitWidth(100);
        moleView.setFitHeight(100);
        moleView.setPreserveRatio(true);
        moleView.setTranslateY(HIDE_Y_POSITION);

        stack.getChildren().add(moleView);
        return stack;
    }
}