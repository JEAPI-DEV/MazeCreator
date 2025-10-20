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

        JLabel formsLabel = new JLabel("Forms:");
        String[] formOptions = {
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P",
            "Q",
            "R",
            "S",
            "T",
            "U",
            "V",
            "W",
            "X",
            "Y",
            "Z",
        };
        JComboBox<String> formsDropdown = new JComboBox<>(formOptions);
        formsDropdown.addActionListener(e -> {
            String selected = (String) formsDropdown.getSelectedItem();
            Mode formMode = Mode.valueOf("FORM_" + selected);
            editor.setCurrentMode(formMode);
        });

        JButton sheetBtn = new JButton("Sheet");
        sheetBtn.addActionListener(e -> editor.setCurrentMode(Mode.SHEET));

        JButton validateBtn = new JButton("Validate");
        validateBtn.addActionListener(e -> {
            MazeValidator validator = new MazeValidator();
            MazeValidator.ValidationResult result = validator.validateMaze(
                grid
            );

            StringBuilder message = new StringBuilder();
            if (result.isValid) {
                message.append("Maze is valid!\n");
            } else {
                message.append("Maze has errors:\n");
                for (String error : result.errors) {
                    message.append("- ").append(error).append("\n");
                }
            }

            if (!result.warnings.isEmpty()) {
                message.append("\nWarnings:\n");
                for (String warning : result.warnings) {
                    message.append("- ").append(warning).append("\n");
                }
            }

            JOptionPane.showMessageDialog(editor, message.toString());
        });

        JButton templatesBtn = new JButton("Templates");
        templatesBtn.addActionListener(e -> {
            String[] templateNames = MazeTemplates.getTemplateNames();
            String selected = (String) JOptionPane.showInputDialog(
                editor,
                "Choose a template:",
                "Maze Templates",
                JOptionPane.PLAIN_MESSAGE,
                null,
                templateNames,
                templateNames.length > 0 ? templateNames[0] : null
            );

            if (selected != null) {
                String[] keys = MazeTemplates.getTemplateKeys();
                for (int i = 0; i < templateNames.length; i++) {
                    if (templateNames[i].equals(selected)) {
                        MazeTemplates.applyTemplate(
                            keys[i],
                            grid,
                            gridSizeSpinner
                        );
                        break;
                    }
                }
            }
        });

        panel.add(floorBtn);
        panel.add(wallBtn);
        panel.add(startBtn);
        panel.add(finishBtn);
        panel.add(playerLabel);
        panel.add(playerSpinner);
        panel.add(gridSizeLabel);
        panel.add(gridSizeSpinner);
        panel.add(clearBtn);
        panel.add(formsLabel);
        panel.add(formsDropdown);
        panel.add(sheetBtn);
        panel.add(loadBtn);
        panel.add(saveBtn);
        panel.add(validateBtn);
        panel.add(templatesBtn);

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

    public JSpinner getGridSizeSpinner() {
        return gridSizeSpinner;
    }
}
