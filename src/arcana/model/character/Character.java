package arcana.model.character;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arcana.model.inventory.Inventory;
import arcana.model.item.Item;
import arcana.model.sprite.SpriteConfig;
import arcana.model.sprite.SpriteRegistry;

public class Character {
    private static final int SKILL_SLOT_COUNT = 3;
    private static final int HOTBAR_SLOT_COUNT = 8;

    private int x;
    private int y;
    private String name;
    private int exp;
    private int gold;
    private int level;
    private int maxHp;
    private int currentHp;
    private int maxMp;
    private int currentMp;
    private int selectedHotbarSlot;
    private Inventory inventory;
    private ArrayList<Skill> skills;
    private ArrayList<String> equippedSkillSlots;
    private SpriteConfig spriteConfig;
    private String lastAreaName;

    public Character(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.exp = 0;
        this.gold = 0;
        this.level = 1;
        this.maxHp = 100;
        this.currentHp = this.maxHp;
        this.maxMp = 50;
        this.currentMp = this.maxMp;
        this.selectedHotbarSlot = 0;
        this.inventory = new Inventory();
        this.skills = new ArrayList<Skill>();
        this.equippedSkillSlots = new ArrayList<String>();
        this.spriteConfig = SpriteRegistry.PLAYER;

        addSkill(new Skill("Fire Magic", "Serangan api dasar.", 30));
        addSkill(new Skill("Water Magic", "Serangan air dasar.", 25));
        addSkill(new Skill("Arcane Blast", "Ledakan sihir kuat yang didapat dari quiz atau grimoire.", 42));
        unlockSkill("Fire Magic");
        unlockSkill("Water Magic");
        initializeSkillSlots();
    }

    public void moveUp(int speed) {
        y -= speed;
    }

    public void moveDown(int speed) {
        y += speed;
    }

    public void moveLeft(int speed) {
        x -= speed;
    }

