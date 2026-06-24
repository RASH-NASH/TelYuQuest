package arcana.model.map;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameMap {
    private static final String DEFAULT_START_AREA_NAME = "Village";

    private final Map<String, Area> areasByName;
    private Area currentArea;

    public GameMap() {
        areasByName = new LinkedHashMap<String, Area>();
        registerArea(createVillageArea());
        registerArea(createAcademyYardArea());
        registerArea(createLakeArea());
        registerArea(createGuildHallArea());
        registerArea(createMainHallArea());
        currentArea = getAreaOrDefault(DEFAULT_START_AREA_NAME);
    }
    
    private Area createVillageArea() {
        Area village = new Area("Village", "Desa utama tempat memulai petualangan.",
                "assets/area/village.png", new Rectangle(0, 0, 1672, 940),
                new Rectangle(596, 400, 1, 1));

        village.addDoor(new Door(new Rectangle(1620, 410, 52, 200), "Lake", new Rectangle(100, 94, 1, 1)));
        village.addDoor(new Door(new Rectangle(90, 630, 44, 110), "Academy Yard", new Rectangle(726, 336, 1, 1)));

        addBlocks(village, "Mountain", new int[][] {
                { 0, 0, 360, 610 }, { 0, 760, 360, 180 }, { 0, 610, 90, 150 } });
        addBlocks(village, "House", new int[][] {
                { 500, 145, 90, 100 }, { 700, 140, 120, 90 }, { 860, 140, 120, 120 },
                { 450, 270, 120, 120 }, { 450, 520, 130, 120 }, { 620, 650, 120, 100 },
                { 760, 650, 130, 100 }, { 930, 520, 110, 130 } });
        addBlocks(village, "Market", new int[][] {
                { 1110, 240, 90, 90 }, { 1270, 240, 90, 90 }, { 1420, 240, 90, 90 },
                { 1090, 540, 90, 90 }, { 1260, 540, 90, 90 }, { 1410, 540, 90, 90 } });

        return village;
    }

    private void addBlocks(Area area, String label, int[][] blocks) {
        for (int i = 0; i < blocks.length; i++) {
            int[] b = blocks[i];
            area.addObstacle(new MapObstacle(label + " " + (i + 1), b[0], b[1], b[2], b[3], true));
        }
    }

    private Area createAcademyYardArea() {
        Area yard = new Area(
                "Academy Yard",
                "Halaman akademi tempat monster berkeliaran untuk latihan bertarung.",
                "assets/area/yardd.png",
                new Rectangle(0, 0, 960, 640),
                new Rectangle(90, 300, 1, 1));

        yard.addDoor(new Door(new Rectangle(0, 318, 170, 70), "Lake", new Rectangle(872, 88, 1, 1)));
        yard.addDoor(new Door(new Rectangle(790, 318, 170, 70), "Village", new Rectangle(332, 640, 1, 1)));
        yard.addDoor(new Door(new Rectangle(445, 52, 60, 48), "Main Hall", new Rectangle(320, 560, 1, 1))); 
        
        yard.addObstacle(new MapObstacle("North Wall Left", 64, 24, 336, 72, true));
        yard.addObstacle(new MapObstacle("Main Hall Wall Left", 400, 0, 45, 104, true));
        yard.addObstacle(new MapObstacle("Main Hall Wall Right", 505, 0, 55, 104, true));
        yard.addObstacle(new MapObstacle("North Wall Right", 560, 24, 336, 72, true));
        yard.addObstacle(new MapObstacle("West Wall Upper", 64, 0, 40, 280, true));
        yard.addObstacle(new MapObstacle("West Gate Upper Stone", 64, 280, 108, 38, true));
        yard.addObstacle(new MapObstacle("West Gate Lower Stone", 64, 388, 108, 72, true));
        yard.addObstacle(new MapObstacle("West Wall Lower", 64, 460, 40, 180, true));
        yard.addObstacle(new MapObstacle("East Wall Upper", 856, 0, 40, 280, true));
        yard.addObstacle(new MapObstacle("East Gate Upper Stone", 788, 280, 108, 38, true));
        yard.addObstacle(new MapObstacle("East Gate Lower Stone", 788, 388, 108, 72, true));
        yard.addObstacle(new MapObstacle("East Wall Lower", 856, 460, 40, 180, true));
        yard.addObstacle(new MapObstacle("South Wall", 64, 572, 832, 48, true));
        return yard;
    }
    
    private Area createLakeArea() {
        Area lake = new Area(
                "Lake",
                "Area danau dengan shop, guild, dan spot memancing.",
                "assets/area/Lake.png",
                new Rectangle(0, 0, 960, 640),
                new Rectangle(56, 88, 1, 1));

        lake.addDoor(new Door(new Rectangle(0, 100, 42, 32), "Village", new Rectangle(1560, 480, 1, 1)));
        lake.addDoor(new Door(new Rectangle(406, 216, 56, 44), "Guild Hall", new Rectangle(236, 258, 1, 1)));
        lake.addDoor(new Door(new Rectangle(926, 96, 34, 40), "Academy Yard", new Rectangle(190, 336, 1, 1)));

        lake.addObstacle(new MapObstacle("Lake North West Water", 0, 0, 168, 103, true));
        lake.addObstacle(new MapObstacle("Lake South West Water", 0, 136, 168, 158, true));
        lake.addObstacle(new MapObstacle("River Above Bridge", 168, 0, 122, 101, true));
        lake.addObstacle(new MapObstacle("River Below Bridge", 168, 140, 122, 500, true));
        lake.addObstacle(new MapObstacle("Open Lake Left", 0, 294, 316, 346, true));
        lake.addObstacle(new MapObstacle("Open Lake Right", 936, 294, 24, 346, true));
        lake.addObstacle(new MapObstacle("Deep Lake Left Of Pier", 290, 473, 418, 167, true));
        lake.addObstacle(new MapObstacle("Deep Lake Right Of Pier", 736, 473, 224, 167, true));
        lake.addObstacle(new MapObstacle("Guild Building", 362, 138, 146, 76, true));
        lake.addObstacle(new MapObstacle("Shop Building", 758, 130, 132, 110, true));
        lake.addObstacle(new MapObstacle("Large Tree", 292, 18, 58, 70, true));
        lake.addObstacle(new MapObstacle("Well", 34, 226, 30, 38, true));

        lake.addInteractionZone(new InteractionZone(InteractionType.SHOP, "Lake Shop",
                "Buka shop dengan tombol B di depan pintu.", 786, 244, 72, 48));
        lake.addInteractionZone(new InteractionZone(InteractionType.FISHING, "Lake Shore",
                "Tekan F di pinggiran air untuk memancing.", 316, 264, 390, 36));
        lake.addInteractionZone(new InteractionZone(InteractionType.FISHING, "Fishing Pier",
                "Tekan F di dermaga untuk memancing.", 686, 302, 70, 328));
        lake.addInteractionZone(new InteractionZone(InteractionType.FISHING, "West River Bank",
                "Tekan F di pinggiran sungai untuk memancing.", 126, 132, 42, 154));
        return lake;
    }

    private Area createGuildHallArea() {
        Area guildHall = new Area(
                "Guild Hall",
                "Interior guild untuk menerima quest.",
                "assets/area/guild/interrior_guild.png",
                new Rectangle(0, 0, 512, 512),
                new Rectangle(236, 286, 1, 1));

        guildHall.addDoor(new Door(new Rectangle(188, 324, 55, 10), "Lake", new Rectangle(426, 264, 1, 1)));
        
        guildHall.addObstacle(new MapObstacle("Guild Wall Top", 0, 0, 512, 205, true));

        // Kiri dibuat bertingkat supaya kiri bawah gak tembus
        guildHall.addObstacle(new MapObstacle("Guild Wall Left Upper", 0, 205, 80, 83, true));
        guildHall.addObstacle(new MapObstacle("Guild Wall Left Lower", 0, 288, 124, 224, true));

        // Kanan juga dibuat bertingkat
        guildHall.addObstacle(new MapObstacle("Guild Wall Right Upper", 385, 205, 127, 83, true));
        guildHall.addObstacle(new MapObstacle("Guild Wall Right Lower", 388, 288, 124, 224, true));

        // Bawah kiri dan kanan, jangan nutup area door
        guildHall.addObstacle(new MapObstacle("Guild Wall Bottom Left", 0, 320, 188, 192, true));
        guildHall.addObstacle(new MapObstacle("Guild Wall Bottom Right", 236, 320, 248, 192, true));

        // Nutup lubang bawah tepat di bawah door
        guildHall.addObstacle(new MapObstacle("Guild Wall Bottom Center", 188, 334, 55, 178, true));

        // =========================
        // FURNITURE / INNER OBJECTS
        // =========================

        guildHall.addObstacle(new MapObstacle("Guild Side Shelves", 80, 205, 84, 36, true));
        guildHall.addObstacle(new MapObstacle("Guild Staff Desk", 178, 232, 86, 26, true));
        guildHall.addObstacle(new MapObstacle("Guild Sofa", 300, 285, 55, 27, true));

        guildHall.addInteractionZone(new InteractionZone(InteractionType.QUEST, "Guild Quest Desk",
                "Tekan E di depan staff guild untuk membuka panel quest.", 150, 250, 170, 70));

        return guildHall;
    }

    private Area createMainHallArea() {
        Area mainHall = new Area(
                "Main Hall",
                "Ruang utama akademi tempat meja quiz berada.",
                "assets/area/hall.png",
                new Rectangle(0, 0, 829, 700),
                new Rectangle(320, 560, 1, 1));

        mainHall.addDoor(new Door(new Rectangle(224, 672, 200, 68), "Academy Yard", new Rectangle(461, 110, 1, 1)));

        mainHall.addObstacle(new MapObstacle("Top Wall", 0, 0, 829, 182, true));
        mainHall.addObstacle(new MapObstacle("Left Wall", 0, 182, 14, 478, true));
        mainHall.addObstacle(new MapObstacle("Right Wall", 815, 182, 14, 478, true));
        mainHall.addObstacle(new MapObstacle("Bottom Wall Left", 0, 655, 288, 45, true));
        mainHall.addObstacle(new MapObstacle("Bottom Wall Right", 405, 655, 424, 45, true));
        mainHall.addObstacle(new MapObstacle("Lecture Table", 210, 295, 215, 95, true));
        mainHall.addObstacle(new MapObstacle("Quiz Desk", 548, 268, 112, 72, true));
        mainHall.addObstacle(new MapObstacle("Left Benches", 10, 572, 158, 55, true));
        mainHall.addObstacle(new MapObstacle("Left Banner Pillar", 186, 560, 46, 72, true));
        mainHall.addObstacle(new MapObstacle("Right Banner Pillar", 414, 560, 52, 72, true));
        mainHall.addObstacle(new MapObstacle("Right Benches", 490, 572, 165, 55, true));
        mainHall.addObstacle(new MapObstacle("Corner Cabinet", 686, 556, 120, 78, true));
        mainHall.addObstacle(new MapObstacle("Globe", 724, 184, 86, 82, true));
        mainHall.addObstacle(new MapObstacle("Notice Board", 762, 300, 52, 86, true));

        mainHall.addInteractionZone(new InteractionZone(InteractionType.QUIZ, "Academy Quiz Desk",
                "Dekati meja akademi lalu tekan E untuk membuka quiz.", 528, 340, 150, 68));
        return mainHall;
    }

    public void registerArea(Area area) {
        areasByName.put(normalizeAreaName(area.getAreaName()), area);
    }

    public Area getCurrentArea() {
        return currentArea;
    }

    public Area getArea(String areaName) {
        return areasByName.get(normalizeAreaName(areaName));
    }

    public Area getAreaOrDefault(String areaName) {
        Area area = getArea(areaName);
        if (area != null) {
            return area;
        }

        if (areasByName.isEmpty()) {
            return null;
        }

        return areasByName.values().iterator().next();
    }

    public Area changeArea(String areaName) {
        Area nextArea = getArea(areaName);
        if (nextArea != null) {
            currentArea = nextArea;
        }
        return currentArea;
    }

    public List<Area> getAreas() {
        return Collections.unmodifiableList(new ArrayList<Area>(areasByName.values()));
    }

    private String normalizeAreaName(String areaName) {
        return areaName == null ? "" : areaName.trim().toLowerCase();
    }
}
