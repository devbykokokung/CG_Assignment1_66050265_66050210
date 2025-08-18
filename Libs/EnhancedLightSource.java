package Libs;
import java.awt.*;

/**
 * Enhanced Light Source with Powerful Effects
 * Uses only Java 2D API for realistic lighting
 * 
 */
public class EnhancedLightSource {
    private Vector3D currentPosition;
    private double orbitRadius = 400;
    private double orbitSpeed = 1.2;
    private Color coreColor = new Color(255, 255, 225);
    private double intensity = 1.0;
    private double pulsePhase = 0;
    
    public EnhancedLightSource(Vector3D position) {
        this.currentPosition = new Vector3D(position);
    }
    
    public void update(double time, double blackHoleIntensity) {
        // orbit affected by black hole
        double angle = time * orbitSpeed;
        orbitRadius = 380 + Math.sin(time * 0.5) * 50; // Varying orbit
        
        currentPosition.x = Math.cos(angle) * orbitRadius;
        currentPosition.y = Math.sin(angle) * orbitRadius * 0.4; // Elliptical
        currentPosition.z = Math.sin(angle * 1.5) * 100 + 50; // Complex Z movement
        
        // PULSE intensity
        pulsePhase += 0.1;
        intensity = 1.0 + Math.sin(pulsePhase) * 0.3 + blackHoleIntensity * 0.2;
    }
    
    public void draw(Graphics2D g, int centerX, int centerY, Midpoint midpoint, BresenhamLine bresenham) {
        Point screenPos = currentPosition.project(centerX, centerY, 350);
        
        // Draw multiple layers for powerful light effect
        drawLightCore(g, screenPos, midpoint);
        drawLightHalos(g, screenPos, midpoint);
        drawLightRays(g, screenPos, centerX, centerY, bresenham);
        drawLightFlares(g, screenPos, midpoint);
        drawVolumetricLight(g, screenPos, centerX, centerY, bresenham);
    }

    //***== Draw lightcore (a music artist..)(It's Nightcore.. Bad joke btw.) ==***
    private void drawLightCore(Graphics2D g, Point center, Midpoint midpoint) {
        // white core with pulsing effect
        // Althou this part viewer might not see. But it used to be a main spotlight. 
        int coreSize = (int)(12 * intensity);
        
        // Multiple layers for intensity
        for (int layer = 0; layer < 5; layer++) {
            int size = coreSize - layer * 2;
            if (size <= 0) break;
            
            int brightness = (int)(255 - layer * 30);
            Color layerColor = new Color(brightness, brightness, brightness);
            
            midpoint.plotFilledCircle(g, center.x, center.y, size, layerColor);
        }
    }
    private void drawLightHalos(Graphics2D g, Point center, Midpoint midpoint) {
        // Multiple concentric halos with fading alpha
        for (int halo = 1; halo <= 8; halo++) {
            int radius = (int)(15 + halo * 8 * intensity);
    
            // Compute alpha safely well.. This bug was discover when loop at around 5th loops
            int rawAlpha = (int)(80 / halo * intensity);
            int alpha = Math.max(0, Math.min(255, rawAlpha)); // clamp to [0, 255] 
            // if dont have this it will have an erro but still runnable tho
    
            // temperature variation
            int red = 255;
            int green = (int)(255 - halo * 10);
            int blue = (int)(240 - halo * 20);
    
            // Clamp RGB as well-
            red = Math.max(0, Math.min(255, red));
            green = Math.max(0, Math.min(255, green));
            blue = Math.max(0, Math.min(255, blue));
    
            Color haloColor = new Color(red, green, blue, alpha);
            midpoint.drawCircle(g, center.x, center.y, radius, haloColor);
    
            // THIS ADD inner glow for first few halos
            if (halo <= 3) {
                int innerAlpha = Math.max(0, Math.min(255, alpha / 2));
                Color innerGlow = new Color(255, 255, 200, innerAlpha);
                midpoint.drawCircle(g, center.x, center.y, radius - 2, innerGlow);
            }
        }
    }
    
