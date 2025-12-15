package com.whackamole.models;

import java.io.Serializable;

/**
 * Module 4: A simple POJO to hold score data.
 * Must implement Serializable to be written to an ObjectOutputStream.
 */
public class PlayerScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private String playerName;
    private int score;

    public PlayerScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public String toString() {
        return playerName + ": " + score;
    }
}