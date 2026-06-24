package arcana.model.activity;

import arcana.model.character.Character;
import arcana.model.item.Fish;

public class Fishing extends Activity {
	public Fishing() {
		super("Fishing", 20, 10);
	}
	
	@Override
	public String giveReward(Character character) {
		String rewardSummary = applyBaseReward(character);
		Fish rewardFish = new Fish("Moonlake Fish", 8);
		character.addItem(rewardFish);
		return rewardSummary + ", mendapat " + rewardFish.getName() + ".";
	}
}
