package arcana.model.monster;

import java.awt.Rectangle;

import arcana.model.sprite.SpriteRegistry;

public class ArcaneWisp extends Monster {
    private static final long RESPAWN_DELAY_MILLIS = 40000;

    public ArcaneWisp(int x, int y, int width, int height, Rectangle patrolArea) {
        super("Arcane Wisp", x, y, width, height, 80, 20, 45, 30, patrolArea, SpriteRegistry.ARCANE_WISP);
        configureBehavior();
    }

    private void configureBehavior() {
        setStationary(true);
        setRoamSpeed(1.8);
        setChaseSpeed(2.8);
        setDetectionRange(165);
        setBattleTriggerRange(18);
        setChaseLeash(96);
        setRespawnDelayMillis(RESPAWN_DELAY_MILLIS);
    }
}
