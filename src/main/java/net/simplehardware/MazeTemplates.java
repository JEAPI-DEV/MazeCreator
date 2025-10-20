package net.simplehardware;

import java.util.HashMap;
import java.util.Map;

public class MazeTemplates {

    public static class Template {
        public String name;
        public String description;
        public int size;
        public int players;
        public String pattern;

        public Template(String name, String description, int size, int players, String pattern) {
            this.name = name;
            this.description = description;
            this.size = size;
            this.players = players;
            this.pattern = pattern;
        }
    }

    private static final Map<String, Template> templates = new HashMap<>();

    static {
        // Basic 2-player template
        templates.put("basic2p", new Template(
            "Basic 2-Player",
            "Simple maze for 2 players with forms A-C",
            5,
            2,
            "#0#0#0#0#0/" +
            "#0@1A1B1!1/" +
            "#0 0 0 0#0/" +
            "#0@2A2B2!2/" +
            "#0#0#0#0#0"
        ));

        // Basic 4-player template
        templates.put("basic4p", new Template(
            "Basic 4-Player",
            "Balanced maze for 4 players with forms A-B",
            7,
            4,
            "#0#0#0#0#0#0#0/" +
            "#0@1A1 0 0B1!1/" +
            "#0 0 0 0 0 0#0/" +
            "#0@2A2 0 0B2!2/" +
            "#0 0 0 0 0 0#0/" +
            "#0@3A3 0 0B3!3/" +
            "#0@4A4 0 0B4!4"
        ));

        // Training template
        templates.put("training", new Template(
            "Training Maze",
            "Simple training maze with one form per player",
            6,
            2,
            "#0#0#0#0#0#0/" +
            "#0@1 0A1 0!1/" +
            "#0 0#0#0 0#0/" +
            "#0 0#0#0 0#0/" +
            "#0@2 0A2 0!2/" +
            "#0#0#0#0#0#0"
        ));

        // Complex template
        templates.put("complex", new Template(
            "Complex Maze",
            "Advanced maze with multiple forms",
            8,
            2,
            "#0#0#0#0#0#0#0#0/" +
            "#0@1A1 0 0 0B1!1/" +
            "#0 0#0#0#0#0 0#0/" +
            "#0C1 0 0 0 0D1#0/" +
            "#0#0 0 0 0 0#0#0/" +
            "#0C2 0 0 0 0D2#0/" +
            "#0 0#0#0#0#0 0#0/" +
            "#0@2A2 0 0 0B2!2"
        ));
    }

    public static Template getTemplate(String key) {
        return templates.get(key);
    }

    public static String[] getTemplateKeys() {
        return templates.keySet().toArray(new String[0]);
    }

    public static String[] getTemplateNames() {
        return templates.values().stream()
            .map(t -> t.name)
            .toArray(String[]::new);
    }

    public static void applyTemplate(String key, MazeGrid grid, javax.swing.JSpinner gridSizeSpinner) {
        Template template = getTemplate(key);
        if (template == null) return;

        // Resize grid
        grid.resizeGrid(template.size);
        gridSizeSpinner.setValue(template.size);

        // Parse and apply pattern
        String[] rows = template.pattern.split("/");
        CellButton[][] cells = grid.getCells();

        for (int y = 0; y < template.size && y < rows.length; y++) {
            String row = rows[y];
            for (int x = 0; x < template.size && x < row.length() / 2; x++) {
                char chType = row.charAt(2 * x);
                char chOwner = row.charAt(2 * x + 1);
                int pid = Character.isDigit(chOwner) ? chOwner - '0' : 0;

                Mode mode = switch (chType) {
                    case '#' -> Mode.WALL;
                    case '@' -> Mode.START;
                    case '!' -> Mode.FINISH;
                    case 'S' -> Mode.SHEET;
                    case 'A' -> Mode.FORM_A;
                    case 'B' -> Mode.FORM_B;
                    case 'C' -> Mode.FORM_C;
                    case 'D' -> Mode.FORM_D;
                    case 'E' -> Mode.FORM_E;
                    case 'F' -> Mode.FORM_F;
                    case 'G' -> Mode.FORM_G;
                    case 'H' -> Mode.FORM_H;
                    case 'I' -> Mode.FORM_I;
                    case 'J' -> Mode.FORM_J;
                    case 'K' -> Mode.FORM_K;
                    case 'L' -> Mode.FORM_L;
                    case 'M' -> Mode.FORM_M;
                    case 'N' -> Mode.FORM_N;
                    case 'O' -> Mode.FORM_O;
                    case 'P' -> Mode.FORM_P;
                    case 'Q' -> Mode.FORM_Q;
                    case 'R' -> Mode.FORM_R;
                    case '$' -> Mode.FORM_S;
                    case 'T' -> Mode.FORM_T;
                    case 'U' -> Mode.FORM_U;
                    case 'V' -> Mode.FORM_V;
                    case 'W' -> Mode.FORM_W;
                    case 'X' -> Mode.FORM_X;
                    case 'Y' -> Mode.FORM_Y;
                    case 'Z' -> Mode.FORM_Z;
                    default -> Mode.FLOOR;
                };

                cells[x][y].setMode(mode, pid);
            }
        }
    }

    public static Template createCustomTemplate(String name, String description, MazeGrid grid) {
        CellButton[][] cells = grid.getCells();
        int size = cells.length;
        StringBuilder pattern = new StringBuilder();

        for (int y = 0; y < size; y++) {
            if (y > 0) pattern.append("/");
            for (int x = 0; x < size; x++) {
                CellButton cell = cells[x][y];
                Mode mode = cell.getMode();
                char content = switch (mode) {
                    case WALL -> '#';
                    case START -> '@';
                    case FINISH -> '!';
                    case SHEET -> 'S';
                    case FORM_A -> 'A';
                    case FORM_B -> 'B';
                    case FORM_C -> 'C';
                    case FORM_D -> 'D';
                    case FORM_E -> 'E';
                    case FORM_F -> 'F';
                    case FORM_G -> 'G';
                    case FORM_H -> 'H';
                    case FORM_I -> 'I';
                    case FORM_J -> 'J';
                    case FORM_K -> 'K';
                    case FORM_L -> 'L';
                    case FORM_M -> 'M';
                    case FORM_N -> 'N';
                    case FORM_O -> 'O';
                    case FORM_P -> 'P';
                    case FORM_Q -> 'Q';
                    case FORM_R -> 'R';
                    case FORM_S -> '$';
                    case FORM_T -> 'T';
                    case FORM_U -> 'U';
                    case FORM_V -> 'V';
                    case FORM_W -> 'W';
                    case FORM_X -> 'X';
                    case FORM_Y -> 'Y';
                    case FORM_Z -> 'Z';
                    default -> ' ';
                };

                if (content == '#' || content == ' ') {
                    pattern.append(content).append('0');
                } else {
                    pattern.append(content).append(cell.getPlayerId());
                }
            }
        }

        return new Template(name, description, size, 4, pattern.toString());
    }
}
