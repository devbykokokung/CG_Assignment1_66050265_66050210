package Libs;

import java.awt.*;
// Bézier Curve Algorithm Implementation
public class BezierCurve {
    public void drawCubicBezier(Graphics2D g, Point p0, Point p1, Point p2, Point p3, Color color) {
        g.setColor(color);
        
        int numPoints = 50; // Resolution
        Point prevPoint = p0;
        
        for (int i = 1; i <= numPoints; i++) {
            double t = (double) i / numPoints;
            Point currentPoint = calculateBezierPoint(p0, p1, p2, p3, t);
            
            // Draw line segment using simple line drawing
            drawLineSegment(g, prevPoint, currentPoint);
            
            prevPoint = currentPoint;
        }
    }
    
    public void drawQuadraticBezier(Graphics2D g, Point p0, Point p1, Point p2, Color color) {
        g.setColor(color);
        
        int numPoints = 30;
        Point prevPoint = p0;
        
        for (int i = 1; i <= numPoints; i++) {
            double t = (double) i / numPoints;
            Point currentPoint = calculateQuadraticBezierPoint(p0, p1, p2, t);
            
            drawLineSegment(g, prevPoint, currentPoint);
            prevPoint = currentPoint;
        }
    }
    
    private Point calculateBezierPoint(Point p0, Point p1, Point p2, Point p3, double t) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;
        
        int x = (int)(uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x);
        int y = (int)(uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y);
        
        return new Point(x, y);
    }
    
    private Point calculateQuadraticBezierPoint(Point p0, Point p1, Point p2, double t) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        
        int x = (int)(uu * p0.x + 2 * u * t * p1.x + tt * p2.x);
        int y = (int)(uu * p0.y + 2 * u * t * p1.y + tt * p2.y);
        
        return new Point(x, y);
    }
    
    private void drawLineSegment(Graphics2D g, Point p1, Point p2) {
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
}