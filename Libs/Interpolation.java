package Libs;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Interpolation {

    /**
     * Blend two images with given alpha, avoiding white flashes.
     * @param img1 First image
     * @param img2 Second image
     * @param alpha Blend factor [0.0 - 1.0]
     * @return Blended BufferedImage
     */
    public static BufferedImage blend(BufferedImage img1, BufferedImage img2, float alpha) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            throw new IllegalArgumentException("Images must be the same size for blending.");
        }

        BufferedImage blended = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = blended.createGraphics();

        // Clear to fully transparent before drawing
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, blended.getWidth(), blended.getHeight());

        // Draw img1 fully opaque --- I used to do image 1 with alpha but result to make
        // the screen flashing due to background color
        g2.setComposite(AlphaComposite.SrcOver);
        g2.drawImage(img1, 0, 0, null);

        // Draw img2 with alpha
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2.drawImage(img2, 0, 0, null);

        g2.dispose();
        return blended;
    }

    /**
     * Generate interpolated frames between originals with smooth blending.
     * @param originalFrames Array of key frames
     * @param stepsPerGap Number of frames between each key frame
     * @return Array including key and interpolated frames
     */
    public static BufferedImage[] generateInterpolatedFrames(BufferedImage[] originalFrames, int stepsPerGap) {
        if (originalFrames.length < 2) return originalFrames;

        int totalFrames = (originalFrames.length - 1) * (stepsPerGap + 1) + 1;
        BufferedImage[] result = new BufferedImage[totalFrames];

        int index = 0;
        for (int i = 0; i < originalFrames.length - 1; i++) {
            result[index++] = originalFrames[i];
            for (int s = 1; s <= stepsPerGap; s++) {
                float alpha = (float) s / (stepsPerGap + 1);
                result[index++] = blend(originalFrames[i], originalFrames[i + 1], alpha);
            }
        }
        result[index] = originalFrames[originalFrames.length - 1];
        return result;
    }
}
