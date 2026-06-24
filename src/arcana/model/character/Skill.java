package arcana.model.character;

public class Skill {
    private final String skillName;
    private final String description;
    private final int power;
    private boolean unlocked;

    public Skill(String skillName, String description) {
        this(skillName, description, 0);
    }

    public Skill(String skillName, String description, int power) {
        this.skillName = skillName;
        this.description = description;
        this.power = Math.max(0, power);
        this.unlocked = false;
    }

    public void unlock() {
        unlocked = true;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public String getSkillName() {
        return skillName;
    }

    public String getDescription() {
        return description;
    }

    public int getPower() {
        return power;
    }
}
