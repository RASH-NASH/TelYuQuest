package arcana.model.map;

import java.awt.Rectangle;

public class MapObstacle {
    private String name;
    private Rectangle bounds;
    private boolean solid;

    public MapObstacle(String name, int x, int y, int width, int height, boolean solid) {
        this.name = name;
        this.bounds = new Rectangle(x, y, width, height);
        this.solid = solid;
    }

    public String getName() {
        return name;
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean intersects(Rectangle actorBounds) {
        return bounds.intersects(actorBounds);
    }
}
