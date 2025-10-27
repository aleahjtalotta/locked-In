# Youtube Implementation
https://youtu.be/q7pfS47aeBs

# locked-In
"Locked In" is a spooky-themed virtual escape room. Explore eerie rooms, solve lightweight puzzles, and escape before the timer runs out.

## Prerequisites
- Java 17 (or newer) JDK available on your `PATH`
- Maven 3.8+ (the JavaFX Maven plugin downloads the JavaFX runtime automatically)

## Build
```bash
mvn clean package
```
The build produces `target/locked-in-1.0.0-SNAPSHOT.jar`.

## Run the JavaFX App
Launch the interactive desktop UI (defaults to the `JSON` data directory):
```bash
mvn javafx:run
```
Use a different data folder by passing an argument:
```bash
mvn javafx:run -Djavafx.run.args="path/to/data"
```

To run the packaged jar directly you must supply the JavaFX modules on the module path:
```bash
java --module-path /path/to/javafx/lib \
     --add-modules javafx.controls \
     -jar target/locked-in-1.0.0-SNAPSHOT.jar
```
Replace `/path/to/javafx/lib` with the location of the JavaFX SDK matching your platform.

## Run the Command-Line Driver
The original text-based menu is still available:
```bash
mvn exec:java
```
Pass an alternate data directory with:
```bash
mvn exec:java -Dexec.args="path/to/data"
```

Game progress (players, puzzles, hints, etc.) is persisted to the chosen data directory via the built-in `DataWriter`.

## Audio Narration
Puzzle stories are narrated aloud using the host operating system:
- macOS: relies on the `say` command
- Linux: expects `espeak` on the `PATH`
- Windows: uses PowerShell’s `System.Speech` synthesizer

If the relevant tool is unavailable, narration quietly falls back to text-only.
