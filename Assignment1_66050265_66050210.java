import javax.swing.*;
import javax.swing.Timer;
import Frames.FrameIndex;
import Libs.BezierCurve;
import Libs.BresenhamLine;
import Libs.Cube;
import Libs.EnhancedLightSource;
import Libs.Interpolation;
import Libs.Midpoint;
import Libs.Object3D;
import Libs.OptimizedParticleSystem;
import Libs.Prism;
import Libs.Sphere;
import Libs.Vector3D;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/** Make sure to compile ALL file before running 
 * I have CompileAllFile.java for you
 *  - Vector3D .java
// - Object3D .java , Cube, Sphere, Prism
// - EnhancedLightSource.java
// - ParticleSystem.java
// - BresenhamLine, MidpointCircle, BezierCurve
// - Interpolation class 
// - BirbPath.java
// - BirbDraw.java
* 
 */
/**
 * Black Hole Simulation with Physics
 * 
 * SOURCES & INSPIRATION:
 * - Bresenham Line Algorithm: "Computer Graphics: Principles and Practice" by
 * Foley & van Dam and by Withchaya Towongpaichayon
 * - Bezier Curves: by Withchaya Towongpaichayon
 * - Midpoint by by Withchaya Towongpaichayon
 * - 3D Projection Math: Standard computer graphics textbooks
 * - Black Hole Physics: Inspired by Interstellar movie effects
 * - Screen Shake: Game development techniques from Juice it or lose it (GDC talk)
 *
 * AND ==============================================
 * Particle System From Game Programming Exit Exam by Birbpng 66050265 (me)
 * Vector3D From Java Game Year 1 that was not used by Birbpng 66050265 (me)
 * also lighting
 * 
 * All algorithms implemented using only Java 2D API
 * 😭😭😭😭😭😭😭😭😭😭😭😭
 * I thought i cant use setcolor and fill
 */

