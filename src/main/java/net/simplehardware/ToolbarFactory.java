package net.simplehardware;

import java.awt.*;
import javax.swing.*;

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
        JSpinner playerSpinner = new JSpinner(
            new SpinnerNumberModel(1, 1, 8, 1)
        );
        playerSpinner.addChangeListener(e ->
            editor.setCurrentPlayerId((int) playerSpinner.getValue())
        );

        JLabel gridSizeLabel = new JLabel("Grid Size:");
        gridSizeSpinner = new JSpinner(
            new SpinnerNumberModel(grid.getGridSize(), 10, 500, 1)
        );
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
        loadBtn.addActionListener(e ->
            MazeIO.loadFromJson(editor, grid, gridSizeSpinner)
        );

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
            int n = grid.getCells().length;
            for (int i = 0; i < n; i++) {
                grid.getCells()[i][0].setMode(Mode.WALL, 0);
                grid.getCells()[i][n - 1].setMode(Mode.WALL, 0);
                grid.getCells()[0][i].setMode(Mode.WALL, 0);
                grid.getCells()[n - 1][i].setMode(Mode.WALL, 0);
            }
        });

        JButton genBtn = new JButton("Gen Labyrinth");
        genBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        genBtn.addActionListener(e -> {
            boolean ok = LabyrinthGenerator.generateBalancedMaze(grid, 4);
            if (!ok) {
                JOptionPane.showMessageDialog(
                    editor,
                    "Failed to generate a balanced maze after multiple attempts. Try different grid size."
                );
            } else {
                JOptionPane.showMessageDialog(
                    editor,
                    "Generated a balanced maze for 4 players."
                );
            }
        });

        JTextArea note = new JTextArea(
            "Note: Generation\nis Experimental\n(works better with\nodd Grid sizes.)"
        );
        note.setEditable(false);
        note.setOpaque(false);
        note.setFocusable(false);
        note.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(topWall);
        panel.add(Box.createVerticalStrut(10));
        panel.add(genBtn);
        panel.add(Box.createVerticalStrut(5));
        panel.add(note);

        return panel;
    }

    public JPanel createFormsToolbar() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 6, 2, 2));
        panel.setBorder(BorderFactory.createTitledBorder("Forms"));

        // Add form buttons A-Z
        Mode[] forms = {
            Mode.FORM_A,
            Mode.FORM_B,
            Mode.FORM_C,
            Mode.FORM_D,
            Mode.FORM_E,
            Mode.FORM_F,
            Mode.FORM_G,
            Mode.FORM_H,
            Mode.FORM_I,
            Mode.FORM_J,
            Mode.FORM_K,
            Mode.FORM_L,
            Mode.FORM_M,
            Mode.FORM_N,
            Mode.FORM_O,
            Mode.FORM_P,
            Mode.FORM_Q,
            Mode.FORM_R,
            Mode.FORM_S,
            Mode.FORM_T,
            Mode.FORM_U,
            Mode.FORM_V,
            Mode.FORM_W,
            Mode.FORM_X,
            Mode.FORM_Y,
            Mode.FORM_Z,
        };

        for (Mode form : forms) {
            String letter = form.name().substring(5);
            JButton btn = new JButton(letter);
            btn.setPreferredSize(new Dimension(40, 30));
            btn.addActionListener(e -> editor.setCurrentMode(form));
            panel.add(btn);
        }

        // Add sheet button
        JButton sheetBtn = new JButton("Sheet");
        sheetBtn.addActionListener(e -> editor.setCurrentMode(Mode.SHEET));
        panel.add(sheetBtn);

        return panel;
    }

    public JSpinner getGridSizeSpinner() {
        return gridSizeSpinner;
    }
}
