package arcana.model.monster;

import java.awt.Rectangle;

import arcana.model.sprite.SpriteRegistry;

public class Slime extends Monster {
    private static final long RESPAWN_DELAY_MILLIS = 30000;

    public Slime(int x, int y, int width, int height) {
        super("Slime", x, y, width, height, 100, 15, 35, 25, new Rectangle(420, 230, 260, 210),
                SpriteRegistry.SLIME);
        configureBehavior();
    }

    public Slime(int x, int y, int width, int height, Rectangle patrolArea) {
        super("Slime", x, y, width, height, 100, 15, 35, 25, patrolArea, SpriteRegistry.SLIME);
        configureBehavior();
    }

    private void configureBehavior() {
        setRoamSpeed(1.5);
        setChaseSpeed(2.2);
        setDetectionRange(130);
        setBattleTriggerRange(14);
        setRespawnDelayMillis(RESPAWN_DELAY_MILLIS);
    }
}
