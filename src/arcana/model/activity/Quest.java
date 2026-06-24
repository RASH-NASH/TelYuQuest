package arcana.model.activity;

import arcana.model.character.Character;

public class Quest extends Activity {
    public enum ObjectiveType {
        DEFEAT_MONSTER,
        CATCH_FISH
    }

    private String questName;
    private String description;
    private ObjectiveType objectiveType;
    private String targetName;
    private int targetAmount;
    private int progress;
    private int expReward;
    private int goldReward;
    private boolean accepted;
    private boolean rewardClaimed;

    public Quest() {
        this("Guild Errand", "Bantu staff guild menyelesaikan tugas ringan.", ObjectiveType.CATCH_FISH,
                "Fish", 1, 40, 20);
    }

    public Quest(String questName, String description, ObjectiveType objectiveType, String targetName, int targetAmount,
            int expReward, int goldReward) {
        super("Quest", expReward, goldReward);
        this.questName = questName;
        this.description = description;
        this.objectiveType = objectiveType;
        this.targetName = targetName;
        this.targetAmount = targetAmount;
        this.expReward = expReward;
        this.goldReward = goldReward;
        this.progress = 0;
        this.accepted = false;
        this.rewardClaimed = false;
    }

    public void accept() {
        accepted = true;
        progress = 0;
        rewardClaimed = false;
    }

    public boolean matchesMonster(String monsterName) {
        return objectiveType == ObjectiveType.DEFEAT_MONSTER
                && targetName.equalsIgnoreCase(monsterName);
    }

    public boolean matchesFishCatch() {
        return objectiveType == ObjectiveType.CATCH_FISH;
    }

    public void addProgress(int amount) {
        if (!accepted || rewardClaimed || amount <= 0) {
            return;
        }
        progress = Math.min(targetAmount, progress + amount);
    }

    public boolean isComplete() {
        return progress >= targetAmount;
    }

    public boolean canClaimReward() {
        return accepted && isComplete() && !rewardClaimed;
    }

    public String claimReward(Character character) {
        if (!canClaimReward()) {
            return "Quest belum selesai.";
        }

        character.gainExp(expReward);
        character.addGold(goldReward);
        rewardClaimed = true;
        accepted = false;
        return questName + " selesai: +" + expReward + " EXP, +" + goldReward + " Gold.";
    }

    @Override
    public String execute(Character character) {
        return claimReward(character);
    }

    @Override
    public String giveReward(Character character) {
        return claimReward(character);
    }

    public String getQuestName() {
        return questName;
    }

    public String getDescription() {
        return description;
    }

    public ObjectiveType getObjectiveType() {
        return objectiveType;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public int getProgress() {
        return progress;
    }

    public int getExpReward() {
        return expReward;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public String getObjectiveText() {
        if (objectiveType == ObjectiveType.DEFEAT_MONSTER) {
            return "Kalahkan " + targetAmount + " " + targetName + ".";
        }
        return "Pancing " + targetAmount + " ikan.";
    }
}
