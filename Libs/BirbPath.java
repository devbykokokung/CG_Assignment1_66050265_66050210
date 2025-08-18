// i thought i cant use like .curve .line.. etc so i made this file scene week 3 of study
package Libs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BirbPath {
    // Custom point class to store path points
    private static class PathPoint {
        double x, y;

        PathPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // Polygon class to store complete shapes for filling
    private static class Polygon {
        List<PathPoint> points = new ArrayList<>();

        void addPoint(double x, double y) {
            points.add(new PathPoint(x, y));
        }

        boolean isEmpty() {
            return points.isEmpty();
        }

        void clear() {
            points.clear();
        }

        Polygon copy() {
            Polygon copy = new Polygon();
            for (PathPoint p : this.points) {
                copy.addPoint(p.x, p.y);
            }
            return copy;
        }
    }

    private static abstract class Segment {
        abstract void draw(Graphics2D g2);

        abstract void drawCustom(BufferedImage img, int color);

        abstract void addToPolygon(Polygon polygon, double currentX, double currentY);

        abstract void addEdges(List<Edge> edges);
    }

    // Edge class for scanline fill algorithm
    private static class Edge {
        int yMax;
        double x;
        double deltaX;

        Edge(int yMin, int yMax, double xMin, double xMax) {
            this.yMax = yMax;
            this.x = xMin;
            this.deltaX = (yMax == yMin) ? 0 : (xMax - xMin) / (yMax - yMin);
        }
    }

    private static class LineSegment extends Segment {
        final int x0, y0, x1, y1;

        LineSegment(double x0, double y0, double x1, double y1) {
            this.x0 = (int) Math.round(x0);
            this.y0 = (int) Math.round(y0);
            this.x1 = (int) Math.round(x1);
            this.y1 = (int) Math.round(y1);
        }

        // Bresenham's line algorithm for drawing a line pixel by pixel
        @Override
        void draw(Graphics2D g2) {
            int dx = Math.abs(x1 - x0);
            int dy = Math.abs(y1 - y0);

            int sx = x0 < x1 ? 1 : -1;
            int sy = y0 < y1 ? 1 : -1;

            int err = dx - dy;
            int x = x0;
            int y = y0;

            while (true) {
                g2.drawLine(x, y, x, y); // draw pixel

                if (x == x1 && y == y1)
                    break;
                int e2 = 2 * err;
                if (e2 > -dy) {
                    err -= dy;
                    x += sx;
                }
                if (e2 < dx) {
                    err += dx;
                    y += sy;
                }
            }
        }

        @Override
        void drawCustom(BufferedImage img, int color) {
            drawLinePixels(img, x0, y0, x1, y1, color);
        }

        private void drawLinePixels(BufferedImage img, int x0, int y0, int x1, int y1, int color) {
            int dx = Math.abs(x1 - x0);
            int dy = Math.abs(y1 - y0);

            int sx = x0 < x1 ? 1 : -1;
            int sy = y0 < y1 ? 1 : -1;

            int err = dx - dy;
            int x = x0;
            int y = y0;

            while (true) {
                if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
                    img.setRGB(x, y, color);
                }

                if (x == x1 && y == y1)
                    break;

                int e2 = 2 * err;
                if (e2 > -dy) {
                    err -= dy;
                    x += sx;
                }
                if (e2 < dx) {
                    err += dx;
                    y += sy;
                }
            }
        }

        @Override
        void addToPolygon(Polygon polygon, double currentX, double currentY) {
            polygon.addPoint(x1, y1);
        }

        @Override
        void addEdges(List<Edge> edges) {
            if (y0 != y1) {
                int yMin = Math.min(y0, y1);
                int yMax = Math.max(y0, y1);
                double xMin = (y0 < y1) ? x0 : x1;
                double xMax = (y0 < y1) ? x1 : x0;
                edges.add(new Edge(yMin, yMax, xMin, xMax));
            }
        }
    }

    private static class CubicBezierSegment extends Segment {
        final double x0, y0, c1x, c1y, c2x, c2y, x1, y1;
        private static final double FLATNESS = 0.5;

        CubicBezierSegment(double x0, double y0, double c1x, double c1y,
                double c2x, double c2y, double x1, double y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.c1x = c1x;
            this.c1y = c1y;
            this.c2x = c2x;
            this.c2y = c2y;
            this.x1 = x1;
            this.y1 = y1;
        }

        // Recursively subdivide curve until flat enough, then draw lines
        @Override
        void draw(Graphics2D g2) {
            drawBezier(g2, x0, y0, c1x, c1y, c2x, c2y, x1, y1);
        }

        @Override
        void drawCustom(BufferedImage img, int color) {
            drawBezierCustom(img, color, x0, y0, c1x, c1y, c2x, c2y, x1, y1);
        }

        private void drawBezier(Graphics2D g2, double x0, double y0, double c1x, double c1y,
                double c2x, double c2y, double x1, double y1) {
            if (isFlatEnough(x0, y0, c1x, c1y, c2x, c2y, x1, y1)) {
                new LineSegment(x0, y0, x1, y1).draw(g2);
            } else {
                // Subdivide curve using De Casteljau's algorithm
                double mid1x = (x0 + c1x) / 2;
                double mid1y = (y0 + c1y) / 2;
                double mid2x = (c1x + c2x) / 2;
                double mid2y = (c1y + c2y) / 2;
                double mid3x = (c2x + x1) / 2;
                double mid3y = (c2y + y1) / 2;

                double mid4x = (mid1x + mid2x) / 2;
                double mid4y = (mid1y + mid2y) / 2;
                double mid5x = (mid2x + mid3x) / 2;
                double mid5y = (mid2y + mid3y) / 2;

                double mid6x = (mid4x + mid5x) / 2;
                double mid6y = (mid4y + mid5y) / 2;

                // LEFT half
                drawBezier(g2, x0, y0, mid1x, mid1y, mid4x, mid4y, mid6x, mid6y);
                // RIGHT half
                drawBezier(g2, mid6x, mid6y, mid5x, mid5y, mid3x, mid3y, x1, y1);
            }
        }

        // Didnt use this one now T-T
        private void drawBezierCustom(BufferedImage img, int color, double x0, double y0, double c1x, double c1y,
                double c2x, double c2y, double x1, double y1) {
            if (isFlatEnough(x0, y0, c1x, c1y, c2x, c2y, x1, y1)) {
                // Approximate with a straight line
                new LineSegment(x0, y0, x1, y1).drawCustom(img, color);
            } else {
                // Subdivide curve using De Casteljau's algorithm
                double mid1x = (x0 + c1x) / 2;
                double mid1y = (y0 + c1y) / 2;
                double mid2x = (c1x + c2x) / 2;
                double mid2y = (c1y + c2y) / 2;
                double mid3x = (c2x + x1) / 2;
                double mid3y = (c2y + y1) / 2;

                double mid4x = (mid1x + mid2x) / 2;
                double mid4y = (mid1y + mid2y) / 2;
                double mid5x = (mid2x + mid3x) / 2;
                double mid5y = (mid2y + mid3y) / 2;

                double mid6x = (mid4x + mid5x) / 2;
                double mid6y = (mid4y + mid5y) / 2;

                // Left half
                drawBezierCustom(img, color, x0, y0, mid1x, mid1y, mid4x, mid4y, mid6x, mid6y);
                // Right half
                drawBezierCustom(img, color, mid6x, mid6y, mid5x, mid5y, mid3x, mid3y, x1, y1);
            }
        }

        // Check if curve is flat enough using control point distances
        private boolean isFlatEnough(double x0, double y0, double c1x, double c1y,
                double c2x, double c2y, double x1, double y1) {
            double ux = 3 * c1x - 2 * x0 - x1;
            double uy = 3 * c1y - 2 * y0 - y1;
            double vx = 3 * c2x - 2 * x1 - x0;
            double vy = 3 * c2y - 2 * y1 - y0;
            return (ux * ux + uy * uy + vx * vx + vy * vy) <= FLATNESS * FLATNESS;
        }

        @Override
        void addToPolygon(Polygon polygon, double currentX, double currentY) {
            // Flatten the curve and add points
            flattenAndAddToPolygon(polygon, x0, y0, c1x, c1y, c2x, c2y, x1, y1);
        }

        private void flattenAndAddToPolygon(Polygon polygon, double x0, double y0, double c1x, double c1y,
                double c2x, double c2y, double x1, double y1) {
            if (isFlatEnough(x0, y0, c1x, c1y, c2x, c2y, x1, y1)) {
                polygon.addPoint(x1, y1);
            } else {
                // Subdivide and recurse
                double mid1x = (x0 + c1x) / 2;
                double mid1y = (y0 + c1y) / 2;
                double mid2x = (c1x + c2x) / 2;
                double mid2y = (c1y + c2y) / 2;
                double mid3x = (c2x + x1) / 2;
                double mid3y = (c2y + y1) / 2;

                double mid4x = (mid1x + mid2x) / 2;
                double mid4y = (mid1y + mid2y) / 2;
                double mid5x = (mid2x + mid3x) / 2;
                double mid5y = (mid2y + mid3y) / 2;

                double mid6x = (mid4x + mid5x) / 2;
                double mid6y = (mid4y + mid5y) / 2;

                flattenAndAddToPolygon(polygon, x0, y0, mid1x, mid1y, mid4x, mid4y, mid6x, mid6y);
                flattenAndAddToPolygon(polygon, mid6x, mid6y, mid5x, mid5y, mid3x, mid3y, x1, y1);
            }
        }

        @Override
        void addEdges(List<Edge> edges) {
            // Flatten the curve and add line segments
            flattenAndAddEdges(edges, x0, y0, c1x, c1y, c2x, c2y, x1, y1);
        }

        private void flattenAndAddEdges(List<Edge> edges, double x0, double y0, double c1x, double c1y,
                double c2x, double c2y, double x1, double y1) {
            if (isFlatEnough(x0, y0, c1x, c1y, c2x, c2y, x1, y1)) {
                new LineSegment(x0, y0, x1, y1).addEdges(edges);
            } else {
                // Subdivide and recurse
                double mid1x = (x0 + c1x) / 2;
                double mid1y = (y0 + c1y) / 2;
                double mid2x = (c1x + c2x) / 2;
                double mid2y = (c1y + c2y) / 2;
                double mid3x = (c2x + x1) / 2;
                double mid3y = (c2y + y1) / 2;

                double mid4x = (mid1x + mid2x) / 2;
                double mid4y = (mid1y + mid2y) / 2;
                double mid5x = (mid2x + mid3x) / 2;
                double mid5y = (mid2y + mid3y) / 2;

                double mid6x = (mid4x + mid5x) / 2;
                double mid6y = (mid4y + mid5y) / 2;

                flattenAndAddEdges(edges, x0, y0, mid1x, mid1y, mid4x, mid4y, mid6x, mid6y);
                flattenAndAddEdges(edges, mid6x, mid6y, mid5x, mid5y, mid3x, mid3y, x1, y1);
            }
        }
    }

    private static class QuadBezierSegment extends Segment {
        final double x0, y0, cx, cy, x1, y1;
        private static final double FLATNESS = 0.5;

        QuadBezierSegment(double x0, double y0, double cx, double cy, double x1, double y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.cx = cx;
            this.cy = cy;
            this.x1 = x1;
            this.y1 = y1;
        }

        @Override
        void draw(Graphics2D g2) {
            drawQuadBezier(g2, x0, y0, cx, cy, x1, y1);
        }

        @Override
        void drawCustom(BufferedImage img, int color) {
            drawQuadBezierCustom(img, color, x0, y0, cx, cy, x1, y1);
        }

        private void drawQuadBezier(Graphics2D g2, double x0, double y0,
                double cx, double cy, double x1, double y1) {
            if (isQuadFlatEnough(x0, y0, cx, cy, x1, y1)) {
                new LineSegment(x0, y0, x1, y1).draw(g2);
            } else {
                // Subdivide quadratic curve
                double mx0 = (x0 + cx) / 2;
                double my0 = (y0 + cy) / 2;
                double mx1 = (cx + x1) / 2;
                double my1 = (cy + y1) / 2;
                double mx = (mx0 + mx1) / 2;
                double my = (my0 + my1) / 2;

                drawQuadBezier(g2, x0, y0, mx0, my0, mx, my);
                drawQuadBezier(g2, mx, my, mx1, my1, x1, y1);
            }
        }

        private void drawQuadBezierCustom(BufferedImage img, int color, double x0, double y0,
                double cx, double cy, double x1, double y1) {
            if (isQuadFlatEnough(x0, y0, cx, cy, x1, y1)) {
                new LineSegment(x0, y0, x1, y1).drawCustom(img, color);
            } else {
                // Subdivide quadratic curve
                double mx0 = (x0 + cx) / 2;
                double my0 = (y0 + cy) / 2;
                double mx1 = (cx + x1) / 2;
                double my1 = (cy + y1) / 2;
                double mx = (mx0 + mx1) / 2;
                double my = (my0 + my1) / 2;

                drawQuadBezierCustom(img, color, x0, y0, mx0, my0, mx, my);
                drawQuadBezierCustom(img, color, mx, my, mx1, my1, x1, y1);
            }
        }

        private boolean isQuadFlatEnough(double x0, double y0, double cx, double cy,
                double x1, double y1) {
            double ux = 2 * cx - x0 - x1;
            double uy = 2 * cy - y0 - y1;
            return (ux * ux + uy * uy) <= FLATNESS * FLATNESS;
        }

        @Override
        void addToPolygon(Polygon polygon, double currentX, double currentY) {
            flattenQuadAndAddToPolygon(polygon, x0, y0, cx, cy, x1, y1);
        }

        private void flattenQuadAndAddToPolygon(Polygon polygon, double x0, double y0,
                double cx, double cy, double x1, double y1) {
            if (isQuadFlatEnough(x0, y0, cx, cy, x1, y1)) {
                polygon.addPoint(x1, y1);
            } else {
                double mx0 = (x0 + cx) / 2;
                double my0 = (y0 + cy) / 2;
                double mx1 = (cx + x1) / 2;
                double my1 = (cy + y1) / 2;
                double mx = (mx0 + mx1) / 2;
                double my = (my0 + my1) / 2;

                flattenQuadAndAddToPolygon(polygon, x0, y0, mx0, my0, mx, my);
                flattenQuadAndAddToPolygon(polygon, mx, my, mx1, my1, x1, y1);
            }
        }

        @Override
        void addEdges(List<Edge> edges) {
            flattenQuadAndAddEdges(edges, x0, y0, cx, cy, x1, y1);
        }

        private void flattenQuadAndAddEdges(List<Edge> edges, double x0, double y0,
                double cx, double cy, double x1, double y1) {
            if (isQuadFlatEnough(x0, y0, cx, cy, x1, y1)) {
                new LineSegment(x0, y0, x1, y1).addEdges(edges);
            } else {
                double mx0 = (x0 + cx) / 2;
                double my0 = (y0 + cy) / 2;
                double mx1 = (cx + x1) / 2;
                double my1 = (cy + y1) / 2;
                double mx = (mx0 + mx1) / 2;
                double my = (my0 + my1) / 2;

                flattenQuadAndAddEdges(edges, x0, y0, mx0, my0, mx, my);
                flattenQuadAndAddEdges(edges, mx, my, mx1, my1, x1, y1);
            }
        }
    }

    private static class CloseSegment extends Segment {
        final double x0, y0, x1, y1;

        CloseSegment(double x0, double y0, double x1, double y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }

        @Override
        void draw(Graphics2D g2) {
            if (x0 != x1 || y0 != y1) {
                new LineSegment(x0, y0, x1, y1).draw(g2);
            }
        }

        @Override
        void drawCustom(BufferedImage img, int color) {
            if (x0 != x1 || y0 != y1) {
                new LineSegment(x0, y0, x1, y1).drawCustom(img, color);
            }
        }

        @Override
        void addToPolygon(Polygon polygon, double currentX, double currentY) {
            // Close path - doesn't add points, just closes the shape
        }

        @Override
        void addEdges(List<Edge> edges) {
            if (x0 != x1 || y0 != y1) {
                new LineSegment(x0, y0, x1, y1).addEdges(edges);
            }
        }
    }

    // Instance vars
    private final List<Segment> segments = new ArrayList<>();
    private final List<Polygon> polygons = new ArrayList<>();
    private Polygon currentPolygon = new Polygon();
    private double currentX = 0;
    private double currentY = 0;
    private double subpathStartX = 0;
    private double subpathStartY = 0;
    private boolean hasCurrentSubpath = false;

    private Color fillColor = Color.BLACK;
    private Color strokeColor = Color.BLACK;
    private float strokeWidth = 1.0f;
    private int windingRule = 0; // Custom winding rule (0 = non-zero, 1 = even-odd)
    private boolean antialiasing = true;

    // Path building methods
    public BirbPath moveTo(double x, double y) {
        currentX = x;
        currentY = y;
        subpathStartX = x;
        subpathStartY = y;
        hasCurrentSubpath = true;
        // Start new polygon
        if (!currentPolygon.isEmpty()) {
            polygons.add(currentPolygon);
            currentPolygon = new Polygon();
        }
        currentPolygon.addPoint(x, y);
        return this;
    }

    public BirbPath lineTo(double x, double y) {
        if (!hasCurrentSubpath) {
            moveTo(currentX, currentY);
        }
        segments.add(new LineSegment(currentX, currentY, x, y));
        currentPolygon.addPoint(x, y);
        currentX = x;
        currentY = y;
        return this;
    }

    public BirbPath quadTo(double cx, double cy, double x, double y) {
        if (!hasCurrentSubpath) {
            moveTo(currentX, currentY);
        }
        segments.add(new QuadBezierSegment(currentX, currentY, cx, cy, x, y));
        // Add flattened curve points to current polygon
        QuadBezierSegment segment = new QuadBezierSegment(currentX, currentY, cx, cy, x, y);
        segment.addToPolygon(currentPolygon, currentX, currentY);
        currentX = x;
        currentY = y;
        return this;
    }

    public BirbPath curveTo(double c1x, double c1y, double c2x, double c2y, double x, double y) {
        if (!hasCurrentSubpath) {
            moveTo(currentX, currentY);
        }
        segments.add(new CubicBezierSegment(currentX, currentY, c1x, c1y, c2x, c2y, x, y));
        // Add flattened curve points to current polygon
        CubicBezierSegment segment = new CubicBezierSegment(currentX, currentY, c1x, c1y, c2x, c2y, x, y);
        segment.addToPolygon(currentPolygon, currentX, currentY);
        currentX = x;
        currentY = y;
        return this;
    }

    public BirbPath closePath() {
        if (hasCurrentSubpath && (currentX != subpathStartX || currentY != subpathStartY)) {
            segments.add(new CloseSegment(currentX, currentY, subpathStartX, subpathStartY));
            currentPolygon.addPoint(subpathStartX, subpathStartY);
        }
        // Complete current polygon and add to list
        if (!currentPolygon.isEmpty()) {
            polygons.add(currentPolygon);
            currentPolygon = new Polygon();
        }
        currentX = subpathStartX;
        currentY = subpathStartY;
        hasCurrentSubpath = false;
        return this;
    }

    // i use to use like rect and oval to draw as a picture LIKE Doraemon with bunch
    // of circle and rect
    // Convenience methods for common shapes
    public BirbPath rect(double x, double y, double width, double height) {
        return moveTo(x, y)
                .lineTo(x + width, y)
                .lineTo(x + width, y + height)
                .lineTo(x, y + height)
                .closePath();
    }

    // use to use like rect and oval to draw as a picture LIKE Doraemon with bunch
    // of circle and rect
    public BirbPath circle(double cx, double cy, double radius) {
        double k = 0.552; // 4/3 * (sqrt(2) - 1)
        double kr = k * radius;

        return moveTo(cx, cy - radius)
                .curveTo(cx + kr, cy - radius, cx + radius, cy - kr, cx + radius, cy)
                .curveTo(cx + radius, cy + kr, cx + kr, cy + radius, cx, cy + radius)
                .curveTo(cx - kr, cy + radius, cx - radius, cy + kr, cx - radius, cy)
                .curveTo(cx - radius, cy - kr, cx - kr, cy - radius, cx, cy - radius)
                .closePath();
    }

    // use to use like rect and oval to draw as a picture LIKE Doraemon with bunch
    // of circle and rect
    public BirbPath ellipse(double cx, double cy, double rx, double ry) {
        double kx = 0.552 * rx; // 4/3 * (sqrt(2) - 1)
        double ky = 0.552 * ry; // 4/3 * (sqrt(2) - 1)

        return moveTo(cx, cy - ry)
                .curveTo(cx + kx, cy - ry, cx + rx, cy - ky, cx + rx, cy)
                .curveTo(cx + rx, cy + ky, cx + kx, cy + ry, cx, cy + ry)
                .curveTo(cx - kx, cy + ry, cx - rx, cy + ky, cx - rx, cy)
                .curveTo(cx - rx, cy - ky, cx - kx, cy - ry, cx, cy - ry)
                .closePath();
    }

    // Style setters (fluent interface)
    public BirbPath setFillColor(Color c) {
        this.fillColor = c;
        return this;
    }

    public BirbPath setStrokeColor(Color c) {
        this.strokeColor = c;
        return this;
    }

    public BirbPath setStrokeWidth(float w) {
        this.strokeWidth = w;
        return this;
    }

    public BirbPath setWindingRule(int rule) {
        this.windingRule = rule;
        return this;
    }

    public BirbPath setAntialiasing(boolean enabled) {
        this.antialiasing = enabled;
        return this;
    }

    // Create polygon list for proper filling
    private List<Polygon> getAllPolygons() {
        List<Polygon> allPolygons = new ArrayList<>(polygons);
        if (!currentPolygon.isEmpty()) {
            allPolygons.add(currentPolygon);
        }
        return allPolygons;
    }

    // Custom fill implementation using scanline algorithm
    public void customFill(BufferedImage img) {
        if (segments.isEmpty())
            return;

        int fillRGB = fillColor.getRGB();

        // Get all edges from the path
        List<Edge> edges = new ArrayList<>();
        for (Segment segment : segments) {
            segment.addEdges(edges);
        }

        if (edges.isEmpty())
            return;

        // Find the bounds
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Edge edge : edges) {
            minY = Math.min(minY, edge.yMax - 1);
            maxY = Math.max(maxY, edge.yMax);
        }

        // Ensure bounds are within image
        minY = Math.max(0, minY);
        maxY = Math.min(img.getHeight() - 1, maxY);

        // Scanline fill algorithm
        for (int y = minY; y <= maxY; y++) {
            List<Double> intersections = new ArrayList<>();

            // Find intersections with scanline
            for (Edge edge : edges) {
                if (y >= minY && y < edge.yMax) {
                    intersections.add(edge.x);
                    edge.x += edge.deltaX;
                }
            }

            // Sort intersections
            Collections.sort(intersections);

            // Fill between pairs of intersections
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int x1 = Math.max(0, (int) Math.ceil(intersections.get(i)));
                int x2 = Math.min(img.getWidth() - 1, (int) Math.floor(intersections.get(i + 1)));

                for (int x = x1; x <= x2; x++) {
                    if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
                        img.setRGB(x, y, fillRGB);
                    }
                }
            }
        }
    }

    // CUSTOM stroke
    public void customStroke(BufferedImage img) {
        int strokeRGB = strokeColor.getRGB();
        for (Segment s : segments) {
            s.drawCustom(img, strokeRGB);
        }
    }

    // And yeah i didn't know that i can use g2.draw
    // Draw the path outline using custom pixel-level drawing
    public void draw(Graphics2D g2) {
        // setupGraphics(g2);

        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();

        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(strokeColor);

        for (Segment s : segments) {
            s.draw(g2);
        }

        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }

    // Fill the path interior using custom polygon filling
    public void fill(Graphics2D g2) {
        Color oldColor = g2.getColor();
        g2.setColor(fillColor);

        // Create temporary image for polygon filling
        Rectangle bounds = getBounds();
        if (bounds.width > 0 && bounds.height > 0) {
            BufferedImage tempImg = new BufferedImage(bounds.width + bounds.x + 10,
                    bounds.height + bounds.y + 10,
                    BufferedImage.TYPE_INT_ARGB);

            // Fill polygons on temp image
            customFillPolygons(tempImg);

            // Draw the filled image back to graphics
            g2.drawImage(tempImg, 0, 0, null);
        }

        g2.setColor(oldColor);
    }

    // Custom polygon filling method
    private void customFillPolygons(BufferedImage img) {
        List<Polygon> allPolygons = getAllPolygons();
        int fillRGB = fillColor.getRGB();

        for (Polygon poly : allPolygons) {
            if (poly.points.size() >= 3) {
                fillPolygon(img, poly, fillRGB);
            }
        }
    }

    // Fill a single polygon using scanline algorithm
    private void fillPolygon(BufferedImage img, Polygon poly, int color) {
        if (poly.points.isEmpty())
            return;

        // Find bounds
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (PathPoint p : poly.points) {
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        int yMin = Math.max(0, (int) Math.floor(minY));
        int yMax = Math.min(img.getHeight() - 1, (int) Math.ceil(maxY));

        // Scanline fill
        for (int y = yMin; y <= yMax; y++) {
            List<Double> intersections = new ArrayList<>();

            // Find intersections with polygon edges
            for (int i = 0; i < poly.points.size(); i++) {
                PathPoint p1 = poly.points.get(i);
                PathPoint p2 = poly.points.get((i + 1) % poly.points.size());

                if ((p1.y <= y && p2.y > y) || (p2.y <= y && p1.y > y)) {
                    double x = p1.x + (y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y);
                    intersections.add(x);
                }
            }

            Collections.sort(intersections);

            // Fill between pairs
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int x1 = Math.max(0, (int) Math.ceil(intersections.get(i)));
                int x2 = Math.min(img.getWidth() - 1, (int) Math.floor(intersections.get(i + 1)));

                for (int x = x1; x <= x2; x++) {
                    if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
                        img.setRGB(x, y, color);
                    }
                }
            }
        }
    }

    // Custom fill and stroke that doesn't use g2.setColor or g2.fill
    public void customFillAndStroke(BufferedImage img) {
        customFill(img);
        customStroke(img);
    }

    // Fill and stroke in one operation
    public void fillAndStroke(Graphics2D g2) {
        fill(g2);
        draw(g2);
    }

    // Get the bounding box of the path
    public Rectangle getBounds() {
        if (segments.isEmpty()) {
            return new Rectangle();
        }

        // Calculate bounds from all polygons
        List<Polygon> allPolygons = getAllPolygons();
        if (allPolygons.isEmpty()) {
            return new Rectangle();
        }

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Polygon poly : allPolygons) {
            for (PathPoint p : poly.points) {
                minX = Math.min(minX, p.x);
                maxX = Math.max(maxX, p.x);
                minY = Math.min(minY, p.y);
                maxY = Math.max(maxY, p.y);
            }
        }

        if (minX == Double.MAX_VALUE) {
            return new Rectangle();
        }

        return new Rectangle((int) minX, (int) minY,
                (int) Math.ceil(maxX - minX),
                (int) Math.ceil(maxY - minY));
    }

    // Test if a point is inside the filled path using ray casting
    public boolean contains(double x, double y) {
        List<Polygon> allPolygons = getAllPolygons();

        for (Polygon poly : allPolygons) {
            if (pointInPolygon(x, y, poly)) {
                return true;
            }
        }
        return false;
    }

    // Point in polygon test using ray casting algorithm
    private boolean pointInPolygon(double x, double y, Polygon poly) {
        if (poly.points.size() < 3)
            return false;

        boolean inside = false;
        int j = poly.points.size() - 1;

        for (int i = 0; i < poly.points.size(); j = i++) {
            PathPoint pi = poly.points.get(i);
            PathPoint pj = poly.points.get(j);

            if (((pi.y > y) != (pj.y > y)) &&
                    (x < (pj.x - pi.x) * (y - pi.y) / (pj.y - pi.y) + pi.x)) {
                inside = !inside;
            }
        }

        return inside;
    }

    // Clear the path
    public BirbPath clear() {
        segments.clear();
        polygons.clear();
        currentPolygon.clear();
        currentX = 0;
        currentY = 0;
        subpathStartX = 0;
        subpathStartY = 0;
        hasCurrentSubpath = false;
        return this;
    }

    // Get a copy of this path
    public BirbPath copy() {
        BirbPath copy = new BirbPath();
        copy.segments.addAll(this.segments);
        copy.polygons.addAll(this.polygons);
        copy.currentPolygon = this.currentPolygon.copy();
        copy.currentX = this.currentX;
        copy.currentY = this.currentY;
        copy.subpathStartX = this.subpathStartX;
        copy.subpathStartY = this.subpathStartY;
        copy.hasCurrentSubpath = this.hasCurrentSubpath;
        copy.fillColor = this.fillColor;
        copy.strokeColor = this.strokeColor;
        copy.strokeWidth = this.strokeWidth;
        copy.windingRule = this.windingRule;
        copy.antialiasing = this.antialiasing;
        return copy;
    }

    // private void setupGraphics(Graphics2D g2) {
    // if (antialiasing) {
    // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    // RenderingHints.VALUE_ANTIALIAS_ON);
    // g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
    // RenderingHints.VALUE_STROKE_PURE);
    // } else {
    // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    // RenderingHints.VALUE_ANTIALIAS_OFF);
    // }
    // }

    // Example usage and testing
    public static void main(String[] args) {
        // Create a test image
        BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        // Clear background to white manually
        int white = Color.WHITE.getRGB();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                img.setRGB(x, y, white);
            }
        }

        // Create and draw a complex path using custom methods
        BirbPath path = new BirbPath()
                .setFillColor(new Color(100, 150, 255, 128))
                .setStrokeColor(Color.BLUE)
                .setStrokeWidth(2.0f)
                .moveTo(50, 50)
                .lineTo(150, 50)
                .curveTo(200, 50, 200, 100, 150, 100)
                .lineTo(50, 100)
                .closePath()
                .moveTo(250, 150)
                .circle(300, 200, 50)
                .rect(100, 200, 100, 80);

        // Fill and stroke the path using custom implementation
        path.customFillAndStroke(img);

        g2.dispose();

        System.out.println("BirbPath custom fill test completed. Bounds: " + path.getBounds());
        System.out.println("Contains point (100, 75): " + path.contains(100, 75));
        System.out.println("Contains point (300, 200): " + path.contains(300, 200));
    }
}