package arcana.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import arcana.model.battle.BattleManager;
import arcana.model.character.Character;
import arcana.model.monster.ArcaneWisp;
import arcana.model.monster.Golem;
import arcana.model.monster.Monster;
import arcana.model.monster.MonsterAction;
import arcana.model.sprite.SpriteConfig;

public class BattlePanel extends JPanel {
    private static final int ENTITY_SIZE = 160;
    private static final int STATUS_BAR_WIDTH = 110;
    private static final int STATUS_BAR_HEIGHT = 10;
    private static final String BATTLE_BACKGROUND_PATH = "assets/battle_scene/dummy_area_scene.png";
    private static final String FIREBALL_EFFECT_PATH = "assets/skill_animation/fireball.png";
    private static final String GOLEM_ROCK_EFFECT_PATH = "assets/skill_animation/golem_rock.png";
    private static final String WISP_FIREBALL_EFFECT_PATH = "assets/skill_animation/wisp_fireball.png";
    private static final int FIREBALL_EFFECT_FRAME_COUNT = 14;
    private static final int GOLEM_ROCK_EFFECT_FRAME_COUNT = 13;
    private static final int WISP_FIREBALL_EFFECT_FRAME_COUNT = 15;
    private static final int FIREBALL_RENDER_SIZE = 128;
    private static final int ENEMY_EFFECT_RENDER_SIZE = 96;
    private static final int FIREBALL_START_X = 250;
    private static final int FIREBALL_END_X = 800;
    private static final int FIREBALL_Y = 122;
    private static final int ENEMY_EFFECT_START_X = 640;
    private static final int ENEMY_EFFECT_END_X = 255;
    private static final int ENEMY_EFFECT_Y = 140;
    private static final String BATTLE_BGM_PATH = "assets/music/battle_bgm.wav";
    private static final String BATTLE_SKILL_SFX_PATH = "assets/sfx/battle_skill.wav";
    private static final String FIRE_MAGIC_VOICE_PATH = "assets/voice/incantation/fire_magic.wav";
    private static final String WATER_MAGIC_VOICE_PATH = "assets/voice/incantation/water_magic.wav";
    private static final String ARCANE_BLAST_VOICE_PATH = "assets/voice/incantation/arcane_blast.wav";
    private static final String BATTLE_DEFEND_SFX_PATH = "assets/sfx/battle_defend.wav";
    private static final String BATTLE_EXIT_SFX_PATH = "assets/sfx/battle_exit.wav";
    private static final String BATTLE_VICTORY_SFX_PATH = "assets/sfx/battle_victory.wav";
    private static final String BATTLE_DEFEAT_SFX_PATH = "assets/sfx/battle_defeat.wav";
    private static final int BATTLE_ANIMATION_INTERVAL_MILLIS = 120;
    private static final int PLAYER_ATTACK_PHASE_TICKS = 12;
    private static final int FIREBALL_RELEASE_TICK = 5;
    private static final int PLAYER_DEFEND_PHASE_TICKS = 6;
    private static final int ENEMY_ACTION_PHASE_TICKS = 7;
    private static final int POST_ACTION_CLEANUP_TICKS = 2;

    private GameFrame frame;
    private Character player;
    private BattleManager battleManager;
    private ArenaPanel arenaPanel;
    private Clip battleBgmClip;
    private boolean battleResultSoundPlayed;
    private boolean battleWinProgressConsumed;
    private Timer actionSequenceTimer;

    private JLabel titleLabel;
    private JLabel playerHpLabel;
    private JLabel playerMpLabel;
    private JLabel enemyHpLabel;
    private JLabel messageLabel;

    private List<JButton> skillButtons;
    private JButton defendButton;
    private JButton backButton;

