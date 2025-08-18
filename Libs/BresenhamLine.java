package Libs;

import java.awt.*;

// Bresenham Line Drawing Algorithm Implementation
public class BresenhamLine {
    public void drawLine(Graphics2D g, Point p1, Point p2, Color color) {
        g.setColor(color);
        
        int x0 = p1.x, y0 = p1.y;
        int x1 = p2.x, y1 = p2.y;
        
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        
        int err = dx - dy;
        
        while (true) {
            g.fillRect(x0, y0, 1, 1); // Plot pixel
            
            if (x0 == x1 && y0 == y1) break;
            
            int e2 = 2 * err;
            
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
    
    public void drawLine(Graphics2D g, int x0, int y0, int x1, int y1, Color color) {
        drawLine(g, new Point(x0, y0), new Point(x1, y1), color);
    }
}

