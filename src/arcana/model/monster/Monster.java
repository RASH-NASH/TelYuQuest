package arcana.model.monster;

import java.awt.Rectangle;
import java.util.Random;

import arcana.model.sprite.SpriteConfig;

public abstract class Monster {
    private static final long DEFAULT_RESPAWN_DELAY_MILLIS = 120000;
    private static final int DEFAULT_DETECTION_RANGE = 140;
    private static final int DEFAULT_BATTLE_TRIGGER_RANGE = 18;
    private static final int DEFAULT_PATROL_REACHED_THRESHOLD = 6;
    private static final int DEFAULT_CHASE_LEASH = 72;
    private static final double DEFAULT_ROAM_SPEED = 1.6;
    private static final double DEFAULT_CHASE_SPEED = 2.4;

    private String name;
    private int x;
    private int y;
    private int width;
    private int height;
    private int hp;
    private int maxHp;
    private int attackPower;
    private int expReward;
    private int goldReward;
    private int patrolLeft;
    private int patrolTop;
    private int patrolRight;
    private int patrolBottom;
    private int spawnX;
    private int spawnY;
    private double preciseX;
    private double preciseY;
    private long defeatedAtMillis;
    private long respawnDelayMillis;
    private int detectionRange;
    private int battleTriggerRange;
    private int patrolReachedThreshold;
    private int chaseLeash;
    private double roamSpeed;
    private double chaseSpeed;
    private int patrolTargetX;
    private int patrolTargetY;
    private boolean chasingPlayer;
    private boolean stationary;
    private MonsterAction currentAction;
    private SpriteConfig spriteConfig;
    private Random random;

