package net.simplehardware;

import java.util.*;

public class LabyrinthGenerator {

    private static final Random RNG = new Random();

    public static boolean generateBalancedMaze(MazeGrid grid, int players) {
        int n = grid.getGridSize();
        if (n < 5) return false;

        CellButton[][] cells = grid.getCells();

        // --- Step 1: generate structural maze ---
        generateRecursiveBacktrackerMaze(cells);

        // --- Step 2: find finish + 4 balanced starts ---
        return placeBalancedPlayers(grid, players);
    }

    // ------------------------------------------------
    // 1. Recursive Backtracking Maze Generator
    // ------------------------------------------------
    private static void generateRecursiveBacktrackerMaze(CellButton[][] cells) {
        int n = cells.length;
        // Fill with walls
        for (int y = 0; y < n; y++)
            for (int x = 0; x < n; x++)
                cells[x][y].setMode(Mode.WALL, 0);

        boolean[][] visited = new boolean[n][n];
        int startX = (RNG.nextInt(n / 2)) * 2 + 1;
        int startY = (RNG.nextInt(n / 2)) * 2 + 1;

        dfsMaze(cells, visited, startX, startY);
    }

    private static void dfsMaze(CellButton[][] cells, boolean[][] visited, int x, int y) {
        int n = cells.length;
        visited[x][y] = true;
        cells[x][y].setMode(Mode.FLOOR, 0);

        int[][] dirs = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        Collections.shuffle(Arrays.asList(dirs), RNG);

        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx > 0 && ny > 0 && nx < n - 1 && ny < n - 1 && !visited[nx][ny]) {
                // carve wall between (x,y) and (nx,ny)
                cells[x + d[0] / 2][y + d[1] / 2].setMode(Mode.FLOOR, 0);
                dfsMaze(cells, visited, nx, ny);
            }
        }
    }

    // ------------------------------------------------
    // 2. Balanced Player & Finish Placement
    // ------------------------------------------------
    private static boolean placeBalancedPlayers(MazeGrid grid, int players) {
        int n = grid.getGridSize();
        CellButton[][] cells = grid.getCells();

        List<int[]> floors = collectFloors(cells);
        if (floors.size() < players + 1) return false;

        for (int attempt = 0; attempt < 40; attempt++) {
            // Pick finish near center
            int[] finish = pickCenterish(floors, n);
            int[][] dist = bfsDistances(cells, finish[0], finish[1]);

            // Filter good starts (far enough from finish)
            List<int[]> goodStarts = new ArrayList<>();
            for (int[] f : floors) {
                int d = dist[f[0]][f[1]];
                if (d > n / 3) goodStarts.add(new int[]{f[0], f[1], d});
            }
            if (goodStarts.size() < players) continue;

            Collections.shuffle(goodStarts, RNG);
            List<int[]> starts = goodStarts.subList(0, players);

            double[] dists = starts.stream().mapToDouble(s -> s[2]).toArray();
            double mean = Arrays.stream(dists).average().orElse(0);
            double tol = mean * 0.2;

            boolean ok = true;
            for (double d : dists) {
                if (Math.abs(d - mean) > tol) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            // Clear old markers
            clearMarkers(cells);
            // Place finish + starts
            cells[finish[0]][finish[1]].setMode(Mode.FINISH, 0);
            for (int i = 0; i < players; i++)
                cells[starts.get(i)[0]][starts.get(i)[1]].setMode(Mode.START, i + 1);
            return true;
        }
        return false;
    }

    private static void clearMarkers(CellButton[][] cells) {
        for (CellButton[] row : cells)
            for (CellButton c : row)
                if (c.getMode() == Mode.START || c.getMode() == Mode.FINISH)
                    c.setMode(Mode.FLOOR, 0);
    }

    private static List<int[]> collectFloors(CellButton[][] cells) {
        int n = cells.length;
        List<int[]> list = new ArrayList<>();
        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++)
                if (cells[x][y].getMode() == Mode.FLOOR)
                    list.add(new int[]{x, y});
        return list;
    }

    private static int[] pickCenterish(List<int[]> list, int n) {
        int cx = n / 2, cy = n / 2;
        list.sort(Comparator.comparingDouble(a ->
                Math.hypot(a[0] - cx, a[1] - cy)));
        return list.get(RNG.nextInt(Math.min(list.size(), 20)));
    }

    private static int[][] bfsDistances(CellButton[][] cells, int sx, int sy) {
        int n = cells.length;
        int[][] dist = new int[n][n];
        for (int[] row : dist) Arrays.fill(row, -1);
        Queue<int[]> q = new ArrayDeque<>();
        dist[sx][sy] = 0;
        q.add(new int[]{sx, sy});
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int nx = c[0] + d[0], ny = c[1] + d[1];
                if (nx >= 0 && ny >= 0 && nx < n && ny < n &&
                        dist[nx][ny] == -1 && cells[nx][ny].getMode() != Mode.WALL) {
                    dist[nx][ny] = dist[c[0]][c[1]] + 1;
                    q.add(new int[]{nx, ny});
                }
            }
        }
        return dist;
    }
}
