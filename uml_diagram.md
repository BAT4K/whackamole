classDiagram
direction LR

    %% --- Module 1: The Core OOP Hierarchy ---
    class HoleOccupant {
        <<abstract>>
        #boolean visible
        #int timeRemaining
        #int ticksLived
        +whack()* int
        +getDisplayNode()* Node
        +tick() boolean
        +hide() void
    }
    class Mole {
        -boolean isHit
        +whack() int
        +getDisplayNode() Node
    }
    class Bomb {
        -boolean isHit
        +whack() int
        +getDisplayNode() Node
    }
    class BonusMole {
        -boolean isHit
        +whack() int
        +getDisplayNode() Node
    }
    class Empty {
        +whack() int
        +getDisplayNode() Node
        +tick() boolean
    }
    
    HoleOccupant <|-- Mole
    HoleOccupant <|-- Bomb
    HoleOccupant <|-- BonusMole
    HoleOccupant <|-- Empty

    %% --- Module 2: The Game Engine ---
    class GameEngine {
        <<Runnable>>
        -boolean gameIsRunning
        -AtomicInteger score
        -HoleOccupant[] grid
        -GameUIUpdater uiUpdater
        +run() void
        +whack(int index) void
        +stopGame() void
        +isGameRunning() boolean
    }
    class Runnable {
        <<interface>>
        +run() void
    }
    Runnable <|.. GameEngine
    GameEngine "1" o-- "15" HoleOccupant : contains

    %% --- Module 3: The GUI ---
    class GameUIUpdater {
        <<interface>>
        +updateHole(int, HoleOccupant) void
        +updateScore(int) void
        +updateTime(int) void
        +showGameOver() void
        +playHitAnimation(int, HoleOccupant) void
        +triggerGameOverSequence(int) void
    }
    class WhackAMoleGame {
        <<Application>>
        -GameEngine gameEngine
        -HighScoreManager highScoreManager
        -GridPane gameGrid
        -List~StackPane~ holeContainers
        -ImageView malletCursorView
        -Timeline malletAnimation
        -MediaPlayer gameEndPlayer
        +start(Stage) void
        +playHitAnimation(int, HoleOccupant) void
        +triggerGameOverSequence(int) void
        -hideOccupant(StackPane, Node) void
        -playMalletAnimation() void
    }
    class Application {
        +start(Stage) void
    }
    Application <|-- WhackAMoleGame
    GameUIUpdater <|.. WhackAMoleGame
    WhackAMoleGame "1" o-- "1" GameEngine : owns
    GameEngine ..> GameUIUpdater : updates
    
    %% --- Module 4: Data Persistence ---
    class HighScoreManager {
        -String SCORE_FILE
        +saveScores(List~PlayerScore~) void
        +loadScores() List~PlayerScore~
    }
    class PlayerScore {
        <<Serializable>>
        -String playerName
        -int score
        +getScore() int
    }
    class Serializable {
        <<interface>>
    }
    Serializable <|.. PlayerScore
    WhackAMoleGame "1" o-- "1" HighScoreManager : uses
    HighScoreManager ..> PlayerScore : serializes
    
    %% --- Module 5: Exceptions ---
    class HighScoreException {
        <<Checked Exception>>
    }
    class InvalidGameStateException {
        <<Unchecked Exception>>
    }
    class Exception { }
    class RuntimeException { }
    Exception <|-- HighScoreException
    RuntimeException <|-- InvalidGameStateException
    
    HighScoreManager ..> HighScoreException : throws
    GameEngine ..> InvalidGameStateException : throws

    %% --- Utilities (for JAR building) ---
    class Launcher {
         +main(String[]) void
    }
    Launcher ..> WhackAMoleGame : launches

    class ResourceLoader {
        <<static utility>>
        +load(String) Image
    }
    WhackAMoleGame ..> ResourceLoader : uses
    Mole ..> ResourceLoader : uses
    Bomb ..> ResourceLoader : uses
    BonusMole ..> ResourceLoader : uses