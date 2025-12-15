package com.whackamole.game;

import com.whackamole.models.HoleOccupant;

/**
 * An interface to decouple the GameEngine from the JavaFX GUI.
 * This allows the engine to send updates without knowing about JavaFX.
 */
public interface GameUIUpdater {
    void updateHole(int index, HoleOccupant occupant);
    void updateScore(int score);
    void updateTime(int time);
    void showGameOver();
    void updateHighScore(int highScore);
    void tickTimerEffects(int time);

    /**
     * Tells the GUI to play the "hit" animation for the occupant at a given index.
     * @param index The grid index that was hit.
     * @param occupant The occupant that was hit (for polymorphic animation).
     */
    void playHitAnimation(int index, HoleOccupant occupant);

    /**
     * Tells the GUI to start the multi-stage game over sequence.
     * @param finalScore The player's final score.
     */
    void triggerGameOverSequence(int finalScore);
}