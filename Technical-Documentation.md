# Whack-A-Mole: Technical Documentation

**Project:** Whack-A-Mole JavaFX Application
**Author:** Hans James 2410110138
**Date:** 16/11/2025

---

## 1. Final OOP Design and Justification

The core of this application is built on a robust, extensible Object-Oriented (OOP) foundation that directly implements the principles of Abstraction, Inheritance, and Polymorphism.

### Design Justification: The `HoleOccupant` Hierarchy

The central design choice was the creation of the `HoleOccupant` abstract class. This class serves as the base for all objects that can appear from a hole, including `Mole`, `Bomb`, `BonusMole`, and even the `Empty` hole state.

* **Abstraction:** `HoleOccupant` provides a common template, defining a shared "contract" that all subclasses must honor. This includes the abstract methods `whack()` (returning a score code) and `getDisplayNode()` (returning the visual component), as well as shared state (`visible`, `timeRemaining`) and common behavior (`tick()`).
* **Inheritance:** Concrete classes like `Mole`, `Bomb`, and `BonusMole` extend `HoleOccupant`, inheriting its common features and providing their own unique implementations for the abstract methods.
* **Polymorphism:** This is the most critical principle in action. The `GameEngine` and `WhackAMoleGame` GUI do not know or care *what* type of `HoleOccupant` is in a given hole.
    * When the game spawns an object, it simply calls `occupant.getDisplayNode()` and adds the returned `Node` (an `ImageView`) to the scene.
    * When a hole is clicked, the `GameEngine` calls `occupant.whack()`. At runtime, polymorphism executes the correct method:
        * `Mole.whack()` returns `100` (score points).
        * `Bomb.whack()` returns `-1` (a code for "Game Over").
        * `BonusMole.whack()` returns `-2` (a code for "Add Time").
    * The `GameEngine` then interprets this integer code to update the game state. This demonstrates polymorphism as the "whack" action has different results based on the object's type, without the `GameEngine` needing to check `instanceof` before calling the method.

This design is highly extensible. The final implementation of hit-specific animations is another perfect example: the `GameEngine` calls `uiUpdater.playHitAnimation(index, occupant)`. The GUI (`WhackAMoleGame`) then *does* use `instanceof` to check the type and play the correct, specific animation (e.g., the multi-stage `MoleHatCracks.png` animation for `BonusMole`).

## 2. Detailed Concurrency Strategy

This application is fully concurrent, separating the game logic (the "engine") from the user interface (the "presentation") to ensure a responsive and stable application.

### The Two Threads

1.  **JavaFX Application Thread (UI Thread):** This is the main thread, responsible for all GUI operations: drawing the screen, updating text, and handling user input (like mouse clicks).
2.  **GameEngine Thread (Logic Thread):** This is a separate thread, manually created by instantiating `Thread(new GameEngine(this))`. Its sole responsibility is to run the game loop, manage game state (score, time, difficulty), and control the lifecycle of the moles and bombs.

### Thread-Safe Interaction

A core rule of JavaFX is that **only the UI thread can modify the GUI**. Our `GameEngine` thread is therefore forbidden from directly touching any `Label` or `GridPane`.

We solve this using two key mechanisms:

1.  **The `GameUIUpdater` Interface:** The `GameEngine` is given a `GameUIUpdater` (which is implemented by `WhackAMoleGame`). The engine communicates its state changes by calling methods on this interface, such as `uiUpdater.updateHole(...)` or `uiUpdater.playHitAnimation(...)`. This decouples the engine from the GUI.
2.  **`Platform.runLater(...)`:** The `WhackAMoleGame` class implements all `GameUIUpdater` methods. Critically, every single one of these methods wraps its GUI-modifying code inside a `Platform.runLater(...)` block. This marshals the code (e.g., `scoreValueLabel.setText(...)`) from the `GameEngine` thread back onto the JavaFX UI thread, guaranteeing thread-safety.

### Handling `InterruptedException`

The `GameEngine`'s loop uses `Thread.sleep()` to set the game's pace. This call can throw `InterruptedException`, which we handle as a **signal, not an error**. When the user closes the main window, the `primaryStage.setOnCloseRequest` handler calls `gameThread.interrupt()`. This triggers the `InterruptedException` in the `run()` method's `catch` block, which safely sets `gameIsRunning = false` and allows the thread to terminate cleanly.

## 3. Rationale for Custom Exceptions

The project implements a professional, multi-layered exception handling strategy by distinguishing between *recoverable* errors and *unrecoverable* programmer bugs.

### `HighScoreException` (Checked Exception)

* **Type:** `extends Exception` (Checked).
* **Rationale:** This exception is used by the `HighScoreManager`. A failure to read or write the `scores.dat` file (due to an `IOException` or `ClassNotFoundException`) is a **foreseeable and recoverable** problem. It is not a bug in the program, but a potential runtime issue (e.g., file corruption, permissions issue).
* **Handling:** By making this a *checked* exception, the compiler forces the calling code (in `WhackAMoleGame`) to handle it. The `loadScores()` method is wrapped in a `try-catch` block. If a `HighScoreException` is caught, the game handles it gracefully by showing an alert to the user and starting the game with a high score of 0.

### `InvalidGameStateException` (Unchecked Exception)

* **Type:** `extends RuntimeException` (Unchecked).
* **Rationale:** This exception is used to signify an "impossible" state or a **programmer error**. For example, if the `GameEngine`'s logic ever tried to spawn a mole in a hole that was already occupied, it would indicate a bug in the `spawnNewOccupant()` method.
* **Handling:** Because this is an *unchecked* exception, it is not meant to be caught. If this exception is ever thrown, the application is intended to crash. This "fail-fast" approach is desirable for programmer errors, as it immediately surfaces the bug during development, rather than allowing the application to continue in a corrupt or unpredictable state.