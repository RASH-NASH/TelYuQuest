package arcana.model.sprite;

public class SpriteConfig {
    private String idlePath;
    private String walkUpPath;
    private String walkDownPath;
    private String walkLeftPath;
    private String walkRightPath;
    private String attackPath;
    private String blockPath;
    private String defendPath;
    private String hurtPath;
    private String defeatedPath;
    private int frameCount;
    private int idleFrameCount;
    private int walkUpFrameCount;
    private int walkDownFrameCount;
    private int walkLeftFrameCount;
    private int walkRightFrameCount;
    private int attackFrameCount;
    private int blockFrameCount;
    private int defendFrameCount;
    private int hurtFrameCount;
    private int defeatedFrameCount;

    public SpriteConfig(String idlePath, String walkUpPath, String walkDownPath, String walkLeftPath,
            String walkRightPath, String attackPath, String blockPath, String defendPath, String hurtPath,
            String defeatedPath, int frameCount) {
        this(idlePath, walkUpPath, walkDownPath, walkLeftPath, walkRightPath, attackPath, blockPath, defendPath,
                hurtPath, defeatedPath, frameCount, frameCount, frameCount, frameCount, frameCount, frameCount,
                frameCount, frameCount, frameCount, frameCount);
    }

    public SpriteConfig(String idlePath, String walkUpPath, String walkDownPath, String walkLeftPath,
            String walkRightPath, String attackPath, String blockPath, String defendPath, String hurtPath,
            String defeatedPath, int idleFrameCount, int walkUpFrameCount, int walkDownFrameCount,
            int walkLeftFrameCount, int walkRightFrameCount, int attackFrameCount, int blockFrameCount,
            int defendFrameCount, int hurtFrameCount, int defeatedFrameCount) {
        this.idlePath = idlePath;
        this.walkUpPath = walkUpPath;
        this.walkDownPath = walkDownPath;
        this.walkLeftPath = walkLeftPath;
        this.walkRightPath = walkRightPath;
        this.attackPath = attackPath;
        this.blockPath = blockPath;
        this.defendPath = defendPath;
        this.hurtPath = hurtPath;
        this.defeatedPath = defeatedPath;
        this.idleFrameCount = idleFrameCount;
        this.walkUpFrameCount = walkUpFrameCount;
        this.walkDownFrameCount = walkDownFrameCount;
        this.walkLeftFrameCount = walkLeftFrameCount;
        this.walkRightFrameCount = walkRightFrameCount;
        this.attackFrameCount = attackFrameCount;
        this.blockFrameCount = blockFrameCount;
        this.defendFrameCount = defendFrameCount;
        this.hurtFrameCount = hurtFrameCount;
        this.defeatedFrameCount = defeatedFrameCount;
        this.frameCount = Math.max(
                Math.max(Math.max(idleFrameCount, walkUpFrameCount), Math.max(walkDownFrameCount, walkLeftFrameCount)),
                Math.max(Math.max(walkRightFrameCount, attackFrameCount),
                        Math.max(blockFrameCount, Math.max(defendFrameCount, Math.max(hurtFrameCount,
                                defeatedFrameCount)))));
    }

    public String getIdlePath() {
        return idlePath;
    }

    public String getWalkUpPath() {
        return walkUpPath;
    }

    public String getWalkDownPath() {
        return walkDownPath;
    }

    public String getWalkLeftPath() {
        return walkLeftPath;
    }

    public String getWalkRightPath() {
        return walkRightPath;
    }

    public String getAttackPath() {
        return attackPath;
    }

    public String getBlockPath() {
        return blockPath;
    }

    public String getDefendPath() {
        return defendPath;
    }

    public String getHurtPath() {
        return hurtPath;
    }

    public String getDefeatedPath() {
        return defeatedPath;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getIdleFrameCount() {
        return idleFrameCount;
    }

    public int getWalkUpFrameCount() {
        return walkUpFrameCount;
    }

    public int getWalkDownFrameCount() {
        return walkDownFrameCount;
    }

    public int getWalkLeftFrameCount() {
        return walkLeftFrameCount;
    }

    public int getWalkRightFrameCount() {
        return walkRightFrameCount;
    }

    public int getAttackFrameCount() {
        return attackFrameCount;
    }

    public int getBlockFrameCount() {
        return blockFrameCount;
    }

    public int getDefendFrameCount() {
        return defendFrameCount;
    }

    public int getHurtFrameCount() {
        return hurtFrameCount;
    }

    public int getDefeatedFrameCount() {
        return defeatedFrameCount;
    }
}
