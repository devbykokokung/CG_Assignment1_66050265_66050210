package Libs;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Optimized Particle System for Better Performance
 * Reduced particle count with enhanced visual effects
 */
public class OptimizedParticleSystem {
    private List<Particle> particles;
    private Random random;
    private int maxParticles;
    
    public OptimizedParticleSystem(int maxParticles) {
        this.maxParticles = maxParticles;
        particles = new ArrayList<>();
        random = new Random();
    }
    
    public void addMiniExplosion(Vector3D position, int particleCount) {
        // Ensure we don't exceed particle limit
        if (particles.size() + particleCount > maxParticles) {
            int toRemove = particles.size() + particleCount - maxParticles;
            for (int i = 0; i < toRemove && !particles.isEmpty(); i++) {
                particles.remove(0); // Remove oldest particles
            }
        }
        
        for (int i = 0; i < particleCount; i++) {
            Vector3D pos = new Vector3D(position);
            
            // Random velocity in all directions
            Vector3D velocity = new Vector3D(
                (random.nextDouble() - 0.5) * 8,
                (random.nextDouble() - 0.5) * 8,
                (random.nextDouble() - 0.5) * 8
            );
            
            Color particleColor = new Color(
                200 + random.nextInt(55),
                100 + random.nextInt(155),
                random.nextInt(100),
                255
            );
            
            double life = 0.8 + random.nextDouble() * 1.2; // Shorter life for performance
            particles.add(new Particle(pos, velocity, particleColor, life));
        }
    }
    
    public void addBigBangExplosion(Vector3D center, int particleCount) {
        // Clear some old particles to make room
        if (particles.size() + particleCount > maxParticles) {
            int toRemove = particles.size() + particleCount - maxParticles;
            for (int i = 0; i < toRemove && !particles.isEmpty(); i++) {
                particles.remove(0);
            }
        }
        
        for (int i = 0; i < particleCount; i++) {
            // Create particles in all directions with high velocity
            double phi = random.nextDouble() * Math.PI * 2;
            double theta = random.nextDouble() * Math.PI;
            double speed = 8 + random.nextDouble() * 20; // Increased speed
            
            Vector3D velocity = new Vector3D(
                Math.sin(theta) * Math.cos(phi) * speed,
                Math.sin(theta) * Math.sin(phi) * speed,
                Math.cos(theta) * speed
            );
            
            Vector3D position = new Vector3D(center);
            
            // Bright explosive colors
            Color particleColor;
            switch (random.nextInt(5)) {
                case 0: particleColor = new Color(255, 255, 255, 255); break; // White
                case 1: particleColor = new Color(255, 220, 0, 255); break;   // Bright Yellow
                case 2: particleColor = new Color(255, 150, 0, 255); break;   // Orange
                case 3: particleColor = new Color(255, 0, 150, 255); break;   // Pink
                default: particleColor = new Color(200, 200, 255, 255); break; // Light Blue
            }
            
            double life = 2.0 + random.nextDouble() * 3.0;
            
            if (i < particleCount / 3) {
                // Some larger particles for dramatic effect
                particles.add(new BigBangParticle(position, velocity, particleColor, life));
            } else {
                particles.add(new Particle(position, velocity, particleColor, life));
            }
        }
    }
    
    public void update() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.update();
            
            if (particle.isDead()) {
                iterator.remove();
            }
        }
    }
    
    public void draw(Graphics2D g, int centerX, int centerY, Midpoint midpoint, BresenhamLine bresenham) {
        // Sort particles by z-depth for proper rendering
        particles.sort((a, b) -> Double.compare(b.position.z, a.position.z));
        
        for (Particle particle : particles) {
            particle.draw(g, centerX, centerY, midpoint, bresenham);
        }
    }
    
    public int getParticleCount() {
        return particles.size();
    }
    
    public void clear() {
        particles.clear();
    }
}
