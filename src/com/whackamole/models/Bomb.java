package com.whackamole.models;

import com.whackamole.services.ResourceLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Module 1: Concrete implementation for a Bomb (penalty).
 */
public class Bomb extends HoleOccupant {

    private static List<Image> BOMB_FRAMES;
    private static final int HIDE_Y_POSITION = -5;
    private boolean isHit = false;

    public Bomb(int timeRemaining) {
        super(timeRemaining);
    }

    @Override
    public int whack() {
        if (isHit || !visible) return 0;
        isHit = true;
        this.hide();
        return -1;
    }

    @Override
    public Node getDisplayNode() {
        return createBombNode();
    }

    /**
     * Creates a node containing ONLY the animated bomb sprite.
     */
    private Node createBombNode() {
        if (BOMB_FRAMES == null) {
            BOMB_FRAMES = Stream.of(
                    "Bomb1.png", "Bomb2.png", "Bomb3.png", "Bomb4.png"
            ).map(ResourceLoader::load).collect(Collectors.toList());
        }

        StackPane stack = new StackPane();
        stack.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);

        ImageView bombView = new ImageView(BOMB_FRAMES.get(0));
        bombView.setFitWidth(100);
        bombView.setFitHeight(100);
        bombView.setPreserveRatio(true);
        bombView.setTranslateY(HIDE_Y_POSITION);

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        for (int i = 0; i < BOMB_FRAMES.size(); i++) {
            final int frameIndex = i;
            KeyFrame kf = new KeyFrame(
                    Duration.millis(150 * (i + 1)),
                    e -> bombView.setImage(BOMB_FRAMES.get(frameIndex))
            );
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();

        stack.getChildren().add(bombView);
        return stack;
    }
}