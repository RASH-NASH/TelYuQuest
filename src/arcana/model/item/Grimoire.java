package arcana.model.item;

public class Grimoire extends Item {
    private final String relatedSkillName;

    public Grimoire(String name, int price, String relatedSkillName) {
        super(name, price);
        this.relatedSkillName = relatedSkillName == null ? "" : relatedSkillName.trim();
    }

    public String getRelatedSkillName() {
        return relatedSkillName;
    }

    public boolean unlocksSkill(String skillName) {
        return relatedSkillName.equalsIgnoreCase(skillName);
    }
}
