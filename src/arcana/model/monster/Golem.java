package arcana.model.monster;

import java.awt.Rectangle;

import arcana.model.sprite.SpriteRegistry;

public class Golem extends Monster {
    private static final long RESPAWN_DELAY_MILLIS = 60000;

    public Golem(int x, int y, int width, int height, Rectangle patrolArea) {
        super("Golem", x, y, width, height, 160, 24, 65, 45, patrolArea, SpriteRegistry.GOLEM);
        configureBehavior();
    }

    private void configureBehavior() {
        setStationary(true);
        setRoamSpeed(1.1);
        setChaseSpeed(1.7);
        setDetectionRange(150);
        setBattleTriggerRange(20);
        setPatrolReachedThreshold(8);
        setRespawnDelayMillis(RESPAWN_DELAY_MILLIS);
    }
}
