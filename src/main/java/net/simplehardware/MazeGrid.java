package net.simplehardware;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Random;

public class MazeGrid {
    private int gridSize;
    private CellButton[][] cells;
    private JPanel gridPanel;
    private final MazeEditor editor;
    private final JScrollPane scrollPane;
    private double zoomScale = 1.0;
    private static final int BASE_CELL_SIZE = 60;

    public MazeGrid(int size, MazeEditor editor) {
        this.gridSize = size;
        this.editor = editor;
        buildGridPanel();
        scrollPane = new JScrollPane(gridPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.addMouseWheelListener(new ZoomHandler());

        // keyboard shortcut ctrl+0 to reset zoom
        scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control 0"), "resetZoom");
        scrollPane.getActionMap().put("resetZoom", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                zoomScale = 1.0;
                applyZoom();
            }
        });
    }

    private void buildGridPanel() {
        gridPanel = new JPanel(new GridLayout(gridSize, gridSize));
        cells = new CellButton[gridSize][gridSize];
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                CellButton cell = new CellButton(x, y, editor);
                cell.setPreferredSize(new Dimension((int)(BASE_CELL_SIZE * zoomScale), (int)(BASE_CELL_SIZE * zoomScale)));
                cells[x][y] = cell;
                gridPanel.add(cell);
            }
        }
    }

    /**
     * Resize grid. If newSize > oldSize then copy old cells; if newSize < oldSize
     * then create fresh grid (no copy).
     */
    public void resizeGrid(int newSize) {
        if (newSize == gridSize) return;

        Mode[][] oldModes = null;
        int[][] oldPlayerIds = null;

        // Only copy if increasing size
        if (newSize > gridSize) {
            oldModes = new Mode[gridSize][gridSize];
            oldPlayerIds = new int[gridSize][gridSize];
            for (int x = 0; x < gridSize; x++) {
                for (int y = 0; y < gridSize; y++) {
                    oldModes[x][y] = cells[x][y].getMode();
                    oldPlayerIds[x][y] = cells[x][y].getPlayerId();
                }
            }
        }

        gridSize = newSize;
        buildGridPanel();

        if (oldModes != null) {
            int copyLimitX = Math.min(oldModes.length, gridSize);
            int copyLimitY = Math.min(oldModes[0].length, gridSize);
            for (int x = 0; x < copyLimitX; x++) {
                for (int y = 0; y < copyLimitY; y++) {
                    cells[x][y].setMode(oldModes[x][y], oldPlayerIds[x][y]);
                }
            }
        }

        scrollPane.setViewportView(gridPanel);
        applyZoom();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public CellButton[][] getCells() {
        return cells;
    }

    public int getGridSize() {
        return gridSize;
    }

    private void applyZoom() {
        int newSize = Math.max(4, (int) (BASE_CELL_SIZE * zoomScale));
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                cells[x][y].setPreferredSize(new Dimension(newSize, newSize));
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    public void generateRandomMaze(double wallProbability) {
        if (wallProbability < 0.0) wallProbability = 0.35;
        if (wallProbability > 1.0) wallProbability = 0.9;

        Random rnd = new Random();
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                if (x == 0 || y == 0 || x == gridSize - 1 || y == gridSize - 1) {
                    cells[x][y].setMode(Mode.WALL, 0);
                } else {
                    if (rnd.nextDouble() < wallProbability) {
                        cells[x][y].setMode(Mode.WALL, 0);
                    } else {
                        cells[x][y].setMode(Mode.FLOOR, 0);
                    }
                }
            }
        }
        placeRandomStartAndFinish(rnd);
    }

    private void placeRandomStartAndFinish(Random rnd) {
        int sx = -1, sy = -1, fx = -1, fy = -1;
        // find start
        for (int attempts = 0; attempts < 1000 && (sx == -1); attempts++) {
            int x = 1 + rnd.nextInt(Math.max(1, gridSize - 2));
            int y = 1 + rnd.nextInt(Math.max(1, gridSize - 2));
            if (cells[x][y].getMode() == Mode.FLOOR) {
                sx = x; sy = y;
            }
        }
        // find finish different from start
        for (int attempts = 0; attempts < 1000 && (fx == -1); attempts++) {
            int x = 1 + rnd.nextInt(Math.max(1, gridSize - 2));
            int y = 1 + rnd.nextInt(Math.max(1, gridSize - 2));
            if (cells[x][y].getMode() == Mode.FLOOR && (x != sx || y != sy)) {
                fx = x; fy = y;
            }
        }

        if (sx != -1) cells[sx][sy].setMode(Mode.START, 1);
        if (fx != -1) cells[fx][fy].setMode(Mode.FINISH, 1);
    }

    private class ZoomHandler implements MouseWheelListener {
        private static final double MIN_ZOOM = 0.25;
        private static final double MAX_ZOOM = 3.0;
        private static final double STEP = 0.1;

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (!e.isControlDown()) return;
            if (gridSize <= 20) return;

            double oldScale = zoomScale;
            int rotation = e.getWheelRotation();
            if (rotation < 0) zoomScale = Math.min(zoomScale + STEP, MAX_ZOOM);
            else zoomScale = Math.max(zoomScale - STEP, MIN_ZOOM);

            // Snap back to 1.0 when close
            if (Math.abs(zoomScale - 1.0) < 0.05) zoomScale = 1.0;
            if (zoomScale == oldScale) return;

            Point viewPos = scrollPane.getViewport().getViewPosition();
            Point mouse = e.getPoint();
            double scaleFactor = zoomScale / oldScale;

            applyZoom();

            int newX = (int) ((viewPos.x + mouse.x) * scaleFactor - mouse.x);
            int newY = (int) ((viewPos.y + mouse.y) * scaleFactor - mouse.y);
            scrollPane.getViewport().setViewPosition(new Point(Math.max(0, newX), Math.max(0, newY)));
        }
    }
}
