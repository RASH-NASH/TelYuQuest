package arcana.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import arcana.model.activity.Activity;
import arcana.model.activity.Fishing;
import arcana.model.activity.Quest;
import arcana.model.activity.QuestLog;
import arcana.model.activity.Quiz;
import arcana.model.activity.QuizQuestion;
import arcana.model.activity.QuizSession;
import arcana.model.character.Character;
import arcana.model.character.Skill;
import arcana.model.exception.CollisionBlockedException;
import arcana.model.exception.InteractionUnavailableException;
import arcana.model.exception.NotEnoughGoldException;
import arcana.model.exception.OutOfMapBoundsException;
import arcana.model.item.Fish;
import arcana.model.item.FishingRod;
import arcana.model.item.Grimoire;
import arcana.model.item.Item;
import arcana.model.map.Area;
import arcana.model.map.Door;
import arcana.model.map.GameMap;
import arcana.model.map.InteractionType;
import arcana.model.map.InteractionZone;
import arcana.model.map.MapObstacle;
import arcana.model.monster.ArcaneWisp;
import arcana.model.monster.Golem;
import arcana.model.monster.Monster;
import arcana.model.monster.MonsterAction;
import arcana.model.monster.Slime;
import arcana.model.shop.Shop;
import arcana.model.sprite.SpriteConfig;

public class GamePanel extends JPanel implements KeyListener, MouseListener {
    private static final int ENTITY_SIZE = 32;
    private static final int PLAYER_COLLISION_WIDTH = 14;
    private static final int PLAYER_COLLISION_HEIGHT = 10;
    private static final int PLAYER_COLLISION_OFFSET_X = 9;
    private static final int PLAYER_COLLISION_OFFSET_Y = 20;
    private static final int PLAYER_SPEED = 4;
    private static final double CAMERA_ZOOM = 1.35;
    private static final int GAME_TICK_MS = 50;
    private static final boolean SHOW_DOOR_HITBOX = false;
    private static final int PLAYER_ANIMATION_TICK_INTERVAL = 2;
    private static final int HOTBAR_SLOT_COUNT = 8;
    private static final int INVENTORY_SLOT_COUNT = 40;
    private static final int INVENTORY_COLUMNS = 8;
    private static final int INVENTORY_ASSET_WIDTH = 640;
    private static final int INVENTORY_ASSET_HEIGHT = 360;
    private static final int INVENTORY_ASSET_SLOT_X = 287;
    private static final int INVENTORY_ASSET_SLOT_Y = 74;
    private static final int INVENTORY_ASSET_SLOT_SIZE = 32;
    private static final int INVENTORY_ASSET_SLOT_GAP_X = 2;
    private static final int INVENTORY_ASSET_SLOT_GAP_Y = 7;
    private static final int FISHING_BAR_WIDTH = 220;
    private static final int FISHING_GREEN_ZONE_WIDTH = 48;
    private static final int FISHING_ANIMATION_TICK_MS = 16;
    private static final String FISHING_ROD_ICON_PATH = "assets/fishing/fishing-rod-icon.png";
    private static final String FISH_ICON_PATH = "assets/inventory/fish_icon_32.png";
    private static final String GRIMOIRE_EMBER_ICON_PATH = "assets/inventory/grimoire_ember_icon_32.png";
    private static final String GRIMOIRE_TIDE_ICON_PATH = "assets/inventory/grimoire_tide_icon_32.png";
    private static final String GRIMOIRE_ARCANE_ICON_PATH = "assets/inventory/grimoire_arcane_icon_32.png";
    private static final String FISHING_CASTING_ROD_PATH = "assets/fishing/casting_rod.png";
    private static final String FISHING_PULL_SOMETHING_PATH = "assets/fishing/pull_something.png";
    private static final int FISHING_CASTING_ROD_FRAME_COUNT = 14;
    private static final int FISHING_PULL_SOMETHING_FRAME_COUNT = 9;
    private static final String DEFAULT_AREA_BGM_PATH = "assets/music/yard_bgm.wav";
    private static final String MAIN_HALL_BGM_PATH = "assets/music/main_hall_bgm.wav";
    private static final String AREA_TRANSITION_SFX_PATH = "assets/sfx/area_transition.wav";
    private static final String UI_TOGGLE_SFX_PATH = "assets/sfx/ui_toggle.wav";
    private static final String SHOP_BUY_SFX_PATH = "assets/sfx/shop_buy.wav";
    private static final String SHOP_DENY_SFX_PATH = "assets/sfx/shop_deny.wav";
    private static final String INVENTORY_HOTBAR_ASSET_PATH = "assets/ui/inventory/hotbar.png";
    private static final String INVENTORY_PANEL_ASSET_PATH = "assets/inventory/Inventory.png";
    private static final String INVENTORY_SLOT_ASSET_PATH = "assets/ui/inventory/inventory_slot.png";
    private static final String SHOP_PANEL_ASSET_PATH = "assets/area/shop/shop_ui.png";
    private static final String SHOP_SLOT_ASSET_PATH = "assets/ui/shop/shop_slot.png";
    private static final String GRIMOIRE_PANEL_ASSET_PATH = "assets/ui/grimoire/grimoire_panel.png";
    private static final String GRIMOIRE_PAGE_ASSET_PATH = "assets/ui/grimoire/grimoire_page.png";
    private static final String QUIZ_PANEL_ASSET_PATH = "assets/ui/academy/quiz_panel.png";
    private static final String QUIZ_TIMER_ASSET_PATH = "assets/ui/academy/quiz_timer.png";
    private static final String SKILL_LOADOUT_PANEL_ASSET_PATH = "assets/ui/skills/skill_loadout_panel.png";
    private static final String SKILL_SLOT_ASSET_PATH = "assets/ui/skills/skill_slot.png";
    private static final String STATE_IDLE = "idle";
    private static final String STATE_WALK = "walk";
    private static final String STATE_ATTACK = "attack";

    private GameFrame frame;
    private Character player;
    private Shop shop;
    private GameMap gameMap;
    private ArrayList<Monster> monsters;
    private QuestLog questLog;
    private String activityMessage;
    private Random random;
    private FishingMiniGameState fishingState;
    private ShopOverlayState shopOverlayState;
    private InventoryOverlayLayout inventoryOverlayLayout;

    private BufferedImage areaBackground;
    private BufferedImage inventoryPanelImage;
    private BufferedImage shopPanelImage;
    private BufferedImage fishingRodIconImage;
    private BufferedImage fishIconImage;
    private BufferedImage grimoireEmberIconImage;
    private BufferedImage grimoireTideIconImage;
    private BufferedImage grimoireArcaneIconImage;
    private BufferedImage[] fishingCastingRodFrames;
    private BufferedImage[] fishingPullSomethingFrames;
    private BufferedImage[] idleFrames;
    private BufferedImage[] walkUpFrames;
    private BufferedImage[] walkDownFrames;
    private BufferedImage[] walkLeftFrames;
    private BufferedImage[] walkRightFrames;
    private BufferedImage[] attackFrames;
    private ArrayList<BufferedImage[]> monsterIdleFrames;
    private ArrayList<BufferedImage[]> monsterWalkFrames;
    private Clip areaBgmClip;
    private String currentAreaBgmPath;

    private int currentFrame;
    private int animationFrame;
    private int playerAnimationTick;
    private int cameraX;
    private int cameraY;
    private String playerState;
    private String direction;
    private boolean battleTriggered;
    private long battleCooldownUntil = 0;   // sampai kapan (ms) monster gak boleh trigger battle
    private boolean doorTransitionLocked;
    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean inventoryVisible;
    private boolean shopVisible;
    private boolean questPanelVisible;
    private boolean grimoireReaderVisible;
    private boolean academyQuizVisible;
    private QuizSession quizSession;
    private boolean skillLoadoutVisible;
    private InventoryOverlayState inventoryOverlayState;
    private int selectedSkillSlotIndex;
    private Timer gameTimer;
    private Timer fishingAnimationTimer;

    public GamePanel(GameFrame frame, Character player, Shop shop, boolean fromSave) {
        this.frame = frame;
        this.player = player;
        this.shop = shop;
        this.gameMap = new GameMap();
        this.monsters = new ArrayList<Monster>();
        this.questLog = new QuestLog();
        this.monsterIdleFrames = new ArrayList<BufferedImage[]>();
        this.monsterWalkFrames = new ArrayList<BufferedImage[]>();
        this.random = new Random();
        this.fishingState = new FishingMiniGameState(FISHING_BAR_WIDTH, FISHING_GREEN_ZONE_WIDTH, 9);
        this.shopOverlayState = new ShopOverlayState();
        this.inventoryOverlayLayout = new InventoryOverlayLayout(
                INVENTORY_ASSET_WIDTH,
                INVENTORY_ASSET_HEIGHT,
                INVENTORY_ASSET_SLOT_X,
                INVENTORY_ASSET_SLOT_Y,
                INVENTORY_ASSET_SLOT_SIZE,
                INVENTORY_ASSET_SLOT_GAP_X,
                INVENTORY_ASSET_SLOT_GAP_Y,
                INVENTORY_COLUMNS);
        this.activityMessage = "";
        this.currentFrame = 0;
        this.animationFrame = 0;
        this.playerAnimationTick = 0;
        this.cameraX = 0;
        this.cameraY = 0;
        this.playerState = STATE_IDLE;
        this.direction = "down";
        this.battleTriggered = false;
        this.doorTransitionLocked = false;
        this.inventoryVisible = false;
        this.shopVisible = false;
        this.questPanelVisible = false;
        this.grimoireReaderVisible = false;
        this.academyQuizVisible = false;
        this.quizSession = null;
        this.skillLoadoutVisible = false;
        this.inventoryOverlayState = new InventoryOverlayState(INVENTORY_SLOT_COUNT, INVENTORY_COLUMNS);
        this.selectedSkillSlotIndex = 0;

        setBackground(new Color(24, 34, 32));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        if (fromSave && player.getLastAreaName() != null) {
            gameMap.changeArea(player.getLastAreaName());
            if (!isWalkable(player.getX(), player.getY())) {
                movePlayerToSpawn(gameMap.getCurrentArea().getDefaultSpawnBounds());
            }
        } else {
            movePlayerToSpawn(gameMap.getCurrentArea().getDefaultSpawnBounds());
        }

        loadPlayerSprites();
        loadUiAssets();
        loadAreaBackground();
        syncAreaBgm();
        createMonsters();
        updateCamera();

        gameTimer = new Timer(GAME_TICK_MS, e -> {
            animationFrame++;
            updatePlayerMovement();
            playerAnimationTick++;
            if (playerAnimationTick >= PLAYER_ANIMATION_TICK_INTERVAL) {
                animatePlayer();
                playerAnimationTick = 0;
            }
            updateMonsters();
            updateShopAvailability();
            updateCamera();
            checkBattleTrigger();
            checkQuizTimeout();
            repaint();
        });
        gameTimer.start();
    }