public class Assignment1_66050265_66050210 extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int CENTER_X = WIDTH / 2;
    private static final int CENTER_Y = HEIGHT / 2;

   
    private enum Phase {
        ORBITING, CONSUMING, SUPERNOVA, FRAME_ANIMATION, RESETTING
    }

    private Phase currentPhase = Phase.ORBITING;
    private Timer timer;
    private BufferedImage offscreen;
    private Graphics2D g2d;
    private int timerdelay = 15;

    private Camera camera;
    private ScreenShake screenShake;

    private int blackHoleRadius = 50;
    private double blackHoleMass = 22500;
    private double blackHoleIntensity = 2.5;

    private List<Object3D> objects;
    private EnhancedLightSource lightSource;
    private OptimizedParticleSystem particleSystem;
    private List<Point> stars;

    private double time = 0;
    private int frameCount = 0;
    private boolean bigBangFlash = false;
    private double flashIntensity = 0;
    private double supernovaDelay = 0;

    private BresenhamLine bresenham;
    private Midpoint midpoint;
    private BezierCurve bezierCurve;

    private int particleLimit = 20;

    private BufferedImage[] interpolatedFrames;
    private int currentFrameIndex = 1;
    private int interpolationSteps = 9;
    private final Map<Integer, Integer> frameDelays = new HashMap<>();
    private final int defaultDelay = 5;

    public Assignment1_66050265_66050210() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        offscreen = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g2d = offscreen.createGraphics();

        camera = new Camera();
        screenShake = new ScreenShake();
        bresenham = new BresenhamLine();
        midpoint = new Midpoint();
        bezierCurve = new BezierCurve();

        initializeAnimationFrames();
        initializeSimulation();

        timer = new Timer(timerdelay, this);
        timer.start();
    }

    private void initializeSimulation() {
        objects = new ArrayList<>();
        particleSystem = new OptimizedParticleSystem(particleLimit);

        int[] objectSizes = { 100, 50, 70, 175, 62, 60, 28, 42 };

        for (int i = 0; i < 8; i++) {
            double angle = (i * Math.PI * 2) / 8;
            double distance = 250 + Math.random() * 350;

            Vector3D position = new Vector3D(
                    Math.cos(angle) * distance,
                    Math.sin(angle) * distance,
                    (Math.random() - 0.5) * 150);

            Object3D obj;
            double size = objectSizes[i];

            switch (i % 3) {
                case 0:
                    obj = new Cube(position, size);
                    break;
                case 1:
                    obj = new Sphere(position, size * 0.8);
                    break;
                default:
                    obj = new Prism(position, size * 0.9);
                    break;
            }

            Vector3D velocity = new Vector3D(-Math.sin(angle) * 2.5, Math.cos(angle) * 2.5, 0);
            obj.setVelocity(velocity);
            objects.add(obj);
        }

        lightSource = new EnhancedLightSource(new Vector3D(400, -250, 120));

        stars = new ArrayList<>();
        for (int i = 0; i < 800; i++) {
            stars.add(new Point((int) (Math.random() * WIDTH), (int) (Math.random() * HEIGHT)));
        }

        currentPhase = Phase.ORBITING;
        time = 0;
        frameCount = 0;
        supernovaDelay = 0;
        camera.reset();
        screenShake.stop();
        currentFrameIndex = 0;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
        frameCount++;
        time += 0.016;
    }

    private void update() {
        if (currentPhase != Phase.FRAME_ANIMATION) {
            camera.update(time);
        }

        screenShake.update();

        switch (currentPhase) {
            case ORBITING:
                updateOrbiting();
                break;
            case CONSUMING:
                updateConsuming();
                break;
            case SUPERNOVA:
                updateSupernova();
                break;
            case FRAME_ANIMATION:
                time = 0;
                blackHoleIntensity = 2.0;
                updateFrameAnimation();
                break;
            case RESETTING:
                if (time > 0.01) {
                    initializeSimulation();
                    camera.reset();
                }
                break;
        }

        if (currentPhase != Phase.FRAME_ANIMATION) {
            lightSource.update(time, blackHoleIntensity);
            particleSystem.update();
        }
    }

    private void updateFrameAnimation() {
        currentFrameIndex = (currentFrameIndex + 1) % interpolatedFrames.length;
        int keyFrameIndex = (currentFrameIndex / (interpolationSteps + 1)) + 1;
        int delay = frameDelays.getOrDefault(keyFrameIndex, defaultDelay);
        timer.setDelay(delay);

        if (currentFrameIndex == 0 && frameCount > 21) {
            currentPhase = Phase.RESETTING;
            time = 0;
            timer.setDelay(timerdelay);
        }
    }

    private void initializeAnimationFrames() {
        frameDelays.put(9, 20);
        frameDelays.put(10, 15);
        frameDelays.put(11, 10);
        frameDelays.put(12, 20);
        frameDelays.put(15, 15);
        frameDelays.put(17, 15);
        frameDelays.put(20, 20);

        FrameIndex fi = new FrameIndex();
        BufferedImage[] originalFrames = fi.render();
        interpolatedFrames = Interpolation.generateInterpolatedFrames(originalFrames, interpolationSteps);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentPhase == Phase.FRAME_ANIMATION) {
            paintFrameAnimation(g);
        } else {
            paintBlackHoleSimulation(g);
        }
    }

    private void paintFrameAnimation(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (interpolatedFrames != null && currentFrameIndex < interpolatedFrames.length) {
            BufferedImage currentFrame = interpolatedFrames[currentFrameIndex];
            if (currentFrame != null) {
                g2.drawImage(currentFrame, 0, 0, null);
            }
        }
        g2.dispose();
    }

    private void updateOrbiting() {
        Iterator<Object3D> iterator = objects.iterator();
        while (iterator.hasNext()) {
            Object3D obj = iterator.next();

            Vector3D toBlackHole = new Vector3D(
                    -obj.getPosition().x,
                    -obj.getPosition().y,
                    -obj.getPosition().z);

            double distance = toBlackHole.magnitude();

            if (!obj.beingAbsorbed && distance < blackHoleRadius) {
                obj.startAbsorption();
            }

            if (obj.beingAbsorbed && obj.shrinkFactor < 0.15) {
                particleSystem.addMiniExplosion(obj.getPosition(), 5);
                screenShake.addShake(0.5, 0.3);
                blackHoleIntensity += 0.2;
                if (obj.beingAbsorbed && obj.shrinkFactor < 0.10) {
                    iterator.remove();
                    continue;
                }
            }

            double force = blackHoleMass / (distance * distance + 100);
            toBlackHole.normalize();
            toBlackHole.multiply(force * 0.8);
            obj.getVelocity().add(toBlackHole);

            if (distance < 200) {
                double spaghettiStrength = (200 - distance) / 200.0;
                obj.addSpaghettification(0.04 * spaghettiStrength);
            }

            obj.update();
        }

        if (objects.isEmpty()) {
            currentPhase = Phase.SUPERNOVA;
            supernovaDelay = 0;
            bigBangFlash = true;
            flashIntensity = 1.0;
            screenShake.addShake(2.0, 1.5);
        }
    }

    private void updateConsuming() {
        updateOrbiting();
    }

    private void updateSupernova() {
        supernovaDelay += 0.016;

        if (supernovaDelay < 1) {
            if (frameCount % 2 == 0) {
                particleSystem.addBigBangExplosion(new Vector3D(0, 0, 0), 15);
            }

            if (bigBangFlash) {
                flashIntensity = Math.max(0, flashIntensity - 0.03);
                if (flashIntensity <= 0) {
                    bigBangFlash = false;
                }
            }
            if (supernovaDelay >= 0.5)
                flashIntensity = Math.max(0, flashIntensity + 0.06);
        } else {
            if (supernovaDelay >= 1) {
                camera.reset();
                flashIntensity = Math.min(0, flashIntensity - 0.1);

                if (interpolatedFrames != null && interpolatedFrames.length >= 0) {
                    currentPhase = Phase.FRAME_ANIMATION;
                    time = 0;
                    screenShake.stop();
                    if (frameCount % 2 == 0) {
                        screenShake.addShake(2.0, 1.5);
                        screenShake.stop();
                    }

                    int initialDelay = frameDelays.getOrDefault(1, defaultDelay);
                    timer.setDelay(initialDelay);
                } else {
                    currentPhase = Phase.RESETTING;
                    time = 0;
                }
            }
        }
    }

    private void paintBlackHoleSimulation(Graphics g) {
        BufferedImage blackBg = new BufferedImage(WIDTH + 100, HEIGHT + 100, BufferedImage.TYPE_INT_ARGB);
        Midpoint.plotRect(blackBg, 0, 0, blackBg.getWidth(), blackBg.getHeight(), Color.BLACK);

        Graphics2D shakeG2d = (Graphics2D) g2d.create();
        Point shakeOffset = screenShake.getOffset();
        shakeG2d.translate(shakeOffset.x, shakeOffset.y);

        shakeG2d.drawImage(blackBg, -50, -50, null);

        Point cameraOffset = camera.getOffset();
        shakeG2d.translate(cameraOffset.x, cameraOffset.y);

        drawEnhancedStarfield(shakeG2d);
        drawGravitationalLensing(shakeG2d);
        drawEnhancedAccretionDisk(shakeG2d);
        drawRealisticBlackHole(shakeG2d);

        objects.sort((a, b) -> Double.compare(b.getPosition().z, a.getPosition().z));

        for (Object3D obj : objects) {
            obj.draw(shakeG2d, CENTER_X, CENTER_Y, bresenham, midpoint, lightSource);
        }

        lightSource.draw(shakeG2d, CENTER_X, CENTER_Y, midpoint, bresenham);
        particleSystem.draw(shakeG2d, CENTER_X, CENTER_Y, midpoint, bresenham);

        shakeG2d.dispose();

        if (bigBangFlash) {
            Graphics2D flashG = (Graphics2D) g2d.create();
            flashG.setColor(new Color(255, 255, 255, (int) (255 * flashIntensity)));
            flashG.fillRect(0, 0, WIDTH, HEIGHT);
            flashG.dispose();
        }

        g.drawImage(offscreen, 0, 0, null);
    }

    private void drawEnhancedStarfield(Graphics2D g) {
        for (int i = 0; i < stars.size(); i++) {
            Point star = stars.get(i);
            double twinkle = Math.sin(time * 3 + i * 0.1) * 0.5 + 0.5;
            int brightness = (int) (100 + 155 * twinkle);
            Color starColor = new Color(brightness, brightness, brightness);
            int size = (i % 10 == 0) ? 2 : 1;
            Midpoint.plotEllipse(g, star.x - size / 2, star.y - size / 2, size, size, starColor);
        }
    }

    private void drawGravitationalLensing(Graphics2D g) {
        for (int ring = 1; ring <= 4; ring++) {
            int radius = blackHoleRadius + ring * 25;
            for (int i = 0; i < 16; i++) {
                double angle = (i * Math.PI * 2 / 16) + time * 0.5;
                Point p1 = new Point(
                        (int) (CENTER_X + Math.cos(angle) * radius),
                        (int) (CENTER_Y + Math.sin(angle) * radius));
                Point p2 = new Point(
                        (int) (CENTER_X + Math.cos(angle + 0.3) * (radius + 15)),
                        (int) (CENTER_Y + Math.sin(angle + 0.3) * (radius + 15)));
                Point p3 = new Point(
                        (int) (CENTER_X + Math.cos(angle + 0.6) * (radius + 10)),
                        (int) (CENTER_Y + Math.sin(angle + 0.6) * (radius + 10)));
                Point p4 = new Point(
                        (int) (CENTER_X + Math.cos(angle + 1.0) * radius),
                        (int) (CENTER_Y + Math.sin(angle + 1.0) * radius));
                Color lensColor = new Color(150, 200, 255, 20);
                bezierCurve.drawCubicBezier(g, p1, p2, p3, p4, lensColor);
            }
        }
    }

    private void drawEnhancedAccretionDisk(Graphics2D g) {
        for (int layer = 0; layer < 3; layer++) {
            for (int i = 0; i < 12; i++) {
                double angle = (time * (2 + layer * 0.5) + i * Math.PI / 6) % (Math.PI * 2);
                double radius1 = 90 + layer * 20 + i * 8;
                double radius2 = 100 + layer * 20 + i * 8;
                Point p1 = new Point(
                        (int) (CENTER_X + Math.cos(angle) * radius1),
                        (int) (CENTER_Y + Math.sin(angle) * radius1 * 0.3));
                Point p2 = new Point(
                        (int) (CENTER_X + Math.cos(angle + 0.8) * (radius1 + radius2) / 2),
                        (int) (CENTER_Y + Math.sin(angle + 0.8) * (radius1 + radius2) / 2 * 0.3));
                Point p3 = new Point(
                        (int) (CENTER_X + Math.cos(angle + 1.2) * (radius1 + radius2) / 2),
                        (int) (CENTER_Y + Math.sin(angle + 1.2) * (radius1 + radius2) / 2 * 0.3));
                Point p4 = new Point(
                        (int) (CENTER_X + Math.cos(angle + 2.0) * radius2),
                        (int) (CENTER_Y + Math.sin(angle + 2.0) * radius2 * 0.3));
                int red = 255;
                int green = (int) (128 + 127 * Math.sin(time * 4 + i + layer));
                int blue = layer * 50;
                Color diskColor = new Color(red, green, blue, 80 - layer * 20);
                bezierCurve.drawCubicBezier(g, p1, p2, p3, p4, diskColor);
            }
        }
    }

    private void drawRealisticBlackHole(Graphics2D g) {
        for (int r = blackHoleRadius; r > 0; r--) {
            double intensity = (double) r / blackHoleRadius;
            Color horizonColor = new Color(
                    (int) (80 * intensity),
                    0,
                    (int) (120 * intensity));
            Midpoint.plotEllipse(g, CENTER_X - r, CENTER_Y - r, r * 2, r * 2, horizonColor);
        }

        for (int ring = 1; ring <= 5; ring++) {
            int radius = blackHoleRadius + ring * 12;
            double alpha = (6 - ring) * 15;
            int alphaClamped = (int) Math.max(0, Math.min(255, alpha));
            Color ringColor = new Color(200, 100, 255, alphaClamped);
            Midpoint.plotEllipse(g, CENTER_X - radius, CENTER_Y - radius, radius * 2, radius * 2, ringColor);
        }

        if (frameCount % 5 == 0) {
            for (int i = 0; i < 8; i++) {
                double angle = Math.random() * Math.PI * 2;
                int x = (int) (CENTER_X + Math.cos(angle) * (blackHoleRadius + 5));
                int y = (int) (CENTER_Y + Math.sin(angle) * (blackHoleRadius + 5));
                Color radiation = new Color(255, 255, 255, 100);
                Midpoint.plotEllipse(g, x - 2, y - 2, 4, 4, radiation);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_R:
                initializeSimulation();
                break;
            case KeyEvent.VK_SPACE:
                if (timer.isRunning()) {
                    timer.stop();
                } else {
                    timer.start();
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Blackhole-Supernova Reborn as CELESTIAL GOD OF CG");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Assignment1_66050265_66050210 simulation = new Assignment1_66050265_66050210();
            frame.add(simulation);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
            simulation.requestFocus();
        });
    }
}

