# Whack-A-Mole (JavaFX)

A robust, fully concurrent implementation of the classic **Whack-A-Mole** arcade game.  
Built with **Java (JDK 24)** and **JavaFX (SDK 25.0.1)**, this project serves as a capstone demonstration of **Object-Oriented Programming**, **multithreading**, and **data persistence**.

---

## Game Mechanics

- **Standard Mole** — Whack for **+100 points**
- **Bonus Mole (Hard Hat)** — Whack to **extend time by 5 seconds**
- **Bomb** — ❌ Do **NOT** whack (instant **Game Over**)
- **Dynamic Difficulty**
  - Game speed increases over time
  - Bomb spawn probability rises
  - Bonus moles become rarer

---

## Key Features

- **OOP Architecture**
  - Strict use of **abstraction** and **polymorphism**
  - `Mole`, `Bomb`, and `BonusMole` inherit from the abstract `HoleOccupant` class

- **True Multithreading**
  - `GameEngine` runs on a dedicated logic thread
  - JavaFX Application Thread remains responsive at all times

- **Data Persistence**
  - High scores are serialized and saved to `scores.dat`
  - Scores persist across application restarts

- **Robust Error Handling**
  - Custom exceptions:
    - `HighScoreException`
    - `InvalidGameStateException`

- **Rich Media**
  - Custom sprites and animations (e.g., cracking helmets)
  - Sound effects for every interaction

---

## Tech Stack & Requirements

- **Language:** Java 24
- **UI Framework:** JavaFX 25.0.1
- **Build Tool:** Standard Java SDK (no Maven/Gradle required, though supported)
- **IDE:** IntelliJ IDEA / Eclipse / VS Code

---

## How to Run

### Option 1: Run the JAR

If you have the JavaFX SDK installed:

```bash
java --module-path /path/to/javafx-sdk-25.0.1/lib      --add-modules javafx.controls,javafx.media      -jar whackamole.jar
```

---

### Option 2: Run from IntelliJ IDEA

1. Clone this repository
2. Open the project in **IntelliJ IDEA**
3. Go to **File → Project Structure → Libraries**
4. Add the `lib` folder from **JavaFX SDK 25.0.1**
5. Edit the Run Configuration for:

```
com.whackamole.Launcher
```

6. Add the following **VM Options**:

```text
--module-path "/path/to/javafx-sdk-25.0.1/lib" --add-modules javafx.controls,javafx.media
```

7. Run the application

---

## Project Structure

The project follows a **package-by-feature** modular architecture:

```text
src/com/whackamole/
├── Launcher.java               # Entry point (bypasses JavaFX module checks)
├── WhackAMoleGame.java         # Main JavaFX application & UI controller
├── exceptions/
│   ├── HighScoreException.java
│   └── InvalidGameStateException.java
├── game/
│   ├── GameEngine.java         # Runnable game loop & difficulty scaling
│   └── GameUIUpdater.java      # Thread-safe UI update interface
├── models/
│   ├── HoleOccupant.java       # Abstract base class
│   ├── Mole.java
│   ├── Bomb.java
│   ├── BonusMole.java
│   ├── Empty.java
│   └── PlayerScore.java        # Serializable score model
└── services/
    ├── HighScoreManager.java   # File I/O & serialization
    └── ResourceLoader.java     # Safe classpath resource loading
```

---

## Architecture Highlights

### The `HoleOccupant` Hierarchy

The game relies on **polymorphism** to handle grid interactions.  
The `GameEngine` never checks concrete types (`instanceof` is avoided entirely).

```java
// Abstract method in HoleOccupant
public abstract int whack();

// Bomb implementation
@Override
public int whack() {
    return -1; // Signals Game Over
}

// Mole implementation
@Override
public int whack() {
    return 100; // Returns score
}
```

---

### Concurrency Strategy

- **Logic Thread**
  - `GameEngine` manages ticks, entity spawning, and difficulty scaling

- **UI Thread**
  - `WhackAMoleGame` handles rendering and user input

- **Thread Communication**
  - UI updates are routed through `GameUIUpdater`
  - All UI mutations are wrapped in `Platform.runLater()`

This ensures **thread safety** and **buttery-smooth gameplay**.

---

## Author

**Hans James**  
Project Date: **16/11/2025**

---

## License

This project is released under the **MIT License**.