    private void loadPlayerSprites() {
        SpriteConfig sprites = player.getSpriteConfig();

        idleFrames = SpriteLoader.loadAnimation(sprites.getIdlePath(), sprites.getIdleFrameCount(), Color.RED,
                ENTITY_SIZE, ENTITY_SIZE);
        walkUpFrames = SpriteLoader.loadAnimation(sprites.getWalkUpPath(), sprites.getWalkUpFrameCount(), Color.RED,
                ENTITY_SIZE, ENTITY_SIZE);
        walkDownFrames = SpriteLoader.loadAnimation(sprites.getWalkDownPath(), sprites.getWalkDownFrameCount(), Color.RED,
                ENTITY_SIZE, ENTITY_SIZE);
        walkLeftFrames = SpriteLoader.loadAnimation(sprites.getWalkLeftPath(), sprites.getWalkLeftFrameCount(), Color.RED,
                ENTITY_SIZE, ENTITY_SIZE);
        walkRightFrames = SpriteLoader.loadAnimation(sprites.getWalkRightPath(), sprites.getWalkRightFrameCount(), Color.RED,
                ENTITY_SIZE, ENTITY_SIZE);
        attackFrames = SpriteLoader.loadAnimation(sprites.getAttackPath(), sprites.getAttackFrameCount(), Color.RED,
                ENTITY_SIZE, ENTITY_SIZE);
    }

    private void loadAreaBackground() {
        Area currentArea = gameMap.getCurrentArea();
        Rectangle bounds = currentArea.getBounds();
        areaBackground = SpriteLoader.loadImage(currentArea.getBackgroundAssetPath(), new Color(46, 78, 60),
                bounds.width, bounds.height);
    }

    private void loadUiAssets() {
        inventoryPanelImage = SpriteLoader.loadImage(INVENTORY_PANEL_ASSET_PATH, new Color(26, 34, 48), 640, 360);
        shopPanelImage = SpriteLoader.loadImage(SHOP_PANEL_ASSET_PATH, new Color(35, 22, 12), 640, 360);
        fishingRodIconImage = SpriteLoader.loadImage(FISHING_ROD_ICON_PATH, new Color(190, 130, 70), 32, 32);
        fishIconImage = SpriteLoader.loadImage(FISH_ICON_PATH, new Color(90, 198, 186), 32, 32);
        grimoireEmberIconImage = SpriteLoader.loadImage(GRIMOIRE_EMBER_ICON_PATH, new Color(180, 70, 50), 32, 32);
        grimoireTideIconImage = SpriteLoader.loadImage(GRIMOIRE_TIDE_ICON_PATH, new Color(70, 130, 220), 32, 32);
        grimoireArcaneIconImage = SpriteLoader.loadImage(GRIMOIRE_ARCANE_ICON_PATH, new Color(130, 80, 180), 32, 32);
        fishingCastingRodFrames = SpriteLoader.loadAnimation(FISHING_CASTING_ROD_PATH,
                FISHING_CASTING_ROD_FRAME_COUNT, new Color(190, 130, 70), 96, 96);
        fishingPullSomethingFrames = SpriteLoader.loadAnimation(FISHING_PULL_SOMETHING_PATH,
                FISHING_PULL_SOMETHING_FRAME_COUNT, new Color(90, 180, 220), 96, 96);
    }

    private void syncAreaBgm() {
        String nextAreaBgmPath = resolveAreaBgmPath();
        if (nextAreaBgmPath.equals(currentAreaBgmPath) && areaBgmClip != null && areaBgmClip.isRunning()) {
            return;
        }

        stopClip(areaBgmClip);
        areaBgmClip = openClip(nextAreaBgmPath);
        currentAreaBgmPath = nextAreaBgmPath;

        if (areaBgmClip != null) {
            areaBgmClip.setFramePosition(0);
            areaBgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            areaBgmClip.start();
        }
    }

    private String resolveAreaBgmPath() {
        if (isInAcademyArea()) {
            return MAIN_HALL_BGM_PATH;
        }

        return DEFAULT_AREA_BGM_PATH;
    }

    private boolean isMonsterFarmingArea() {
        String areaName = gameMap.getCurrentArea().getAreaName();
        return "Village".equalsIgnoreCase(areaName) || "Academy Yard".equalsIgnoreCase(areaName);
    }

