package Libs;

import java.awt.*;
import java.awt.image.BufferedImage;

// Midpoint  
public class Midpoint {
    // Draw a single pixel
    // AND yeah i thought i cant use g.draw 😭😢😢
    private static void plotPixel(Graphics2D g, int x, int y) {
        g.drawLine(x, y, x, y);
    }

    // Circle using midpoint algorithm
    public void drawCircle(Graphics2D g, int centerX, int centerY, int radius, Color color) {
        g.setColor(color);

        int x = 0;
        int y = radius;
        int d = 1 - radius;

        plotCirclePoints(g, centerX, centerY, x, y);

        while (x < y) {
            if (d < 0) {
                d = d + 2 * x + 3;
            } else {
                d = d + 2 * (x - y) + 5;
                y--;
            }
            x++;
            plotCirclePoints(g, centerX, centerY, x, y);
        }
    }

    public static void plotRect(BufferedImage img, int x, int y, int width, int height, Color color) {
        // 😭 I though I cant use Setcolor T-T
        int rgb = color.getRGB();
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (i >= 0 && i < img.getWidth() && j >= 0 && j < img.getHeight()) {
                    img.setRGB(i, j, rgb);
                }
            }
        }
    }

    public void plotFilledCircle(Graphics2D g, int centerX, int centerY, int radius, Color color) {
        // I know later that i can use setcolor
        g.setColor(color);

        for (int r = 0; r <= radius; r++) {
            drawCircle(g, centerX, centerY, r, color);
        }
    }

    private void plotCirclePoints(Graphics2D g, int cx, int cy, int x, int y) {
        plotPixel(g, cx + x, cy + y);
        plotPixel(g, cx - x, cy + y);
        plotPixel(g, cx + x, cy - y);
        plotPixel(g, cx - x, cy - y);
        plotPixel(g, cx + y, cy + x);
        plotPixel(g, cx - y, cy + x);
        plotPixel(g, cx + y, cy - x);
        plotPixel(g, cx - y, cy - x);
    }

    // Ellipse public entry point
    public static void plotEllipse(Graphics2D g, int x, int y, int width, int height, Color c) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double dx = (i - width / 2.0);
                double dy = (j - height / 2.0);
                double rx = width / 2.0;
                double ry = height / 2.0;
                if ((dx*dx)/(rx*rx) + (dy*dy)/(ry*ry) <= 1) {
                    img.setRGB(i, j, c.getRGB());
                }
            }
        }
        g.drawImage(img, x, y, null);
    }
    

    // Midpoint ellipse algorithm
    public static void drawOval(Graphics2D g, int centerX, int centerY, int rx, int ry) {
        int x = 0;
        int y = ry;

        long rxSq = (long) rx * rx;
        long rySq = (long) ry * ry;
        long twoRxSq = 2 * rxSq;
        long twoRySq = 2 * rySq;

        long px = 0;
        long py = twoRxSq * y;

        // Region 1
        long p = Math.round(rySq - (rxSq * ry) + (0.25 * rxSq));
        while (px < py) {
            plotEllipsePoints(g, centerX, centerY, x, y);
            x++;
            px += twoRySq;
            if (p < 0) {
                p += rySq + px;
            } else {
                y--;
                py -= twoRxSq;
                p += rySq + px - py;
            }
        }

        // Region 2
        p = Math.round(rySq * (x + 0.5) * (x + 0.5) +
                       rxSq * (y - 1) * (y - 1) -
                       rxSq * rySq);
        while (y >= 0) {
            plotEllipsePoints(g, centerX, centerY, x, y);
            y--;
            py -= twoRxSq;
            if (p > 0) {
                p += rxSq - py;
            } else {
                x++;
                px += twoRySq;
                p += rxSq - py + px;
            }
        }
    }

    // Now static so it can be called from drawOval
    private static void plotEllipsePoints(Graphics2D g, int cx, int cy, int x, int y) {
        plotPixel(g, cx + x, cy + y);
        plotPixel(g, cx - x, cy + y);
        plotPixel(g, cx + x, cy - y);
        plotPixel(g, cx - x, cy - y);
    }
}