    public void moveRight(int speed) {
        x += speed;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void gainExp(int amount) {
        if (amount <= 0) {
            return;
        }

        this.exp += amount;
        while (this.exp >= getExpToNextLevel()) {
            this.exp -= getExpToNextLevel();
            levelUp();
        }
    }

    public void addGold(int amount) {
        this.gold += amount;
        if (this.gold < 0) {
            this.gold = 0;
        }
    }

    public boolean spendGold(int amount) {
        if (gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }

    public void levelUp() {
        this.level++;
        this.maxHp += 10;
        this.maxMp += 5;
        this.currentHp = this.maxHp;
        this.currentMp = this.maxMp;
    }

    public int takeDamage(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int appliedDamage = Math.min(currentHp, amount);
        currentHp -= appliedDamage;
        return appliedDamage;
    }

    public int heal(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int previousHp = currentHp;
        currentHp = Math.min(maxHp, currentHp + amount);
        return currentHp - previousHp;
    }

    public void restoreFullHp() {
        currentHp = maxHp;
    }

    public void restoreFullMp() {
        currentMp = maxMp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = Math.max(0, Math.min(maxHp, currentHp));
    }

    public void setCurrentMp(int currentMp) {
        this.currentMp = Math.max(0, Math.min(maxMp, currentMp));
    }
    
    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public void setExp(int exp) {
        this.exp = Math.max(0, exp);
    }

    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = Math.max(1, maxHp);
    }

    public void setMaxMp(int maxMp) {
        this.maxMp = Math.max(0, maxMp);
    }

    public boolean hasEnoughMp(int amount) {
        return amount <= 0 || currentMp >= amount;
    }

    public boolean spendMp(int amount) {
        if (amount <= 0) {
            return true;
        }
        if (currentMp < amount) {
            return false;
        }
        currentMp -= amount;
        return true;
    }

    public int restoreMp(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int previousMp = currentMp;
        currentMp = Math.min(maxMp, currentMp + amount);
        return currentMp - previousMp;
    }

    public void addItem(Item item) {
        inventory.addItem(item);
    }

    public boolean removeItem(Item item) {
        return inventory.removeItem(item);
    }

    public boolean removeItem(String itemName) {
        return inventory.removeItem(itemName);
    }

    public boolean hasItem(String itemName) {
        return inventory.hasItem(itemName);
    }

    public boolean selectHotbarSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= HOTBAR_SLOT_COUNT) {
            return false;
        }
        selectedHotbarSlot = slotIndex;
        return true;
    }

    public int getSelectedHotbarSlot() {
        return selectedHotbarSlot;
    }

    public Item getHeldItem() {
        return inventory.getItemAt(selectedHotbarSlot);
    }

    public Item getInventoryItemAt(int slotIndex) {
        return inventory.getItemAt(slotIndex);
    }

    public boolean removeItemAt(int slotIndex) {
        return inventory.removeItemAt(slotIndex);
    }

    public boolean setInventoryItemAt(int slotIndex, Item item) {
        return inventory.setItemAt(slotIndex, item);
    }

    public boolean swapInventoryItems(int firstSlotIndex, int secondSlotIndex) {
        return inventory.swapItems(firstSlotIndex, secondSlotIndex);
    }

    public int getInventorySlotCount() {
        return inventory.getMaxSlotCount();
    }

    public void addSkill(Skill skill) {
        if (findSkill(skill.getSkillName()) == null) {
            skills.add(skill);
        }
    }

    public boolean unlockSkill(String skillName) {
        Skill skill = findSkill(skillName);
        if (skill == null) {
            skill = createDefaultSkill(skillName);
            skills.add(skill);
        }

        if (skill.isUnlocked()) {
            return false;
        }

        skill.unlock();
        return true;
    }

    public boolean hasSkill(String skillName) {
        return findSkill(skillName) != null;
    }

    public boolean isSkillUnlocked(String skillName) {
        Skill skill = findSkill(skillName);
        return skill != null && skill.isUnlocked();
    }

    public Skill getSkill(String skillName) {
        return findSkill(skillName);
    }

    public int getSkillPower(String skillName) {
        Skill skill = findSkill(skillName);
        if (skill == null || !skill.isUnlocked()) {
            return 0;
        }
        return skill.getPower();
    }

    public int getSkillMpCost(String skillName) {
        if ("Fire Magic".equalsIgnoreCase(skillName)) {
            return 10;
        }
        if ("Water Magic".equalsIgnoreCase(skillName)) {
            return 8;
        }
        if ("Arcane Blast".equalsIgnoreCase(skillName)) {
            return 15;
        }
        return 6;
    }

    public int getSkillSlotCount() {
        return SKILL_SLOT_COUNT;
    }

    public String getEquippedSkillName(int slotIndex) {
        if (!isValidSkillSlotIndex(slotIndex)) {
            return null;
        }
        return equippedSkillSlots.get(slotIndex);
    }

    public Skill getEquippedSkill(int slotIndex) {
        String skillName = getEquippedSkillName(slotIndex);
        if (skillName == null || skillName.isBlank()) {
            return null;
        }
        return findSkill(skillName);
    }

    public boolean equipSkillToSlot(int slotIndex, String skillName) {
        if (!isValidSkillSlotIndex(slotIndex)) {
            return false;
        }

        if (skillName == null || skillName.isBlank()) {
            equippedSkillSlots.set(slotIndex, null);
            return true;
        }

        Skill skill = findSkill(skillName);
        if (skill == null || !skill.isUnlocked()) {
            return false;
        }

        equippedSkillSlots.set(slotIndex, skill.getSkillName());
        return true;
    }

    public boolean clearSkillSlot(int slotIndex) {
        if (!isValidSkillSlotIndex(slotIndex)) {
            return false;
        }
        equippedSkillSlots.set(slotIndex, null);
        return true;
    }

    public List<Skill> getUnlockedSkills() {
        ArrayList<Skill> unlockedSkills = new ArrayList<Skill>();
        for (Skill skill : skills) {
            if (skill.isUnlocked()) {
                unlockedSkills.add(skill);
            }
        }
        return Collections.unmodifiableList(unlockedSkills);
    }

    public int getExpToNextLevel() {
        return 100 + ((level - 1) * 25);
    }

    public int getTotalExp() {
        int totalExp = exp;
        for (int currentLevel = 1; currentLevel < level; currentLevel++) {
            totalExp += 100 + ((currentLevel - 1) * 25);
        }
        return totalExp;
    }

    private Skill findSkill(String skillName) {
        for (Skill skill : skills) {
            if (skill.getSkillName().equalsIgnoreCase(skillName)) {
                return skill;
            }
        }
        return null;
    }

    private Skill createDefaultSkill(String skillName) {
        if ("Fire Magic".equalsIgnoreCase(skillName)) {
            return new Skill("Fire Magic", "Serangan api dasar.", 30);
        }
        if ("Water Magic".equalsIgnoreCase(skillName)) {
            return new Skill("Water Magic", "Serangan air dasar.", 25);
        }
        if ("Arcane Blast".equalsIgnoreCase(skillName)) {
            return new Skill("Arcane Blast", "Ledakan sihir kuat yang didapat dari quiz atau grimoire.", 42);
        }
        return new Skill(skillName, "Skill baru dari akademi.", 20);
    }

    private void initializeSkillSlots() {
        for (int i = 0; i < SKILL_SLOT_COUNT; i++) {
            equippedSkillSlots.add(null);
        }

        equipSkillToSlot(0, "Fire Magic");
        equipSkillToSlot(1, "Water Magic");
    }

    private boolean isValidSkillSlotIndex(int slotIndex) {
        return slotIndex >= 0 && slotIndex < equippedSkillSlots.size();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public String getName() {
        return this.name;
    }

    public int getExp() {
        return this.exp;
    }

    public int getGold() {
        return this.gold;
    }

    public int getLevel() {
        return this.level;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    public int getCurrentMp() {
        return currentMp;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ArrayList<Skill> getSkills() {
        return skills;
    }

    public SpriteConfig getSpriteConfig() {
        return spriteConfig;
    }
    
    public String getLastAreaName() {
        return lastAreaName;
    }

    public void setLastAreaName(String lastAreaName) {
        this.lastAreaName = lastAreaName;
    }
}
