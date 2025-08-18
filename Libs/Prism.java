package Libs;

import java.awt.*;

// Prism implementation
public class Prism extends Object3D {
    public Prism(Vector3D position, double size) {
        super(position, size, new Color(100, 100, 255));
    }
    
    @Override
    protected void generateGeometry() {
        double height = size;
        double radius = size * 0.7;
        int sides = 6; // Hexagonal prism
        
        // Bottom vertices
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            vertices.add(new Vector3D(
                radius * Math.cos(angle),
                -height / 2,
                radius * Math.sin(angle)
            ));
        }
        
        // Top vertices
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            vertices.add(new Vector3D(
                radius * Math.cos(angle),
                height / 2,
                radius * Math.sin(angle)
            ));
        }
        
        // Bottom face
        int[] bottomFace = new int[sides];
        for (int i = 0; i < sides; i++) {
            bottomFace[i] = i;
        }
        faces.add(new Face(bottomFace));
        
        // Top face
        int[] topFace = new int[sides];
        for (int i = 0; i < sides; i++) {
            topFace[i] = sides + (sides - 1 - i); // Reverse order for correct normal
        }
        faces.add(new Face(topFace));
        
        // Side faces
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            faces.add(new Face(i, next, sides + next, sides + i));
        }
    }
}