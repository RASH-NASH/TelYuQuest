package arcana.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class SpriteLoader {
    private SpriteLoader() {
    }

    public static BufferedImage loadImage(String resourcePath, Color fallbackColor, int width, int height) {
        if (resourcePath != null && !resourcePath.trim().isEmpty()) {
            try {
                URL resource = SpriteLoader.class.getClassLoader().getResource(resourcePath);
                if (resource != null) {
                    return ImageIO.read(resource);
                }
            } catch (IOException e) {
                System.out.println("Sprite gagal dibaca: " + resourcePath);
            }
        }

        return createPlaceholder(fallbackColor, width, height);
    }

    public static BufferedImage[] loadAnimation(String resourcePath, int totalFrames, Color fallbackColor, int width,
            int height) {
        BufferedImage sheet = loadImage(resourcePath, fallbackColor, width, height);

        if (totalFrames <= 1 || sheet.getWidth() < totalFrames) {
            return new BufferedImage[] { sheet };
        }

        BufferedImage[] frames = new BufferedImage[totalFrames];
        int frameHeight = sheet.getHeight();

        for (int i = 0; i < totalFrames; i++) {
            int startX = Math.round((float) (i * sheet.getWidth()) / totalFrames);
            int endX = Math.round((float) ((i + 1) * sheet.getWidth()) / totalFrames);
            int frameWidth = Math.max(1, endX - startX);
            frames[i] = sheet.getSubimage(startX, 0, frameWidth, frameHeight);
        }

        return frames;
    }

    private static BufferedImage createPlaceholder(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRoundRect(6, 6, width - 12, height - 12, 16, 16);
        g.setColor(Color.WHITE);
        g.drawRoundRect(6, 6, width - 12, height - 12, 16, 16);
        g.dispose();
        return image;
    }
}