class Camera {
    private Vector3D position;
    private Vector3D target;
    private double orbitRadius = 50;
    private double orbitSpeed = 1.3;

    public Camera() {
        position = new Vector3D(0, 0, 0);
        target = new Vector3D(0, 0, 0);
    }

    public void update(double time) {
        double angle = time * orbitSpeed;
        target.x = Math.cos(angle) * orbitRadius;
        target.y = Math.sin(angle * 0.7) * orbitRadius * 0.5;
        target.z = Math.sin(angle * 0.5) * 30;

        position.x += (target.x - position.x) * 0.02;
        position.y += (target.y - position.y) * 0.02;
        position.z += (target.z - position.z) * 0.02;
    }

    public Point getOffset() {
        return new Point((int) position.x, (int) position.y);
    }

    public void reset() {
        position = new Vector3D(0, 0, 0);
        target = new Vector3D(0, 0, 0);
    }
}

class ScreenShake {
    private double intensity;
    private double duration;
    private double elapsed;
    private Random random = new Random();

    public void addShake(double intensity, double duration) {
        this.intensity = Math.max(this.intensity, intensity);
        this.duration = Math.max(this.duration, duration);
        this.elapsed = 0;
    }

    public void update() {
        if (elapsed < duration) {
            elapsed += 0.016;
        }

        if (elapsed >= duration) {
            intensity = 0;
            duration = 0;
        }
    }

    public Point getOffset() {
        if (intensity <= 0)
            return new Point(0, 0);

        double currentIntensity = intensity * (1 - elapsed / duration);
        int x = (int) ((random.nextDouble() - 0.5) * currentIntensity * 20);
        int y = (int) ((random.nextDouble() - 0.5) * currentIntensity * 20);

        return new Point(x, y);
    }

    public void stop() {
        intensity = 0;
        duration = 0;
        elapsed = 0;
    }
}
