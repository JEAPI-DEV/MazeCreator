package net.simplehardware;

import java.util.*;

public class MazeValidator {

    public static class ValidationResult {
        public boolean isValid;
        public List<String> errors;
        public List<String> warnings;

        public ValidationResult() {
            this.isValid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }

        public void addError(String error) {
            this.errors.add(error);
            this.isValid = false;
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
    }

    public ValidationResult validateMaze(MazeGrid grid) {
        ValidationResult result = new ValidationResult();
        CellButton[][] cells = grid.getCells();
        int size = cells.length;

        Map<Integer, Integer> playerStarts = new HashMap<>();
        Map<Integer, Integer> playerFinishes = new HashMap<>();
        Map<Integer, Set<Character>> playerForms = new HashMap<>();

        // Collect player data
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                CellButton cell = cells[x][y];
                Mode mode = cell.getMode();
                int playerId = cell.getPlayerId();

                if (mode == Mode.START) {
                    playerStarts.put(playerId, playerStarts.getOrDefault(playerId, 0) + 1);
                } else if (mode == Mode.FINISH) {
                    playerFinishes.put(playerId, playerFinishes.getOrDefault(playerId, 0) + 1);
                } else if (isFormMode(mode)) {
                    char formLetter = mode.name().charAt(5);
                    playerForms.computeIfAbsent(playerId, k -> new HashSet<>()).add(formLetter);
                }
            }
        }

        // Validate player starts and finishes
        Set<Integer> allPlayers = new HashSet<>();
        allPlayers.addAll(playerStarts.keySet());
        allPlayers.addAll(playerFinishes.keySet());
        allPlayers.addAll(playerForms.keySet());

        for (int playerId : allPlayers) {
            int starts = playerStarts.getOrDefault(playerId, 0);
            int finishes = playerFinishes.getOrDefault(playerId, 0);

            if (starts == 0) {
                result.addError("Player " + playerId + " has no start position");
            } else if (starts > 1) {
                result.addError("Player " + playerId + " has " + starts + " start positions, should have exactly 1");
            }

            if (finishes == 0) {
                result.addError("Player " + playerId + " has no finish position");
            } else if (finishes > 1) {
                result.addError("Player " + playerId + " has " + finishes + " finish positions, should have exactly 1");
            }
        }

        // Validate form sequences
        for (int playerId : playerForms.keySet()) {
            if (!verifyFormSequence(playerId, playerForms.get(playerId), result)) {
                // Error already added by verifyFormSequence
            }
        }

        // Check balance between players
        checkPlayerBalance(allPlayers, playerForms, result);

        return result;
    }

    public boolean checkPlayerBalance(int playerId, CellButton[][] cells) {
        // Simple balance check - could be expanded
        return true;
    }

    private void checkPlayerBalance(Set<Integer> allPlayers, Map<Integer, Set<Character>> playerForms, ValidationResult result) {
        if (allPlayers.size() <= 1) return;

        int minForms = Integer.MAX_VALUE;
        int maxForms = 0;

        for (int playerId : allPlayers) {
            int formCount = playerForms.getOrDefault(playerId, new HashSet<>()).size();
            minForms = Math.min(minForms, formCount);
            maxForms = Math.max(maxForms, formCount);
        }

        if (maxForms - minForms > 2) {
            result.addWarning("Unbalanced forms: some players have " + maxForms + " forms while others have " + minForms);
        }
    }

    public boolean verifyFormSequence(int playerId, Set<Character> forms, ValidationResult result) {
        if (forms.isEmpty()) return true;

        char maxForm = Collections.max(forms);
        for (char c = 'A'; c <= maxForm; c++) {
            if (!forms.contains(c)) {
                result.addError("Player " + playerId + " missing form " + c + " in sequence (has forms up to " + maxForm + ")");
                return false;
            }
        }
        return true;
    }

    public boolean checkPathReachability(CellButton[][] cells, int startX, int startY, int targetX, int targetY) {
        int size = cells.length;
        boolean[][] visited = new boolean[size][size];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startX, startY});
        visited[startX][startY] = true;

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int x = pos[0], y = pos[1];

            if (x == targetX && y == targetY) {
                return true;
            }

            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];

                if (nx >= 0 && nx < size && ny >= 0 && ny < size && !visited[nx][ny]) {
                    if (cells[nx][ny].getMode() != Mode.WALL) {
                        visited[nx][ny] = true;
                        queue.offer(new int[]{nx, ny});
                    }
                }
            }
        }

        return false;
    }

    private boolean isFormMode(Mode mode) {
        return mode.name().startsWith("FORM_");
    }
}
