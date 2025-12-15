# Whack-A-Mole (JavaFX) 🐹🔨

Classic **Whack-a-Mole** built with **Java (JDK 24)** and **JavaFX (SDK 25.0.1)** — a capstone project demonstrating OOP, multithreading, and data persistence.

## Features
- **OOP by design**: `Mole`, `Bomb`, `BonusMole` extend `HoleOccupant`.
- **Concurrent game engine** using `GameEngine` thread.
- **JavaFX UI** with animations and sprites.
- **Persistent high scores** in `scores.dat`.
- **Custom exceptions** for error handling.
- **Audio & animations** throughout the gameplay.

## Requirements
- Java (JDK 24)
- JavaFX SDK 25.0.1

## Running

### Option 1 — Run the JAR

```bash
java --module-path /path/to/javafx-sdk-25.0.1/lib      --add-modules javafx.controls,javafx.media      -jar whackamole.jar
```

### Option 2 — Run from an IDE

```bash
git clone https://github.com/BAT4K/whackamole.git
```

Configure JavaFX in **Project Structure → Libraries**.

Set VM options:

```
--module-path "C:\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.media --enable-native-access=javafx.graphics,javafx.media
```

Run `com.whackamole.Launcher`.

## Project Structure

```
/src
/resources
README.md
```
