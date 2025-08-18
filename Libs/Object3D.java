package Libs;

import java.awt.*;
import java.util.*;
import java.util.List;

// Base 3D Object class
public abstract class Object3D {
    protected Vector3D position;
    protected Vector3D velocity;
    protected Vector3D rotation;
    protected double size;
    protected Color color;
    protected double tastyspaghettificationFactor = 1.0;
    protected List<Vector3D> vertices;
    protected List<Face> faces;
    public boolean beingAbsorbed = false;
    public double shrinkFactor = 1.0;

    public Object3D(Vector3D position, double size, Color color) {
        this.position = new Vector3D(position);
        this.velocity = new Vector3D(0, 0, 0);
        this.rotation = new Vector3D(0, 0, 0);
        this.size = size;
        this.color = color;
        this.vertices = new ArrayList<>();
        this.faces = new ArrayList<>();
        generateGeometry();
    }

    public void startAbsorption() {
        beingAbsorbed = true;
    }

    protected abstract void generateGeometry();

    public void update() {
        position.add(velocity);
        rotation.x += 0.02;
        rotation.y += 0.01;
        rotation.z += 0.015;

        // apply velocity
        velocity.multiply(0.999);
        if (beingAbsorbed) {
            shrinkFactor *= 0.9; // Shrink the obj
        }

    }

    public void addSpaghettification(double factor) {
        tastyspaghettificationFactor += factor;
    }

    public void draw(Graphics2D g, int centerX, int centerY, BresenhamLine bresenham, Midpoint midpoint,
            EnhancedLightSource lightSource) {
        // Transform vertices
        List<Vector3D> transformedVertices = new ArrayList<>();
        for (Vector3D vertex : vertices) {
            Vector3D transformed = new Vector3D(vertex);

            // spaghettification (stretching toward black hole)
            // May not be notice
            if (tastyspaghettificationFactor > 1.0) {
                double stretchX = 1.0 + (tastyspaghettificationFactor - 1.0);
                double stretchY = 1.0 / Math.sqrt(stretchX);
                transformed.x *= stretchX;
                transformed.y *= stretchY;
            }

            // APPLY rotation
            transformed = transformed.rotateX(rotation.x);
            transformed = transformed.rotateY(rotation.y);
            transformed = transformed.rotateZ(rotation.z);

            // APPLY position
            transformed.add(position);

            transformedVertices.add(transformed);
            transformed.x *= shrinkFactor;
            transformed.y *= shrinkFactor;
            transformed.z *= shrinkFactor;
        }

        // Sort faces by depth (painter's algorithm)
        faces.sort((f1, f2) -> {
            double z1 = 0, z2 = 0;
            for (int idx : f1.vertexIndices) {
                z1 += transformedVertices.get(idx).z;
            }
            for (int idx : f2.vertexIndices) {
                z2 += transformedVertices.get(idx).z;
            }
            return Double.compare(z2 / f1.vertexIndices.length, z1 / f1.vertexIndices.length);
        });
        // drawFace
        for (Face face : faces) {
            drawFace(g, centerX, centerY, transformedVertices, face, bresenham, midpoint, lightSource);
        }
    }

    protected void drawFace(Graphics2D g, int centerX, int centerY, List<Vector3D> vertices, Face face,
            BresenhamLine bresenham, Midpoint midpoint, EnhancedLightSource lightSource) {
        Point[] screenPoints = new Point[face.vertexIndices.length];

        for (int i = 0; i < face.vertexIndices.length; i++) {
            Vector3D vertex = vertices.get(face.vertexIndices[i]);
            screenPoints[i] = vertex.project(centerX, centerY, 300);
        }

        // Calculate face normal for lighting but (simple)
        if (face.vertexIndices.length >= 3) {
            Vector3D v1 = new Vector3D(vertices.get(face.vertexIndices[1]));
            Vector3D v2 = new Vector3D(vertices.get(face.vertexIndices[0]));
            Vector3D v3 = new Vector3D(vertices.get(face.vertexIndices[2]));

            v1.subtract(v2);
            v3.subtract(v2);
            Vector3D normal = v1.cross(v3);
            normal.normalize();

            // Enhanced lighting calculation with light source
            Vector3D lightDir = new Vector3D(lightSource.getPosition());
            Vector3D faceCenter = new Vector3D(0, 0, 0);
            for (int idx : face.vertexIndices) {
                faceCenter.add(vertices.get(idx));
            }
            faceCenter.multiply(1.0 / face.vertexIndices.length);

            lightDir.subtract(faceCenter);
            lightDir.normalize();

            double lighting = Math.abs(normal.dot(lightDir));

            // light source
            double lightInfluence = lightSource.getLightingInfluence(faceCenter);
            lighting = Math.max(0.2, lighting * (0.5 + lightInfluence * 0.5));

            Color faceColor = new Color(
                    (int) (color.getRed() * lighting),
                    (int) (color.getGreen() * lighting),
                    (int) (color.getBlue() * lighting),
                    color.getAlpha());

            // Fill face
            if (screenPoints.length >= 3) {
                fillPolygon(g, screenPoints, faceColor, midpoint);
            }
        }

        // Draw edges using Bresenham
        Color edgeColor = color.darker();
        for (int i = 0; i < screenPoints.length; i++) {
            Point p1 = screenPoints[i];
            Point p2 = screenPoints[(i + 1) % screenPoints.length];
            bresenham.drawLine(g, p1, p2, edgeColor);
        }
    }

    private void fillPolygon(Graphics2D g, Point[] points, Color color, Midpoint midpoint) {
        // Simple polygon filling using scanlines
        g.setColor(color);

        if (points.length < 3)
            return;

        // Find bounding box
        int minY = points[0].y, maxY = points[0].y;
        for (Point p : points) {
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        // Simple fill (not perfect but works for demo)
        Polygon poly = new Polygon();
        for (Point p : points) {
            poly.addPoint(p.x, p.y);
        }
        g.fillPolygon(poly);
    }

    // Getters and setters
    public Vector3D getPosition() {
        return position;
    }

    public Vector3D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }

    public double getSize() {
        return size;
    }

    public Color getColor() {
        return color;
    }
}