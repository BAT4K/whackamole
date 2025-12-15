package com.whackamole;

import com.whackamole.exceptions.HighScoreException;
import com.whackamole.game.GameEngine;
import com.whackamole.game.GameUIUpdater;
import com.whackamole.models.*;
import com.whackamole.services.HighScoreManager;
import com.whackamole.services.ResourceLoader;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Cursor;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Module 3: Main Application Class (GUI)
 * Implements the GameUIUpdater interface to safely update the UI from the GameEngine thread.
 */
public class WhackAMoleGame extends Application implements GameUIUpdater {

    // --- GUI Components ---
    private Label scoreValueLabel;
    private Label highScoreValueLabel;
    private Label timeValueLabel;
    private GridPane gameGrid;
    private StackPane gameOverlay;
    private StackPane startupOverlay;
    private Label overlayScoreLabel;
    private Label overlayHighScoreLabel;
    private Button startButton;
    private Button exitButton;
    private Button resetScoreButton;
    private Button muteButton;
    private ImageView malletCursorView;

    // --- Game Logic ---
    private GameEngine gameEngine;
    private Thread gameThread;
    private HighScoreManager highScoreManager;
    private int currentHighScore;
    private static final int GRID_SIZE = 15;
    private List<StackPane> holeContainers = new ArrayList<>();

    // --- Aesthetic Constants ---
    private static final String FONT_FAMILY = "Inter";
    private static final Color DARK_BLUE = Color.web("#2c3e50");
    private static final Color LIGHT_BLUE = Color.web("#ADD8E6");
    private static final Color WOOD_COLOR = Color.web("#a0785a");
    private static final String BUTTON_GREEN = "#449d44";
    private static final String BUTTON_GREY = "#5a5a5a";
    private static final String BUTTON_YELLOW = "#ec971f";
    private static final String BUTTON_HOVER_GREEN = "#5cb85c";
    private static final String BUTTON_HOVER_GREY = "#777777";
    private static final String BUTTON_HOVER_YELLOW = "#f0ad4e";

    // --- Images ---
    private static Image HOLE_BACK_IMG;
    private static Image HOLE_FRONT_IMG;
    private static Image MOLE_HIT_IMG;
    private static Image MOLE_HAT_CRACKS_IMG;
    private static Image MOLE_HAT_HIT_IMG;
    private static Image APP_ICON;
    private static Image STARTUP_LOGO;
    private static Image GAME_OVER_LOGO;
    private static Image BACKGROUND_IMG;
    private static Image MUTE_IMG;
    private static Image UNMUTE_IMG;

    // --- Mallet Animation ---
    private static List<Image> MALLET_FRAMES;
    private Timeline malletAnimation;
    private boolean isTimerRed = false;

    // --- Audio Players ---
    private MediaPlayer introPlayer;
    private MediaPlayer mainMusicPlayer;
    private MediaPlayer gameOverPlayer;
    private MediaPlayer gameStartPlayer;
    private MediaPlayer gameEndPlayer;
    private AudioClip hoverSound;
    private AudioClip selectSound;
    private AudioClip moleAppearSound;
    private AudioClip bombAppearSound;
    private AudioClip hitMissSound;
    private AudioClip hitMoleSound;
    private AudioClip hitBonusMoleSound;
    private AudioClip timerTickSound;
    private boolean isMuted = false;
    private static final double MUSIC_VOLUME = 0.25;
    private static final double SFX_VOLUME = 0.8;

    // --- Animation values ---
    private static final int PEEK_Y_POSITION = -15;
    private static final int HIDE_Y_POSITION = -5;

    // --- Mute Button Views ---
    private ImageView muteIconView;
    private ImageView unmuteIconView;

    /**
     * Loads all images and sounds after the JavaFX toolkit is initialized.
     */
    private void loadAssets() {
        if (HOLE_BACK_IMG != null) return; // Only load once

        // Load Images
        HOLE_BACK_IMG = ResourceLoader.load("HoleBack.png");
        HOLE_FRONT_IMG = ResourceLoader.load("HoleFront.png");
        MOLE_HIT_IMG = ResourceLoader.load("MoleHit.png");
        MOLE_HAT_CRACKS_IMG = ResourceLoader.load("MoleHatCracks.png");
        MOLE_HAT_HIT_IMG = ResourceLoader.load("MoleHatHit.png");
        APP_ICON = ResourceLoader.load("logo.png");
        STARTUP_LOGO = ResourceLoader.load("WhackAMole.png");
        GAME_OVER_LOGO = ResourceLoader.load("GameOver.png");
        BACKGROUND_IMG = ResourceLoader.load("background.png");

        MUTE_IMG = ResourceLoader.load("mute.png");
        UNMUTE_IMG = ResourceLoader.load("unmute.png");

        // Load Mallet Frames
        MALLET_FRAMES = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            MALLET_FRAMES.add(ResourceLoader.load("mallet" + i + ".png"));
        }

