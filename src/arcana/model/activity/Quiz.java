package arcana.model.activity;

import arcana.model.character.Character;

public class Quiz extends Activity  {
	
	public Quiz() {
		super("Quiz", 30, 15);
	}
	
	@Override 
	public String giveReward(Character character) {
		String rewardSummary = applyBaseReward(character);
		boolean arcaneBlastUnlocked = character.unlockSkill("Arcane Blast");
		if (arcaneBlastUnlocked) {
			return rewardSummary + ", Arcane Blast terbuka.";
		}
		return rewardSummary + ", Arcane Blast sudah tersedia.";
	}
}
