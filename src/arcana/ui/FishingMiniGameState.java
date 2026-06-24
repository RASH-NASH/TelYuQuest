package arcana.ui;

import java.util.Random;

public class FishingMiniGameState {
    private final int barWidth;
    private final int greenZoneWidth;
    private final int arrowSpeed;

    private boolean visible;
    private boolean pullAnimationActive;
    private int arrowPosition;
    private int arrowDirection;
    private int greenZoneStart;
    private int animationFrame;
    private int animationTick;

    public FishingMiniGameState(int barWidth, int greenZoneWidth, int arrowSpeed) {
        this.barWidth = barWidth;
        this.greenZoneWidth = greenZoneWidth;
        this.arrowSpeed = arrowSpeed;
        reset();
    }

    public void start(Random random) {
        visible = true;
        pullAnimationActive = false;
        arrowPosition = 0;
        arrowDirection = 1;
        greenZoneStart = random.nextInt(Math.max(1, barWidth - greenZoneWidth + 1));
        animationFrame = 0;
        animationTick = 0;
    }

    public boolean finishTimingAttempt() {
        boolean success = arrowPosition >= greenZoneStart && arrowPosition <= greenZoneStart + greenZoneWidth;
        visible = false;
        if (!success) {
            return false;
        }

        visible = true;
        pullAnimationActive = true;
        animationFrame = 0;
        animationTick = 0;
        return true;
    }

    public void cancel() {
        reset();
    }

    public boolean update(int castingFrameCount, int pullFrameCount) {
        if (!visible) {
            return false;
        }

        updateAnimationFrame(castingFrameCount, pullFrameCount);
        if (pullAnimationActive) {
            return animationFrame >= Math.max(0, pullFrameCount - 1);
        }

        arrowPosition += arrowDirection * arrowSpeed;
        if (arrowPosition <= 0) {
            arrowPosition = 0;
            arrowDirection = 1;
        } else if (arrowPosition >= barWidth) {
            arrowPosition = barWidth;
            arrowDirection = -1;
        }

        return false;
    }

    private void updateAnimationFrame(int castingFrameCount, int pullFrameCount) {
        int frameCount = pullAnimationActive ? pullFrameCount : castingFrameCount;
        if (frameCount <= 1) {
            animationFrame = 0;
            return;
        }

        animationTick++;
        if (animationTick < 3) {
            return;
        }

        animationTick = 0;
        if (pullAnimationActive) {
            animationFrame = Math.min(animationFrame + 1, frameCount - 1);
        } else {
            animationFrame = (animationFrame + 1) % frameCount;
        }
    }

    private void reset() {
        visible = false;
        pullAnimationActive = false;
        arrowPosition = 0;
        arrowDirection = 1;
        greenZoneStart = 0;
        animationFrame = 0;
        animationTick = 0;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isPullAnimationActive() {
        return pullAnimationActive;
    }

    public int getArrowPosition() {
        return arrowPosition;
    }

    public int getGreenZoneStart() {
        return greenZoneStart;
    }

    public int getAnimationFrame() {
        return animationFrame;
    }
}
