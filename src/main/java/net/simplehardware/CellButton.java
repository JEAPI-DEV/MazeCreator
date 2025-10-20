package net.simplehardware;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CellButton extends JPanel {
    final int x, y;
    private Mode mode = Mode.FLOOR;
    private int playerId = 0;
    private final MazeEditor editor;

    public CellButton(int x, int y, MazeEditor editor) {
        this.x = x;
        this.y = y;
        this.editor = editor;
        setPreferredSize(new Dimension(60, 60));
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Click and drag
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    applyCurrentMode();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                    Mode m = editor.getCurrentMode();
                    if (m == Mode.WALL || m == Mode.FLOOR) {
                        applyCurrentMode();
                    }
                }
            }
        });
    }

    private void applyCurrentMode() {
        Mode current = editor.getCurrentMode();
        int pid = (current == Mode.START || current == Mode.FINISH)
                ? editor.getCurrentPlayerId() : 0;
        setMode(current, pid);
    }

    public void setMode(Mode m, int pid) {
        this.mode = m;
        this.playerId = (m == Mode.START || m == Mode.FINISH) ? pid : 0;
        updateColor();
        repaint();
    }

    private void updateColor() {
        switch (mode) {
            case FLOOR -> setBackground(Color.LIGHT_GRAY);
            case WALL -> setBackground(new Color(120, 20, 20));
            case START -> setBackground(Color.CYAN);
            case FINISH -> setBackground(Color.PINK);
        }
    }

    public Mode getMode() { return mode; }
    public int getPlayerId() { return playerId; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mode == Mode.START || mode == Mode.FINISH) {
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
            String text = (mode == Mode.START ? "@" : "!") + playerId;
            FontMetrics fm = g.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(text)) / 2;
            int ty = (getHeight() + fm.getAscent()) / 2 - 4;
            g.drawString(text, tx, ty);
        }
    }
}
