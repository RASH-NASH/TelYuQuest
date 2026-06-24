package arcana.model.activity;

import arcana.model.character.Character;

public abstract class Activity implements Rewardable {
	private String activityName;
	private int expReward;
	private int goldReward;
	
	public Activity(String activityName, int expReward, int goldReward) {
		this.activityName = activityName;
		this.expReward = expReward;
		this.goldReward = goldReward;
	}
	
	public String getActivityName() {
		return this.activityName;
	}
	
	public String execute(Character character) {
		return character.getName() + " menyelesaikan " + activityName.toLowerCase() + ": " + giveReward(character);
	}

	protected String applyBaseReward(Character character) {
		character.gainExp(expReward);
		character.addGold(goldReward);
		return "+" + expReward + " EXP, +" + goldReward + " Gold";
	}
}
