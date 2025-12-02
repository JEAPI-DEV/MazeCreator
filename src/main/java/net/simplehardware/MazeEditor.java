package net.simplehardware;

import net.simplehardware.models.Mode;
import java.awt.*;
import javax.swing.*;

public class MazeEditor extends JFrame {

    private Mode currentMode = Mode.FLOOR;
    private int currentPlayerId = 1;

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

        setVisible(true);
    }

    // Mode and player getters/setters used by CellButton and toolbars
    public void setCurrentMode(Mode mode) {
        this.currentMode = mode;
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentPlayerId(int id) {
        this.currentPlayerId = id;
    }

    public int getCurrentPlayerId() {
        return currentPlayerId;
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--generateMaze")) {
            handleCliGeneration(args);
            return;
        }

        try {
            javax.swing.LookAndFeel materialLF = new mdlaf.MaterialLookAndFeel();
            UIManager.setLookAndFeel(materialLF);
            System.out.println("Material UI Look and Feel applied successfully");
        } catch (Exception e) {
            System.err.println("Failed to apply Material UI Look and Feel: " + e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                System.out.println("System Look and Feel applied as fallback");
            } catch (Exception fallbackException) {
                System.err.println("Failed to set system look and feel: " + fallbackException.getMessage());
            }
        }
        SwingUtilities.invokeLater(MazeEditor::new);
    }

    private static void handleCliGeneration(String[] args) {
        int forms = 2;
        int prefSteps = 20;
        int mazeSize = 20;
        String outputFile = "output.json";

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--forms":
                    if (i + 1 < args.length)
                        forms = Integer.parseInt(args[++i]);
                    break;
                case "--prefSteps":
                    if (i + 1 < args.length)
                        prefSteps = Integer.parseInt(args[++i]);
                    break;
                case "--mazesize":
                    if (i + 1 < args.length)
                        mazeSize = Integer.parseInt(args[++i]);
                    break;
                case "--output":
                    // Check if next arg is not a flag, if so use it as filename
                    // Otherwise, we might expect the filename at the end
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        outputFile = args[++i];
                    }
                    break;
                default:
                    if (!args[i].startsWith("-")) {
                        outputFile = args[i];
                    }
                    break;
            }
        }

        System.out.println("Generating maze with settings:");
        System.out.println("Forms: " + forms);
        System.out.println("Preferred Steps: " + prefSteps);
        System.out.println("Size: " + mazeSize);
        System.out.println("Output: " + outputFile);

        MazeGrid grid = new MazeGrid(mazeSize, null);
        net.simplehardware.generators.SymmetricGenerator.generate(grid, forms, prefSteps, progress -> {
            System.out.println("Progress: " + progress + "%");
        });

        try {
            net.simplehardware.utils.MazeIO.exportJsonToFile(grid, "CLI_Generated_Maze", outputFile);
            System.out.println("Maze generated and saved to " + outputFile);
        } catch (java.io.IOException e) {
            System.err.println("Failed to save maze: " + e.getMessage());
            e.printStackTrace();
        }
        System.exit(0);
    }
}
