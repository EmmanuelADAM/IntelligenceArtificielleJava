package src.chemin.gui;

import src.chemin.AlgoStar;
import src.chemin.environment.Cell;
import src.chemin.environment.Entrepot;
import src.chemin.environment.TypeCell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * panel on which the environment is displayed<br>
 * user can change the position of the goal by a mouse click
 *
 * @author emmanuel adam
 */
public class PanelEnvironment extends JPanel {
    /**
     * entrepot (environment) linked to this gui
     */
    private final Entrepot ent;

    /**
     * width of a cell in pixel
     */
    private int depX;
    /**
     * height of a cell in pixel
     */
    private int depY;

    /**
     * define the panel and the mouse behaviour
     */
    PanelEnvironment(int _width, int _height, Entrepot _ent) {
        setSize(_width, _height);
        this.setToolTipText("click to change the goal");
        ent = _ent;
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int i = e.getX() / depX;
                int j = e.getY() / depY;
//				System.err.println("x="+i+", y="+j);
                AlgoStar.logger.log(Level.INFO, "clic en x="+i+", y="+j);
                ent.setGoal(i, j);
                ent.removeVisit();
                ent.recompute();
            }
        });
    }

    /**
     * paint the environment
     */
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        // to have a good rendering

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(qualityHints);

        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        Cell[][] content = ent.getContent();

        //compute the size of a cell in pixels
        depX = (int) (((double) getWidth()) / ((double) ent.getWidth()));
        depY = (int) (((double) getHeight()) / ((double) ent.getHeight()));

        double coefShadowContainer = TypeCell.OBSTACLE.getValue() / 4;

        for (int i = 0; i < ent.getWidth(); i++)
            for (int j = 0; j < ent.getHeight(); j++) {
                Cell c = content[i][j];
                switch (c.getType()) {
                    case HERBE -> {
                        int diff = (int) (coefShadowContainer * (c.getValue() - TypeCell.HERBE.getValue()));
                        Color green = adjustLuminosity(Color.GREEN, -diff);
                        g2d.setColor(green);
                        g2d.fillRect(c.x * depX, c.y * depY, depX, depY);
                    }
                    case SABLE -> {
                        int diff = (int) (coefShadowContainer * (c.getValue() - TypeCell.SABLE.getValue()));
                        Color yellow = adjustLuminosity(Color.YELLOW, -diff);
                        g2d.setColor(yellow);
                        g2d.fillRect(c.x * depX, c.y * depY, depX, depY);
                    }
                    case EAU -> {
                        int diff = (int) (coefShadowContainer * (c.getValue() - TypeCell.EAU.getValue()));
                        Color blue = adjustLuminosity(Color.BLUE, -diff);
                        g2d.setColor(blue);
                        g2d.fillRect(i * depX, j * depY, depX, depY);
                    }
                }
                if (c.isContainer()) {
                    g2d.setColor(Color.BLACK);
                    g2d.fillOval(c.x * depX, c.y * depY, depX, depY);
                } else if (c.isStart()) {
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(c.x * depX, c.y * depY, depX, depY);
                } else if (c.isGoal()) {
                    g2d.setColor(Color.RED);
                    g2d.fillRect(c.x * depX, c.y * depY, depX, depY);
                }
                if (c.getF() > 0) {// if the cell has been evaluated
                    Color col = adjustLuminosity(g2d.getColor(), 150);//.brighter().brighter().brighter();
                    g2d.setColor(col);
                    g2d.fillRect(c.x * depX, c.y * depY, depX, depY);
                    if (c.getParent() != null) {
                        g2d.setColor(Color.BLACK);
//						g2d.drawLine((int) ((c.getParent().x + 0.5) * depX), (int) ((c.getParent().y + 0.5) * depY),
//								(int) ((c.x + 0.5) * depX), (int) ((c.y + 0.5) * depY));
                        g2d.drawLine((c.getParent().x * depX), (c.getParent().y * depY),
                                (c.x * depX), (c.y * depY));
                    }
                }
            }
        ArrayList<Cell> solution = ent.getSolution();
        if (solution != null) {
            g2d.setColor(Color.RED);
            int smaller = (Math.min(depX, depY));
            BasicStroke penSize = new BasicStroke(smaller - 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10f);

            g2d.setStroke(penSize);

            Cell c0 = ent.getStart();
            for (Cell c1 : solution) {
                g2d.drawLine(c0.getX() * depX + depX / 2, c0.getY() * depY + depY / 2, c1.getX() * depX + depX / 2, c1.getY() * depY + depY / 2);
                c0 = c1;
            }
        }
    }

    /**
     * compute a new color, lighter or lighter than the original
     *
     * @param col        original color
     * @param adjustment degree of darkness (if negative) or lightness (if positive)
     * @return a new color lighter of darker than col
     */
    private Color adjustLuminosity(Color col, int adjustment) {
        Color c;
        int r = (col.getRed() + adjustment);
        r = (Math.max(Math.min(r, 255), 0));
        int v = (col.getGreen() + adjustment);
        v = (Math.max(Math.min(v, 255), 0));
        int b = (col.getBlue() + adjustment);
        b = (Math.max(Math.min(b, 255), 0));
        c = new Color(r, v, b);
        return c;
    }

}
