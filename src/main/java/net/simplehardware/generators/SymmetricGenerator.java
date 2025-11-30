package net.simplehardware.generators;

import net.simplehardware.MazeGrid;
import net.simplehardware.models.CellButton;
import net.simplehardware.models.Mode;

import net.simplehardware.utils.Pathfinder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

import java.util.function.Consumer;

public class SymmetricGenerator {

    private static final Random RNG = new Random();

    private record CellState(Mode mode, int playerId) {
    }

    public static void generate(MazeGrid grid, int numForms, int preferredMoves, Consumer<Integer> progressCallback) {
        int n = grid.getGridSize();
        CellButton[][] cells = grid.getCells();
        CellState[][] bestLayoutState = null;
        double bestOverallScore = -Double.MAX_VALUE;

        int maxAttempts = 100;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.println("--- Attempt " + attempt + "/" + maxAttempts + " ---");
            System.out.flush();

            // 1. Initialize with Walls
            System.out.println("Step 1: Initializing Walls");
            System.out.flush();
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    cells[x][y].setMode(Mode.WALL, 0);
                }
            }

            // 2. Create Rooms (4-Way Symmetric)
            System.out.println("Step 2: Creating Rooms");
            System.out.flush();
            int halfN = n / 2;
            int numRooms = (halfN * halfN) / 50;
            if (numRooms < 1)
                numRooms = 1;

            for (int i = 0; i < numRooms; i++) {
                int w = RNG.nextInt(3) + 3;
                int h = RNG.nextInt(3) + 3;
                int xBound = halfN - w - 1;
                int yBound = halfN - h - 1;
                if (xBound > 0 && yBound > 0) {
                    int x = RNG.nextInt(xBound) + 1;
                    int y = RNG.nextInt(yBound) + 1;
                    createRoom4Way(cells, x, y, w, h);
                }
            }

            // 3. Maze Generation
            System.out.println("Step 3: DFS Generation");
            System.out.flush();
            boolean[][] visited = new boolean[n][n];
            generateSymmetric4Way(cells, visited, 1, 1, n);

            // 4. Ensure Connectivity
            System.out.println("Step 4: Ensuring Connectivity");
            System.out.flush();
            int centerX = n / 2;
            int centerY = n / 2;
            for (int x = centerX - 2; x <= centerX + 2; x++) {
                if (x > 0 && x < n - 1) {
                    cells[x][centerY].setMode(Mode.FLOOR, 0);
                    if (n % 2 == 0)
                        cells[x][centerY - 1].setMode(Mode.FLOOR, 0);
                }
            }
            for (int y = centerY - 2; y <= centerY + 2; y++) {
                if (y > 0 && y < n - 1) {
                    cells[centerX][y].setMode(Mode.FLOOR, 0);
                    if (n % 2 == 0)
                        cells[centerX - 1][y].setMode(Mode.FLOOR, 0);
                }
            }

            // 5. Place Objectives
            System.out.println("Step 5: Collecting Floor Cells");
            System.out.flush();
            List<int[]> floorCells = new ArrayList<>();
            int limitX = n / 2;
            int limitY = n / 2;

            for (int x = 1; x < limitX; x++) {
                for (int y = 1; y < limitY; y++) {
                    if (cells[x][y].getMode() == Mode.FLOOR) {
                        floorCells.add(new int[] { x, y });
                    }
                }
            }

            System.out.println("Floor cells found: " + floorCells.size());
            System.out.flush();

            if (floorCells.size() >= 10) {
                floorCells.sort((a, b) -> {
                    if (a[1] != b[1])
                        return Integer.compare(a[1], b[1]);
                    return Integer.compare(a[0], b[0]);
                });

                int size = floorCells.size();
                int bandSize = size / 3;
                List<int[]> startPool = floorCells.subList(0, bandSize);
                List<int[]> formPool = floorCells.subList(bandSize, size - bandSize);
                List<int[]> finishPool = floorCells.subList(size - bandSize, size);

                int numPoints = 2 + numForms;
                // Restore normal settings
                int totalIterations = 1000;
                AtomicInteger progressCounter = new AtomicInteger(
                        0);
                final int currentAttempt = attempt;

                final Object updateLock = new Object();
                final double[] bestScoreVal = { -Double.MAX_VALUE };
                final java.util.concurrent.atomic.AtomicReference<List<int[]>> bestPlacementRef = new java.util.concurrent.atomic.AtomicReference<>(
                        new ArrayList<>());

                System.out.println("Step 6: Starting Parallel Optimization (" + totalIterations + " iterations)");
                System.out.flush();

                int cores = Runtime.getRuntime().availableProcessors();
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                        .newFixedThreadPool(cores);
                System.out.println("Step 6: ExecutorService created with " + cores + " threads");
                System.out.flush();

                for (int i = 0; i < totalIterations; i++) {
                    final int iterationIndex = i;
                    executor.submit(() -> {
                        try {
                            java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom
                                    .current();
                            List<int[]> q1Points = new ArrayList<>();

                            if (numPoints == 3) {
                                q1Points.add(startPool.get(random.nextInt(startPool.size())));
                                q1Points.add(formPool.get(random.nextInt(formPool.size())));
                                q1Points.add(finishPool.get(random.nextInt(finishPool.size())));
                            } else {
                                Set<Integer> indices = new HashSet<>();
                                while (indices.size() < numPoints) {
                                    indices.add(random.nextInt(floorCells.size()));
                                }
                                for (int idx : indices) {
                                    q1Points.add(floorCells.get(idx));
                                }
                            }

                            Collections.shuffle(q1Points, random);
                            List<int[]> currentConfig = new ArrayList<>();

                            int[] startQ1 = q1Points.get(0);
                            int[] startAbs = getPointInQuadrant(startQ1[0], startQ1[1], 0, n);
                            currentConfig.add(new int[] { startAbs[0], startAbs[1], Mode.START.ordinal() });

                            for (int j = 0; j < numForms; j++) {
                                int[] formQ1 = q1Points.get(1 + j);
                                int q = random.nextInt(4);
                                int[] formAbs = getPointInQuadrant(formQ1[0], formQ1[1], q, n);
                                char formChar = (char) ('A' + j);
                                Mode m = Mode.valueOf("FORM_" + formChar);
                                currentConfig.add(new int[] { formAbs[0], formAbs[1], m.ordinal() });
                            }

                            int[] finishQ1 = q1Points.get(q1Points.size() - 1);
                            int q = random.nextInt(4);
                            int[] finishAbs = getPointInQuadrant(finishQ1[0], finishQ1[1], q, n);
                            currentConfig.add(new int[] { finishAbs[0], finishAbs[1], Mode.FINISH.ordinal() });

                            int pathLength = 0;
                            boolean validPath = true;
                            double minSegmentDist = Double.MAX_VALUE;

                            for (int j = 0; j < currentConfig.size() - 1; j++) {
                                int[] p1 = currentConfig.get(j);
                                int[] p2 = currentConfig.get(j + 1);
                                int dist = Pathfinder.findShortestPath(cells, new Pathfinder.Point(p1[0], p1[1]),
                                        new Pathfinder.Point(p2[0], p2[1]));
                                if (dist == -1) {
                                    validPath = false;
                                    break;
                                }
                                pathLength += dist;
                                double segmentDist = Math.abs(p1[0] - p2[0]) + Math.abs(p1[1] - p2[1]);
                                if (segmentDist < minSegmentDist)
                                    minSegmentDist = segmentDist;
                            }

                            if (validPath) {
                                double score;
                                if (preferredMoves > 0) {
                                    int diff = Math.abs(pathLength - preferredMoves);
                                    double clusteringPenalty = (minSegmentDist < 3.0) ? 1000.0 : 0.0;
                                    score = 10000 - (diff * 25) - clusteringPenalty + (minSegmentDist * 0.5);
                                } else {
                                    score = pathLength + (minSegmentDist * 10);
                                }

                                synchronized (updateLock) {
                                    if (score > bestScoreVal[0]) {
                                        bestScoreVal[0] = score;
                                        bestPlacementRef.set(new ArrayList<>(currentConfig));
                                    }
                                }
                            }

                            int p = progressCounter.incrementAndGet();
                            if (p % 100 == 0 && progressCallback != null) {
                                int globalStep = (currentAttempt - 1) * totalIterations + p;
                                int totalSteps = maxAttempts * totalIterations;
                                int percent = (int) ((globalStep / (double) totalSteps) * 100);
                                progressCallback.accept(percent);
                            }
                        } catch (Throwable e) {
                            System.err.println("CRITICAL ERROR IN TASK " + iterationIndex);
                            e.printStackTrace();
                        }
                    });
                }

                executor.shutdown();
                try {
                    // Wait for all tasks to finish
                    if (!executor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                        System.err.println("Executor timed out!");
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }

                List<int[]> bestPlacement = bestPlacementRef.get();
                Double bestAttemptScoreVal = bestScoreVal[0];
                double bestAttemptScore = (bestAttemptScoreVal == null) ? -Double.MAX_VALUE : bestAttemptScoreVal;

                System.out.println("Attempt " + attempt + " Best Score: " + bestAttemptScore);
                System.out.flush();

                if (bestPlacement != null) {
                    // Apply placement to current grid to save it temporarily for this attempt
                    // First, clear any previous objectives from this attempt
                    for (int x = 0; x < n; x++) {
                        for (int y = 0; y < n; y++) {
                            Mode m = cells[x][y].getMode();
                            if (m == Mode.START || m == Mode.FINISH || m.toString().startsWith("FORM_")) {
                                cells[x][y].setMode(Mode.FLOOR, 0);
                            }
                        }
                    }

                    for (int[] item : bestPlacement) {
                        Mode m = Mode.values()[item[2]];
                        placeObjectivesSymmetric(cells, item[0], item[1], m, n);
                    }

                    // Check if this is the best overall
                    if (bestAttemptScore > bestOverallScore) {
                        bestOverallScore = bestAttemptScore;
                        // Save this layout (Mode and Player ID)
                        bestLayoutState = new CellState[n][n];
                        for (int xx = 0; xx < n; xx++) {
                            for (int yy = 0; yy < n; yy++) {
                                bestLayoutState[xx][yy] = new CellState(cells[xx][yy].getMode(),
                                        cells[xx][yy].getPlayerId());
                            }
                        }
                    }

                    // Early exit if good enough (e.g. score > 9000 means diff is small)
                    // Score = 10000 - diff*10 ...
                    // If diff is 0, score is 10000 + ...
                    // If diff is 10% of 200 = 20, penalty is 200. Score ~ 9800.
                    if (bestAttemptScore > 9500) {
                        System.out.println("Found excellent match! Stopping early.");
                        break;
                    }
                }

            } else {
                // Fallback placement for this attempt
                fallbackPlacement(cells, n, halfN);
                // If this is the first valid layout or better than previous, save it
                // For fallback, we don't have a score, so we just take the first one if no
                // other valid maze was found.
                if (bestLayoutState == null) {
                    bestLayoutState = new CellState[n][n];
                    for (int xx = 0; xx < n; xx++) {
                        for (int yy = 0; yy < n; yy++) {
                            bestLayoutState[xx][yy] = new CellState(cells[xx][yy].getMode(),
                                    cells[xx][yy].getPlayerId());
                        }
                    }
                }
            }
        }

        // Restore best layout found across all attempts
        if (bestLayoutState != null) {
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    CellState state = bestLayoutState[x][y];
                    cells[x][y].setMode(state.mode(), state.playerId());
                }
            }
        } else {
            // If no valid layout was ever found (e.g., grid too small, floorCells.size() <
            // 10 always)
            // Re-run fallback one last time on the current (potentially empty) grid
            System.err
                    .println("Warning: No valid maze layout found after " + maxAttempts + " attempts. Using fallback.");
            fallbackPlacement(cells, n, n / 2);
        }

        if (progressCallback != null)
            progressCallback.accept(100); // Ensure 100% at end
    }

    private static int[] getPointInQuadrant(int x, int y, int quadrant, int n) {
        // x, y are in Q1 (Top-Left) relative to 0,0
        // Q1: x, y
        // Q2: n-1-x, y (Top-Right)
        // Q3: x, n-1-y (Bottom-Left)
        // Q4: n-1-x, n-1-y (Bottom-Right)

        return switch (quadrant) {
            case 1 -> new int[] { n - 1 - x, y };
            case 2 -> new int[] { x, n - 1 - y };
            case 3 -> new int[] { n - 1 - x, n - 1 - y };
            default -> new int[] { x, y };
        };
    }

    private static void permute(List<int[]> arr, int k, List<List<int[]>> result) {
        if (k == arr.size()) {
            result.add(new ArrayList<>(arr));
        } else {
            for (int i = k; i < arr.size(); i++) {
                Collections.swap(arr, i, k);
                permute(arr, k + 1, result);
                Collections.swap(arr, i, k);
            }
        }
    }

    private static void fallbackPlacement(CellButton[][] cells, int n, int halfN) {
        placeObjectivesSymmetric(cells, 1, 1, Mode.START, n);
        placeObjectivesSymmetric(cells, 1, halfN - 2, Mode.FINISH, n);
        placeObjectivesSymmetric(cells, halfN - 2, halfN - 2, Mode.FORM_A, n);
    }

    private static void placeObjectivesSymmetric(CellButton[][] cells, int x, int y, Mode mode, int n) {
        // P1: Top-Left
        cells[x][y].setMode(mode, 1);

        // P2: Top-Right (Mirror X)
        cells[n - 1 - x][y].setMode(mode, 2);

        // P3: Bottom-Left (Mirror Y)
        cells[x][n - 1 - y].setMode(mode, 3);

        // P4: Bottom-Right (Mirror XY)
        cells[n - 1 - x][n - 1 - y].setMode(mode, 4);
    }

    private static void createRoom4Way(CellButton[][] cells, int x, int y, int w, int h) {
        int n = cells.length;
        // Top-Left
        createRoom(cells, x, y, w, h);
        // Top-Right
        createRoom(cells, n - 1 - x - (w - 1), y, w, h);
        // Bottom-Left
        createRoom(cells, x, n - 1 - y - (h - 1), w, h);
        // Bottom-Right
        createRoom(cells, n - 1 - x - (w - 1), n - 1 - y - (h - 1), w, h);
    }

    private static void createRoom(CellButton[][] cells, int x, int y, int w, int h) {
        int n = cells.length;
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                if (i > 0 && i < n - 1 && j > 0 && j < n - 1) {
                    cells[i][j].setMode(Mode.FLOOR, 0);
                }
            }
        }
    }

    private static void generateSymmetric4Way(CellButton[][] cells, boolean[][] visited, int startX, int startY,
            int n) {
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[] { startX, startY });

        markVisited4Way(cells, visited, startX, startY, n);

        int[][] dirs = { { 0, 2 }, { 2, 0 }, { 0, -2 }, { -2, 0 } };

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int x = current[0];
            int y = current[1];

            List<int[]> neighbors = new ArrayList<>();
            for (int[] d : dirs) {
                int nx = x + d[0];
                int ny = y + d[1];

                // Check bounds and visited
                // Restrict to Top-Left Quadrant mostly, but allow crossing slightly if needed?
                // Actually, strict quadrant generation is easier.
                // Let's say we only generate for x < n/2 and y < n/2
                // But we need to handle the center lines carefully.

                if (nx > 0 && ny > 0 && nx < n - 1 && ny < n - 1) {
                    // Check if this neighbor (or its mirrors) is visited
                    if (!visited[nx][ny]) {
                        // Also check if we are crossing into other quadrants in a way that overlaps
                        // mirrors?
                        // If we stay in top-left (x < n/2, y < n/2), we are safe.
                        // If we cross n/2, we might collide with our own mirror.

                        if (nx < n / 2 && ny < n / 2) {
                            neighbors.add(d);
                        }
                    }
                }
            }

            if (neighbors.isEmpty()) {
                stack.pop();
                continue;
            }

            int[] d = neighbors.get(RNG.nextInt(neighbors.size()));
            int nx = x + d[0];
            int ny = y + d[1];

            // Carve path
            int wallX = x + d[0] / 2;
            int wallY = y + d[1] / 2;

            markVisited4Way(cells, visited, nx, ny, n);
            markFloor4Way(cells, wallX, wallY, n);
            // markFloor4Way is implicitly done by markVisited4Way for the target cell,
            // but we need to carve the wall between.

            stack.push(new int[] { nx, ny });
        }
    }

    private static void markVisited4Way(CellButton[][] cells, boolean[][] visited, int x, int y, int n) {
        // Top-Left
        visited[x][y] = true;
        cells[x][y].setMode(Mode.FLOOR, 0);

        // Top-Right
        int x2 = n - 1 - x;
        visited[x2][y] = true;
        cells[x2][y].setMode(Mode.FLOOR, 0);

        // Bottom-Left
        int y2 = n - 1 - y;
        visited[x][y2] = true;
        cells[x][y2].setMode(Mode.FLOOR, 0);

        // Bottom-Right
        visited[x2][y2] = true;
        cells[x2][y2].setMode(Mode.FLOOR, 0);
    }

    private static void markFloor4Way(CellButton[][] cells, int x, int y, int n) {
        cells[x][y].setMode(Mode.FLOOR, 0);
        cells[n - 1 - x][y].setMode(Mode.FLOOR, 0);
        cells[x][n - 1 - y].setMode(Mode.FLOOR, 0);
        cells[n - 1 - x][n - 1 - y].setMode(Mode.FLOOR, 0);
    }
}
