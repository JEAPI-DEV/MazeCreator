package net.simplehardware;

import com.google.gson.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class MazeIO {
    public static void loadFromJson(MazeEditor editor, MazeGrid grid, JSpinner gridSizeSpinner) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Maze JSON to Load");
        if (chooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
            try (Reader reader = new FileReader(chooser.getSelectedFile())) {
                MazeInfoData data = new Gson().fromJson(reader, MazeInfoData.class);
                applyMazeData(grid, data, gridSizeSpinner);
                JOptionPane.showMessageDialog(editor, "Maze loaded successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(editor, "Failed to load: " + ex.getMessage());
            }
        }
    }

    private static void applyMazeData(MazeGrid grid, MazeInfoData data, JSpinner spinner) {
        if (data.maze == null) return;
        String[] rows = data.maze.split("/");
        int size = rows.length;
        grid.resizeGrid(size);
        spinner.setValue(size);

        CellButton[][] cells = grid.getCells();
        for (int y = 0; y < size; y++) {
            String row = rows[y];
            for (int x = 0; x < size; x++) {
                char chType = row.charAt(2 * x);
                char chOwner = row.charAt(2 * x + 1);
                int pid = Character.isDigit(chOwner) ? chOwner - '0' : 0;
                Mode mode = switch (chType) {
                    case '#' -> Mode.WALL;
                    case '@' -> Mode.START;
                    case '!' -> Mode.FINISH;
                    default -> Mode.FLOOR;
                };
                cells[x][y].setMode(mode, pid);
            }
        }
    }

    public static void exportJson(MazeEditor editor, MazeGrid grid) {
        String mazeId = JOptionPane.showInputDialog(editor, "Enter Maze ID:");
        String mazeName = JOptionPane.showInputDialog(editor, "Enter Maze Name:");
        if (mazeId == null || mazeName == null) return;

        List<String> lines = new ArrayList<>();
        for (int y = 0; y < grid.getCells().length; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < grid.getCells().length; x++) {
                CellButton c = grid.getCells()[x][y];
                char content = switch (c.getMode()) {
                    case WALL -> '#';
                    case START -> '@';
                    case FINISH -> '!';
                    default -> ' ';
                };
                if (content == '#' || content == ' ') {
                    sb.append(content).append(content);
                } else {
                    sb.append(content).append(c.getPlayerId());
                }
            }
            lines.add(sb.toString());
        }

        MazeInfoData maze = new MazeInfoData();
        maze.id = mazeId;
        maze.name = mazeName;
        maze.forms = new ArrayList<>();
        maze.maze = String.join("/", lines);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(mazeId + ".json"));
        if (chooser.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                gson.toJson(maze, writer);
                JOptionPane.showMessageDialog(editor, "Maze saved successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(editor, "Error saving: " + e.getMessage());
            }
        }
    }
}
