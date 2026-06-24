package arcana.model.map;

import java.awt.Rectangle;

public class Door {
    private Rectangle bounds;
    private String targetAreaName;
    private Rectangle targetSpawnBounds;

    public Door(int x, int y, int width, int height, String targetArea, int targetX, int targetY) {
        this(new Rectangle(x, y, width, height), targetArea, new Rectangle(targetX, targetY, 1, 1));
    }

    public Door(Rectangle bounds, String targetAreaName, Rectangle targetSpawnBounds) {
        this.bounds = new Rectangle(bounds);
        this.targetAreaName = targetAreaName;
        this.targetSpawnBounds = new Rectangle(targetSpawnBounds);
    }

    public boolean isPlayerEntering(int playerX, int playerY, int playerWidth, int playerHeight) {
        return isPlayerEntering(new Rectangle(playerX, playerY, playerWidth, playerHeight));
    }

    public boolean isPlayerEntering(Rectangle playerBounds) {
        return bounds.intersects(playerBounds);
    }

    public String getTargetArea() {
        return targetAreaName;
    }

    public int getTargetX() {
        return targetSpawnBounds.x;
    }

    public int getTargetY() {
        return targetSpawnBounds.y;
    }

    public Rectangle getTargetSpawnBounds() {
        return new Rectangle(targetSpawnBounds);
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }
}