    private void drawLightRays(Graphics2D g, Point center, int blackHoleX, int blackHoleY, BresenhamLine bresenham) {
        // light rays in all directions
        int rayCount = 16;
        
        for (int i = 0; i < rayCount; i++) {
            double angle = (Math.PI * 2 * i / rayCount) + pulsePhase * 0.5;
            
            // Variable ray lengths for dynamic effect
            int rayLength = (int)(120 + Math.sin(pulsePhase + i) * 40) * (int)intensity;
            
            Point rayEnd = new Point(
                (int)(center.x + Math.cos(angle) * rayLength),
                (int)(center.y + Math.sin(angle) * rayLength)
            );
            
            // Draw ray with fading effect
            drawFadingRay(g, center, rayEnd, bresenham);
        }
        
        // Special rays toward black hole (light bending effect) 
        // This one I tried my best to do but mehh
        double dx = blackHoleX - center.x;
        double dy = blackHoleY - center.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            double toBlackHoleAngle = Math.atan2(dy, dx);
            
            // Bent light rays
            for (int bend = -2; bend <= 2; bend++) {
                double bendAngle = toBlackHoleAngle + bend * 0.3;
                
                Point bentEnd = new Point(
                    (int)(center.x + Math.cos(bendAngle) * 200),
                    (int)(center.y + Math.sin(bendAngle) * 200)
                );
                
                Color bentColor = new Color(255, 200, 150, 60);
                bresenham.drawLine(g, center, bentEnd, bentColor);
            }
        }
    }
    
    private void drawFadingRay(Graphics2D g, Point start, Point end, BresenhamLine bresenham) {
        // Draw ray with fading alpha along its length
        int segments = 15;
        
        for (int seg = 0; seg < segments; seg++) {
            double t1 = (double)seg / segments;
            double t2 = (double)(seg + 1) / segments;
            
            Point p1 = new Point(
                (int)(start.x + (end.x - start.x) * t1),
                (int)(start.y + (end.y - start.y) * t1)
            );
            
            Point p2 = new Point(
                (int)(start.x + (end.x - start.x) * t2),
                (int)(start.y + (end.y - start.y) * t2)
            );
            
            // Fade out along ray
            double alpha = (1.0 - t1) * intensity;
            int alphaValue = (int)(120 * alpha);
            alphaValue = Math.max(0, Math.min(255, alphaValue)); // clamp to [0,255]

            Color rayColor = new Color(255, 255, 200, alphaValue);
            bresenham.drawLine(g, p1, p2, rayColor);
        }
    }
    
    private void drawLightFlares(Graphics2D g, Point center, Midpoint midpoint) {
        // Lens flare effects
        for (int flare = 0; flare < 4; flare++) {
            double flareAngle = pulsePhase + flare * Math.PI / 2;
            
            int flareX = (int)(center.x + Math.cos(flareAngle) * (30 + flare * 10));
            int flareY = (int)(center.y + Math.sin(flareAngle) * (30 + flare * 10));
            
            int flareSize = (int)(8 - flare * 1.5);
            int alpha = (int)(100 - flare * 20);
            
            Color flareColor = new Color(255, 200, 100, Math.max(alpha, 10));
            midpoint.plotFilledCircle(g, flareX, flareY, flareSize, flareColor);
        }
        
        // == Cross flare effect ==
        int crossLength = (int)(50 * intensity);
        
        // == Vertical line ==
        Point crossTop = new Point(center.x, center.y - crossLength);
        Point crossBottom = new Point(center.x, center.y + crossLength);
        
        // == Horizontal line ==  
        Point crossLeft = new Point(center.x - crossLength, center.y);
        Point crossRight = new Point(center.x + crossLength, center.y);
        
        // multiple lines for thickness fro cross
        for (int thickness = -2; thickness <= 2; thickness++) {
            Point vTop = new Point(crossTop.x + thickness, crossTop.y);
            Point vBottom = new Point(crossBottom.x + thickness, crossBottom.y);
            Point hLeft = new Point(crossLeft.x, crossLeft.y + thickness);
            Point hRight = new Point(crossRight.x, crossRight.y + thickness);
            
            g.setColor(new Color(255, 255, 255, 40 - Math.abs(thickness) * 10));
            g.drawLine(vTop.x, vTop.y, vBottom.x, vBottom.y);
            g.drawLine(hLeft.x, hLeft.y, hRight.x, hRight.y);
        }
    }
    
    private void drawVolumetricLight(Graphics2D g, Point center, int blackHoleX, int blackHoleY, BresenhamLine bresenham) {
        // Volumetric light scattering effect
        for (int vol = 0; vol < 6; vol++) {
            double volumeAngle = (vol * Math.PI / 3) + pulsePhase * 0.3;
            
            // Create triangular light volume
            Point apex = center;
            
            double spreadAngle = Math.PI / 8; // ~ 22.5 degrees spread **
            Point left = new Point(
                (int)(center.x + Math.cos(volumeAngle - spreadAngle) * 150),
                (int)(center.y + Math.sin(volumeAngle - spreadAngle) * 150)
            );
            
            Point right = new Point(
                (int)(center.x + Math.cos(volumeAngle + spreadAngle) * 150),
                (int)(center.y + Math.sin(volumeAngle + spreadAngle) * 150)
            );
            
            // Draw volume edges
            Color volumeColor = new Color(255, 255, 200, 30);
            bresenham.drawLine(g, apex, left, volumeColor);
            bresenham.drawLine(g, apex, right, volumeColor);
            bresenham.drawLine(g, left, right, volumeColor);
            
            // Fill volume with particles
            for (int particle = 0; particle < 5; particle++) {
                double t = Math.random();
                double s = Math.random() * t; // Inside triangle **
                
                int px = (int)(apex.x + (left.x - apex.x) * t + (right.x - left.x) * s);
                int py = (int)(apex.y + (left.y - apex.y) * t + (right.y - left.y) * s);
                
                Color particleColor = new Color(255, 255, 200, 20);
                Midpoint midpoint = new Midpoint();
                midpoint.plotFilledCircle(g, px, py, 1, particleColor);
            }
        }
    }
    
    // Get lighting influence for objects
    public double getLightingInfluence(Vector3D objectPosition) {
        Vector3D lightToObject = new Vector3D(objectPosition);
        lightToObject.subtract(currentPosition);
        
        double distance = lightToObject.magnitude();
        double influence = intensity / (1 + distance * 0.01);
        
        return Math.min(influence, 1.0);
    }
    
    public Vector3D getPosition() {
        return new Vector3D(currentPosition);
    }
    
    public Color getLightColor() {
        return coreColor;
    }
    
    public double getIntensity() {
        return intensity;
    }
}