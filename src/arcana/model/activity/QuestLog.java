package arcana.model.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arcana.model.activity.Quest.ObjectiveType;
import arcana.model.character.Character;

public class QuestLog {
    private final ArrayList<Quest> guildQuests;
    private int selectedQuestIndex;

    public QuestLog() {
        guildQuests = new ArrayList<Quest>();
        guildQuests.add(new Quest("Slime Control", "Bersihkan slime liar di area farming akademi.",
                ObjectiveType.DEFEAT_MONSTER, "Slime", 3, 75, 35));
        guildQuests.add(new Quest("Stone Sentinel", "Uji kekuatanmu dengan mengalahkan golem penjaga.",
                ObjectiveType.DEFEAT_MONSTER, "Golem", 1, 110, 55));
        guildQuests.add(new Quest("Wisp Disturbance", "Redakan gangguan Arcane Wisp di sekitar academy yard.",
                ObjectiveType.DEFEAT_MONSTER, "Arcane Wisp", 2, 95, 45));
        guildQuests.add(new Quest("Lake Provision", "Penuhi stok makanan guild dengan hasil pancingan.",
                ObjectiveType.CATCH_FISH, "Fish", 10, 90, 50));
        selectedQuestIndex = 0;
    }

    public List<Quest> getQuests() {
        return Collections.unmodifiableList(guildQuests);
    }

    public int getSelectedQuestIndex() {
        return selectedQuestIndex;
    }

    public void setSelectedQuestIndex(int selectedQuestIndex) {
        if (guildQuests.isEmpty()) {
            this.selectedQuestIndex = 0;
            return;
        }
        this.selectedQuestIndex = Math.max(0, Math.min(selectedQuestIndex, guildQuests.size() - 1));
    }

    public void moveSelection(int step) {
        if (guildQuests.isEmpty()) {
            selectedQuestIndex = 0;
            return;
        }
        selectedQuestIndex = (selectedQuestIndex + step + guildQuests.size()) % guildQuests.size();
    }

    public Quest getActiveQuest() {
        for (Quest quest : guildQuests) {
            if (quest.isAccepted() && !quest.isRewardClaimed()) {
                return quest;
            }
        }
        return null;
    }

    public void alignSelectionWithActiveQuest() {
        Quest activeQuest = getActiveQuest();
        if (activeQuest == null) {
            return;
        }
        selectedQuestIndex = guildQuests.indexOf(activeQuest);
    }

    public String acceptSelectedQuest() {
        Quest activeQuest = getActiveQuest();
        if (activeQuest != null) {
            return "Selesaikan quest aktif dulu: " + activeQuest.getQuestName() + ".";
        }

        Quest selectedQuest = guildQuests.get(selectedQuestIndex);
        selectedQuest.accept();
        return "Quest diterima: " + selectedQuest.getQuestName() + ".";
    }

    public String claimOrAccept(Character character) {
        Quest activeQuest = getActiveQuest();
        if (activeQuest == null) {
            return acceptSelectedQuest();
        }

        if (!activeQuest.canClaimReward()) {
            return "Quest belum selesai: " + activeQuest.getProgress() + "/" + activeQuest.getTargetAmount() + ".";
        }

        return activeQuest.claimReward(character);
    }

    public String recordMonsterDefeated(String monsterName) {
        Quest activeQuest = getActiveQuest();
        if (activeQuest == null || !activeQuest.matchesMonster(monsterName)) {
            return null;
        }

        activeQuest.addProgress(1);
        return "Progress quest " + activeQuest.getQuestName() + ": " + activeQuest.getProgress() + "/"
                + activeQuest.getTargetAmount() + ".";
    }

    public String recordFishCaught() {
        Quest activeQuest = getActiveQuest();
        if (activeQuest == null || !activeQuest.matchesFishCatch()) {
            return null;
        }

        activeQuest.addProgress(1);
        return "Progress quest " + activeQuest.getQuestName() + ": " + activeQuest.getProgress() + "/"
                + activeQuest.getTargetAmount() + ".";
    }
}
