package Libs;

public class Vector3D {
    public double x, y, z;
    
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector3D(Vector3D other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }
    
    public void add(Vector3D other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
    }
    
    public void subtract(Vector3D other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
    }
    
    public void multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
    }
    
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }
    
    public void normalize() {
        double mag = magnitude();
        if (mag > 0) {
            x /= mag;
            y /= mag;
            z /= mag;
        }
    }
    
    public double dot(Vector3D other) {
        return x * other.x + y * other.y + z * other.z;
    }
    
    public Vector3D cross(Vector3D other) {
        return new Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
    
    public Vector3D rotateX(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector3D(x, y * cos - z * sin, y * sin + z * cos);
    }
    
    public Vector3D rotateY(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector3D(x * cos + z * sin, y, -x * sin + z * cos);
    }
    
    public Vector3D rotateZ(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector3D(x * cos - y * sin, x * sin + y * cos, z);
    }
    
    // Project 3D point to 2D screen coordinates
    public java.awt.Point project(int centerX, int centerY, double focalLength) {
        double scale = focalLength / (focalLength + z);
        return new java.awt.Point(
            (int)(centerX + x * scale),
            (int)(centerY + y * scale)
        );
    }
    
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}