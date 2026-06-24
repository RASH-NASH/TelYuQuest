package arcana.model.sprite;

public class SpriteRegistry {
    /*
     * Path dibaca dari folder src, contoh:
     * src/assets/characters/emma_walk_down.png -> assets/characters/emma_walk_down.png
     */
    public static final SpriteConfig PLAYER = new SpriteConfig(
            "assets/characters/emma_idle_wand.png",
            "assets/characters/emma_walk_up.png",
            "assets/characters/emma_walk_down.png",
            "assets/characters/emma_walk_left.png",
            "assets/characters/emma_walk_right.png",
            "assets/characters/emma_attack_format.png",
            "assets/characters/emma_block.png",
            "assets/characters/emma_defend.png",
            "assets/characters/emma_hurt.png",
            "assets/characters/emma_defeated.png",
            4,
            4,
            6,
            6,
            6,
            14,
            1,
            1,
            1,
            1);

    public static final SpriteConfig SLIME = new SpriteConfig(
            "assets/slime/Monster_Slime_Idle-Sheet.png",
            "assets/slime/Monster_Slime_Walk-Sheet.png",
            "assets/slime/Monster_Slime_Walk-Sheet.png",
            "assets/slime/Monster_Slime_Walk-Sheet.png",
            "assets/slime/Monster_Slime_Walk-Sheet.png",
            "assets/slime/Monster_Slime_Attack1-Sheet.png",
            "assets/slime/Monster_Slime_Block-Sheet.png",
            "assets/slime/Monster_Slime_Block-Sheet.png",
            "assets/slime/Monster_Slime_Hurt-Sheet.png",
            "assets/slime/Monster_Slime_Death-Sheet.png",
            6,
            8,
            8,
            8,
            8,
            8,
            6,
            6,
            4,
            10);

    public static final SpriteConfig ARCANE_WISP = new SpriteConfig(
            "assets/wisp/Wisp_Idle.png",
            "assets/wisp/Wisp_Idle.png",
            "assets/wisp/Wisp_Idle.png",
            "assets/wisp/Wisp_Idle.png",
            "assets/wisp/Wisp_Idle.png",
            "assets/wisp/Wisp_Attack_Format.png",
            "assets/wisp/Wisp_Idle.png",
            "assets/wisp/Wisp_Idle.png",
            "assets/wisp/Wisp_Idle.png",
            "assets/wisp/Wisp_Idle.png",
            1,
            1,
            1,
            1,
            1,
            3,
            1,
            1,
            1,
            1);

    public static final SpriteConfig GOLEM = new SpriteConfig(
            "assets/golem/Golem_Idle.png",
            "assets/golem/Golem_Idle.png",
            "assets/golem/Golem_Idle.png",
            "assets/golem/Golem_Idle.png",
            "assets/golem/Golem_Idle.png",
            "assets/golem/Golem_Attack_Format.png",
            "assets/golem/Golem_Idle.png",
            "assets/golem/Golem_Idle.png",
            "assets/golem/Golem_Idle.png",
            "assets/golem/Golem_Idle.png",
            1,
            1,
            1,
            1,
            1,
            3,
            1,
            1,
            1,
            1);

    private SpriteRegistry() {
    }
}
