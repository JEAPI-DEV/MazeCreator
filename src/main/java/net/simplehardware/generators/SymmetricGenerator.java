package net.simplehardware.generators;

import net.simplehardware.MazeGrid;
import net.simplehardware.models.CellButton;
import net.simplehardware.models.Mode;

import net.simplehardware.utils.Pathfinder;

import java.util.*;

public class SymmetricGenerator {

    private static final Random RNG = new Random();

    public static void generate(MazeGrid grid, int numForms, int preferredMoves) {
        int n = grid.getGridSize();
        CellButton[][] cells = grid.getCells();

        // 1. Initialize with Walls
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                cells[x][y].setMode(Mode.WALL, 0);
            }
        }

        // 2. Create Rooms (4-Way Symmetric)
        // We only generate in the top-left quadrant and mirror to others
        int halfN = n / 2;
        int numRooms = (halfN * halfN) / 50; // Adjust density
        if (numRooms < 1)
            numRooms = 1;

        for (int i = 0; i < numRooms; i++) {
            int w = RNG.nextInt(3) + 3; // Width 3-5
            int h = RNG.nextInt(3) + 3; // Height 3-5

            // Ensure bounds are positive and fit in top-left quadrant
            // x range: 1 to halfN - w - 1
            // y range: 1 to halfN - h - 1

            int xBound = halfN - w - 1;
            int yBound = halfN - h - 1;

            if (xBound > 0 && yBound > 0) {
                int x = RNG.nextInt(xBound) + 1;
                int y = RNG.nextInt(yBound) + 1;
                createRoom4Way(cells, x, y, w, h);
            }
        }

        // 3. Maze Generation (4-Way Symmetric DFS)
        boolean[][] visited = new boolean[n][n];

        // Start DFS from (1, 1) which is in top-left
        generateSymmetric4Way(cells, visited, 1, 1, n);

        // 4. Ensure Connectivity (Center Connection)
        // Connect the quadrants in the center
        int centerX = n / 2;
        int centerY = n / 2;

        // Horizontal center connection
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            if (x > 0 && x < n - 1) {
                cells[x][centerY].setMode(Mode.FLOOR, 0);
                if (n % 2 == 0)
                    cells[x][centerY - 1].setMode(Mode.FLOOR, 0); // Wider corridor for even grids
            }
        }
        // Vertical center connection
        for (int y = centerY - 2; y <= centerY + 2; y++) {
            if (y > 0 && y < n - 1) {
                cells[centerX][y].setMode(Mode.FLOOR, 0);
                if (n % 2 == 0)
                    cells[centerX - 1][y].setMode(Mode.FLOOR, 0);
            }
        }

        // 5. Place Objectives (4 Players) - Optimized Placement
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

        if (floorCells.size() >= 10) { // Need enough cells for banding
            // Sort by Y then X to create vertical bands
            floorCells.sort((a, b) -> {
                if (a[1] != b[1])
                    return Integer.compare(a[1], b[1]);
                return Integer.compare(a[0], b[0]);
            });

            int size = floorCells.size();
            int bandSize = size / 3;

            // Define pools
            List<int[]> startPool = floorCells.subList(0, bandSize); // Top
            List<int[]> formPool = floorCells.subList(bandSize, size - bandSize); // Middle
            List<int[]> finishPool = floorCells.subList(size - bandSize, size); // Bottom

            // Optimization Loop
            List<int[]> bestPlacement = null; // List of {x, y, modeOrdinal} (absolute coords)
            double bestScore = -Double.MAX_VALUE;

            // Reduce iterations because we have inner loops for quadrants
            int iterations = 100;

            int numPoints = 2 + numForms;

            for (int i = 0; i < iterations; i++) {
                // Pick candidates from respective pools (Q1 coordinates)
                List<int[]> q1Points = new ArrayList<>();

                if (numPoints == 3) {
                    q1Points.add(startPool.get(RNG.nextInt(startPool.size())));
                    q1Points.add(formPool.get(RNG.nextInt(formPool.size())));
                    q1Points.add(finishPool.get(RNG.nextInt(finishPool.size())));
                } else {
                    Set<Integer> indices = new HashSet<>();
                    while (indices.size() < numPoints) {
                        indices.add(RNG.nextInt(floorCells.size()));
                    }
                    for (int idx : indices) {
                        q1Points.add(floorCells.get(idx));
                    }
                }

                // Try all permutations of roles
                List<List<int[]>> allPermutations = new ArrayList<>();
                permute(q1Points, 0, allPermutations);

                for (List<int[]> roles : allPermutations) {
                    // roles[0] = Start, roles[1..N] = Forms, roles[Last] = Finish

                    // Try Quadrant Assignments
                    // Start is always Q1 (0)
                    // Forms and Finish can be Q1, Q2, Q3, Q4 (0-3)

                    // To keep complexity down, we'll just try a few random quadrant assignments per
                    // permutation
                    // instead of all 4^(N-1). Or maybe just 16 random assignments.

                    for (int qAttempt = 0; qAttempt < 20; qAttempt++) {
                        List<int[]> currentConfig = new ArrayList<>();

                        // Assign Start (Always Q1)
                        int[] startQ1 = roles.get(0);
                        int[] startAbs = getPointInQuadrant(startQ1[0], startQ1[1], 0, n); // Q1
                        currentConfig.add(new int[] { startAbs[0], startAbs[1], Mode.START.ordinal() });

                        // Assign Forms
                        for (int j = 0; j < numForms; j++) {
                            int[] formQ1 = roles.get(1 + j);
                            // Random quadrant 0-3
                            int q = RNG.nextInt(4);
                            int[] formAbs = getPointInQuadrant(formQ1[0], formQ1[1], q, n);
                            char formChar = (char) ('A' + j);
                            Mode m = Mode.valueOf("FORM_" + formChar);
                            currentConfig.add(new int[] { formAbs[0], formAbs[1], m.ordinal() });
                        }

                        // Assign Finish
                        int[] finishQ1 = roles.get(roles.size() - 1);
                        // Random quadrant 0-3
                        int q = RNG.nextInt(4);
                        int[] finishAbs = getPointInQuadrant(finishQ1[0], finishQ1[1], q, n);
                        currentConfig.add(new int[] { finishAbs[0], finishAbs[1], Mode.FINISH.ordinal() });

                        // Calculate Path
                        int pathLength = 0;
                        boolean validPath = true;
                        double minSegmentDist = Double.MAX_VALUE;

                        for (int j = 0; j < currentConfig.size() - 1; j++) {
                            int[] p1 = currentConfig.get(j);
                            int[] p2 = currentConfig.get(j + 1);

                            int dist = Pathfinder.findShortestPath(cells,
                                    new Pathfinder.Point(p1[0], p1[1]),
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

                        if (!validPath)
                            continue;

                        // Score
                        double score;
                        if (preferredMoves > 0) {
                            int diff = Math.abs(pathLength - preferredMoves);
                            double clusteringPenalty = (minSegmentDist < 3.0) ? 1000.0 : 0.0;
                            score = 10000 - (diff * 10) - clusteringPenalty + (minSegmentDist * 0.5);
                        } else {
                            score = pathLength + (minSegmentDist * 10);
                        }

                        if (score > bestScore) {
                            bestScore = score;
                            bestPlacement = new ArrayList<>(currentConfig);
                        }
                    }
                }
            }

            if (bestPlacement != null) {
                for (int[] item : bestPlacement) {
                    Mode m = Mode.values()[item[2]];
                    placeObjectivesSymmetric(cells, item[0], item[1], m, n);
                }
            } else {
                fallbackPlacement(cells, n, halfN);
            }

        } else {
            fallbackPlacement(cells, n, halfN);
        }
    }

    private static int[] getPointInQuadrant(int x, int y, int quadrant, int n) {
        // x, y are in Q1 (Top-Left) relative to 0,0
        // Q1: x, y
        // Q2: n-1-x, y (Top-Right)
        // Q3: x, n-1-y (Bottom-Left)
        // Q4: n-1-x, n-1-y (Bottom-Right)

        switch (quadrant) {
            case 0:
                return new int[] { x, y };
            case 1:
                return new int[] { n - 1 - x, y };
            case 2:
                return new int[] { x, n - 1 - y };
            case 3:
                return new int[] { n - 1 - x, n - 1 - y };
            default:
                return new int[] { x, y };
        }
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
