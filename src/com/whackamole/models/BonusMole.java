package com.whackamole.models;

import com.whackamole.services.ResourceLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Module 1: Concrete implementation for a BonusMole (high value).
 */
public class BonusMole extends HoleOccupant {

    private static Image BONUS_MOLE_IMG;
    private static final int HIDE_Y_POSITION = -5;
    private boolean isHit = false;

    public BonusMole(int timeRemaining) {
        super(timeRemaining);
    }

    @Override
    public int whack() {
        if (isHit || !visible) return 0;
        isHit = true;
        this.hide();
        return -2;
    }

    @Override
    public Node getDisplayNode() {
        return createBonusNode();
    }

    /**
     * Creates a node containing ONLY the bonus mole sprite.
     */
    private Node createBonusNode() {
        if (BONUS_MOLE_IMG == null) {
            BONUS_MOLE_IMG = ResourceLoader.load("MoleHat.png");
        }

        StackPane stack = new StackPane();
        stack.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);

        ImageView bonusView = new ImageView(BONUS_MOLE_IMG);
        bonusView.setFitWidth(100);
        bonusView.setFitHeight(100);
        bonusView.setPreserveRatio(true);
        bonusView.setTranslateY(HIDE_Y_POSITION);

        stack.getChildren().add(bonusView);
        return stack;
    }
}