    public BattlePanel(GameFrame frame, Character player) {
        this.frame = frame;
        this.player = player;

        setLayout(new BorderLayout());

        titleLabel = new JLabel("Battle Scene", SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        arenaPanel = new ArenaPanel();
        add(arenaPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        playerHpLabel = new JLabel("Player HP: ", SwingConstants.CENTER);
        playerMpLabel = new JLabel("Player MP: ", SwingConstants.CENTER);
        enemyHpLabel = new JLabel("Enemy HP: ", SwingConstants.CENTER);
        messageLabel = new JLabel("Battle message", SwingConstants.CENTER);

        infoPanel.add(playerHpLabel);
        infoPanel.add(playerMpLabel);
        infoPanel.add(enemyHpLabel);
        infoPanel.add(messageLabel);
        bottomPanel.add(infoPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5));

        skillButtons = new ArrayList<JButton>();
        for (int i = 0; i < player.getSkillSlotCount(); i++) {
            final int slotIndex = i;
            JButton skillButton = new JButton();
            skillButton.addActionListener(e -> {
                if (battleManager == null || battleManager.isBattleOver() || !battleManager.isPlayerTurn()) {
                    return;
                }

                String skillName = player.getEquippedSkillName(slotIndex);
                playSkillCastAudio(skillName);
                battleManager.useEquippedSkill(slotIndex);
                refreshBattle();
                continueActionSequence();
            });
            skillButtons.add(skillButton);
            buttonPanel.add(skillButton);
        }

        defendButton = new JButton("Defend");
        backButton = new JButton("Run / Back");

        defendButton.addActionListener(e -> {
            if (battleManager == null || battleManager.isBattleOver() || !battleManager.isPlayerTurn()) {
                return;
            }

            playSoundEffect(BATTLE_DEFEND_SFX_PATH);
            battleManager.defend();
            refreshBattle();
            continueActionSequence();
        });

        backButton.addActionListener(e -> {
            playSoundEffect(BATTLE_EXIT_SFX_PATH);
            stopBattleAudio();
            frame.showGamePanel();
        });

        buttonPanel.add(defendButton);
        buttonPanel.add(backButton);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        Timer battleAnimationTimer = new Timer(BATTLE_ANIMATION_INTERVAL_MILLIS, e -> arenaPanel.repaint());
        battleAnimationTimer.start();
    }

    public void startBattle(Monster monster) {
        stopBattleAudio();
        battleManager = new BattleManager(player, monster);
        battleResultSoundPlayed = false;
        battleWinProgressConsumed = false;
        stopActionSequenceTimer();
        playBattleBgm();
        refreshBattle();
    }

    public String consumeDefeatedEnemyNameForQuest() {
        if (battleManager == null || battleWinProgressConsumed || !battleManager.isPlayerWin()) {
            return null;
        }

        battleWinProgressConsumed = true;
        return battleManager.getEnemy().getName();
    }

    public void onBattleHidden() {
        stopActionSequenceTimer();
        stopBattleAudio();
        if (battleManager != null) {
            battleManager.leaveBattle();
        }
    }

    private void refreshBattle() {
        if (battleManager == null) {
            return;
        }

        titleLabel.setText(buildTitleText());
        playerHpLabel.setText("Player HP: " + battleManager.getPlayerHp() + "/" + battleManager.getPlayerMaxHp());
        playerMpLabel.setText("Player MP: " + battleManager.getPlayerMp() + "/" + battleManager.getPlayerMaxMp());
        enemyHpLabel.setText(battleManager.getEnemy().getName() + " HP: " + battleManager.getEnemyHp() + "/"
                + battleManager.getEnemyMaxHp());
        messageLabel.setText(battleManager.getBattleMessage());

        boolean battleOver = battleManager.isBattleOver();
        boolean playerTurn = battleManager.isPlayerTurn();
        for (int i = 0; i < skillButtons.size(); i++) {
            JButton skillButton = skillButtons.get(i);
            String skillName = player.getEquippedSkillName(i);
            if (skillName == null || skillName.isBlank()) {
                skillButton.setText("Empty Slot");
                skillButton.setEnabled(false);
            } else {
                int mpCost = player.getSkillMpCost(skillName);
                skillButton.setText(skillName + " (" + mpCost + " MP)");
                skillButton.setEnabled(playerTurn && player.hasEnoughMp(mpCost));
            }
        }
        defendButton.setEnabled(playerTurn);

        if (battleOver) {
            backButton.setText("Back");
            if (!battleResultSoundPlayed) {
                stopBattleAudio();
                if (battleManager.getEnemyHp() <= 0) {
                    playSoundEffect(BATTLE_VICTORY_SFX_PATH);
                } else {
                    playSoundEffect(BATTLE_DEFEAT_SFX_PATH);
                }
                battleResultSoundPlayed = true;
            }
        } else {
            backButton.setText("Run");
        }

        arenaPanel.repaint();
    }

    private String buildTitleText() {
        if (battleManager == null) {
            return "Battle Scene";
        }

        if (battleManager.isBattleOver()) {
            return battleManager.isPlayerWin() ? "Battle Won" : "Battle Lost";
        }

        return battleManager.isPlayerTurn() ? "Battle Scene - Your Turn" : "Battle Scene - Enemy Turn";
    }

    private void continueActionSequence() {
        if (battleManager == null || !battleManager.isActionSequenceActive()) {
            return;
        }

        stopActionSequenceTimer();
        actionSequenceTimer = new Timer(getSequenceDelayMillis(), e -> {
            battleManager.progressActionSequence();
            refreshBattle();
            if (battleManager != null && battleManager.isActionSequenceActive()) {
                continueActionSequence();
            }
        });
        actionSequenceTimer.setRepeats(false);
        actionSequenceTimer.start();
    }

    private void stopActionSequenceTimer() {
        if (actionSequenceTimer != null) {
            actionSequenceTimer.stop();
            actionSequenceTimer = null;
        }
    }

    private int getSequenceDelayMillis() {
        if (battleManager == null) {
            return BATTLE_ANIMATION_INTERVAL_MILLIS;
        }

        if (battleManager.hasPendingEnemyTurn()) {
            if ("attack".equals(battleManager.getPlayerActionState())) {
                return PLAYER_ATTACK_PHASE_TICKS * BATTLE_ANIMATION_INTERVAL_MILLIS;
            }
            return PLAYER_DEFEND_PHASE_TICKS * BATTLE_ANIMATION_INTERVAL_MILLIS;
        }

        MonsterAction enemyAction = battleManager.getEnemy().getCurrentAction();
        if (enemyAction == MonsterAction.ATTACK || enemyAction == MonsterAction.BLOCK
                || enemyAction == MonsterAction.DEFEND) {
            return ENEMY_ACTION_PHASE_TICKS * BATTLE_ANIMATION_INTERVAL_MILLIS;
        }

        return POST_ACTION_CLEANUP_TICKS * BATTLE_ANIMATION_INTERVAL_MILLIS;
    }

    private void playBattleBgm() {
        battleBgmClip = openClip(BATTLE_BGM_PATH);
        if (battleBgmClip != null) {
            battleBgmClip.setFramePosition(0);
            battleBgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            battleBgmClip.start();
        }
    }

    private void stopBattleAudio() {
        if (battleBgmClip == null) {
            return;
        }

        battleBgmClip.stop();
        battleBgmClip.close();
        battleBgmClip = null;
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

    private void playSkillCastAudio(String skillName) {
        if (skillName == null || skillName.isBlank()) {
            return;
        }

        playSoundEffect(BATTLE_SKILL_SFX_PATH);

        String incantationVoicePath = getIncantationVoicePath(skillName);
        if (incantationVoicePath != null) {
            playSoundEffect(incantationVoicePath);
        }
    }

    private String getIncantationVoicePath(String skillName) {
        if ("Fire Magic".equalsIgnoreCase(skillName)) {
            return FIRE_MAGIC_VOICE_PATH;
        }
        if ("Water Magic".equalsIgnoreCase(skillName)) {
            return WATER_MAGIC_VOICE_PATH;
        }
        if ("Arcane Blast".equalsIgnoreCase(skillName)) {
            return ARCANE_BLAST_VOICE_PATH;
        }

        return buildGenericIncantationVoicePath(skillName);
    }

    private String buildGenericIncantationVoicePath(String skillName) {
        String normalizedSkillName = skillName.trim().toLowerCase().replace(' ', '_');
        normalizedSkillName = normalizedSkillName.replaceAll("[^a-z0-9_]+", "");
        if (normalizedSkillName.isBlank()) {
            return null;
        }

        return "assets/voice/incantation/" + normalizedSkillName + ".wav";
    }

    private class ArenaPanel extends JPanel {
        private BufferedImage battleBackground;
        private BufferedImage[] fireballFrames;
        private BufferedImage[] golemRockFrames;
        private BufferedImage[] wispFireballFrames;
        private BufferedImage[] playerAttackFrames;
        private BufferedImage[] playerIdleFrames;
        private BufferedImage[] cachedEnemyFrames;
        private Monster cachedEnemy;
        private MonsterAction cachedEnemyAction;
        private int battleFrame;
        private String lastPlayerVisualState;
        private int playerActionFrameIndex;
        private int playerAttackVisualTick;
        private String lastPlayerSkillEffect;
        private MonsterAction lastEnemyVisualAction;
        private int enemyActionFrameIndex;
        private int enemyAttackVisualTick;

        public ArenaPanel() {
            setBackground(new Color(35, 38, 54));
            battleBackground = SpriteLoader.loadImage(BATTLE_BACKGROUND_PATH, new Color(46, 68, 88), 960, 420);
            fireballFrames = SpriteLoader.loadAnimation(FIREBALL_EFFECT_PATH, FIREBALL_EFFECT_FRAME_COUNT,
                    new Color(255, 120, 40), FIREBALL_RENDER_SIZE, FIREBALL_RENDER_SIZE);
            golemRockFrames = SpriteLoader.loadAnimation(GOLEM_ROCK_EFFECT_PATH, GOLEM_ROCK_EFFECT_FRAME_COUNT,
                    new Color(145, 120, 98), ENEMY_EFFECT_RENDER_SIZE, ENEMY_EFFECT_RENDER_SIZE);
            wispFireballFrames = SpriteLoader.loadAnimation(WISP_FIREBALL_EFFECT_PATH, WISP_FIREBALL_EFFECT_FRAME_COUNT,
                    new Color(90, 170, 255), ENEMY_EFFECT_RENDER_SIZE, ENEMY_EFFECT_RENDER_SIZE);
            SpriteConfig playerSprites = player.getSpriteConfig();
            playerIdleFrames = SpriteLoader.loadAnimation(playerSprites.getIdlePath(), playerSprites.getIdleFrameCount(),
                    Color.BLUE, ENTITY_SIZE, ENTITY_SIZE);
            playerAttackFrames = SpriteLoader.loadAnimation(playerSprites.getAttackPath(),
                    playerSprites.getAttackFrameCount(), Color.CYAN, ENTITY_SIZE, ENTITY_SIZE);
            battleFrame = 0;
            lastPlayerVisualState = "";
            playerActionFrameIndex = 0;
            playerAttackVisualTick = 0;
            lastPlayerSkillEffect = "";
            lastEnemyVisualAction = null;
            enemyActionFrameIndex = 0;
            enemyAttackVisualTick = 0;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (battleManager == null) {
                return;
            }

            battleFrame++;
            drawBattleBackground(g);
            BufferedImage playerImage = getPlayerFrame();
            g.drawImage(playerImage, 160, 120, ENTITY_SIZE, ENTITY_SIZE, null);
            drawPlayerStatus(g);
            drawPlayerSkillEffect(g);

            Monster enemy = battleManager.getEnemy();
            BufferedImage[] enemyFrames = getEnemyFrames(enemy);
            BufferedImage enemyImage = getFrame(enemyFrames);
            g.drawImage(enemyImage, 650, 120, ENTITY_SIZE, ENTITY_SIZE, null);
            drawEnemySkillEffect(g, enemy);
            drawEnemyStatus(g, enemy);
        }

        private void drawBattleBackground(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(battleBackground, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(18, 31, 44, 140));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(new Color(81, 122, 77, 190));
            g2d.fillOval(80, 240, 250, 90);
            g2d.fillOval(560, 240, 250, 90);
            g2d.setColor(new Color(255, 255, 255, 40));
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2d.drawString("Battle background asset path: " + BATTLE_BACKGROUND_PATH, 12, getHeight() - 12);
            g2d.dispose();
        }

        private BufferedImage getPlayerFrame() {
            String playerState = battleManager.getPlayerActionState();
            if (!playerState.equals(lastPlayerVisualState)) {
                lastPlayerVisualState = playerState;
                playerActionFrameIndex = 0;
                playerAttackVisualTick = 0;
            }

            String currentSkillEffect = battleManager.getLastPlayerSkillName();
            if (currentSkillEffect == null) {
                currentSkillEffect = "";
            }
            if (!currentSkillEffect.equals(lastPlayerSkillEffect)) {
                lastPlayerSkillEffect = currentSkillEffect;
            }

            if ("attack".equals(playerState)) {
                if (playerAttackVisualTick < PLAYER_ATTACK_PHASE_TICKS) {
                    playerAttackVisualTick++;
                }
                return getActionFrame(playerAttackFrames, false, true);
            }
            playerAttackVisualTick = 0;
            return getActionFrame(playerIdleFrames, true, false);
        }

        private void drawPlayerSkillEffect(Graphics g) {
            if (!"attack".equals(battleManager.getPlayerActionState())) {
                return;
            }

            String skillName = battleManager.getLastPlayerSkillName();
            if (skillName == null || !skillName.equalsIgnoreCase("Fire Magic")) {
                return;
            }

            if (playerAttackVisualTick < FIREBALL_RELEASE_TICK) {
                return;
            }

            int fireballTravelTicks = Math.max(1, PLAYER_ATTACK_PHASE_TICKS - FIREBALL_RELEASE_TICK);
            double progress = Math.min(1.0,
                    (playerAttackVisualTick - FIREBALL_RELEASE_TICK) / (double) fireballTravelTicks);
            int drawX = FIREBALL_START_X + (int) Math.round((FIREBALL_END_X - FIREBALL_START_X) * progress);
            int fireballFrameIndex = Math.min(fireballFrames.length - 1,
                    (int) Math.round((fireballFrames.length - 1) * progress));
            BufferedImage fireballFrame = fireballFrames[fireballFrameIndex];
            g.drawImage(fireballFrame, drawX, FIREBALL_Y, FIREBALL_RENDER_SIZE, FIREBALL_RENDER_SIZE, null);
        }

        private void drawEnemySkillEffect(Graphics g, Monster enemy) {
            if (enemy.getCurrentAction() != MonsterAction.ATTACK) {
                enemyAttackVisualTick = 0;
                return;
            }

            BufferedImage[] effectFrames = getEnemySkillEffectFrames(enemy);
            if (effectFrames == null || effectFrames.length == 0) {
                return;
            }

            if (enemyAttackVisualTick < ENEMY_ACTION_PHASE_TICKS) {
                enemyAttackVisualTick++;
            }

            double progress = Math.min(1.0, enemyAttackVisualTick / (double) ENEMY_ACTION_PHASE_TICKS);
            int drawX = ENEMY_EFFECT_START_X
                    + (int) Math.round((ENEMY_EFFECT_END_X - ENEMY_EFFECT_START_X) * progress);
            int effectFrameIndex = Math.min(effectFrames.length - 1,
                    (int) Math.round((effectFrames.length - 1) * progress));
            BufferedImage effectFrame = effectFrames[effectFrameIndex];
            g.drawImage(effectFrame, drawX, ENEMY_EFFECT_Y, ENEMY_EFFECT_RENDER_SIZE, ENEMY_EFFECT_RENDER_SIZE, null);
        }

        private BufferedImage[] getEnemySkillEffectFrames(Monster enemy) {
            if (enemy instanceof Golem) {
                return golemRockFrames;
            }
            if (enemy instanceof ArcaneWisp) {
                return wispFireballFrames;
            }
            return null;
        }

        private void drawPlayerStatus(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            drawStatusBar(g2d, 185, 286, STATUS_BAR_WIDTH, STATUS_BAR_HEIGHT, battleManager.getPlayerHp(),
                    battleManager.getPlayerMaxHp(), new Color(214, 70, 70), "HP");
            drawStatusBar(g2d, 185, 301, STATUS_BAR_WIDTH, STATUS_BAR_HEIGHT, battleManager.getPlayerMp(),
                    battleManager.getPlayerMaxMp(), new Color(62, 144, 255), "MP");
            g2d.dispose();
        }

        private void drawEnemyStatus(Graphics g, Monster enemy) {
            Graphics2D g2d = (Graphics2D) g.create();
            drawStatusBar(g2d, 675, 286, STATUS_BAR_WIDTH, STATUS_BAR_HEIGHT, battleManager.getEnemyHp(),
                    battleManager.getEnemyMaxHp(), new Color(214, 70, 70), "HP");
            g2d.dispose();
        }

        private void drawStatusBar(Graphics2D g2d, int x, int y, int width, int height, int currentValue, int maxValue,
                Color fillColor, String label) {
            int safeMaxValue = Math.max(1, maxValue);
            int clampedValue = Math.max(0, Math.min(currentValue, safeMaxValue));
            int fillWidth = (int) Math.round((clampedValue / (double) safeMaxValue) * width);

            g2d.setColor(new Color(12, 18, 28, 220));
            g2d.fillRoundRect(x - 2, y - 2, width + 4, height + 4, 12, 12);
            g2d.setColor(new Color(42, 49, 60, 210));
            g2d.fillRoundRect(x, y, width, height, 10, 10);
            g2d.setColor(fillColor);
            g2d.fillRoundRect(x, y, fillWidth, height, 10, 10);
            g2d.setColor(new Color(255, 255, 255, 160));
            g2d.drawRoundRect(x, y, width, height, 10, 10);
        }

        private BufferedImage getFrame(BufferedImage[] frames) {
            return frames[battleFrame % frames.length];
        }

        private BufferedImage getActionFrame(BufferedImage[] frames, boolean loop, boolean playerFrame) {
            if (frames == null || frames.length == 0) {
                return SpriteLoader.loadImage(null, Color.WHITE, ENTITY_SIZE, ENTITY_SIZE);
            }

            if (loop) {
                return getFrame(frames);
            }

            int frameIndex = playerFrame ? playerActionFrameIndex : enemyActionFrameIndex;
            BufferedImage frame = frames[Math.min(frameIndex, frames.length - 1)];

            if (playerFrame) {
                if (playerActionFrameIndex < frames.length - 1) {
                    playerActionFrameIndex++;
                }
            } else {
                if (enemyActionFrameIndex < frames.length - 1) {
                    enemyActionFrameIndex++;
                }
            }

            return frame;
        }

        private BufferedImage[] getEnemyFrames(Monster enemy) {
            MonsterAction action = enemy.getCurrentAction();
            if (action != lastEnemyVisualAction) {
                lastEnemyVisualAction = action;
                enemyActionFrameIndex = 0;
                enemyAttackVisualTick = 0;
            }
            if (enemy != cachedEnemy || action != cachedEnemyAction || cachedEnemyFrames == null) {
                SpriteConfig config = enemy.getSpriteConfig();
                String path = getEnemySpritePath(config, action);
                int frameCount = getEnemyFrameCount(config, action);
                cachedEnemyFrames = SpriteLoader.loadAnimation(path, frameCount, Color.GREEN, ENTITY_SIZE, ENTITY_SIZE);
                cachedEnemy = enemy;
                cachedEnemyAction = action;
            }

            boolean loop = action == MonsterAction.IDLE;
            return new BufferedImage[] { getActionFrame(cachedEnemyFrames, loop, false) };
        }

        private String getEnemySpritePath(SpriteConfig config, MonsterAction action) {
            if (action == MonsterAction.ATTACK) {
                return config.getAttackPath();
            } else if (action == MonsterAction.BLOCK) {
                return config.getBlockPath();
            } else if (action == MonsterAction.DEFEND) {
                return config.getDefendPath();
            } else if (action == MonsterAction.HURT) {
                return config.getHurtPath();
            } else if (action == MonsterAction.DEFEATED) {
                return config.getDefeatedPath();
            }

            return config.getIdlePath();
        }

        private int getEnemyFrameCount(SpriteConfig config, MonsterAction action) {
            if (action == MonsterAction.ATTACK) {
                return config.getAttackFrameCount();
            } else if (action == MonsterAction.BLOCK) {
                return config.getBlockFrameCount();
            } else if (action == MonsterAction.DEFEND) {
                return config.getDefendFrameCount();
            } else if (action == MonsterAction.HURT) {
                return config.getHurtFrameCount();
            } else if (action == MonsterAction.DEFEATED) {
                return config.getDefeatedFrameCount();
            }

            return config.getIdleFrameCount();
        }
    }
}
