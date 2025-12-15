package com.whackamole.models;

import javafx.scene.Node;

/**
 * Module 1: Abstract base class for all objects that can occupy a hole.
 * Demonstrates Abstraction. All occupants share common state and behavior.
 */
public abstract class HoleOccupant {

    protected boolean visible;
    protected int timeRemaining;
    protected int ticksLived;

    public HoleOccupant(int timeRemaining) {
        this.timeRemaining = timeRemaining;
        this.visible = true;
        this.ticksLived = 0;
    }

    /**
     * The abstract "contract" for what happens when an occupant is "whacked".
     * @return The change in score.
     */
    public abstract int whack();

    /**
     * The abstract "contract" for getting the visual representation.
     * @return The Node to be displayed in the GUI.
     */
    public abstract Node getDisplayNode();

    /**
     * A common, implemented behavior.
     * @return true if the occupant's time has expired, false otherwise.
     */
    public boolean tick() {
        ticksLived++;
        if (ticksLived >= timeRemaining) {
            this.visible = false;
            return true;
        }
        return false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void hide() {
        this.visible = false;
    }
}