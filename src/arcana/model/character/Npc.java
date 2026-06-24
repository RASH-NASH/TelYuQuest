package arcana.model.character;

import arcana.model.sprite.SpriteConfig;

public class Npc {
    private String name;
    private String dialogue;
    private int x;
    private int y;
    private SpriteConfig spriteConfig;

    public Npc(String name, String dialogue, int x, int y, SpriteConfig spriteConfig) {
        this.name = name;
        this.dialogue = dialogue;
        this.x = x;
        this.y = y;
        this.spriteConfig = spriteConfig;
    }

    public String talk() {
        return name + ": " + dialogue;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public SpriteConfig getSpriteConfig() {
        return spriteConfig;
    }
}
