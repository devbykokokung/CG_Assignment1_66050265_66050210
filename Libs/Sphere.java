package Libs;

import java.awt.*;

// Sphere implementation
public class Sphere extends Object3D {
    public Sphere(Vector3D position, double radius) {
        super(position, radius, new Color(100, 255, 100));
    }
    
    @Override
    protected void generateGeometry() {
        int segments = 8;
        int rings = 6;
        
        // Generate sphere vertices
        for (int ring = 0; ring <= rings; ring++) {
            double phi = Math.PI * ring / rings;
            double y = size * Math.cos(phi);
            double ringRadius = size * Math.sin(phi);
            
            for (int seg = 0; seg < segments; seg++) {
                double theta = 2 * Math.PI * seg / segments;
                double x = ringRadius * Math.cos(theta);
                double z = ringRadius * Math.sin(theta);
                vertices.add(new Vector3D(x, y, z));
            }
        }
        
        // Generate faces (triangles)
        for (int ring = 0; ring < rings; ring++) {
            for (int seg = 0; seg < segments; seg++) {
                int current = ring * segments + seg;
                int next = ring * segments + (seg + 1) % segments;
                int below = (ring + 1) * segments + seg;
                int belowNext = (ring + 1) * segments + (seg + 1) % segments;
                
                if (ring < rings) {
                    faces.add(new Face(current, next, belowNext));
                    faces.add(new Face(current, belowNext, below));
                }
            }
        }
    }
}
