package arcana.model.battle;

import arcana.model.character.Character;
import arcana.model.monster.Monster;
import arcana.model.monster.MonsterAction;

public class BattleManager {
    private static final String PLAYER_STATE_IDLE = "idle";
    private static final String PLAYER_STATE_ATTACK = "attack";
    private static final String PLAYER_STATE_DEFEND = "defend";
    private static final String PLAYER_STATE_HURT = "hurt";
    private static final int DEFEND_MP_RECOVERY = 8;

    private enum SequencePhase {
        IDLE,
        PLAYER_ACTION,
        ENEMY_ACTION,
        CLEANUP
    }

    private final Character player;
    private final Monster enemy;
    private boolean playerTurn;
    private boolean playerDefending;
    private boolean actionSequenceActive;
    private boolean pendingEnemyTurn;
    private int pendingEnemyAttackDamage;
    private SequencePhase sequencePhase;
    private boolean rewardGiven;
    private String battleMessage;
    private String playerActionState;
    private String lastPlayerSkillName;

    public BattleManager(Character player, Monster enemy) {
        this.player = player;
        this.enemy = enemy;
        this.playerTurn = true;
        this.playerDefending = false;
        this.actionSequenceActive = false;
        this.pendingEnemyTurn = false;
        this.pendingEnemyAttackDamage = 0;
        this.sequencePhase = SequencePhase.IDLE;
        this.rewardGiven = false;
        this.playerActionState = PLAYER_STATE_IDLE;
        this.lastPlayerSkillName = null;
        this.enemy.resetBattleAction();
        this.battleMessage = "A wild " + enemy.getName() + " appeared!";
    }

    public void useEquippedSkill(int slotIndex) {
        String skillName = player.getEquippedSkillName(slotIndex);
        if (skillName == null || skillName.isBlank()) {
            battleMessage = "Slot skill " + (slotIndex + 1) + " masih kosong.";
            return;
        }

        usePlayerSkill(skillName, skillName + " belum tersedia.");
    }

    public void defend() {
        if (!canPlayerAct()) {
            return;
        }

        int recoveredMp = player.restoreMp(DEFEND_MP_RECOVERY);
        playerTurn = false;
        actionSequenceActive = true;
        pendingEnemyTurn = true;
        pendingEnemyAttackDamage = 0;
        sequencePhase = SequencePhase.PLAYER_ACTION;
        playerActionState = PLAYER_STATE_DEFEND;
        playerDefending = true;
        battleMessage = player.getName() + " braces for the next attack and restores " + recoveredMp + " MP.";
    }

    private void usePlayerSkill(String skillName, String lockedMessage) {
        if (!canPlayerAct()) {
            return;
        }

        if (!player.isSkillUnlocked(skillName)) {
            battleMessage = lockedMessage;
            return;
        }

        int damage = player.getSkillPower(skillName);
        if (damage <= 0) {
            battleMessage = skillName + " tidak bisa digunakan saat ini.";
            return;
        }

        int mpCost = player.getSkillMpCost(skillName);
        if (!player.spendMp(mpCost)) {
            battleMessage = player.getName() + " tidak punya cukup MP untuk " + skillName + " (" + mpCost + " MP).";
            return;
        }

        playerTurn = false;
        actionSequenceActive = true;
        playerActionState = PLAYER_STATE_ATTACK;
        lastPlayerSkillName = skillName;
        playerDefending = false;
        pendingEnemyAttackDamage = 0;
        sequencePhase = SequencePhase.PLAYER_ACTION;
        int finalDamage = enemy.takeDamage(damage);
        battleMessage = player.getName() + " used " + skillName + " for " + finalDamage + " damage (" + mpCost
                + " MP).";

        if (enemy.isDefeated()) {
            finishWin();
        } else {
            pendingEnemyTurn = true;
        }
    }

    private boolean canPlayerAct() {
        return playerTurn && !actionSequenceActive && !isBattleOver();
    }

