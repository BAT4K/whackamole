package com.whackamole.services;

import com.whackamole.exceptions.HighScoreException;
import com.whackamole.models.PlayerScore;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 4: Handles all file I/O and persistence logic using Serialization.
 */
public class HighScoreManager {

    private static final String SCORE_FILE = "scores.dat";

    /**
     * Saves a list of scores to a file using Object Serialization.
     * @param scores The list of PlayerScore objects to save.
     * @throws HighScoreException if an IOException occurs.
     */
    public void saveScores(List<PlayerScore> scores) throws HighScoreException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORE_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            throw new HighScoreException("Failed to save high scores to file.", e);
        }
    }

    /**
     * Loads a list of scores from a file using Object Deserialization.
     * @return The list of PlayerScore objects.
     * @throws HighScoreException if an IOException or ClassNotFoundException occurs.
     */
    @SuppressWarnings("unchecked")
    public List<PlayerScore> loadScores() throws HighScoreException {
        List<PlayerScore> scores = new ArrayList<>();
        File file = new File(SCORE_FILE);

        if (!file.exists()) {
            return scores;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                scores = (List<PlayerScore>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new HighScoreException("Failed to load high scores from file.", e);
        }

        return scores;
    }
}