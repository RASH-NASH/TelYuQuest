package arcana.model.map;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Area {
    private String areaName;
    private String description;
    private String backgroundAssetPath;
    private Rectangle bounds;
    private Rectangle defaultSpawnBounds;
    private ArrayList<Door> doors;
    private ArrayList<MapObstacle> obstacles;
    private ArrayList<InteractionZone> interactionZones;

    public Area(String areaName, String description, String backgroundAssetPath, int width, int height) {
        this(areaName, description, backgroundAssetPath, new Rectangle(0, 0, width, height),
                new Rectangle(0, 0, 1, 1));
    }

    public Area(String areaName, String description, String backgroundAssetPath, Rectangle bounds,
            Rectangle defaultSpawnBounds) {
        this.areaName = areaName;
        this.description = description;
        this.backgroundAssetPath = backgroundAssetPath;
        this.bounds = new Rectangle(bounds);
        this.defaultSpawnBounds = new Rectangle(defaultSpawnBounds);
        this.doors = new ArrayList<Door>();
        this.obstacles = new ArrayList<MapObstacle>();
        this.interactionZones = new ArrayList<InteractionZone>();
    }

    public void addDoor(Door door) {
        doors.add(door);
    }

    public void addObstacle(MapObstacle obstacle) {
        obstacles.add(obstacle);
    }

    public void addInteractionZone(InteractionZone interactionZone) {
        interactionZones.add(interactionZone);
    }

    public InteractionZone findInteractionZone(InteractionType interactionType, Rectangle actorBounds) {
        for (InteractionZone interactionZone : interactionZones) {
            if (interactionZone.getInteractionType() == interactionType && interactionZone.contains(actorBounds)) {
                return interactionZone;
            }
        }
        return null;
    }

    public String getAreaName() {
        return areaName;
    }

    public String getDescription() {
        return description;
    }

    public String getBackgroundAssetPath() {
        return backgroundAssetPath;
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public Rectangle getDefaultSpawnBounds() {
        return new Rectangle(defaultSpawnBounds);
    }

    public List<Door> getDoors() {
        return Collections.unmodifiableList(doors);
    }

    public List<MapObstacle> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    public List<InteractionZone> getInteractionZones() {
        return Collections.unmodifiableList(interactionZones);
    }
}
