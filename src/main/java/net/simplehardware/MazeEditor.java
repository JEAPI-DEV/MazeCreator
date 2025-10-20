package net.simplehardware;

import java.awt.*;
import javax.swing.*;

public class MazeEditor extends JFrame {

    private Mode currentMode = Mode.FLOOR;
    private int currentPlayerId = 1;
    private JLabel modeIndicator;

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
        add(toolbarFactory.createFormsToolbar(), BorderLayout.SOUTH);

        // Add status bar
        modeIndicator = new JLabel("Current Mode: FLOOR | Player: 1");
        modeIndicator.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(modeIndicator, BorderLayout.EAST);

        setVisible(true);
    }

    // Mode and player getters/setters used by CellButton and toolbars
    public void setCurrentMode(Mode mode) {
        this.currentMode = mode;
        updateModeIndicator();
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentPlayerId(int id) {
        this.currentPlayerId = id;
        updateModeIndicator();
    }

    public int getCurrentPlayerId() {
        return currentPlayerId;
    }

    private void updateModeIndicator() {
        String modeText = currentMode.name().replace("_", " ");
        modeIndicator.setText(
            "Current Mode: " + modeText + " | Player: " + currentPlayerId
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MazeEditor::new);
    }
}
