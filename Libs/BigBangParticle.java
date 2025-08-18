package Libs;

import java.awt.*;

// Enhanced particle for Big Bang explosion
public class BigBangParticle extends Particle {
    private double glowIntensity;
    
    public BigBangParticle(Vector3D position, Vector3D velocity, Color color, double life) {
        super(position, velocity, color, life);
        this.size = 3 + Math.random() * 5;
        this.glowIntensity = 1.0;
        this.trail = true; // Always has trail
    }
    
    @Override
    public void update() {
        super.update();
        glowIntensity *= 0.98; // Glow fades over time
    }
    
    @Override
    public void draw(Graphics2D g, int centerX, int centerY, Midpoint midpoint, BresenhamLine bresenham) {
        Point screenPos = position.project(centerX, centerY, 300);
        
        // Bounds check
        if (screenPos.x < -100 || screenPos.x > 1300 || screenPos.y < -100 || screenPos.y > 900) {
            return;
        }
        
        // Draw glow effect
        for (int glow = 3; glow >= 1; glow--) {
            int glowSize = (int)(size + glow * 2 * glowIntensity);
            int alpha = (int)(color.getAlpha() / (glow * 2) * glowIntensity);
            
            Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(alpha, 0));
            midpoint.plotFilledCircle(g, screenPos.x, screenPos.y, glowSize, glowColor);
        }
        
        // Draw main particle
        midpoint.plotFilledCircle(g, screenPos.x, screenPos.y, (int)size, color);
        
        // Enhanced trail effect
        if (velocity.magnitude() > 1) {
            for (int t = 1; t <= 2; t++) {
                Vector3D trailStart = new Vector3D(position);
                Vector3D scaledVel = new Vector3D(velocity);
                scaledVel.multiply(t * 2);
                trailStart.subtract(scaledVel);
                Point trailPos = trailStart.project(centerX, centerY, 300);
                
                int trailAlpha = color.getAlpha() / (t * 3);
                Color trailColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(trailAlpha, 0));
                
                if (trailPos.x > -50 && trailPos.x < 1250 && trailPos.y > -50 && trailPos.y < 850) {
                    bresenham.drawLine(g, trailPos, screenPos, trailColor);
                }
            }
        }
    }
}