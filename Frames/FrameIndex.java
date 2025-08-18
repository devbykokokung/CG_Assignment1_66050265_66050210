package Frames;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

public class FrameIndex {
    int WIDTH = 600;
    int HEIGHT = 600;
    int totalFrames = 21;

    public FrameIndex(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
    }

    public FrameIndex() {
        this(600, 600);
    }

    public BufferedImage[] render() {
        BufferedImage bufferedImages[] = new BufferedImage[totalFrames];

        bufferedImages[0] = renderFrameToImage(Frame1.class);
        bufferedImages[1] = renderFrameToImage(Frame2.class);
        bufferedImages[2] = renderFrameToImage(Frame3.class);
        bufferedImages[3] = renderFrameToImage(Frame4.class);
        bufferedImages[4] = renderFrameToImage(Frame5.class);
        bufferedImages[5] = renderFrameToImage(Frame6.class);
        bufferedImages[6] = renderFrameToImage(Frame7.class);
        bufferedImages[7] = renderFrameToImage(Frame8.class);
        bufferedImages[8] = renderFrameToImage(Frame9.class);
        bufferedImages[9] = renderFrameToImage(Frame10.class);
        bufferedImages[10] = renderFrameToImage(Frame11.class);
        bufferedImages[11] = renderFrameToImage(Frame12.class);
        bufferedImages[12] = renderFrameToImage(Frame13.class);
        bufferedImages[13] = renderFrameToImage(Frame14.class);
        bufferedImages[14] = renderFrameToImage(Frame15.class);
        bufferedImages[15] = renderFrameToImage(Frame16.class);
        bufferedImages[16] = renderFrameToImage(Frame17.class);
        bufferedImages[17] = renderFrameToImage(Frame18.class);
        bufferedImages[18] = renderFrameToImage(Frame19.class);
        bufferedImages[19] = renderFrameToImage(Frame20.class);
        bufferedImages[20] = renderFrameToImage(Frame21.class);

        return bufferedImages;
    }

    private BufferedImage renderFrameToImage(Class<?> frameClass) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        try {
            Method drawMethod = frameClass.getDeclaredMethod("drawFrame", Graphics2D.class);
            drawMethod.invoke(null, g2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            g2.dispose();
        }

        return img;
    }
}
