package com.whackamole.game;

import com.whackamole.models.*;
import com.whackamole.exceptions.InvalidGameStateException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Module 2: The Game Engine, implements Runnable.
 * This class runs on a separate thread and manages all game logic and state.
 * Logic updated to be an "endless" game with ramping difficulty.
 */
public class GameEngine implements Runnable {

    private volatile boolean gameIsRunning;
    private volatile int timeRemaining;
    private final AtomicInteger score = new AtomicInteger(0);
    private final HoleOccupant[] grid;
    private final GameUIUpdater uiUpdater;
    private final Random random = new Random();
    private final int gridSize;

    // This set prevents re-spawning in the same hole in the same tick
    private Set<Integer> recentlyUsedHoles = new HashSet<>();

    private long gameEndTime;
    private int lastDisplayedTime;
    private long nextTimerEffectTime;

    private static final int GAME_START_SECONDS = 30;
    private static final int BONUS_TIME_SECONDS = 5;

    private static final int STARTING_TICK_MS = 450;
    private static final double STARTING_BOMB_PROBABILITY = 0.10;
    private static final double STARTING_BONUS_MOLE_PROBABILITY = 0.16;
    private static final int MOLE_LIFESPAN_TICKS = 2;
    private static final int BOMB_LIFESPAN_TICKS = 2;
    private static final int BONUS_MOLE_LIFESPAN_TICKS = 2;

    private static final int TICKS_PER_DIFFICULTY_INCREASE = 18;
    private static final int GAME_TICK_DECREMENT_MS = 20;
    private static final int MIN_GAME_TICK_MS = 200;
    private static final double BOMB_PROBABILITY_INCREASE = 0.01;
    private static final double BOMB_RAMP_ACCELERATION = 0.01;
    private static final double MAX_BOMB_PROBABILITY = 0.50;
    private static final double BONUS_MOLE_PROBABILITY_DECREASE = 0.0015;
    private static final double MIN_BONUS_MOLE_PROBABILITY = 0.12;

    private int ticksElapsed;
    private int currentGameTickMs;
    private double currentBombProbability;
    private double currentBonusMoleProbability;
    private double currentBombIncreaseAmount;


    public GameEngine(GameUIUpdater uiUpdater, int gridSize) {
        this.uiUpdater = uiUpdater;
        this.gridSize = gridSize;
        this.grid = new HoleOccupant[gridSize];
    }

    /**
     * Initializes the game state. Called before starting the thread.
     */
    public void initializeGame() {
        score.set(0);
        timeRemaining = GAME_START_SECONDS;
        gameIsRunning = true;

        gameEndTime = System.currentTimeMillis() + (GAME_START_SECONDS * 1000);
        lastDisplayedTime = GAME_START_SECONDS;
        nextTimerEffectTime = 0;

        ticksElapsed = 0;
        currentGameTickMs = STARTING_TICK_MS;
        currentBombProbability = STARTING_BOMB_PROBABILITY;
        currentBonusMoleProbability = STARTING_BONUS_MOLE_PROBABILITY;
        currentBombIncreaseAmount = BOMB_PROBABILITY_INCREASE;

        // Fill the grid with Empty objects to prevent crashes
        synchronized (grid) {
            recentlyUsedHoles.clear();
            for (int i = 0; i < gridSize; i++) {
                grid[i] = new Empty();
                uiUpdater.updateHole(i, grid[i]);
            }
        }

        uiUpdater.updateScore(score.get());
        uiUpdater.updateTime(timeRemaining);
    }

    /**
     * Module 2: The main game loop, executed on a new thread.
     * Decouples game logic from high-frequency timer effects.
     */
    @Override
    public void run() {
        long lastGameTickTime = System.currentTimeMillis(); // For game logic (spawning)

        try {
            while (gameIsRunning) {
                long now = System.currentTimeMillis();

                // --- 1. GAME LOGIC (MOLES/SPAWNING) ---
                // This logic now runs on its own schedule, independent of the main loop's sleep
                if (now - lastGameTickTime >= currentGameTickMs) {
                    lastGameTickTime = now; // Reset the game logic tick timer

                    // Clear the "do not use" list at the start of each *game* tick
                    synchronized (grid) {
                        recentlyUsedHoles.clear();
                    }

                    ticksElapsed++;
                    updateDifficulty();
                    tickOccupants();
                    spawnNewOccupant();
                }

                // --- 2. TIMER AND GAME OVER LOGIC (Runs every loop) ---
                int newTimeRemaining = (int) Math.ceil((gameEndTime - now) / 1000.0);
                if (newTimeRemaining < 0) {
                    newTimeRemaining = 0;
                }

                // Check for game over
                if (now >= gameEndTime) {
                    timeRemaining = 0;
                    if (lastDisplayedTime != 0) {
                        uiUpdater.updateTime(0);
                    }
                    gameIsRunning = false;
                    uiUpdater.triggerGameOverSequence(score.get());
                    continue; // Stop the loop
                }

                // Send a UI update *only* when the second changes
                if (newTimeRemaining != lastDisplayedTime) {
                    lastDisplayedTime = newTimeRemaining;
                    timeRemaining = newTimeRemaining;
                    uiUpdater.updateTime(timeRemaining);
                }

                // --- 3. HIGH-FREQUENCY TIMER EFFECT (Runs every loop) ---
                // This logic is now free to run as fast as the formula dictates
                if (newTimeRemaining <= 10 && newTimeRemaining > 0) {
                    if (now >= nextTimerEffectTime) {
                        // Fire the effect
                        uiUpdater.tickTimerEffects(newTimeRemaining);

                        // (time^2 * 3.5) + 45ms minimum delay
                        long delay = (long) (Math.pow(newTimeRemaining, 2) * 3.5) + 45;

                        nextTimerEffectTime = now + delay;
                    }
                } else if (newTimeRemaining > 10) {
                    // Reset the effect timer if bonus time pushes it > 10
                    nextTimerEffectTime = 0;
                }

                // --- 4. THE NEW SLEEP ---
                // Sleep for a tiny amount (5ms) to prevent 100% CPU usage
                Thread.sleep(5);
            }
        } catch (InterruptedException e) {
            System.out.println("GameEngine thread interrupted, shutting down.");
            gameIsRunning = false;
        }
    }