        // --- Load Sounds ---
        try {
            // Load Intro Music
            URL introUrl = ResourceLoader.class.getResource("/intro.mp3");
            if (introUrl != null) {
                Media introMedia = new Media(introUrl.toExternalForm());
                introPlayer = new MediaPlayer(introMedia);
                introPlayer.setVolume(MUSIC_VOLUME);
                introPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop
            }

            // Load Background Music
            URL musicUrl = ResourceLoader.class.getResource("/main.mp3");
            if (musicUrl != null) {
                Media backgroundMusic = new Media(musicUrl.toExternalForm());
                mainMusicPlayer = new MediaPlayer(backgroundMusic);
                mainMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever
                mainMusicPlayer.setVolume(MUSIC_VOLUME);
            }

            // Load Game Over Music
            URL gameOverUrl = ResourceLoader.class.getResource("/game_over.mp3");
            if (gameOverUrl != null) {
                Media gameOverMedia = new Media(gameOverUrl.toExternalForm());
                gameOverPlayer = new MediaPlayer(gameOverMedia);
                gameOverPlayer.setVolume(MUSIC_VOLUME);
            }

            // Load Game End SFX
            URL gameEndUrl = ResourceLoader.class.getResource("/game_end.mp3");
            if (gameEndUrl != null) {
                Media gameEndMedia = new Media(gameEndUrl.toExternalForm());
                gameEndPlayer = new MediaPlayer(gameEndMedia);
                gameEndPlayer.setVolume(SFX_VOLUME);
            }

            // Load Game Start SFX
            URL gameStartUrl = ResourceLoader.class.getResource("/game_start.mp3");
            if (gameStartUrl != null) {
                Media gameStartMedia = new Media(gameStartUrl.toExternalForm());
                gameStartPlayer = new MediaPlayer(gameStartMedia);
                gameStartPlayer.setVolume(SFX_VOLUME);
            }

            // Load Button SFX
            URL hoverUrl = ResourceLoader.class.getResource("/hover_button.wav");
            if(hoverUrl != null) {
                hoverSound = new AudioClip(hoverUrl.toExternalForm());
                hoverSound.setVolume(SFX_VOLUME);
            }
            URL selectUrl = ResourceLoader.class.getResource("/select_button.wav");
            if(selectUrl != null) {
                selectSound = new AudioClip(selectUrl.toExternalForm());
                selectSound.setVolume(SFX_VOLUME);
            }

            URL moleAppearUrl = ResourceLoader.class.getResource("/mole_appear.wav");
            if(moleAppearUrl != null) {
                moleAppearSound = new AudioClip(moleAppearUrl.toExternalForm());
                moleAppearSound.setVolume(SFX_VOLUME);
                moleAppearSound.setVolume(0.25);
            }
            URL bombAppearUrl = ResourceLoader.class.getResource("/bomb_appear.wav");
            if(bombAppearUrl != null) {
                bombAppearSound = new AudioClip(bombAppearUrl.toExternalForm());
                bombAppearSound.setVolume(SFX_VOLUME);
            }

            URL hitMissUrl = ResourceLoader.class.getResource("/hit_miss.wav");
            if(hitMissUrl != null) {
                hitMissSound = new AudioClip(hitMissUrl.toExternalForm());
                hitMissSound.setVolume(SFX_VOLUME);
            }
            URL hitMoleUrl = ResourceLoader.class.getResource("/hit_mole.wav");
            if(hitMoleUrl != null) {
                hitMoleSound = new AudioClip(hitMoleUrl.toExternalForm());
                hitMoleSound.setVolume(SFX_VOLUME);
            }
            URL hitBonusMoleUrl = ResourceLoader.class.getResource("/hit_bonus_mole.wav");
            if(hitBonusMoleUrl != null) {
                hitBonusMoleSound = new AudioClip(hitBonusMoleUrl.toExternalForm());
                hitBonusMoleSound.setVolume(SFX_VOLUME);
            }

            URL timerTickUrl = ResourceLoader.class.getResource("/timer_countdown.wav");
            if(timerTickUrl != null) {
                timerTickSound = new AudioClip(timerTickUrl.toExternalForm());
                timerTickSound.setVolume(SFX_VOLUME);
            }

        } catch (Exception e) {
            System.err.println("Failed to load audio: " + e.getMessage());
            // Create silent fallbacks
            if (introPlayer == null) introPlayer = new MediaPlayer(null);
            if (mainMusicPlayer == null) mainMusicPlayer = new MediaPlayer(null);
            if (gameOverPlayer == null) gameOverPlayer = new MediaPlayer(null);
            if (gameStartPlayer == null) gameStartPlayer = new MediaPlayer(null);
            if (gameEndPlayer == null) gameEndPlayer = new MediaPlayer(null);
            if (hoverSound == null) hoverSound = new AudioClip(null);
            if (selectSound == null) selectSound = new AudioClip(null);
            if (moleAppearSound == null) moleAppearSound = new AudioClip(null);
            if (bombAppearSound == null) bombAppearSound = new AudioClip(null);
            if (hitMissSound == null) hitMissSound = new AudioClip(null);
            if (hitMoleSound == null) hitMoleSound = new AudioClip(null);
            if (hitBonusMoleSound == null) hitBonusMoleSound = new AudioClip(null);
            if (timerTickSound == null) timerTickSound = new AudioClip(null);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        loadAssets();

        highScoreManager = new HighScoreManager();
        loadHighScore();

        StackPane root = new StackPane();

        BackgroundSize zoomedOutSize = new BackgroundSize(
                0.8, 0.8, true, true, false, false);

        BackgroundImage bgImage = new BackgroundImage(
                BACKGROUND_IMG,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT, zoomedOutSize
        );
        root.setBackground(new Background(bgImage));
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        BorderPane gameUnit = new BorderPane();
        gameUnit.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        gameUnit.setBackground(new Background(new BackgroundFill(LIGHT_BLUE, new CornerRadii(15), Insets.EMPTY)));
        DropShadow gameUnitShadow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.5), 30, 0.4, 0, 0);
        gameUnit.setEffect(gameUnitShadow);

        Node infoBar = createInfoBar();
        Node controlBar = createControlBar();

        StackPane centerContainer = new StackPane();
        gameGrid = createGameGrid();
        gameOverlay = createGameOverOverlay();
        startupOverlay = createStartupOverlay();

        malletCursorView = new ImageView(MALLET_FRAMES.get(0));
        malletCursorView.setFitWidth(72);
        malletCursorView.setFitHeight(72);
        malletCursorView.setPreserveRatio(true);
        malletCursorView.setMouseTransparent(true);
        malletCursorView.setVisible(false);
        StackPane.setAlignment(malletCursorView, Pos.TOP_LEFT);

        root.getChildren().addAll(gameUnit, malletCursorView);

        centerContainer.getChildren().addAll(gameGrid, gameOverlay, startupOverlay);
        gameGrid.setEffect(new GaussianBlur(10));

        gameUnit.setTop(infoBar);
        gameUnit.setCenter(centerContainer);
        gameUnit.setBottom(controlBar);

        gameEngine = new GameEngine(this, GRID_SIZE);
        gameEngine.initializeGame();

        Scene scene = new Scene(root, 850, 650);
        primaryStage.setTitle("Whack-A-Mole");
        primaryStage.getIcons().add(APP_ICON);

        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.setScene(scene);
        primaryStage.show();

        if (introPlayer != null && !isMuted) {
            introPlayer.play();
        }

        root.setOnMouseMoved(event -> {
            malletCursorView.setTranslateX(event.getX() - 35);
            malletCursorView.setTranslateY(event.getY() - 80);
        });

        initializeMalletAnimation();

        primaryStage.setOnCloseRequest(event -> {
            if (introPlayer != null) introPlayer.stop();
            if (mainMusicPlayer != null) mainMusicPlayer.stop();
            if (gameOverPlayer != null) gameOverPlayer.stop();
            if (gameEndPlayer != null) gameEndPlayer.stop();

            if (gameThread != null) {
                gameEngine.stopGame();
                gameThread.interrupt();
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                    System.err.println("Failed to join game thread.");
                }
            }
            Platform.exit();
        });
    }

    /**
     * Creates the top info bar with title, scores, and time.
     */
    private Node createInfoBar() {
        Label titleLabel = createInfoLabel("Whack-A-Mole");
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));

        ImageView logoView = new ImageView(APP_ICON);
        logoView.setFitHeight(30);
        logoView.setPreserveRatio(true);
        HBox titleBox = new HBox(10, logoView, titleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label scoreTextLabel = createInfoLabel("Score: ");
        scoreValueLabel = createInfoLabel("0");
        HBox scoreHBox = new HBox(scoreTextLabel, scoreValueLabel);
        scoreHBox.setAlignment(Pos.CENTER);
        scoreHBox.setMinWidth(130);

        Rectangle scoreUnderline = new Rectangle(130, 1.5);
        scoreUnderline.setFill(Color.WHITE);
        VBox scoreVBox = new VBox(4, scoreHBox, scoreUnderline);
        scoreVBox.setAlignment(Pos.CENTER);

        Label highScoreTextLabel = createInfoLabel("High Score: ");
        highScoreValueLabel = createInfoLabel(String.valueOf(currentHighScore));
        HBox highScoreHBox = new HBox(highScoreTextLabel, highScoreValueLabel);
        highScoreHBox.setAlignment(Pos.CENTER);

        HBox scoreBox = new HBox(30, scoreVBox, highScoreHBox);
        scoreBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(scoreBox, Priority.ALWAYS);

        Label timeTextLabel = createInfoLabel("Time: ");
        timeValueLabel = createInfoLabel("30s");
        timeValueLabel.setMinWidth(40);
        HBox timeHBox = new HBox(timeTextLabel, timeValueLabel);
        timeHBox.setAlignment(Pos.CENTER_RIGHT);

        BorderPane infoBar = new BorderPane();
        infoBar.setLeft(titleBox);
        infoBar.setCenter(scoreBox);
        infoBar.setRight(timeHBox);

        infoBar.setPadding(new Insets(10, 20, 10, 20));
        BorderPane.setAlignment(titleBox, Pos.CENTER_LEFT);
        BorderPane.setAlignment(scoreBox, Pos.CENTER);
        BorderPane.setAlignment(timeHBox, Pos.CENTER_RIGHT);
        titleBox.setMinWidth(200);
        timeHBox.setMinWidth(150);


        infoBar.setBackground(new Background(new BackgroundFill(
                DARK_BLUE, new CornerRadii(15, 15, 0, 0, false), Insets.EMPTY
        )));

        return infoBar;
    }

    /**
     * Creates the main game grid and populates it with static hole sprites.
     */
    private GridPane createGameGrid() {
        gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        gameGrid.setCursor(Cursor.NONE);

        holeContainers.clear();

        for (int i = 0; i < 5; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            gameGrid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setFillHeight(true);
            gameGrid.getRowConstraints().add(row);
        }

        int holeIndex = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                final int index = holeIndex;
                StackPane holeContainer = new StackPane();

                holeContainer.setPrefSize(120, 80);
                holeContainer.setMinSize(120, 80);
                holeContainer.setMaxSize(120, 80);
                holeContainer.setAlignment(Pos.BOTTOM_CENTER);

                ImageView holeBackView = new ImageView(HOLE_BACK_IMG);
                holeBackView.setFitWidth(120);
                holeBackView.setFitHeight(80);
                holeBackView.setPreserveRatio(false);

                ImageView holeFrontView = new ImageView(HOLE_FRONT_IMG);
                holeFrontView.setFitWidth(120);
                holeFrontView.setFitHeight(80);
                holeFrontView.setPreserveRatio(false);
                holeFrontView.setTranslateY(0);

                holeContainer.getChildren().addAll(holeBackView, holeFrontView);

                holeContainer.setOnMouseClicked(event -> {
                    int whackResult = gameEngine.whack(index); // Capture the result
                    playMalletAnimation(); // Always play the swing animation

                    if (whackResult == 0) {
                        playHitMissSound(); // It was a miss
                    }
                });

                gameGrid.add(holeContainer, col, row);

                GridPane.setHalignment(holeContainer, HPos.CENTER);
                GridPane.setValignment(holeContainer, VPos.CENTER);

                holeContainers.add(holeContainer);
                holeIndex++;
            }
        }
        return gameGrid;
    }

    /**
     * Creates the bottom control bar with game buttons.
     */
    private Node createControlBar() {
        BorderPane controlBar = new BorderPane();
        controlBar.setPadding(new Insets(15, 20, 15, 20));

        controlBar.setBackground(new Background(new BackgroundFill(
                DARK_BLUE, new CornerRadii(0, 0, 15, 15, false), Insets.EMPTY
        )));

        // Mute Button (Left)
        unmuteIconView = new ImageView(UNMUTE_IMG);
        unmuteIconView.setFitWidth(32);
        unmuteIconView.setFitHeight(32);
        muteIconView = new ImageView(MUTE_IMG);
        muteIconView.setFitWidth(32);
        muteIconView.setFitHeight(32);

        muteButton = createIconButton(unmuteIconView); // Start with unmute icon
        muteButton.setOnAction(event -> toggleMute());
        controlBar.setLeft(muteButton);
        BorderPane.setAlignment(muteButton, Pos.CENTER_LEFT);

        // Center Buttons
        startButton = createButton("Start Game", BUTTON_GREEN, BUTTON_HOVER_GREEN, Color.WHITE);
        startButton.setOnAction(event -> startGame());

        resetScoreButton = createButton("Reset High Score", BUTTON_YELLOW, BUTTON_HOVER_YELLOW, Color.WHITE);
        resetScoreButton.setOnAction(event -> resetHighScore());

        exitButton = createButton("Exit", BUTTON_GREY, BUTTON_HOVER_GREY, Color.WHITE);
        exitButton.setOnAction(event -> Platform.exit());

        HBox centerButtons = new HBox(20, startButton, resetScoreButton, exitButton);
        centerButtons.setAlignment(Pos.CENTER);
        controlBar.setCenter(centerButtons);

        return controlBar;
    }

    /**
     * Creates the startup logo overlay.
     */
    private StackPane createStartupOverlay() {
        StackPane overlay = new StackPane();
        overlay.setAlignment(Pos.CENTER);
        overlay.setMouseTransparent(false);
        overlay.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.4), CornerRadii.EMPTY, Insets.EMPTY)));
        overlay.setVisible(true);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(550, 300);
        card.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.75), new CornerRadii(25), Insets.EMPTY)));
        card.setPadding(new Insets(15, 20, 15, 20));

        ImageView logoView = new ImageView(STARTUP_LOGO);
        logoView.setFitWidth(400);
        logoView.setPreserveRatio(true);
        logoView.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 5, 0.7, 0, 0));

        card.getChildren().add(logoView);
        overlay.getChildren().add(card);

        return overlay;
    }

    /**
     * Creates the "Game Over" overlay card.
     */
    private StackPane createGameOverOverlay() {
        StackPane overlay = new StackPane();
        overlay.setAlignment(Pos.CENTER);
        overlay.setMouseTransparent(false);

        overlay.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.4), CornerRadii.EMPTY, Insets.EMPTY)));
        overlay.setVisible(false);
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox card = new VBox(-5);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(550, 300);
        card.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.75), new CornerRadii(25), Insets.EMPTY)));
        card.setPadding(new Insets(15, 20, 15, 20));

        ImageView gameOverView = new ImageView(GAME_OVER_LOGO);
        gameOverView.setFitWidth(400);
        gameOverView.setPreserveRatio(true);
        gameOverView.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 5, 0.7, 0, 0));

        overlayScoreLabel = new Label("Your Score: 0");
        overlayScoreLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        overlayScoreLabel.setTextFill(Color.WHITE);

        overlayHighScoreLabel = new Label("High Score: " + currentHighScore);
        overlayHighScoreLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        overlayHighScoreLabel.setTextFill(Color.WHITE);

        VBox scoresBox = new VBox(10);
        scoresBox.setAlignment(Pos.CENTER);
        scoresBox.getChildren().addAll(overlayScoreLabel, overlayHighScoreLabel);

        card.getChildren().addAll(gameOverView, scoresBox);
        overlay.getChildren().add(card);

        return overlay;
    }

    /**
     * Called when the "Start Game" button is pressed.
     */
    private void startGame() {
        // 1. Disable buttons immediately
        startButton.setDisable(true);
        exitButton.setDisable(true);
        resetScoreButton.setDisable(true);

        // Stop other music
        if (introPlayer != null) {
            introPlayer.stop();
        }
        if (gameOverPlayer != null) {
            gameOverPlayer.stop();
        }

        // --- ACTION A: FADE BUTTONS & PLAY SOUND ---
        FadeTransition fadeOutStart = new FadeTransition(Duration.millis(300), startButton);
        fadeOutStart.setToValue(0);
        fadeOutStart.setOnFinished(e -> {
            // Play the start sound *after* the buttons finish fading
            if (gameStartPlayer != null && !isMuted) {
                gameStartPlayer.seek(Duration.ZERO);
                gameStartPlayer.play();
            }
        });

        FadeTransition fadeOutExit = new FadeTransition(Duration.millis(300), exitButton);
        fadeOutExit.setToValue(0);

        FadeTransition fadeOutReset = new FadeTransition(Duration.millis(300), resetScoreButton);
        fadeOutReset.setToValue(0);

        fadeOutStart.play();
        fadeOutExit.play();
        fadeOutReset.play();


        // --- ACTION B: FADE OVERLAY & START GAME (WITH 800ms DELAY) ---
        StackPane overlayToFade = null;
        if (startupOverlay.isVisible()) {
            overlayToFade = startupOverlay;
        } else if (gameOverlay.isVisible()) {
            overlayToFade = gameOverlay;
        }

        if (overlayToFade != null) {
            final StackPane finalOverlay = overlayToFade;
            FadeTransition fadeOutOverlay = new FadeTransition(Duration.millis(500), finalOverlay);
            fadeOutOverlay.setToValue(0);

            fadeOutOverlay.setDelay(Duration.millis(800));

            fadeOutOverlay.setOnFinished(e -> {
                finalOverlay.setVisible(false);
                finalOverlay.setOpacity(1.0); // Reset for next time

                // START THE GAME LOGIC
                gameGrid.setEffect(null);
                if (gameGrid.getScene() != null) gameGrid.getScene().setCursor(Cursor.NONE);
                malletCursorView.setVisible(true);
                if (mainMusicPlayer != null && !isMuted) mainMusicPlayer.play();

                gameEngine.initializeGame();
                gameThread = new Thread(gameEngine);
                gameThread.setDaemon(true);
                gameThread.start();

                startButton.setText("Play Again");
            });
            fadeOutOverlay.play();
        }
    }

    // --- High Score Logic ---

    /**
     * Loads the high score from the serialized file on startup.
     */
    private void loadHighScore() {
        try {
            List<PlayerScore> scores = highScoreManager.loadScores();
            currentHighScore = scores.stream()
                    .mapToInt(PlayerScore::getScore)
                    .max()
                    .orElse(0);
        } catch (HighScoreException e) {
            currentHighScore = 0;
            showError("Failed to load high scores", e.getMessage());
        }
    }

    /**
     * Saves the player's score at the end of the game.
     * @param finalScore The player's final score.
     */
    private void saveHighScore(int finalScore) {
        if (finalScore > currentHighScore) {
            currentHighScore = finalScore;
            updateHighScore(currentHighScore);
        }

        try {
            List<PlayerScore> scores = highScoreManager.loadScores();
            scores.add(new PlayerScore("Player", finalScore));
            scores.sort(Comparator.comparingInt(PlayerScore::getScore).reversed());
            List<PlayerScore> topScores = new ArrayList<>(scores.subList(0, Math.min(scores.size(), 10)));
            highScoreManager.saveScores(topScores);
        } catch (HighScoreException e) {
            showError("Failed to save high score", e.getMessage());
        }
    }

    /**
     * Resets the high score file to zero.
     */
    private void resetHighScore() {
        currentHighScore = 0;
        updateHighScore(0);
        try {
            highScoreManager.saveScores(new ArrayList<>());
        } catch (HighScoreException e) {
            showError("Failed to reset high scores", e.getMessage());
        }
    }

    // --- Utility Methods ---

    /**
     * Helper to create a standard info label.
     */
    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        label.setTextFill(Color.WHITE);
        label.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.5), 3, 0.3, 0, 1));
        return label;
    }

    /**
     * Helper to create a styled button.
     */
    private Button createButton(String text, String bgColor, String hoverColor, Color textColor) {
        Button button = new Button(text);
        button.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        button.setCursor(Cursor.HAND);
        button.setTextFill(textColor);

        String style = "-fx-background-color: " + bgColor + "; -fx-background-radius: 12;";
        String hoverStyle = "-fx-background-color: " + hoverColor + "; -fx-background-radius: 12;";

        button.setStyle(style);

        button.setOnMouseEntered(e -> {
            button.setStyle(hoverStyle);
            playHoverSound();
        });
        button.setOnMouseExited(e -> button.setStyle(style));
        button.setOnMousePressed(e -> playSelectSound());

        button.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0,0,0,0.3), 5, 0.4, 0, 2));
        return button;
    }

    /**
     * Helper to create a styled ICON button.
     */
    private Button createIconButton(ImageView icon) {
        Button button = new Button();
        button.setGraphic(icon);
        button.setCursor(Cursor.HAND);

        String style = "-fx-background-color: transparent; -fx-background-radius: 50;";
        String hoverStyle = "-fx-background-color: #ffffff22; -fx-background-radius: 50;";

        button.setStyle(style);

        button.setOnMouseEntered(e -> {
            button.setStyle(hoverStyle);
            playHoverSound();
        });
        button.setOnMouseExited(e -> button.setStyle(style));
        button.setOnMousePressed(e -> playSelectSound());

        return button;
    }

    /**
     * Helper to show a JavaFX error alert in a thread-safe way.
     */
    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Creates the mallet animation Timeline.
     */
    private void initializeMalletAnimation() {
        malletAnimation = new Timeline();

        int frameDurationMs = 25;

        for (int i = 0; i < 10; i++) {
            final int frameIndex = i;
            KeyFrame kf = new KeyFrame(
                    Duration.millis(i * frameDurationMs),
                    e -> malletCursorView.setImage(MALLET_FRAMES.get(frameIndex))
            );
            malletAnimation.getKeyFrames().add(kf);
        }

        KeyFrame resetFrame = new KeyFrame(
                Duration.millis(10 * frameDurationMs),
                e -> malletCursorView.setImage(MALLET_FRAMES.get(0))
        );
        malletAnimation.getKeyFrames().add(resetFrame);

        malletAnimation.setCycleCount(1);
    }

    /**
     * Plays the mallet swing animation from the start.
     */
    private void playMalletAnimation() {
        if (malletAnimation != null && malletAnimation.getStatus() != javafx.animation.Animation.Status.RUNNING) {
            malletAnimation.playFromStart();
        }
    }

    // --- Audio Helper Methods ---

    /**
     * Toggles all game audio on or off.
     */
    private void toggleMute() {
        isMuted = !isMuted;

        // Mute MediaPlayers
        if (introPlayer != null) introPlayer.setMute(isMuted);
        if (mainMusicPlayer != null) mainMusicPlayer.setMute(isMuted);
        if (gameOverPlayer != null) gameOverPlayer.setMute(isMuted);
        if (gameStartPlayer != null) gameStartPlayer.setMute(isMuted);
        if (gameEndPlayer != null) gameEndPlayer.setMute(isMuted);

        // Mute AudioClips
        double sfxVol = isMuted ? 0.0 : SFX_VOLUME;
        double moleVol = isMuted ? 0.0 : 0.4;

        if (hoverSound != null) hoverSound.setVolume(sfxVol);
        if (selectSound != null) selectSound.setVolume(sfxVol);
        if (moleAppearSound != null) moleAppearSound.setVolume(moleVol);
        if (bombAppearSound != null) bombAppearSound.setVolume(sfxVol);
        if (hitMissSound != null) hitMissSound.setVolume(sfxVol);
        if (hitMoleSound != null) hitMoleSound.setVolume(sfxVol);
        if (hitBonusMoleSound != null) hitBonusMoleSound.setVolume(sfxVol);
        if (timerTickSound != null) timerTickSound.setVolume(sfxVol);

        // Logic for stopping/starting music
        if (isMuted) {
            muteButton.setGraphic(muteIconView); // Show "Muted" icon
            if (introPlayer != null) introPlayer.stop();
            if (mainMusicPlayer != null) mainMusicPlayer.stop();
        } else {
            muteButton.setGraphic(unmuteIconView); // Show "Unmuted" icon
            if (startupOverlay.isVisible() && introPlayer != null) {
                introPlayer.play();
            } else if (gameEngine != null && gameEngine.isGameRunning() && mainMusicPlayer != null) {
                mainMusicPlayer.play();
            }
        }
    }

    /**
     * Plays the button hover sound if not muted.
     */
    private void playHoverSound() {
        if (!isMuted && hoverSound != null) {
            hoverSound.play();
        }
    }

    /**
     * Plays the button select sound if not muted.
     */
    private void playSelectSound() {
        if (!isMuted && selectSound != null) {
            selectSound.play();
        }
    }

    /**
     * Plays the mole appear sound if not muted.
     */
    private void playMoleAppearSound() {
        if (!isMuted && moleAppearSound != null) {
            moleAppearSound.play();
        }
    }

    /**
     * Plays the bomb appear sound if not muted.
     */
    private void playBombAppearSound() {
        if (!isMuted && bombAppearSound != null) {
            bombAppearSound.play();
        }
    }

    /**
     * Plays the hit "miss" sound if not muted.
     */
    private void playHitMissSound() {
        if (!isMuted && hitMissSound != null) {
            hitMissSound.play();
        }
    }

    /**
     * Plays the hit "mole" sound if not muted.
     */
    private void playHitMoleSound() {
        if (!isMuted && hitMoleSound != null) {
            hitMoleSound.play();
        }
    }

    /**
     * Plays the hit "bonus mole" sound if not muted.
     */
    private void playHitBonusMoleSound() {
        if (!isMuted && hitBonusMoleSound != null) {
            hitBonusMoleSound.play();
        }
    }

    /**
     * Plays the timer "tick" sound if not muted.
     */
    private void playTimerTickSound() {
        if (!isMuted && timerTickSound != null) {
            if (timerTickSound.isPlaying()) {
                timerTickSound.stop();
            }
            timerTickSound.play();
        }
    }

    /**
     * Plays the timer "tick" sound and flashes the timer label.
     * Called on every game tick when time is <= 10.
     */
    @Override
    public void tickTimerEffects(int time) {
        Platform.runLater(() -> {
            playTimerTickSound();
            isTimerRed = !isTimerRed;
            timeValueLabel.setTextFill(isTimerRed ? Color.RED : Color.WHITE);
        });
    }


    // --- GameUIUpdater Interface Implementation ---

    /**
     * Inserts or removes an occupant from a hole.
     * Called from the GameEngine thread.
     */
    @Override
    public void updateHole(int index, HoleOccupant occupant) {
        Platform.runLater(() -> {
            if (index < 0 || index >= holeContainers.size()) return;

            StackPane holeContainer = holeContainers.get(index);
            if (holeContainer == null) return;

            Node node = occupant.getDisplayNode();
            StackPane newNodeStack = (StackPane) node;
            List<Node> newChildren = new ArrayList<>(newNodeStack.getChildren());

            if (newChildren.isEmpty()) { // This means an Empty occupant was passed
                if (holeContainer.getChildren().size() > 2) {
                    Node occupantToHide = holeContainer.getChildren().get(1);
                    hideOccupant(holeContainer, occupantToHide);
                }
            } else {
                Node occupantToShow = newChildren.get(0);

                if (holeContainer.getChildren().size() == 2) {
                    holeContainer.getChildren().add(1, occupantToShow);
                } else if (holeContainer.getChildren().size() > 2) {
                    holeContainer.getChildren().set(1, occupantToShow);
                } else {
                    System.err.println("Error: Could not set occupant in hole " + index);
                    return;
                }

                if (occupant instanceof Mole || occupant instanceof BonusMole) {
                    playMoleAppearSound();
                } else if (occupant instanceof Bomb) {
                    playBombAppearSound();
                }

                TranslateTransition peekAnim = new TranslateTransition(Duration.millis(150), occupantToShow);
                peekAnim.setToY(PEEK_Y_POSITION);
                peekAnim.play();
            }
        });
    }


    /**
     * Plays the "hit" animation for a specific occupant.
     * Called from the GameEngine thread.
     */
    @Override
    public void playHitAnimation(int index, HoleOccupant occupant) {
        Platform.runLater(() -> {
            if (index < 0 || index >= holeContainers.size()) return;

            StackPane holeContainer = holeContainers.get(index);
            if (holeContainer == null || holeContainer.getChildren().size() <= 2) {
                return;
            }

            Node occupantNode = holeContainer.getChildren().get(1);
            ImageView occupantView = (ImageView) occupantNode;

            PauseTransition soundDelay = new PauseTransition(Duration.millis(50));
            soundDelay.setOnFinished(e_sound -> {
                if (occupant instanceof BonusMole) {
                    playHitMoleSound();
                    PauseTransition bonusSoundDelay = new PauseTransition(Duration.millis(100));
                    bonusSoundDelay.setOnFinished(e_bonus -> playHitBonusMoleSound());
                    bonusSoundDelay.play();
                } else if (occupant instanceof Mole) {
                    playHitMoleSound();
                }
            });
            soundDelay.play();

            if (occupant instanceof BonusMole) {
                occupantView.setImage(MOLE_HAT_CRACKS_IMG);

                PauseTransition crackPause = new PauseTransition(Duration.millis(80));
                crackPause.setOnFinished(e -> occupantView.setImage(MOLE_HAT_HIT_IMG));

                PauseTransition hitPause = new PauseTransition(Duration.millis(200));
                hitPause.setOnFinished(e2 -> hideOccupant(holeContainer, occupantNode));

                SequentialTransition animation = new SequentialTransition(crackPause, hitPause);
                animation.play();

            } else if (occupant instanceof Mole) {
                occupantView.setImage(MOLE_HIT_IMG);
                PauseTransition hitPause = new PauseTransition(Duration.millis(200));

                hitPause.setOnFinished(e -> {
                    hideOccupant(holeContainer, occupantNode);
                });
                hitPause.play();

            } else if (occupant instanceof Bomb) {
                hideOccupant(holeContainer, occupantNode);
            }
        });
    }

    /**
     * Handles the "hide" (down) animation and removal.
     */
    private void hideOccupant(StackPane holeContainer, Node occupantNode) {
        TranslateTransition hideAnim = new TranslateTransition(Duration.millis(100), occupantNode);
        hideAnim.setToY(HIDE_Y_POSITION);
        hideAnim.setOnFinished(e -> {
            if (holeContainer.getChildren().contains(occupantNode)) {
                holeContainer.getChildren().remove(occupantNode);
            }
        });
        hideAnim.play();
    }

    @Override
    public void updateScore(int score) {
        Platform.runLater(() -> scoreValueLabel.setText(String.valueOf(score)));
    }

    @Override
    public void updateTime(int time) {
        Platform.runLater(() -> {
            timeValueLabel.setText(time + "s");

            if (time > 10) {
                isTimerRed = false;
                timeValueLabel.setTextFill(Color.WHITE);
            }
        });
    }

    @Override
    public void updateHighScore(int highScore) {
        Platform.runLater(() -> {
            highScoreValueLabel.setText(String.valueOf(highScore));
            overlayHighScoreLabel.setText("High Score: " + highScore);
        });
    }

    /**
     * Starts the multi-stage game over sequence.
     * Freezes game, plays "end" sound, then chains to "game over" sound and overlay.
     */
    @Override
    public void triggerGameOverSequence(int finalScore) {
        Platform.runLater(() -> {
            // --- 1. FREEZE THE GAME ---
            if (mainMusicPlayer != null) {
                mainMusicPlayer.stop();
            }
            if (timerTickSound != null) {
                timerTickSound.stop();
            }
            timeValueLabel.setTextFill(Color.WHITE);
            isTimerRed = false;

            malletCursorView.setVisible(false);
            if (gameGrid.getScene() != null) {
                gameGrid.getScene().setCursor(Cursor.DEFAULT);
            }

            // --- 2. PREPARE OVERLAY DATA ---
            saveHighScore(finalScore);
            overlayScoreLabel.setText("Your Score: " + finalScore);
            overlayHighScoreLabel.setText("High Score: " + currentHighScore);

            // --- 3. DEFINE THE FINAL STEP (Fade-in and Game Over music) ---
            Runnable startOverlaySequence = () -> {
                if (gameOverPlayer != null && !isMuted) {
                    gameOverPlayer.seek(Duration.ZERO);
                    gameOverPlayer.play();
                }
                showGameOver();
            };

            // --- 4. PLAY "game_end.mp3" AND CHAIN TO FINAL STEP ---
            if (gameEndPlayer != null && !isMuted) {
                gameEndPlayer.setOnEndOfMedia(startOverlaySequence);
                gameEndPlayer.seek(Duration.ZERO);
                gameEndPlayer.play();
            } else {
                startOverlaySequence.run();
            }
        });
    }

    /**
     * Handles the visual-only fade-in of the game over screen
     * and resets the buttons.
     */
    @Override
    public void showGameOver() {
        Platform.runLater(() -> {
            gameGrid.setEffect(new GaussianBlur(10));

            gameOverlay.setOpacity(0.0);
            gameOverlay.setVisible(true);

            FadeTransition fadeInOverlay = new FadeTransition(Duration.millis(300), gameOverlay);
            fadeInOverlay.setToValue(1.0);
            fadeInOverlay.play();

            // --- Reset button state ---
            startButton.setText("Play Again");
            startButton.setOpacity(1.0);
            startButton.setDisable(false);

            resetScoreButton.setVisible(true);
            resetScoreButton.setOpacity(1.0);
            resetScoreButton.setDisable(false);

            exitButton.setOpacity(1.0);
            exitButton.setDisable(false);
        });
    }

    /**
     * Main method is only needed for the IDE.
     * The double-clickable JAR uses Launcher.java
     */
    public static void main(String[] args) {
        launch(args);
    }
}