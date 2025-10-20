# Maze Creator

This project is a maze creator application built in Java. It allows users to generate and edit mazes, and potentially solve them (used to make Maze for FH Maze Game).

## Project Structure

The project is organized as follows:

*   **`src/`**: Contains the source code for the application.
*   **`Examples/`**: Contains example maze configurations.
    *   **`WAM.json`**: An example maze definition in JSON format.

## How to run

Either build and run it via your IDE, or download the Release version and run with

```bash
java -jar MazeCreator_1.jar
```
Currently, version 1.

## Key Components

*   **`LabyrinthGenerator.java`**: Responsible for generating maze structures.
*   **`MazeEditor.java`**:  Provides functionality for editing mazes.
*   **`MazeGrid.java`**:  View for the maze data structure.
*   **`MazeIO.java`**:  Handles saving and loading operations for the maze.