    /**
     * Increases game speed and bomb frequency over time.
     */
    private void updateDifficulty() {
        if (ticksElapsed > 0 && ticksElapsed % TICKS_PER_DIFFICULTY_INCREASE == 0) {

            if (currentGameTickMs > MIN_GAME_TICK_MS) {
                currentGameTickMs -= GAME_TICK_DECREMENT_MS;
            }

            if (currentBombProbability < MAX_BOMB_PROBABILITY) {
                currentBombProbability += currentBombIncreaseAmount;
                currentBombIncreaseAmount += BOMB_RAMP_ACCELERATION;
            }

            if (currentBonusMoleProbability > MIN_BONUS_MOLE_PROBABILITY) {
                currentBonusMoleProbability -= BONUS_MOLE_PROBABILITY_DECREASE;
            }
        }
    }

    /**
     * Called by the GUI thread when a user clicks a hole.
     * This method is thread-safe.
     * @param index The grid index that was "whacked".
     * @return The result of the whack (0 for miss, >0 for score, <0 for penalty/bonus)
     */
    public int whack(int index) {
        if (!gameIsRunning) return 0;

        int whackResult;
        HoleOccupant occupantToAnimate = null;

        synchronized (grid) {
            HoleOccupant occupant = grid[index];
            whackResult = occupant.whack();

            if (whackResult != 0) {
                occupantToAnimate = occupant;
                grid[index] = new Empty();
                recentlyUsedHoles.add(index);
            }
        }

        if (occupantToAnimate != null) {

            if (whackResult == -1) {
                gameIsRunning = false;
                uiUpdater.triggerGameOverSequence(score.get());

            } else if (whackResult == -2) {
                gameEndTime += (BONUS_TIME_SECONDS * 1000);

            } else if (whackResult > 0) {
                int newScore = score.addAndGet(whackResult);
                uiUpdater.updateScore(newScore);
            }

            uiUpdater.playHitAnimation(index, occupantToAnimate);
        }

        return whackResult;
    }

    /**
     * Stops the game. Called by the main app (e.g., on window close).
     */
    public void stopGame() {
        gameIsRunning = false;
    }

    /**
     * Allows the GUI to check if the game is currently running.
     * @return true if the game loop is active, false otherwise.
     */
    public boolean isGameRunning() {
        return gameIsRunning;
    }

    /**
     * Helper method to advance time for all active occupants.
     */
    private void tickOccupants() {
        synchronized (grid) {
            for (int i = 0; i < gridSize; i++) {
                if (grid[i] != null && !(grid[i] instanceof Empty)) {
                    boolean expired = grid[i].tick();
                    if (expired) {
                        grid[i] = new Empty();
                        recentlyUsedHoles.add(i);
                        uiUpdater.updateHole(i, grid[i]);
                    }
                }
            }
        }
    }

    /**
     * Helper method to randomly spawn a new occupant in an empty hole.
     */
    private void spawnNewOccupant() {

        List<Integer> availableHoles = new ArrayList<>();

        synchronized (grid) {
            for (int i = 0; i < gridSize; i++) {
                if (grid[i] instanceof Empty && !recentlyUsedHoles.contains(i)) {
                    availableHoles.add(i);
                }
            }

            if (availableHoles.isEmpty()) {
                return;
            }

            int index = availableHoles.get(random.nextInt(availableHoles.size()));

            double chance = random.nextDouble();
            if (chance < currentBonusMoleProbability) {
                grid[index] = new BonusMole(BONUS_MOLE_LIFESPAN_TICKS);

            } else if (chance < currentBonusMoleProbability + currentBombProbability) {
                grid[index] = new Bomb(BOMB_LIFESPAN_TICKS);

            } else {
                grid[index] = new Mole(MOLE_LIFESPAN_TICKS);
            }

            recentlyUsedHoles.add(index);
            uiUpdater.updateHole(index, grid[index]);
        }
    }
}