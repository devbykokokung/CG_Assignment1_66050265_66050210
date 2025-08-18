package Libs;

import java.awt.*;

// Optimized base particle class
public class Particle {
    protected Vector3D position;
    protected Vector3D velocity;
    protected Color color;
    protected double life;
    protected double maxLife;
    protected double size;
    protected boolean trail;
    
    public Particle(Vector3D position, Vector3D velocity, Color color, double life) {
        this.position = new Vector3D(position);
        this.velocity = new Vector3D(velocity);
        this.color = color;
        this.life = life;
        this.maxLife = life;
        this.size = 1.5 + Math.random() * 2.5;
        this.trail = velocity.magnitude() > 5; // Only fast particles have trails
    }
    
    public void update() {
        position.add(velocity);
        velocity.multiply(0.97); // Air resistance
        life -= 0.025; // Faster decay for performance
        
        // Fade out as life decreases
        double alpha = Math.max(0, life / maxLife);
        color = new Color(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            (int)(255 * alpha)
        );
        
        // Size decrease over time
        size *= 0.995;
    }
    
    public void draw(Graphics2D g, int centerX, int centerY, Midpoint midpoint, BresenhamLine bresenham) {
        Point screenPos = position.project(centerX, centerY, 300);
        
        // Bounds check for performance
        if (screenPos.x < -50 || screenPos.x > 1250 || screenPos.y < -50 || screenPos.y > 850) {
            return;
        }
        
        // Draw particle core
        midpoint.plotFilledCircle(g, screenPos.x, screenPos.y, (int)size, color);
        
        // Optional trail effect (only for fast particles)
        if (trail && velocity.magnitude() > 2) {
            Vector3D trailStart = new Vector3D(position);
            Vector3D scaledVel = new Vector3D(velocity);
            scaledVel.multiply(2);
            trailStart.subtract(scaledVel);
            Point trailPos = trailStart.project(centerX, centerY, 300);
            
            Color trailColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 4);
            bresenham.drawLine(g, trailPos, screenPos, trailColor);
        }
    }
    
    public boolean isDead() {
        return life <= 0 || size < 0.5;
    }
}