    public Monster(String name, int x, int y, int width, int height, int maxHp, int attackPower, int expReward,
            int goldReward, Rectangle patrolArea, SpriteConfig spriteConfig) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.preciseX = x;
        this.preciseY = y;
        this.spawnX = x;
        this.spawnY = y;
        this.width = width;
        this.height = height;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attackPower = attackPower;
        this.expReward = expReward;
        this.goldReward = goldReward;
        this.spriteConfig = spriteConfig;
        this.random = new Random();
        this.currentAction = MonsterAction.IDLE;
        this.defeatedAtMillis = -1L;
        this.respawnDelayMillis = DEFAULT_RESPAWN_DELAY_MILLIS;
        this.detectionRange = DEFAULT_DETECTION_RANGE;
        this.battleTriggerRange = DEFAULT_BATTLE_TRIGGER_RANGE;
        this.patrolReachedThreshold = DEFAULT_PATROL_REACHED_THRESHOLD;
        this.chaseLeash = DEFAULT_CHASE_LEASH;
        this.roamSpeed = DEFAULT_ROAM_SPEED;
        this.chaseSpeed = DEFAULT_CHASE_SPEED;
        this.chasingPlayer = false;
        this.stationary = false;
        setPatrolArea(patrolArea);
        chooseNewPatrolTarget();
    }

    public void updateMovement(Rectangle mapBounds, Rectangle playerBounds) {
        if (isDefeated()) {
            currentAction = MonsterAction.DEFEATED;
            tryRespawn();
            return;
        }

        if (stationary) {
            currentAction = MonsterAction.IDLE;
            chasingPlayer = false;
            return;
        }

        Rectangle movementBounds = getMovementBounds(mapBounds);
        Rectangle leashBounds = expandBounds(movementBounds, chaseLeash);
        boolean playerDetected = playerBounds != null && canDetectPlayer(playerBounds, leashBounds);

        if (playerDetected) {
            chasingPlayer = true;
            moveTowards(playerBounds.x + playerBounds.width / 2 - width / 2,
                    playerBounds.y + playerBounds.height / 2 - height / 2, chaseSpeed, leashBounds);
            currentAction = MonsterAction.CHASE;
            return;
        }

        if (chasingPlayer && !isInsideBounds(x, y, movementBounds)) {
            moveTowards(patrolTargetX, patrolTargetY, chaseSpeed, leashBounds);
            currentAction = MonsterAction.CHASE;
            if (hasReachedTarget(patrolTargetX, patrolTargetY, patrolReachedThreshold)) {
                chasingPlayer = false;
                chooseNewPatrolTarget();
            }
            return;
        }

        chasingPlayer = false;
        patrol(movementBounds);
    }

    public boolean hasDetectedPlayer(int playerX, int playerY, int playerWidth, int playerHeight) {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        Rectangle detectionArea = expandBounds(getBounds(), detectionRange);
        return detectionArea.intersects(playerRect);
    }

    public boolean canStartBattle(int playerX, int playerY, int playerWidth, int playerHeight) {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        Rectangle battleArea = expandBounds(getBounds(), battleTriggerRange);
        return battleArea.intersects(playerRect);
    }

    public int takeDamage(int damage) {
        int finalDamage = damage;

        if (currentAction == MonsterAction.BLOCK) {
            finalDamage = Math.max(1, damage / 2);
        } else if (currentAction == MonsterAction.DEFEND) {
            finalDamage = Math.max(1, damage - 10);
        }

        hp -= finalDamage;
        if (hp < 0) {
            hp = 0;
        }

        currentAction = isDefeated() ? MonsterAction.DEFEATED : MonsterAction.HURT;
        if (isDefeated()) {
            defeatedAtMillis = System.currentTimeMillis();
            chasingPlayer = false;
        }
        return finalDamage;
    }

    public MonsterAction chooseBattleAction() {
        int roll = random.nextInt(100);

        if (roll < 65) {
            currentAction = MonsterAction.ATTACK;
        } else if (roll < 82) {
            currentAction = MonsterAction.BLOCK;
        } else {
            currentAction = MonsterAction.DEFEND;
        }

        return currentAction;
    }

    public void resetBattleAction() {
        if (!isDefeated()) {
            currentAction = chasingPlayer ? MonsterAction.CHASE : MonsterAction.IDLE;
        }
    }

    public boolean isDefeated() {
        return hp <= 0;
    }

    public boolean isWaitingRespawn() {
        return isDefeated() && defeatedAtMillis > 0;
    }

    public long getRespawnSecondsRemaining() {
        if (!isWaitingRespawn()) {
            return 0;
        }

        long remaining = respawnDelayMillis - (System.currentTimeMillis() - defeatedAtMillis);
        return Math.max(0, (long) Math.ceil(remaining / 1000.0));
    }

    protected void setPatrolArea(Rectangle patrolArea) {
        patrolLeft = patrolArea.x;
        patrolTop = patrolArea.y;
        patrolRight = patrolArea.x + patrolArea.width;
        patrolBottom = patrolArea.y + patrolArea.height;
        patrolTargetX = clamp(spawnX, patrolLeft, Math.max(patrolLeft, patrolRight - width));
        patrolTargetY = clamp(spawnY, patrolTop, Math.max(patrolTop, patrolBottom - height));
    }

    protected void setRespawnDelayMillis(long respawnDelayMillis) {
        this.respawnDelayMillis = respawnDelayMillis;
    }

    protected void setDetectionRange(int detectionRange) {
        this.detectionRange = Math.max(battleTriggerRange + 1, detectionRange);
    }

    protected void setBattleTriggerRange(int battleTriggerRange) {
        this.battleTriggerRange = Math.max(1, battleTriggerRange);
        if (detectionRange <= this.battleTriggerRange) {
            detectionRange = this.battleTriggerRange + 1;
        }
    }

    protected void setPatrolReachedThreshold(int patrolReachedThreshold) {
        this.patrolReachedThreshold = Math.max(1, patrolReachedThreshold);
    }

    protected void setChaseLeash(int chaseLeash) {
        this.chaseLeash = Math.max(0, chaseLeash);
    }

    protected void setRoamSpeed(double roamSpeed) {
        this.roamSpeed = Math.max(0.4, roamSpeed);
    }

    protected void setChaseSpeed(double chaseSpeed) {
        this.chaseSpeed = Math.max(roamSpeed, chaseSpeed);
    }

    protected void setStationary(boolean stationary) {
        this.stationary = stationary;
        if (stationary) {
            chasingPlayer = false;
            currentAction = MonsterAction.IDLE;
        }
    }

    private void tryRespawn() {
        if (!isWaitingRespawn()) {
            return;
        }

        long elapsed = System.currentTimeMillis() - defeatedAtMillis;
        if (elapsed >= respawnDelayMillis) {
            hp = maxHp;
            x = spawnX;
            y = spawnY;
            preciseX = spawnX;
            preciseY = spawnY;
            defeatedAtMillis = -1L;
            chasingPlayer = false;
            currentAction = MonsterAction.IDLE;
            chooseNewPatrolTarget();
        }
    }

    private void patrol(Rectangle movementBounds) {
        if (hasReachedTarget(patrolTargetX, patrolTargetY, patrolReachedThreshold) || random.nextInt(90) == 0) {
            chooseNewPatrolTarget();
        }

        moveTowards(patrolTargetX, patrolTargetY, roamSpeed, movementBounds);
        currentAction = hasReachedTarget(patrolTargetX, patrolTargetY, patrolReachedThreshold)
                ? MonsterAction.IDLE
                : MonsterAction.MOVE;
    }

    private void chooseNewPatrolTarget() {
        int maxX = Math.max(patrolLeft, patrolRight - width);
        int maxY = Math.max(patrolTop, patrolBottom - height);
        patrolTargetX = random.nextInt(maxX - patrolLeft + 1) + patrolLeft;
        patrolTargetY = random.nextInt(maxY - patrolTop + 1) + patrolTop;
    }

    private void moveTowards(int targetX, int targetY, double speed, Rectangle allowedBounds) {
        double deltaX = targetX - preciseX;
        double deltaY = targetY - preciseY;
        double distance = Math.hypot(deltaX, deltaY);

        if (distance <= 0.001) {
            preciseX = clamp(preciseX, allowedBounds.x, allowedBounds.x + allowedBounds.width);
            preciseY = clamp(preciseY, allowedBounds.y, allowedBounds.y + allowedBounds.height);
            syncPosition();
            return;
        }

        double step = Math.min(speed, distance);
        preciseX += (deltaX / distance) * step;
        preciseY += (deltaY / distance) * step;
        preciseX = clamp(preciseX, allowedBounds.x, allowedBounds.x + allowedBounds.width);
        preciseY = clamp(preciseY, allowedBounds.y, allowedBounds.y + allowedBounds.height);
        syncPosition();
    }

    private void syncPosition() {
        x = (int) Math.round(preciseX);
        y = (int) Math.round(preciseY);
    }

    private Rectangle getMovementBounds(Rectangle mapBounds) {
        int minX = Math.max(mapBounds.x, patrolLeft);
        int minY = Math.max(mapBounds.y, patrolTop);
        int maxX = Math.min(mapBounds.width - width, patrolRight - width);
        int maxY = Math.min(mapBounds.height - height, patrolBottom - height);

        return new Rectangle(minX, minY, Math.max(0, maxX - minX), Math.max(0, maxY - minY));
    }

    private Rectangle expandBounds(Rectangle bounds, int amount) {
        return new Rectangle(bounds.x - amount, bounds.y - amount, bounds.width + amount * 2, bounds.height + amount * 2);
    }

    private boolean canDetectPlayer(Rectangle playerBounds, Rectangle leashBounds) {
        Rectangle detectionArea = expandBounds(getBounds(), detectionRange);
        return detectionArea.intersects(playerBounds) && leashBounds.intersects(playerBounds);
    }

    private Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    private boolean hasReachedTarget(int targetX, int targetY, int threshold) {
        return Math.abs(x - targetX) <= threshold && Math.abs(y - targetY) <= threshold;
    }

    private boolean isInsideBounds(int positionX, int positionY, Rectangle bounds) {
        return positionX >= bounds.x
                && positionX <= bounds.x + bounds.width
                && positionY >= bounds.y
                && positionY <= bounds.y + bounds.height;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public int getExpReward() {
        return expReward;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public MonsterAction getCurrentAction() {
        return currentAction;
    }

    public SpriteConfig getSpriteConfig() {
        return spriteConfig;
    }
}