    private Clip openClip(String resourcePath) {
        URL resource = getClass().getClassLoader().getResource(resourcePath);
        if (resource == null) {
            System.out.println("Audio asset belum ada: " + resourcePath);
            return null;
        }

        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(resource)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            return clip;
        } catch (Exception e) {
            System.out.println("Audio gagal dimuat (" + resourcePath + "): " + e.getMessage());
            return null;
        }
    }

    private void playSoundEffect(String resourcePath) {
        Clip soundEffectClip = openClip(resourcePath);
        if (soundEffectClip == null) {
            return;
        }

        soundEffectClip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                soundEffectClip.close();
            }
        });
        soundEffectClip.start();
    }

    private void stopClip(Clip clip) {
        if (clip == null) {
            return;
        }

        clip.stop();
        clip.close();
    }

    private void createMonsters() {
        monsters.clear();
        if (isMonsterFarmingArea()) {
            monsters.add(new Slime(500, 300, ENTITY_SIZE, ENTITY_SIZE, new Rectangle(420, 230, 220, 180)));
            monsters.add(new ArcaneWisp(260, 180, ENTITY_SIZE, ENTITY_SIZE, new Rectangle(180, 140, 180, 160)));
            monsters.add(new Golem(720, 420, ENTITY_SIZE, ENTITY_SIZE, new Rectangle(650, 360, 180, 180)));
        }
        cacheMonsterFrames();
    }

    private void cacheMonsterFrames() {
        monsterIdleFrames.clear();
        monsterWalkFrames.clear();

        for (Monster monster : monsters) {
            SpriteConfig sprites = monster.getSpriteConfig();
            Color fallbackColor = getMonsterFallbackColor(monster);
            monsterIdleFrames.add(SpriteLoader.loadAnimation(sprites.getIdlePath(), sprites.getIdleFrameCount(),
                    fallbackColor, ENTITY_SIZE, ENTITY_SIZE));
            monsterWalkFrames.add(SpriteLoader.loadAnimation(sprites.getWalkDownPath(), sprites.getWalkDownFrameCount(),
                    fallbackColor, ENTITY_SIZE, ENTITY_SIZE));
        }
    }

    private void animatePlayer() {
        BufferedImage[] activeFrames = getCurrentPlayerFrames();
        if (activeFrames == null || activeFrames.length <= 1) {
            currentFrame = 0;
            return;
        }

        currentFrame = (currentFrame + 1) % activeFrames.length;
    }

    private void updateMonsters() {
        Rectangle mapBounds = gameMap.getCurrentArea().getBounds();
        Rectangle playerBounds = createPlayerBounds();
        for (Monster monster : monsters) {
            monster.updateMovement(mapBounds, playerBounds);
        }
    }

    private void updateShopAvailability() {
        if (shopVisible && getCurrentInteractionZone(InteractionType.SHOP) == null) {
            shopVisible = false;
            activityMessage = "Shop ditutup karena kamu menjauh dari pintu shop.";
        }
    }

    private void updateFishingChanneling() {
        if (fishingState.update(fishingCastingRodFrames.length, fishingPullSomethingFrames.length)) {
            completeFishingCatch();
        }
    }

    private void startFishingAnimationTimer() {
        stopFishingAnimationTimer();
        fishingAnimationTimer = new Timer(FISHING_ANIMATION_TICK_MS, e -> {
            updateFishingChanneling();
            repaint();
        });
        fishingAnimationTimer.start();
    }

    private void stopFishingAnimationTimer() {
        if (fishingAnimationTimer != null) {
            fishingAnimationTimer.stop();
            fishingAnimationTimer = null;
        }
    }

    private void updateCamera() {
        Rectangle bounds = gameMap.getCurrentArea().getBounds();
        int viewportWidth = Math.max(1, (int) Math.round(getWidth() / CAMERA_ZOOM));
        int viewportHeight = Math.max(1, (int) Math.round(getHeight() / CAMERA_ZOOM));
        int minCameraX = bounds.x;
        int minCameraY = bounds.y;
        int maxCameraX = Math.max(minCameraX, bounds.x + bounds.width - viewportWidth);
        int maxCameraY = Math.max(minCameraY, bounds.y + bounds.height - viewportHeight);
        int targetX = player.getX() + ENTITY_SIZE / 2 - viewportWidth / 2;
        int targetY = player.getY() + ENTITY_SIZE / 2 - viewportHeight / 2;

        cameraX = Math.max(minCameraX, Math.min(targetX, maxCameraX));
        cameraY = Math.max(minCameraY, Math.min(targetY, maxCameraY));
    }

    private int worldToScreenX(int worldX) {
        return getMapScreenOffsetX() + (int) Math.round((worldX - cameraX) * CAMERA_ZOOM);
    }

    private int worldToScreenY(int worldY) {
        return getMapScreenOffsetY() + (int) Math.round((worldY - cameraY) * CAMERA_ZOOM);
    }

    private int scaledSize() {
        return (int) Math.round(ENTITY_SIZE * CAMERA_ZOOM);
    }

    private int getMapScreenOffsetX() {
        Rectangle bounds = gameMap.getCurrentArea().getBounds();
        int scaledMapWidth = (int) Math.round(bounds.width * CAMERA_ZOOM);
        return Math.max(0, (getWidth() - scaledMapWidth) / 2);
    }

    private int getMapScreenOffsetY() {
        Rectangle bounds = gameMap.getCurrentArea().getBounds();
        int scaledMapHeight = (int) Math.round(bounds.height * CAMERA_ZOOM);
        return Math.max(0, (getHeight() - scaledMapHeight) / 2);
    }

    private BufferedImage[] getCurrentWalkFrames() {
        if (direction.equals("up")) {
            return walkUpFrames;
        } else if (direction.equals("down")) {
            return walkDownFrames;
        } else if (direction.equals("left")) {
            return walkLeftFrames;
        }
        return walkRightFrames;
    }

    private BufferedImage[] getCurrentPlayerFrames() {
        if (STATE_WALK.equals(playerState)) {
            return getCurrentWalkFrames();
        }
        if (STATE_ATTACK.equals(playerState)) {
            return attackFrames;
        }
        return idleFrames;
    }

    private void updatePlayerMovement() {
        boolean moved = false;

        if (upPressed) {
            moved = tryMovePlayer(0, -PLAYER_SPEED, "up");
        } else if (downPressed) {
            moved = tryMovePlayer(0, PLAYER_SPEED, "down");
        } else if (leftPressed) {
            moved = tryMovePlayer(-PLAYER_SPEED, 0, "left");
        } else if (rightPressed) {
            moved = tryMovePlayer(PLAYER_SPEED, 0, "right");
        }

        String nextState = moved ? STATE_WALK : STATE_IDLE;
        if (!playerState.equals(nextState)) {
            currentFrame = 0;
        }
        playerState = nextState;

        if (moved) {
            checkDoorTrigger();
        }
    }

    private void setDirection(String newDirection) {
        if (!direction.equals(newDirection)) {
            direction = newDirection;
            BufferedImage[] activeFrames = getCurrentWalkFrames();
            if (activeFrames == null || activeFrames.length == 0) {
                currentFrame = 0;
            } else {
                currentFrame %= activeFrames.length;
            }
        }
    }

    private void clearMovementInput() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        playerAnimationTick = 0;
        playerState = STATE_IDLE;
        currentFrame = 0;
    }

    public void prepareForBattle() {
        clearMovementInput();
        battleTriggered = true;
        stopClip(areaBgmClip);
        areaBgmClip = null;
        currentAreaBgmPath = null;
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    public void prepareAfterBattle() {
        clearMovementInput();
        battleTriggered = false;
        battleCooldownUntil = System.currentTimeMillis() + 2000;   // jeda 2 detik
        syncAreaBgm();
        updateCamera();
        repaint();
        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawAreaBackground(g);
        drawDoors(g);
        drawPlayer(g);
        drawMonsters(g);
        drawHud(g);
        drawHotbar(g);
        drawInventoryOverlay(g);
        drawShopOverlay(g);
        drawQuestPanelOverlay(g);
        drawFishingPanelOverlay(g);
        drawGrimoireReaderOverlay(g);
        drawAcademyQuizOverlay(g);
        drawSkillLoadoutOverlay(g);
    }

    private void drawAreaBackground(Graphics g) {
        if (areaBackground == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        int drawX = getMapScreenOffsetX() - (int) Math.round(cameraX * CAMERA_ZOOM);
        int drawY = getMapScreenOffsetY() - (int) Math.round(cameraY * CAMERA_ZOOM);
        int drawWidth = (int) Math.round(areaBackground.getWidth() * CAMERA_ZOOM);
        int drawHeight = (int) Math.round(areaBackground.getHeight() * CAMERA_ZOOM);

        g2.drawImage(areaBackground, drawX, drawY, drawWidth, drawHeight, null);
    }

    private void drawHud(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("Arcana Incantation - " + gameMap.getCurrentArea().getAreaName(), 30, 30);
        g.drawString("X=" + player.getX() + " Y=" + player.getY(), getWidth() - 150, 30);
        g.drawString("Player: " + player.getName(), 30, 50);
        Item heldItem = player.getHeldItem();
        String heldItemLabel = heldItem == null ? "(kosong)" : heldItem.getName();
        g.drawString("Item dipegang: Slot " + (player.getSelectedHotbarSlot() + 1) + " - " + heldItemLabel, 30, 65);
        g.drawString("Level: " + player.getLevel() + " | EXP: " + player.getExp() + " | Gold: " + player.getGold(), 30,
                70);
        Quest activeQuest = questLog.getActiveQuest();
        if (activeQuest != null) {
            g.drawString("Quest: " + activeQuest.getQuestName() + " " + activeQuest.getProgress() + "/"
                    + activeQuest.getTargetAmount(), 30, 87);
        }
        g.drawString("WASD = Move | F = Fishing | Q = Quiz | E = Quest | I = Inventory | B = Shop", 30, 102);
        g.drawString("L = Read Grimoire | K = Skill Loadout | 1-8 = Pilih Hotbar / Buy Shop Slot", 30, 117);
        String interactionPrompt = getNearbyInteractionPrompt();
        if (!interactionPrompt.isBlank()) {
            g.drawString(interactionPrompt, 30, 125);
            g.drawString(activityMessage, 30, 145);
            return;
        }
        g.drawString(activityMessage, 30, 137);
    }

    private void drawHotbar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        List<Item> items = player.getInventory().getItems();
        int barWidth = 456;
        int barHeight = 72;
        int barX = (getWidth() - barWidth) / 2;
        int barY = getHeight() - barHeight - 18;
        int slotSize = 46;
        int slotGap = 8;
        int slotStartX = barX + 16;
        int slotStartY = barY + 13;

        g2.setColor(new Color(7, 12, 18, 195));
        g2.fillRoundRect(barX, barY, barWidth, barHeight, 18, 18);
        g2.setColor(new Color(120, 153, 178));
        g2.drawRoundRect(barX, barY, barWidth, barHeight, 18, 18);

        for (int i = 0; i < HOTBAR_SLOT_COUNT; i++) {
            int slotX = slotStartX + i * (slotSize + slotGap);
            int slotY = slotStartY;
            Item item = i < items.size() ? items.get(i) : null;
            drawSlotBox(g2, slotX, slotY, slotSize, i + 1, item, true, i == player.getSelectedHotbarSlot());
        }
    }

    private void drawInventoryOverlay(Graphics g) {
        if (!inventoryVisible) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        List<Item> items = player.getInventory().getItems();
        Rectangle panelBounds = getInventoryPanelBounds();

        g2.setColor(new Color(8, 12, 18, 165));
        g2.fillRoundRect(panelBounds.x - 8, panelBounds.y - 8, panelBounds.width + 16, panelBounds.height + 16, 28, 28);
        g2.drawImage(inventoryPanelImage, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, null);
        drawInventoryStats(g2, panelBounds);

        for (int i = 0; i < INVENTORY_SLOT_COUNT; i++) {
            Rectangle slotBounds = getInventorySlotBounds(i);
            Item item = i < items.size() ? items.get(i) : null;
            boolean selected = i == inventoryOverlayState.getSelectedSlotIndex();
            boolean activeHotbar = i == player.getSelectedHotbarSlot();
            boolean swapSource = inventoryOverlayState.isSwapSource(i);

            drawInventorySlotHighlight(g2, slotBounds, selected, activeHotbar, swapSource);
            if (item != null) {
                drawInventoryOverlayItem(g2, item, slotBounds);
            }
        }

        Item selectedItem = player.getInventoryItemAt(inventoryOverlayState.getSelectedSlotIndex());
        if (selectedItem == null) {
            g2.drawString("Slot " + (inventoryOverlayState.getSelectedSlotIndex() + 1) + " kosong.", panelBounds.x + 18,
                    panelBounds.y + panelBounds.height + 22);
        } else {
            g2.drawString(
                    "Slot " + (inventoryOverlayState.getSelectedSlotIndex() + 1) + ": " + selectedItem.getName() + " | Harga: "
                            + selectedItem.getPrice() + " gold",
                    panelBounds.x + 18, panelBounds.y + panelBounds.height + 22);
        }
    }

    private void drawShopOverlay(Graphics g) {
        if (!shopVisible) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        List<Item> availableItems = shop.getAvailableItems();
        Rectangle panelBounds = shopOverlayState.getPanelBounds(getWidth(), getHeight());
        Item selectedItem = shopOverlayState.getSelectedItem(availableItems);

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.drawImage(shopPanelImage, panelBounds.x, panelBounds.y, panelBounds.width, panelBounds.height, null);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
        g2.setColor(new Color(93, 54, 18));
        g2.drawString(player.getName(), panelBounds.x + 78, panelBounds.y + 182);
        g2.drawString("Gold : " + player.getGold(), panelBounds.x + 78, panelBounds.y + 206);
        g2.drawString("Lvl  : " + player.getLevel(), panelBounds.x + 78, panelBounds.y + 230);
        g2.drawString("Items: " + player.getInventory().getItemCount() + "/" + player.getInventorySlotCount(),
                panelBounds.x + 78, panelBounds.y + 254);
        drawSelectedShopItemDetails(g2, selectedItem, panelBounds);

        for (int i = 0; i < availableItems.size() && i < HOTBAR_SLOT_COUNT; i++) {
            Item item = availableItems.get(i);
            Rectangle slotBounds = shopOverlayState.getItemSlotBounds(i, getWidth(), getHeight());
            drawShopItemSlot(g2, item, i + 1, slotBounds, i == shopOverlayState.getSelectedItemIndex());
        }

        drawShopButtonHighlight(g2, shopOverlayState.getBuyButtonBounds(getWidth(), getHeight()), new Color(54, 154, 62, 110));
        drawShopButtonHighlight(g2, shopOverlayState.getExitButtonBounds(getWidth(), getHeight()), new Color(171, 111, 42, 95));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        g2.setColor(new Color(246, 233, 210));
        g2.drawString("Klik item lalu tekan Buy", panelBounds.x + 280, panelBounds.y + 315);
        g2.drawString("B / Esc = Exit", panelBounds.x + 488, panelBounds.y + 315);
    }

    private void drawShopItemSlot(Graphics2D g2, Item item, int shortcutNumber, Rectangle slotBounds, boolean selected) {
        int x = slotBounds.x;
        int y = slotBounds.y;
        int width = slotBounds.width;
        int height = slotBounds.height;

        if (selected) {
            int pulseAlpha = 120 + ((animationFrame * 7) % 90);
            g2.setColor(new Color(255, 237, 123, pulseAlpha));
            g2.fillRoundRect(x - 3, y - 3, width + 6, height + 6, 10, 10);
            g2.setColor(new Color(255, 255, 214, 240));
            g2.drawRoundRect(x - 1, y - 1, width + 2, height + 2, 9, 9);
        }

        g2.setColor(new Color(95, 52, 12, 170));
        g2.fillRoundRect(x, y, width, height, 8, 8);
        g2.setColor(new Color(255, 235, 200, 215));
        g2.drawRoundRect(x, y, width, height, 8, 8);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
        g2.setColor(new Color(255, 246, 214));
        g2.drawString(String.valueOf(shortcutNumber), x + 4, y + 11);

        BufferedImage iconImage = getItemIconImage(item);
        int iconSize = 20;
        int iconX = x + (width - iconSize) / 2;
        int iconY = y + 12;

        if (iconImage != null) {
            g2.drawImage(iconImage, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2.setColor(new Color(109, 186, 255));
            g2.fillRoundRect(iconX, iconY, iconSize, iconSize, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString("?", iconX + 7, iconY + 14);
        }

        g2.setColor(new Color(255, 241, 212));
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 8f));
        g2.drawString(shortenShopItemName(item.getName()), x + 4, y + 31);
        g2.drawString(item.getPrice() + "g", x + 4, y + 38);
    }

    private String shortenShopItemName(String itemName) {
        if (itemName == null) {
            return "";
        }
        if (itemName.length() <= 7) {
            return itemName;
        }
        return itemName.substring(0, 7) + ".";
    }

    private void drawSelectedShopItemDetails(Graphics2D g2, Item selectedItem, Rectangle panelBounds) {
        if (selectedItem == null) {
            return;
        }

        g2.setColor(new Color(88, 56, 24, 135));
        g2.fillRoundRect(panelBounds.x + 62, panelBounds.y + 74, 156, 46, 10, 10);
        g2.setColor(new Color(255, 235, 200));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        g2.drawString(selectedItem.getName(), panelBounds.x + 70, panelBounds.y + 92);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
        g2.drawString(getShopItemCategory(selectedItem), panelBounds.x + 70, panelBounds.y + 108);
        g2.drawString(selectedItem.getPrice() + " gold", panelBounds.x + 150, panelBounds.y + 108);
    }

    private void drawShopButtonHighlight(Graphics2D g2, Rectangle buttonBounds, Color fillColor) {
        g2.setColor(fillColor);
        g2.fillRoundRect(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height, 8, 8);
        g2.setColor(new Color(255, 248, 221, 185));
        g2.drawRoundRect(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height, 8, 8);
    }

    private String getShopItemCategory(Item item) {
        if (item instanceof FishingRod) {
            return "Tool";
        }
        if (item instanceof Grimoire) {
            return "Grimoire";
        }
        if (item instanceof Fish) {
            return "Consumable";
        }
        return "Item";
    }

    private void drawGrimoireReaderOverlay(Graphics g) {
        if (!grimoireReaderVisible) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        Grimoire activeGrimoire = getHeldGrimoire();
        int panelWidth = 720;
        int panelHeight = 360;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = 90;

        g2.setColor(new Color(58, 39, 22, 230));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);
        g2.setColor(new Color(230, 205, 152));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);
        g2.drawString("Grimoire Reader", panelX + 20, panelY + 24);
        g2.drawString("Panel asset: " + GRIMOIRE_PANEL_ASSET_PATH, panelX + 20, panelY + 44);
        g2.drawString("Page asset: " + GRIMOIRE_PAGE_ASSET_PATH, panelX + 20, panelY + 62);

        if (activeGrimoire == null) {
            g2.drawString("Slot aktif tidak sedang memegang grimoire.", panelX + 24, panelY + 100);
            g2.drawString("Pilih hotbar yang berisi grimoire dulu untuk membuka reader ini.", panelX + 24, panelY + 120);
            return;
        }

        g2.setColor(new Color(240, 226, 204));
        g2.fillRoundRect(panelX + 24, panelY + 84, panelWidth - 48, panelHeight - 110, 18, 18);
        g2.setColor(new Color(70, 52, 34));
        g2.drawString(activeGrimoire.getName(), panelX + 42, panelY + 110);
        g2.drawString("Skill target: " + activeGrimoire.getRelatedSkillName(), panelX + 42, panelY + 130);

        String[] loreLines = buildGrimoireLoreLines(activeGrimoire);
        for (int i = 0; i < loreLines.length; i++) {
            g2.drawString(loreLines[i], panelX + 42, panelY + 170 + (i * 22));
        }

        g2.drawString("Ganti hotbar 1-8 untuk pindah grimoire | L = Tutup", panelX + 42,
                panelY + panelHeight - 24);
    }

    private void drawAcademyQuizOverlay(Graphics g) {
        if (!academyQuizVisible) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        int panelWidth = 680;
        int panelHeight = 320;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = 120;

        g2.setColor(new Color(24, 30, 52, 230));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);
        g2.setColor(new Color(160, 186, 255));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);
        g2.drawString("Academy Quiz", panelX + 20, panelY + 24);
        g2.drawString("Panel asset: " + QUIZ_PANEL_ASSET_PATH, panelX + 20, panelY + 44);
        g2.drawString("Timer asset: " + QUIZ_TIMER_ASSET_PATH, panelX + 20, panelY + 62);
        long secsLeft = (quizSession != null) ? quizSession.getSecondsRemaining() : 0;
        g2.setColor(secsLeft <= 10 ? new Color(255, 120, 120) : new Color(160, 186, 255)); // merah pas mepet
        g2.drawString(String.format("Waktu: %02d:%02d", secsLeft / 60, secsLeft % 60),
                panelX + panelWidth - 120, panelY + 24);

        g2.setColor(new Color(236, 241, 255));
        g2.fillRoundRect(panelX + 24, panelY + 82, panelWidth - 48, panelHeight - 110, 18, 18);
        g2.setColor(new Color(34, 44, 74));
        if (quizSession == null) {
            g2.drawString("Quiz belum siap.", panelX + 42, panelY + 110);
            return;
        }

        QuizQuestion question = quizSession.getCurrentQuestion();
        if (question == null) {
            g2.drawString("Quiz selesai.", panelX + 42, panelY + 110);
            return;
        }

        g2.drawString("Soal " + quizSession.getCurrentNumber() + "/" + quizSession.getTotalQuestions(),
                panelX + 42, panelY + 108);
        g2.drawString(question.getQuestionText(), panelX + 42, panelY + 136);

        String[] options = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            g2.drawString((i + 1) + ". " + options[i], panelX + 58, panelY + 170 + (i * 24));
        }

        g2.drawString("Tekan 1-" + options.length + " untuk menjawab | Esc = Tutup",
                panelX + 42, panelY + panelHeight - 24);
    }
    
    private void checkQuizTimeout() {
        if (!academyQuizVisible || quizSession == null) {
            return;
        }
        if (quizSession.isTimedOut()) {
            activityMessage = "Waktu habis! Quiz akademi gagal.";
            academyQuizVisible = false;
            quizSession = null;
        }
    }
    
    private void handleQuizAnswer(int keyCode) {
        if (quizSession == null) {
            return;
        }

        int chosenIndex = keyCode - KeyEvent.VK_1;
        quizSession.answer(chosenIndex);

        if (!quizSession.isFinished()) {
            return;
        }

        if (quizSession.isPassed()) {
            activityMessage = new Quiz().execute(player);
        } else {
            activityMessage = "Quiz gagal. Ada jawaban yang salah, coba lagi.";
        }
        academyQuizVisible = false;
        quizSession = null;
    }

    private void drawSkillLoadoutOverlay(Graphics g) {
        if (!skillLoadoutVisible) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        int panelWidth = 540;
        int panelHeight = 300;
        int panelX = 36;
        int panelY = 140;

        g2.setColor(new Color(18, 44, 46, 235));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);
        g2.setColor(new Color(146, 220, 190));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 24, 24);
        g2.drawString("Skill Loadout", panelX + 20, panelY + 24);
        g2.drawString("Panel asset: " + SKILL_LOADOUT_PANEL_ASSET_PATH, panelX + 20, panelY + 44);
        g2.drawString("Slot asset: " + SKILL_SLOT_ASSET_PATH, panelX + 20, panelY + 62);

        for (int i = 0; i < player.getSkillSlotCount(); i++) {
            int slotY = panelY + 96 + (i * 56);
            boolean selected = i == selectedSkillSlotIndex;
            String equippedSkillName = player.getEquippedSkillName(i);

            g2.setColor(selected ? new Color(55, 104, 99, 235) : new Color(26, 63, 66, 220));
            g2.fillRoundRect(panelX + 20, slotY, panelWidth - 40, 42, 16, 16);
            g2.setColor(selected ? new Color(232, 255, 246) : new Color(183, 226, 214));
            g2.drawRoundRect(panelX + 20, slotY, panelWidth - 40, 42, 16, 16);

            g2.drawString("Slot " + (i + 1), panelX + 34, slotY + 25);
            g2.drawString(equippedSkillName == null ? "(kosong)" : equippedSkillName, panelX + 110, slotY + 25);
        }

        g2.drawString("1-3 = Pilih slot | Left/Right = Ganti skill | Backspace = Kosongkan | K = Tutup", panelX + 20,
                panelY + panelHeight - 26);
    }

    private void drawSlotBox(Graphics2D g2, int slotX, int slotY, int slotSize, int slotNumber, Item item,
            boolean compact, boolean selected) {
        g2.setColor(selected ? new Color(72, 106, 76, 240) : new Color(36, 50, 63, 230));
        g2.fillRoundRect(slotX, slotY, slotSize, slotSize, 16, 16);
        g2.setColor(selected ? new Color(225, 241, 145) : new Color(140, 167, 190));
        g2.drawRoundRect(slotX, slotY, slotSize, slotSize, 16, 16);
        g2.setColor(new Color(190, 205, 220));
        g2.drawString(String.valueOf(slotNumber), slotX + 6, slotY + 14);

        if (item != null) {
            drawInventoryItem(g2, item, slotX, slotY, slotSize, compact);
        }
    }
    
    private BufferedImage getItemIconImage(Item item) {
        if (item instanceof FishingRod) {
            return fishingRodIconImage;
        }

        if (item instanceof Fish) {
            return fishIconImage;
        }

        if (item instanceof Grimoire) {
            Grimoire grimoire = (Grimoire) item;
            String skillName = grimoire.getRelatedSkillName().toLowerCase();

            if (skillName.contains("fire") || skillName.contains("ember")) {
                return grimoireEmberIconImage;
            }

            if (skillName.contains("water") || skillName.contains("tide")) {
                return grimoireTideIconImage;
            }

            return grimoireArcaneIconImage;
        }

        return null;
    }

    private void drawInventoryItem(Graphics2D g2, Item item, int slotX, int slotY, int slotSize, boolean compact) {
        BufferedImage iconImage = getItemIconImage(item);

        int iconSize = compact ? 24 : 34;
        int iconX = slotX + (slotSize - iconSize) / 2;
        int iconY = slotY + (compact ? 14 : 18);

        if (iconImage != null) {
            g2.drawImage(iconImage, iconX, iconY, iconSize, iconSize, null);
        } else {
            g2.setColor(new Color(109, 186, 255));
            g2.fillRoundRect(iconX, iconY, iconSize, iconSize, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawString("?", iconX + 9, iconY + 17);
        }

        // bagian bawah method kamu yang gambar nama item / harga JANGAN DIHAPUS

        String itemName = item.getName();
        int maxLength = compact ? 6 : 10;
        if (itemName.length() > maxLength) {
            itemName = itemName.substring(0, maxLength) + ".";
        }

        g2.drawString(itemName, slotX + 8, slotY + (compact ? 40 : 56));
        if (!compact) {
            g2.drawString(item.getPrice() + " g", slotX + 8, slotY + 72);
        }
    }

    private void drawQuestPanelOverlay(Graphics g) {
        if (!questPanelVisible) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        int panelWidth = 660;
        int panelHeight = 380;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = 86;

        g2.setColor(new Color(22, 24, 30, 230));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 22, 22);
        g2.setColor(new Color(220, 192, 128));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 22, 22);
        g2.drawString("Guild Quest Board", panelX + 20, panelY + 28);

        Quest activeQuest = questLog.getActiveQuest();
        if (activeQuest != null) {
            g2.setColor(new Color(242, 232, 205));
            g2.fillRoundRect(panelX + 24, panelY + 48, panelWidth - 48, 94, 14, 14);
            g2.setColor(new Color(45, 38, 30));
            g2.drawString("Quest Aktif: " + activeQuest.getQuestName(), panelX + 42, panelY + 76);
            g2.drawString(activeQuest.getObjectiveText(), panelX + 42, panelY + 100);
            g2.drawString("Progress: " + activeQuest.getProgress() + "/" + activeQuest.getTargetAmount(),
                    panelX + 42, panelY + 124);
            if (activeQuest.canClaimReward()) {
                g2.drawString("Selesai. Tekan Enter / E untuk melapor dan klaim reward.", panelX + 280, panelY + 124);
            } else {
                g2.drawString("Selesaikan objective dulu, lalu kembali ke staff guild.", panelX + 280, panelY + 124);
            }
        }

        int listTop = activeQuest == null ? panelY + 56 : panelY + 158;
        List<Quest> quests = questLog.getQuests();
        for (int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);
            int questY = listTop + (i * 52);
            boolean selected = i == questLog.getSelectedQuestIndex();
            boolean lockedByActiveQuest = activeQuest != null && activeQuest != quest;

            g2.setColor(selected ? new Color(72, 82, 68, 235) : new Color(39, 43, 47, 225));
            g2.fillRoundRect(panelX + 24, questY, panelWidth - 48, 42, 12, 12);
            g2.setColor(selected ? new Color(246, 226, 154) : new Color(190, 176, 130));
            g2.drawRoundRect(panelX + 24, questY, panelWidth - 48, 42, 12, 12);
            g2.setColor(lockedByActiveQuest ? new Color(150, 150, 150) : new Color(236, 232, 214));
            g2.drawString((i + 1) + ". " + quest.getQuestName(), panelX + 40, questY + 17);
            g2.drawString(quest.getObjectiveText() + " Reward: +" + quest.getExpReward() + " EXP, +"
                    + quest.getGoldReward() + " Gold", panelX + 40, questY + 34);
        }

        g2.setColor(new Color(235, 221, 184));
        if (activeQuest == null) {
            g2.drawString("1-4 / Up-Down = Pilih quest | Enter / E = Ambil quest | Escape = Tutup", panelX + 28,
                    panelY + panelHeight - 28);
        } else if (activeQuest.canClaimReward()) {
            g2.drawString("Enter / E = Klaim reward | Escape = Tutup", panelX + 28, panelY + panelHeight - 28);
        } else {
            g2.drawString("Satu quest aktif saja. Selesaikan dulu sebelum mengambil quest lain. Escape = Tutup",
                    panelX + 28, panelY + panelHeight - 28);
        }
    }

    private void drawFishingPanelOverlay(Graphics g) {
        if (!fishingState.isVisible()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        int panelWidth = 380;
        int panelHeight = 170;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = 158;
        int barX = panelX + 24;
        int barY = panelY + 98;
        int barHeight = 20;

        g2.setColor(new Color(12, 24, 34, 232));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 22, 22);
        g2.setColor(new Color(120, 196, 210));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 22, 22);

        g2.setColor(new Color(224, 242, 246));
        g2.drawString(fishingState.isPullAnimationActive() ? "Pull!" : "Fishing Channeling", panelX + 24, panelY + 30);
        g2.drawString(fishingState.isPullAnimationActive() ? "Menarik hasil pancingan..." : "Tekan Space saat panah masuk zona hijau.",
                panelX + 24, panelY + 52);

        BufferedImage[] animationFrames = fishingState.isPullAnimationActive() ? fishingPullSomethingFrames : fishingCastingRodFrames;
        if (animationFrames != null && animationFrames.length > 0) {
            BufferedImage animationFrame = animationFrames[Math.min(fishingState.getAnimationFrame(), animationFrames.length - 1)];
            g2.drawImage(animationFrame, panelX + panelWidth - 118, panelY + 34, 92, 92, null);
        }

        if (!fishingState.isPullAnimationActive()) {
            g2.setColor(new Color(28, 48, 58));
            g2.fillRoundRect(barX, barY, FISHING_BAR_WIDTH, barHeight, 12, 12);
            g2.setColor(new Color(80, 210, 118));
            g2.fillRoundRect(barX + fishingState.getGreenZoneStart(), barY + 3, FISHING_GREEN_ZONE_WIDTH, barHeight - 6, 10, 10);
            g2.setColor(new Color(220, 236, 240));
            g2.drawRoundRect(barX, barY, FISHING_BAR_WIDTH, barHeight, 12, 12);

            int arrowX = barX + fishingState.getArrowPosition();
            int[] arrowXPoints = { arrowX, arrowX - 9, arrowX + 9 };
            int[] arrowYPoints = { barY - 8, barY - 24, barY - 24 };
            g2.setColor(new Color(255, 232, 120));
            g2.fillPolygon(arrowXPoints, arrowYPoints, 3);
            g2.drawLine(arrowX, barY - 8, arrowX, barY + barHeight + 8);
        }

        g2.setColor(new Color(174, 211, 220));
        g2.drawString(fishingState.isPullAnimationActive() ? "Tunggu animasi selesai." : "Enter / F juga bisa menarik | Escape = batal",
                panelX + 24, panelY + panelHeight - 18);
    }

    private Rectangle getInventoryPanelBounds() {
        return inventoryOverlayLayout.getPanelBounds(getWidth(), getHeight(), inventoryPanelImage);
    }

    private Rectangle getInventorySlotBounds(int slotIndex) {
        return inventoryOverlayLayout.getSlotBounds(slotIndex, getInventoryPanelBounds());
    }

    private int scaleAssetX(Rectangle panelBounds, int assetX) {
        return inventoryOverlayLayout.scaleAssetX(panelBounds, assetX);
    }

    private int scaleAssetY(Rectangle panelBounds, int assetY) {
        return inventoryOverlayLayout.scaleAssetY(panelBounds, assetY);
    }

    private int scaleAssetWidth(Rectangle panelBounds, int assetWidth) {
        return inventoryOverlayLayout.scaleAssetWidth(panelBounds, assetWidth);
    }

    private int scaleAssetHeight(Rectangle panelBounds, int assetHeight) {
        return inventoryOverlayLayout.scaleAssetHeight(panelBounds, assetHeight);
    }

    private void drawInventoryStats(Graphics2D g2, Rectangle panelBounds) {
        Font originalFont = g2.getFont();
        drawAssetBar(g2, panelBounds, 121, 144, 119, 14, player.getExp(), player.getExpToNextLevel(),
                new Color(199, 138, 42, 175));
        drawAssetBar(g2, panelBounds, 170, 181, 68, 13, player.getCurrentHp(), player.getMaxHp(),
                new Color(210, 62, 66, 190));

        g2.setColor(new Color(255, 245, 210));
        g2.setFont(originalFont.deriveFont(Font.BOLD, Math.max(12f, scaleAssetHeight(panelBounds, 13))));

        drawAssetText(g2, panelBounds, player.getName(), 130, 55);
        drawAssetText(g2, panelBounds, String.valueOf(player.getGold()), 96, 94);
        drawAssetText(g2, panelBounds, String.valueOf(player.getLevel()), 211, 129);
        drawAssetText(g2, panelBounds, player.getExp() + "/" + player.getExpToNextLevel(), 174, 155);
        drawAssetText(g2, panelBounds, player.getCurrentHp() + "/" + player.getMaxHp(), 176, 191);

        g2.setFont(originalFont.deriveFont(Font.PLAIN, Math.max(10f, scaleAssetHeight(panelBounds, 11))));
        g2.setColor(new Color(255, 235, 190));
        drawAssetText(g2, panelBounds, "X Swap", 302, 332);
        drawAssetText(g2, panelBounds, "Enter Use | Del Discard | I Back", 390, 332);
        g2.setFont(originalFont);
    }

    private void drawAssetText(Graphics2D g2, Rectangle panelBounds, String text, int assetX, int assetY) {
        g2.drawString(text, panelBounds.x + scaleAssetX(panelBounds, assetX), panelBounds.y + scaleAssetY(panelBounds, assetY));
    }

    private void drawAssetBar(Graphics2D g2, Rectangle panelBounds, int assetX, int assetY, int assetWidth,
            int assetHeight, int currentValue, int maxValue, Color fillColor) {
        int x = panelBounds.x + scaleAssetX(panelBounds, assetX);
        int y = panelBounds.y + scaleAssetY(panelBounds, assetY);
        int width = Math.max(1, scaleAssetWidth(panelBounds, assetWidth));
        int height = Math.max(1, scaleAssetHeight(panelBounds, assetHeight));
        int safeMaxValue = Math.max(1, maxValue);
        int clampedValue = Math.max(0, Math.min(currentValue, safeMaxValue));
        int fillWidth = (int) Math.round(width * (clampedValue / (double) safeMaxValue));

        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, fillWidth, height, Math.max(4, height), Math.max(4, height));
    }

    private void drawInventorySlotHighlight(Graphics2D g2, Rectangle slotBounds, boolean selected, boolean activeHotbar,
            boolean swapSource) {
        if (activeHotbar) {
            g2.setColor(new Color(88, 214, 255, 90));
            g2.fillRoundRect(slotBounds.x - 2, slotBounds.y - 2, slotBounds.width + 4, slotBounds.height + 4, 18, 18);
        }

        if (swapSource) {
            g2.setColor(new Color(225, 126, 255, 140));
            g2.fillRoundRect(slotBounds.x - 3, slotBounds.y - 3, slotBounds.width + 6, slotBounds.height + 6, 20, 20);
        }

        if (selected) {
            int pulseAlpha = 120 + ((animationFrame * 7) % 90);
            g2.setColor(new Color(255, 237, 123, pulseAlpha));
            g2.fillRoundRect(slotBounds.x - 4, slotBounds.y - 4, slotBounds.width + 8, slotBounds.height + 8, 22, 22);
            g2.setColor(new Color(255, 255, 214, 240));
            g2.drawRoundRect(slotBounds.x - 2, slotBounds.y - 2, slotBounds.width + 4, slotBounds.height + 4, 18, 18);
        }
    }

    private void drawInventoryOverlayItem(Graphics2D g2, Item item, Rectangle slotBounds) {
        BufferedImage iconImage = getItemIconImage(item);

        int iconSize = Math.min(slotBounds.width, slotBounds.height) - 10;
        int iconX = slotBounds.x + (slotBounds.width - iconSize) / 2;
        int iconY = slotBounds.y + (slotBounds.height - iconSize) / 2;

        if (iconImage != null) {
            g2.drawImage(iconImage, iconX, iconY, iconSize, iconSize, null);
            return;
        }

        g2.setColor(new Color(109, 186, 255, 220));
        g2.fillRoundRect(iconX, iconY, iconSize, iconSize, 10, 10);
        g2.setColor(new Color(255, 255, 255, 235));
        g2.drawString("?", iconX + iconSize / 2 - 4, iconY + iconSize / 2 + 5);
    }

    private List<Grimoire> getOwnedGrimoires() {
        ArrayList<Grimoire> grimoires = new ArrayList<Grimoire>();
        for (Item item : player.getInventory().getItems()) {
            if (item instanceof Grimoire) {
                grimoires.add((Grimoire) item);
            }
        }
        return grimoires;
    }

    private Grimoire getHeldGrimoire() {
        Item heldItem = player.getHeldItem();
        if (heldItem instanceof Grimoire) {
            return (Grimoire) heldItem;
        }
        return null;
    }

    private String[] buildGrimoireLoreLines(Grimoire grimoire) {
        String skillName = grimoire.getRelatedSkillName();
        if ("Fireball".equalsIgnoreCase(skillName)) {
            return new String[] {
                    "Lore fragment:",
                    "\"Fireball lahir dari mana yang dipadatkan lalu dilepas secepat ledakan bintang.\"",
                    "Halaman ini membahas panas inti, kestabilan fokus, dan pelepasan energi jarak dekat.",
                    "Catatan pinggirnya terbakar tipis, seolah buku ini memang menyimpan sihir api."
            };
        }
        if ("Water Surge".equalsIgnoreCase(skillName)) {
            return new String[] {
                    "Lore fragment:",
                    "\"Water Surge bergerak dari ritme, tekanan, dan aliran yang tidak pernah memaksa.\"",
                    "Halaman ini membahas arus mana cair, kontrol gelombang, dan dorongan berlapis.",
                    "Tepi kertasnya dingin dan lembap, memberi kesan sihir air yang tenang tapi kuat."
            };
        }
        return new String[] {
                "Lore fragment:",
                "\"" + skillName + " dibangun dari aliran mana, ritme, dan fokus penuh penyihir.\"",
                "Dafa merupakan cofounder dari magic academy di pulau ngawi pada tahun 1999.",
                "UI sengaja ditambal dulu xixixi supaya folder asset dan layout reader nanti ready."
        };
    }

    private void moveInventorySelection(int columnDelta, int rowDelta) {
        inventoryOverlayState.moveSelection(columnDelta, rowDelta);
        if (inventoryOverlayState.getSelectedSlotIndex() < HOTBAR_SLOT_COUNT) {
            player.selectHotbarSlot(inventoryOverlayState.getSelectedSlotIndex());
        }
    }

    private void useSelectedInventoryItem() {
        int selectedInventorySlotIndex = inventoryOverlayState.getSelectedSlotIndex();
        Item selectedItem = player.getInventoryItemAt(selectedInventorySlotIndex);
        if (selectedItem == null) {
            activityMessage = "Slot " + (selectedInventorySlotIndex + 1) + " kosong.";
            return;
        }

        if (selectedInventorySlotIndex < HOTBAR_SLOT_COUNT) {
            player.selectHotbarSlot(selectedInventorySlotIndex);
        }

        if (selectedItem instanceof Grimoire) {
            if (selectedInventorySlotIndex >= HOTBAR_SLOT_COUNT) {
                activityMessage = "Pindahkan grimoire ke hotbar dulu kalau mau dibaca cepat.";
                return;
            }

            inventoryVisible = false;
            grimoireReaderVisible = true;
            activityMessage = "Membuka " + selectedItem.getName() + ".";
            playSoundEffect(UI_TOGGLE_SFX_PATH);
            return;
        }

        if (selectedItem instanceof FishingRod) {
            if (selectedInventorySlotIndex >= HOTBAR_SLOT_COUNT) {
                activityMessage = "Pindahkan Fishing Rod ke hotbar dulu agar bisa dipakai untuk memancing.";
                return;
            }

            inventoryVisible = false;
            activityMessage = "Fishing Rod siap dipakai. Datangi pinggir air lalu tekan F.";
            playSoundEffect(UI_TOGGLE_SFX_PATH);
            return;
        }

        if (selectedItem instanceof Fish) {
            int recoveredHp = player.heal(15);
            player.removeItemAt(selectedInventorySlotIndex);
            activityMessage = selectedItem.getName() + " dipakai. HP pulih " + recoveredHp + ".";
            return;
        }

        activityMessage = selectedItem.getName() + " belum punya efek use khusus.";
    }

    private void removeSelectedInventoryItem() {
        int selectedInventorySlotIndex = inventoryOverlayState.getSelectedSlotIndex();
        Item selectedItem = player.getInventoryItemAt(selectedInventorySlotIndex);
        if (selectedItem == null) {
            activityMessage = "Tidak ada item untuk dihapus di slot ini.";
            return;
        }

        player.removeItemAt(selectedInventorySlotIndex);
        inventoryOverlayState.clearSwapSourceIfMatchesSelectedSlot();
        activityMessage = selectedItem.getName() + " dihapus dari slot " + (selectedInventorySlotIndex + 1) + ".";
    }

    private void swapSelectedInventoryItem() {
        int selectedInventorySlotIndex = inventoryOverlayState.getSelectedSlotIndex();
        Item selectedItem = player.getInventoryItemAt(selectedInventorySlotIndex);
        if (inventoryOverlayState.getSwapSourceSlotIndex() < 0) {
            if (selectedItem == null) {
                activityMessage = "Pilih slot yang ada itemnya dulu untuk ditukar.";
                return;
            }
            inventoryOverlayState.setSwapSourceToSelectedSlot();
            activityMessage = "Slot " + (selectedInventorySlotIndex + 1) + " diambil. Pilih tujuan lalu tekan X lagi.";
            return;
        }

        if (inventoryOverlayState.getSwapSourceSlotIndex() == selectedInventorySlotIndex) {
            inventoryOverlayState.clearSwapSource();
            activityMessage = "Mode tukar dibatalkan.";
            return;
        }

        player.swapInventoryItems(inventoryOverlayState.getSwapSourceSlotIndex(), selectedInventorySlotIndex);
        activityMessage = "Isi slot " + (inventoryOverlayState.getSwapSourceSlotIndex() + 1) + " dan "
                + (selectedInventorySlotIndex + 1) + " ditukar.";
        inventoryOverlayState.clearSwapSource();
    }

    private List<String> getSelectableSkillNames() {
        ArrayList<String> skillNames = new ArrayList<String>();
        skillNames.add(null);
        for (Skill skill : player.getUnlockedSkills()) {
            skillNames.add(skill.getSkillName());
        }
        return skillNames;
    }

    private void cycleEquippedSkillForSelectedSlot(int step) {
        List<String> selectableSkills = getSelectableSkillNames();
        if (selectableSkills.isEmpty()) {
            return;
        }

        String currentSkillName = player.getEquippedSkillName(selectedSkillSlotIndex);
        int currentIndex = selectableSkills.indexOf(currentSkillName);
        if (currentIndex < 0) {
            currentIndex = 0;
        }

        int nextIndex = (currentIndex + step + selectableSkills.size()) % selectableSkills.size();
        String nextSkillName = selectableSkills.get(nextIndex);
        player.equipSkillToSlot(selectedSkillSlotIndex, nextSkillName);

        if (nextSkillName == null) {
            activityMessage = "Slot skill " + (selectedSkillSlotIndex + 1) + " dikosongkan.";
        } else {
            activityMessage = nextSkillName + " dipasang ke slot " + (selectedSkillSlotIndex + 1) + ".";
        }
    }

    private void buySelectedShopItem() {
        List<Item> availableItems = shop.getAvailableItems();
        int itemIndex = shopOverlayState.getSelectedItemIndex();

        if (!shopVisible || itemIndex < 0 || itemIndex >= availableItems.size()) {
            return;
        }

        Item itemToBuy = availableItems.get(itemIndex);
        try {
            shop.buyItem(player, itemToBuy);
            activityMessage = "Berhasil membeli " + itemToBuy.getName() + ".";
            playSoundEffect(SHOP_BUY_SFX_PATH);
        } catch (NotEnoughGoldException e) {
            activityMessage = e.getMessage();
            playSoundEffect(SHOP_DENY_SFX_PATH);
            JOptionPane.showMessageDialog(this, e.getMessage(), "Gold Tidak Cukup", JOptionPane.WARNING_MESSAGE);
        }

        repaint();
        requestFocusInWindow();
    }

    private boolean handleHotbarSelectionShortcut(int keyCode) {
        int slotIndex = -1;
        switch (keyCode) {
            case KeyEvent.VK_1:
                slotIndex = 0;
                break;
            case KeyEvent.VK_2:
                slotIndex = 1;
                break;
            case KeyEvent.VK_3:
                slotIndex = 2;
                break;
            case KeyEvent.VK_4:
                slotIndex = 3;
                break;
            case KeyEvent.VK_5:
                slotIndex = 4;
                break;
            case KeyEvent.VK_6:
                slotIndex = 5;
                break;
            case KeyEvent.VK_7:
                slotIndex = 6;
                break;
            case KeyEvent.VK_8:
                slotIndex = 7;
                break;
            default:
                return false;
        }

        player.selectHotbarSlot(slotIndex);
        inventoryOverlayState.setSelectedSlotIndex(slotIndex);
        Item heldItem = player.getHeldItem();
        activityMessage = heldItem == null
                ? "Slot " + (slotIndex + 1) + " dipilih, tetapi belum ada item."
                : "Sekarang memegang: " + heldItem.getName() + " (slot " + (slotIndex + 1) + ").";
        repaint();
        return true;
    }
    private boolean handleShopPurchaseShortcut(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                buySelectedShopItem();
                return true;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                moveShopSelection(-1);
                return true;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                moveShopSelection(1);
                return true;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                moveShopSelection(-6);
                return true;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                moveShopSelection(6);
                return true;
            case KeyEvent.VK_B:
            case KeyEvent.VK_ESCAPE:
                shopVisible = false;
                playSoundEffect(UI_TOGGLE_SFX_PATH);
                return true;
            default:
                return false;
        }
    }

    private void moveShopSelection(int step) {
        List<Item> availableItems = shop.getAvailableItems();
        int itemCount = Math.min(availableItems.size(), 24);
        shopOverlayState.moveSelection(step, itemCount);
        repaint();
    }

    private void drawDoors(Graphics g) {
        if (!SHOW_DOOR_HITBOX) {
            return;
        }

        g.setColor(new Color(126, 83, 45, 180));
        for (Door door : gameMap.getCurrentArea().getDoors()) {
            Rectangle bounds = door.getBounds();
            int x = worldToScreenX(bounds.x);
            int y = worldToScreenY(bounds.y);
            int width = (int) Math.round(bounds.width * CAMERA_ZOOM);
            int height = (int) Math.round(bounds.height * CAMERA_ZOOM);
            g.fillRect(x, y, width, height);
        }
    }

    private void drawPlayer(Graphics g) {
        BufferedImage currentImage = getFirstFrame(idleFrames);
        BufferedImage[] activeFrames = getCurrentPlayerFrames();
        if (activeFrames != null && activeFrames.length > 0) {
            currentImage = activeFrames[currentFrame % activeFrames.length];
        }

        g.drawImage(currentImage, worldToScreenX(player.getX()), worldToScreenY(player.getY()), scaledSize(), scaledSize(), null);
    }

    private void drawMonsters(Graphics g) {
        for (int i = 0; i < monsters.size(); i++) {
            Monster monster = monsters.get(i);
            if (monster.isDefeated()) {
                g.setColor(getMonsterFallbackColor(monster).darker());
                g.drawString(monster.getName() + " respawn: " + monster.getRespawnSecondsRemaining() + "s",
                        worldToScreenX(monster.getX()) - 10, worldToScreenY(monster.getY()) - 5);
                continue;
            }

            BufferedImage[] frames = getMonsterFrames(monster, i);
            BufferedImage image = frames[animationFrame % frames.length];
            g.drawImage(image, worldToScreenX(monster.getX()), worldToScreenY(monster.getY()), scaledSize(), scaledSize(), null);

            g.setColor(Color.WHITE);
            g.drawString(monster.getName(), worldToScreenX(monster.getX()), worldToScreenY(monster.getY()) - 5);
        }
    }

    private BufferedImage[] getMonsterFrames(Monster monster, int index) {
        if (monster.getCurrentAction() == MonsterAction.MOVE || monster.getCurrentAction() == MonsterAction.CHASE) {
            return monsterWalkFrames.get(index);
        }

        return monsterIdleFrames.get(index);
    }

    private Color getMonsterFallbackColor(Monster monster) {
        if (monster instanceof Slime) {
            return new Color(72, 201, 120);
        }
        if (monster instanceof ArcaneWisp) {
            return new Color(90, 170, 255);
        }
        if (monster instanceof Golem) {
            return new Color(145, 120, 98);
        }
        return Color.GREEN;
    }

    private BufferedImage getFirstFrame(BufferedImage[] frames) {
        if (frames != null && frames.length > 0) {
            return frames[0];
        }
        return SpriteLoader.loadImage(null, Color.RED, ENTITY_SIZE, ENTITY_SIZE);
    }

    private void checkBattleTrigger() {
    	if (System.currentTimeMillis() < battleCooldownUntil) {
    	    return;   // masih masa tenang, jangan deteksi monster dulu
    	}
        if (battleTriggered) {
            return;
        }

        for (Monster monster : monsters) {
            if (!monster.isDefeated()
                    && monster.canStartBattle(player.getX(), player.getY(), ENTITY_SIZE, ENTITY_SIZE)) {
                battleTriggered = true;
                frame.startBattle(monster);
                return;
            }
        }
    }

    private void checkDoorTrigger() {
        Rectangle playerBounds = createPlayerBounds();
        if (doorTransitionLocked) {
            if (!isPlayerTouchingAnyDoor(playerBounds)) {
                doorTransitionLocked = false;
            }
            return;
        }

        for (Door door : gameMap.getCurrentArea().getDoors()) {
            if (door.isPlayerEntering(playerBounds)) {
                enterDoor(door);
                return;
            }
        }
    }

    private boolean isPlayerTouchingAnyDoor(Rectangle playerBounds) {
        for (Door door : gameMap.getCurrentArea().getDoors()) {
            if (door.isPlayerEntering(playerBounds)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryMovePlayer(int deltaX, int deltaY, String newDirection) {
        setDirection(newDirection);

        int previousX = player.getX();
        int previousY = player.getY();
        int nextX = previousX + deltaX;
        int nextY = previousY + deltaY;

        try {
            validatePlayerMove(nextX, nextY);
            player.setPosition(nextX, nextY);
            return previousX != player.getX() || previousY != player.getY();
        } catch (CollisionBlockedException | OutOfMapBoundsException e) {
            player.setPosition(previousX, previousY);
            activityMessage = e.getMessage();
            return false;
        }
    }

    private void validatePlayerMove(int nextX, int nextY) throws CollisionBlockedException, OutOfMapBoundsException {
        Rectangle nextBounds = createPlayerBounds(nextX, nextY);
        validatePlayerInsideMap(nextBounds);
        validatePlayerCollision(nextBounds);
    }

    private void validatePlayerCollision(Rectangle nextBounds) throws CollisionBlockedException {
        for (MapObstacle obstacle : gameMap.getCurrentArea().getObstacles()) {
            if (obstacle.isSolid() && obstacle.intersects(nextBounds)) {
                throw new CollisionBlockedException("Jalan terhalang oleh " + obstacle.getName() + ".");
            }
        }
    }

    private void validatePlayerInsideMap(Rectangle nextBounds) throws OutOfMapBoundsException {
        Rectangle bounds = gameMap.getCurrentArea().getBounds();
        int maxX = bounds.x + bounds.width;
        int maxY = bounds.y + bounds.height;
        if (nextBounds.x < bounds.x || nextBounds.y < bounds.y
                || nextBounds.x + nextBounds.width > maxX
                || nextBounds.y + nextBounds.height > maxY) {
            throw new OutOfMapBoundsException("Kamu tidak bisa keluar dari area " + gameMap.getCurrentArea().getAreaName() + ".");
        }
    }

    private void enterDoor(Door door) {
        Area nextArea = gameMap.changeArea(door.getTargetArea());
        movePlayerToSpawn(door.getTargetSpawnBounds());
        loadAreaBackground();
        createMonsters();
        shopVisible = false;
        questPanelVisible = false;
        fishingState.cancel();
        stopFishingAnimationTimer();
        doorTransitionLocked = true;
        activityMessage = "Masuk ke " + nextArea.getAreaName();
        playSoundEffect(AREA_TRANSITION_SFX_PATH);
        syncAreaBgm();
        updateCamera();
    }

    private void movePlayerToSpawn(Rectangle spawnBounds) {
        if (isWalkable(spawnBounds.x, spawnBounds.y)) {
            player.setPosition(spawnBounds.x, spawnBounds.y);
            return;
        }

        Rectangle fallback = gameMap.getCurrentArea().getDefaultSpawnBounds();
        if (isWalkable(fallback.x, fallback.y)) {
            player.setPosition(fallback.x, fallback.y);
            return;
        }

        Rectangle mapBounds = gameMap.getCurrentArea().getBounds();
        for (int y = mapBounds.y; y < mapBounds.y + mapBounds.height; y += 16) {
            for (int x = mapBounds.x; x < mapBounds.x + mapBounds.width; x += 16) {
                if (isWalkable(x, y)) {
                    player.setPosition(x, y);
                    return;
                }
            }
        }

        player.setPosition(spawnBounds.x, spawnBounds.y);
    }
    
    private boolean isWalkable(int x, int y) {
        Rectangle playerBounds = createPlayerBounds(x, y);
        Rectangle mapBounds = gameMap.getCurrentArea().getBounds();

        if (playerBounds.x < mapBounds.x
                || playerBounds.y < mapBounds.y
                || playerBounds.x + playerBounds.width > mapBounds.x + mapBounds.width
                || playerBounds.y + playerBounds.height > mapBounds.y + mapBounds.height) {
            return false;
        }

        for (MapObstacle obstacle : gameMap.getCurrentArea().getObstacles()) {
            if (obstacle.isSolid() && obstacle.intersects(playerBounds)) {
                return false;
            }
        }

        return true;
    }

    private Rectangle createPlayerBounds() {
        return createPlayerBounds(player.getX(), player.getY());
    }

    private Rectangle createPlayerBounds(int x, int y) {
        return new Rectangle(x + PLAYER_COLLISION_OFFSET_X, y + PLAYER_COLLISION_OFFSET_Y,
                PLAYER_COLLISION_WIDTH, PLAYER_COLLISION_HEIGHT);
    }

    private InteractionType resolveInteractionType(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_F:
                return InteractionType.FISHING;
            case KeyEvent.VK_Q:
                return InteractionType.QUIZ;
            case KeyEvent.VK_E:
                return InteractionType.QUEST;
            default:
                return null;
        }
    }

    private InteractionZone getCurrentInteractionZone(InteractionType interactionType) {
        return gameMap.getCurrentArea().findInteractionZone(interactionType, createPlayerBounds());
    }

    private void toggleShopAtCurrentLocation() {
        if (shopVisible) {
            shopVisible = false;
            playSoundEffect(UI_TOGGLE_SFX_PATH);
            return;
        }

        InteractionZone shopZone = getCurrentInteractionZone(InteractionType.SHOP);
        if (shopZone == null) {
            activityMessage = "Shop hanya bisa dibuka di depan pintu shop area Lake.";
            return;
        }

        clearMovementInput();
        shopOverlayState.resetSelection();
        shopVisible = true;
        activityMessage = shopZone.getLabel() + " dibuka.";
        playSoundEffect(UI_TOGGLE_SFX_PATH);
    }

    private void startFishingChanneling(InteractionZone fishingZone) {
        clearMovementInput();
        fishingState.start(random);
        startFishingAnimationTimer();
        activityMessage = fishingZone.getLabel() + ": tunggu timing yang tepat.";
    }

    private void finishFishingChanneling() {
        boolean success = fishingState.finishTimingAttempt();
        stopFishingAnimationTimer();

        if (success) {
            startFishingAnimationTimer();
            activityMessage = "Kail tersangkut sesuatu. Tarik!";
        } else {
            activityMessage = "Ikan terlepas. Coba pancing lagi dari pinggir air.";
        }
    }

    private void cancelFishingChanneling() {
        fishingState.cancel();
        stopFishingAnimationTimer();
        activityMessage = "Memancing dibatalkan.";
    }

    private void completeFishingCatch() {
        fishingState.cancel();
        stopFishingAnimationTimer();
        activityMessage = new Fishing().execute(player);
        recordFishCaught();
    }

    public void recordMonsterDefeated(String monsterName) {
        String questProgressMessage = questLog.recordMonsterDefeated(monsterName);
        if (questProgressMessage != null) {
            activityMessage = questProgressMessage;
        }
    }

    private void recordFishCaught() {
        String questProgressMessage = questLog.recordFishCaught();
        if (questProgressMessage != null) {
            activityMessage += " " + questProgressMessage;
        }
    }

    private String getNearbyInteractionPrompt() {
        for (InteractionZone interactionZone : gameMap.getCurrentArea().getInteractionZones()) {
            if (interactionZone.contains(createPlayerBounds())) {
                return interactionZone.getPrompt();
            }
        }
        return "";
    }

    private Activity getActivityFromExplorationInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_F:
                return new Fishing();
            case KeyEvent.VK_Q:
                return new Quiz();
            case KeyEvent.VK_E:
                return new Quest();
            default:
                return null;
        }
    }

    private void startActivityFromExplorationInput(int keyCode) {
        Activity activity = getActivityFromExplorationInput(keyCode);
        InteractionType interactionType = resolveInteractionType(keyCode);
        if (activity == null || interactionType == null) {
            return;
        }

        try {
            InteractionZone interactionZone = getCurrentInteractionZone(interactionType);
            if (interactionZone == null) {
                throw new InteractionUnavailableException("Kamu belum berada di area " + activity.getActivityName().toLowerCase() + ".");
            }

            clearMovementInput();
            if (activity instanceof Quiz) {
                if (!isInAcademyArea()) {
                    throw new InteractionUnavailableException("Quiz akademi hanya bisa dibuka di Main Hall.");
                }
                if (getOwnedGrimoires().isEmpty()) {
                    throw new InteractionUnavailableException("Kamu butuh grimoire dulu sebelum membuka quiz akademi.");
                }
                quizSession = new QuizSession();
                academyQuizVisible = true;
                academyQuizVisible = true;
                activityMessage = interactionZone.getLabel() + " dibuka.";
                return;
            }

            if (activity instanceof Fishing) {
                if (!(player.getHeldItem() instanceof FishingRod)) {
                    throw new InteractionUnavailableException(
                            "Pegang Fishing Rod di hotbar dulu sebelum memancing.");
                }
                startFishingChanneling(interactionZone);
                return;
            }

            if (activity instanceof Quest) {
                questLog.alignSelectionWithActiveQuest();
                questPanelVisible = true;
                activityMessage = interactionZone.getLabel() + " dibuka.";
                return;
            }

            activityMessage = activity.execute(player);
        } catch (InteractionUnavailableException e) {
            activityMessage = e.getMessage();
        }
    }

    private boolean isInAcademyArea() {
        return "Main Hall".equalsIgnoreCase(gameMap.getCurrentArea().getAreaName());
    }

    private boolean handleOverlayInput(int keyCode) {
        if (fishingState.isVisible()) {
            if (fishingState.isPullAnimationActive()) {
                return true;
            }
            switch (keyCode) {
                case KeyEvent.VK_SPACE:
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_F:
                    finishFishingChanneling();
                    return true;
                case KeyEvent.VK_ESCAPE:
                    cancelFishingChanneling();
                    return true;
                default:
                    return true;
            }
        }

        if (grimoireReaderVisible) {
            switch (keyCode) {
                case KeyEvent.VK_L:
                case KeyEvent.VK_ESCAPE:
                    grimoireReaderVisible = false;
                    return true;
                case KeyEvent.VK_1:
                case KeyEvent.VK_2:
                case KeyEvent.VK_3:
                case KeyEvent.VK_4:
                case KeyEvent.VK_5:
                case KeyEvent.VK_6:
                case KeyEvent.VK_7:
                case KeyEvent.VK_8:
                    handleHotbarSelectionShortcut(keyCode);
                    if (getHeldGrimoire() == null) {
                        grimoireReaderVisible = false;
                        activityMessage = "Reader grimoire ditutup karena slot aktif bukan grimoire.";
                    }
                    return true;
                default:
                    return true;
            }
        }

        if (inventoryVisible) {
            switch (keyCode) {
                case KeyEvent.VK_I:
                case KeyEvent.VK_ESCAPE:
                    inventoryVisible = false;
                    inventoryOverlayState.clearSwapSource();
                    return true;
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    moveInventorySelection(0, -1);
                    return true;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    moveInventorySelection(0, 1);
                    return true;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    moveInventorySelection(-1, 0);
                    return true;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    moveInventorySelection(1, 0);
                    return true;
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SPACE:
                    useSelectedInventoryItem();
                    return true;
                case KeyEvent.VK_DELETE:
                case KeyEvent.VK_BACK_SPACE:
                    removeSelectedInventoryItem();
                    return true;
                case KeyEvent.VK_X:
                    swapSelectedInventoryItem();
                    return true;
                case KeyEvent.VK_1:
                case KeyEvent.VK_2:
                case KeyEvent.VK_3:
                case KeyEvent.VK_4:
                case KeyEvent.VK_5:
                case KeyEvent.VK_6:
                case KeyEvent.VK_7:
                case KeyEvent.VK_8:
                    handleHotbarSelectionShortcut(keyCode);
                    inventoryOverlayState.setSelectedSlotIndex(player.getSelectedHotbarSlot());
                    return true;
                default:
                    return true;
            }
        }

        if (questPanelVisible) {
            switch (keyCode) {
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_E:
                    activityMessage = questLog.claimOrAccept(player);
                    return true;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    questLog.moveSelection(-1);
                    return true;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    questLog.moveSelection(1);
                    return true;
                case KeyEvent.VK_1:
                    questLog.setSelectedQuestIndex(0);
                    activityMessage = questLog.acceptSelectedQuest();
                    return true;
                case KeyEvent.VK_2:
                    questLog.setSelectedQuestIndex(1);
                    activityMessage = questLog.acceptSelectedQuest();
                    return true;
                case KeyEvent.VK_3:
                    questLog.setSelectedQuestIndex(2);
                    activityMessage = questLog.acceptSelectedQuest();
                    return true;
                case KeyEvent.VK_4:
                    questLog.setSelectedQuestIndex(3);
                    activityMessage = questLog.acceptSelectedQuest();
                    return true;
                case KeyEvent.VK_ESCAPE:
                    questPanelVisible = false;
                    activityMessage = "Panel quest guild ditutup.";
                    return true;
                default:
                    return true;
            }
        }

        if (academyQuizVisible) {
            switch (keyCode) {
                case KeyEvent.VK_1:
                case KeyEvent.VK_2:
                case KeyEvent.VK_3:
                case KeyEvent.VK_4:
                    handleQuizAnswer(keyCode);
                    return true;
                case KeyEvent.VK_ESCAPE:
                    academyQuizVisible = false;
                    quizSession = null;
                    activityMessage = "Quiz akademi dibatalkan.";
                    return true;
                default:
                    return true;
            }
        }

        if (skillLoadoutVisible) {
            switch (keyCode) {
                case KeyEvent.VK_K:
                case KeyEvent.VK_ESCAPE:
                    skillLoadoutVisible = false;
                    return true;
                case KeyEvent.VK_1:
                    selectedSkillSlotIndex = 0;
                    return true;
                case KeyEvent.VK_2:
                    selectedSkillSlotIndex = Math.min(1, player.getSkillSlotCount() - 1);
                    return true;
                case KeyEvent.VK_3:
                    selectedSkillSlotIndex = Math.min(2, player.getSkillSlotCount() - 1);
                    return true;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    cycleEquippedSkillForSelectedSlot(-1);
                    return true;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    cycleEquippedSkillForSelectedSlot(1);
                    return true;
                case KeyEvent.VK_BACK_SPACE:
                case KeyEvent.VK_DELETE:
                    player.clearSkillSlot(selectedSkillSlotIndex);
                    activityMessage = "Slot skill " + (selectedSkillSlotIndex + 1) + " dikosongkan.";
                    return true;
                default:
                    return true;
            }
        }

        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (handleOverlayInput(e.getKeyCode())) {
            repaint();
            return;
        }



        if (shopVisible && handleShopPurchaseShortcut(e.getKeyCode())) {
            return;
        }

        if (!shopVisible && handleHotbarSelectionShortcut(e.getKeyCode())) {
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                upPressed = true;
                break;
            case KeyEvent.VK_S:
                downPressed = true;
                break;
            case KeyEvent.VK_A:
                leftPressed = true;
                break;
            case KeyEvent.VK_D:
                rightPressed = true;
                break;
            case KeyEvent.VK_I:
                inventoryVisible = !inventoryVisible;
                if (inventoryVisible) {
                    inventoryOverlayState.setSelectedSlotIndex(player.getSelectedHotbarSlot());
                    inventoryOverlayState.clearSwapSource();
                }
                playSoundEffect(UI_TOGGLE_SFX_PATH);
                break;
            case KeyEvent.VK_B:
                toggleShopAtCurrentLocation();
                break;
            case KeyEvent.VK_L:
                if (grimoireReaderVisible) {
                    grimoireReaderVisible = false;
                    playSoundEffect(UI_TOGGLE_SFX_PATH);
                    break;
                }

                if (getOwnedGrimoires().isEmpty()) {
                    activityMessage = "Belum ada grimoire di inventory.";
                    break;
                }

                if (getHeldGrimoire() == null) {
                    activityMessage = "Pilih slot hotbar yang berisi grimoire dulu.";
                    break;
                }

                grimoireReaderVisible = true;
                playSoundEffect(UI_TOGGLE_SFX_PATH);
                break;
            case KeyEvent.VK_K:
                skillLoadoutVisible = !skillLoadoutVisible;
                playSoundEffect(UI_TOGGLE_SFX_PATH);
                break;
            default:
                startActivityFromExplorationInput(e.getKeyCode());
                break;
        }

        updatePlayerMovement();
        updateCamera();
        checkBattleTrigger();
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                upPressed = false;
                break;
            case KeyEvent.VK_S:
                downPressed = false;
                break;
            case KeyEvent.VK_A:
                leftPressed = false;
                break;
            case KeyEvent.VK_D:
                rightPressed = false;
                break;
            default:
                break;
        }

        updatePlayerMovement();
        if (STATE_IDLE.equals(playerState)) {
            currentFrame = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!shopVisible) {
            return;
        }

        List<Item> availableItems = shop.getAvailableItems();
        int itemCount = Math.min(availableItems.size(), 24);
        int clickedItemIndex = shopOverlayState.findClickedItemIndex(e.getX(), e.getY(), getWidth(), getHeight(), itemCount);
        if (clickedItemIndex >= 0) {
            shopOverlayState.setSelectedItemIndex(clickedItemIndex, itemCount);
            activityMessage = "Memilih " + availableItems.get(clickedItemIndex).getName() + ".";
            repaint();
            requestFocusInWindow();
            return;
        }

        if (shopOverlayState.getBuyButtonBounds(getWidth(), getHeight()).contains(e.getPoint())) {
            buySelectedShopItem();
            return;
        }

        if (shopOverlayState.getExitButtonBounds(getWidth(), getHeight()).contains(e.getPoint())) {
            shopVisible = false;
            playSoundEffect(UI_TOGGLE_SFX_PATH);
            repaint();
            requestFocusInWindow();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public String getCurrentAreaName() {
        return gameMap.getCurrentArea().getAreaName();
    }
}






