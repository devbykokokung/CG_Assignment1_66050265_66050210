package Libs;

import java.awt.*;

// Cube implementation
public class Cube extends Object3D {
    public Cube(Vector3D position, double size) {
        super(position, size, new Color(255, 100, 100));
    }
    
    @Override
    protected void generateGeometry() {
        double s = size / 2;
        
        // 8 vertices of cube
        vertices.add(new Vector3D(-s, -s, -s)); // 0
        vertices.add(new Vector3D( s, -s, -s)); // 1
        vertices.add(new Vector3D( s,  s, -s)); // 2
        vertices.add(new Vector3D(-s,  s, -s)); // 3
        vertices.add(new Vector3D(-s, -s,  s)); // 4
        vertices.add(new Vector3D( s, -s,  s)); // 5
        vertices.add(new Vector3D( s,  s,  s)); // 6
        vertices.add(new Vector3D(-s,  s,  s)); // 7
        
        // 6 faces
        faces.add(new Face(0, 1, 2, 3)); // front
        faces.add(new Face(5, 4, 7, 6)); // back
        faces.add(new Face(4, 0, 3, 7)); // left
        faces.add(new Face(1, 5, 6, 2)); // right
        faces.add(new Face(3, 2, 6, 7)); // top
        faces.add(new Face(4, 5, 1, 0)); // bottom
    }
}