    private void startEnemyTurn() {
        MonsterAction action = enemy.chooseBattleAction();
        sequencePhase = SequencePhase.ENEMY_ACTION;

        if (action == MonsterAction.ATTACK) {
            pendingEnemyAttackDamage = playerDefending ? Math.max(1, enemy.getAttackPower() / 2) : enemy.getAttackPower();
            battleMessage += " " + enemy.getName() + " prepares to attack.";
        } else if (action == MonsterAction.BLOCK) {
            pendingEnemyAttackDamage = 0;
            battleMessage += " " + enemy.getName() + " is blocking.";
        } else if (action == MonsterAction.DEFEND) {
            pendingEnemyAttackDamage = 0;
            battleMessage += " " + enemy.getName() + " is defending.";
        }
    }

    private void enemyAttack(int damage) {
        int finalDamage = player.takeDamage(damage);

        if (player.getCurrentHp() <= 0) {
            playerActionState = PLAYER_STATE_HURT;
            battleMessage = player.getName() + " lost the battle.";
        } else {
            battleMessage += " " + enemy.getName() + " attacked for " + finalDamage + " damage.";
        }
    }

    private void finishWin() {
        if (!rewardGiven) {
            player.gainExp(enemy.getExpReward());
            player.addGold(enemy.getGoldReward());
            rewardGiven = true;
        }

        battleMessage = enemy.getName() + " defeated! +" + enemy.getExpReward() + " EXP, +" + enemy.getGoldReward()
                + " Gold.";
    }

    public boolean isBattleOver() {
        return enemy.isDefeated() || player.getCurrentHp() <= 0;
    }

    public boolean isPlayerWin() {
        return enemy.isDefeated();
    }

    public int getPlayerHp() {
        return player.getCurrentHp();
    }

    public int getPlayerMaxHp() {
        return player.getMaxHp();
    }

    public int getEnemyHp() {
        return enemy.getHp();
    }

    public int getEnemyMaxHp() {
        return enemy.getMaxHp();
    }

    public int getPlayerMp() {
        return player.getCurrentMp();
    }

    public int getPlayerMaxMp() {
        return player.getMaxMp();
    }

    public String getBattleMessage() {
        return battleMessage;
    }

    public Monster getEnemy() {
        return enemy;
    }

    public String getPlayerActionState() {
        return playerActionState;
    }

    public boolean isPlayerTurn() {
        return playerTurn && !actionSequenceActive && !isBattleOver();
    }

    public String getLastPlayerSkillName() {
        return lastPlayerSkillName;
    }

    public boolean isActionSequenceActive() {
        return actionSequenceActive;
    }

    public boolean hasPendingEnemyTurn() {
        return pendingEnemyTurn;
    }

    public void progressActionSequence() {
        if (isBattleOver()) {
            actionSequenceActive = false;
            pendingEnemyTurn = false;
            pendingEnemyAttackDamage = 0;
            sequencePhase = SequencePhase.IDLE;
            return;
        }

        if (sequencePhase == SequencePhase.PLAYER_ACTION) {
            if (pendingEnemyTurn) {
                pendingEnemyTurn = false;
                startEnemyTurn();
            } else {
                finishActionSequence();
            }
            return;
        }

        if (sequencePhase == SequencePhase.ENEMY_ACTION) {
            if (enemy.getCurrentAction() == MonsterAction.ATTACK) {
                enemyAttack(pendingEnemyAttackDamage);
                pendingEnemyAttackDamage = 0;
            }
            playerDefending = false;
            sequencePhase = SequencePhase.CLEANUP;
            return;
        }

        finishActionSequence();
    }

    private void finishActionSequence() {
        playerActionState = PLAYER_STATE_IDLE;
        enemy.resetBattleAction();
        actionSequenceActive = false;
        pendingEnemyTurn = false;
        pendingEnemyAttackDamage = 0;
        sequencePhase = SequencePhase.IDLE;
        playerDefending = false;
        lastPlayerSkillName = null;
        playerTurn = !isBattleOver();
    }

    public void leaveBattle() {
        if (player.getCurrentHp() <= 0) {
            player.restoreFullHp();
        }
        playerTurn = true;
        playerDefending = false;
        actionSequenceActive = false;
        pendingEnemyTurn = false;
        pendingEnemyAttackDamage = 0;
        sequencePhase = SequencePhase.IDLE;
        playerActionState = PLAYER_STATE_IDLE;
        lastPlayerSkillName = null;
        enemy.resetBattleAction();
    }
}
