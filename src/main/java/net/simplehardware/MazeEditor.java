package net.simplehardware;

import javax.swing.*;
import java.awt.*;

public class MazeEditor extends JFrame {
    private Mode currentMode = Mode.FLOOR;
    private int currentPlayerId = 1;
    private final JSpinner gridSizeSpinner;

    public MazeEditor() {
        setTitle("Maze Editor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 850);
        setLayout(new BorderLayout());

        int gridSize = 10;
        MazeGrid mazeGrid = new MazeGrid(gridSize, this);
        add(mazeGrid.getScrollPane(), BorderLayout.CENTER);

        ToolbarFactory toolbarFactory = new ToolbarFactory(this, mazeGrid);
        add(toolbarFactory.createTopToolbar(), BorderLayout.NORTH);
        add(toolbarFactory.createLeftToolbar(), BorderLayout.WEST);

        gridSizeSpinner = toolbarFactory.getGridSizeSpinner();

        setVisible(true);
    }

    // Mode and player getters/setters used by CellButton and toolbars
    public void setCurrentMode(Mode mode) { this.currentMode = mode; }
    public Mode getCurrentMode() { return currentMode; }
    public void setCurrentPlayerId(int id) { this.currentPlayerId = id; }
    public int getCurrentPlayerId() { return currentPlayerId; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MazeEditor::new);
    }
}
