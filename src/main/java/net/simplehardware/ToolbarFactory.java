package net.simplehardware;

import javax.swing.*;
import java.awt.*;

public class ToolbarFactory {
    private final MazeEditor editor;
    private final MazeGrid grid;
    private JSpinner gridSizeSpinner;

    public ToolbarFactory(MazeEditor editor, MazeGrid grid) {
        this.editor = editor;
        this.grid = grid;
    }

    public JPanel createTopToolbar() {
        JPanel panel = new JPanel();

        JButton floorBtn = new JButton("Floor");
        floorBtn.addActionListener(e -> editor.setCurrentMode(Mode.FLOOR));

        JButton wallBtn = new JButton("Wall");
        wallBtn.addActionListener(e -> editor.setCurrentMode(Mode.WALL));

        JButton startBtn = new JButton("Player Start");
        startBtn.addActionListener(e -> editor.setCurrentMode(Mode.START));

        JButton finishBtn = new JButton("Finish");
        finishBtn.addActionListener(e -> editor.setCurrentMode(Mode.FINISH));

        JLabel playerLabel = new JLabel("Player ID:");
        JSpinner playerSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        playerSpinner.addChangeListener(e -> editor.setCurrentPlayerId((int) playerSpinner.getValue()));

        JLabel gridSizeLabel = new JLabel("Grid Size:");
        gridSizeSpinner = new JSpinner(new SpinnerNumberModel(grid.getGridSize(), 10, 500, 1));
        gridSizeSpinner.addChangeListener(e -> {
            int newSize = (int) gridSizeSpinner.getValue();
            grid.resizeGrid(newSize);
        });

        JButton clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> {
            for (CellButton[] row : grid.getCells()) {
                for (CellButton cell : row) {
                    cell.setMode(Mode.FLOOR, 0);
                }
            }
        });

        JButton loadBtn = new JButton("Load JSON");
        loadBtn.addActionListener(e -> MazeIO.loadFromJson(editor, grid, gridSizeSpinner));

        JButton saveBtn = new JButton("Export JSON");
        saveBtn.addActionListener(e -> MazeIO.exportJson(editor, grid));


        panel.add(floorBtn);
        panel.add(wallBtn);
        panel.add(startBtn);
        panel.add(finishBtn);
        panel.add(playerLabel);
        panel.add(playerSpinner);
        panel.add(gridSizeLabel);
        panel.add(gridSizeSpinner);
        panel.add(clearBtn);
        panel.add(loadBtn);
        panel.add(saveBtn);

        return panel;
    }

    public JPanel createLeftToolbar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Border Tools"));

        JButton topWall = new JButton("Edge Walls");
        topWall.setAlignmentX(Component.CENTER_ALIGNMENT);
        topWall.addActionListener(e -> {
            for (int x = 0; x < grid.getCells().length; x++)
                grid.getCells()[x][0].setMode(Mode.WALL, 0);
            int maxY = grid.getCells().length - 1;
            for (int x = 0; x < grid.getCells().length; x++)
                grid.getCells()[x][maxY].setMode(Mode.WALL, 0);
            for (int y = 0; y < grid.getCells().length; y++)
                grid.getCells()[0][y].setMode(Mode.WALL, 0);
            int maxX = grid.getCells().length - 1;
            for (int y = 0; y < grid.getCells().length; y++)
                grid.getCells()[maxX][y].setMode(Mode.WALL, 0);
        });

        JButton genBtn = new JButton("Gen Labyrinth");
        genBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        genBtn.addActionListener(e -> {
            boolean ok = LabyrinthGenerator.generateBalancedMaze(grid, 4);
            if (!ok) {
                JOptionPane.showMessageDialog(editor, "Failed to generate a balanced maze after multiple attempts. Try different grid size.");
            } else {
                JOptionPane.showMessageDialog(editor, "Generated a balanced maze for 4 players.");
            }
        });
        panel.add(topWall);
        panel.add(Box.createVerticalStrut(10));
        panel.add(genBtn);

        return panel;
    }

    public JSpinner getGridSizeSpinner() {
        return gridSizeSpinner;
    }
}
