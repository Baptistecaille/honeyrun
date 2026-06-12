package Avatar;

public final class GameConstants {

    public static final int TILE_SIZE = 32;

    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1088;
    public static final int RENDER_TIMER_DELAY_MS = 40;

    public static final int MAP_ZOOM = 3;
    public static final int CAMERA_OFFSET_TILES_X = 10;
    public static final int CAMERA_OFFSET_TILES_Y = 5;

    public static final double PLAYER_SPEED = 6.0;
    public static final double MONSTER_SPEED = 1.0;
    public static final double MONSTER_CHASE_SPEED = 2.5;

    public static final double PLAYER_SIZE = 32.0;
    public static final double MONSTER_SIZE = 32.0;
    public static final int SPRITE_SIZE = 96;
    public static final int LOCAL_PLAYER_SCREEN_X = 960;
    public static final int LOCAL_PLAYER_SCREEN_Y = 480;

    public static final int AGGRO_RANGE_BLOCKS = 15;

    public static final double HIVE_X = 960.0;
    public static final double HIVE_Y = 544.0;
    public static final double HIVE_SIZE = 96.0;
    public static final double SPAWN_ZONE_SIZE = 96.0;


    // 4 groupes de 4 monstres au milieu de chaque bord, loin des spawns joueurs (coins)
    public static final double[][] MONSTER_SPAWNS = {
        // Milieu bord supérieur
        { 928.0,  96.0}, { 992.0,  96.0}, { 928.0, 160.0}, { 992.0, 160.0},
        // Milieu bord droit
        {1696.0, 512.0}, {1760.0, 512.0}, {1696.0, 576.0}, {1760.0, 576.0},
        // Milieu bord inférieur
        { 928.0, 896.0}, { 992.0, 896.0}, { 928.0, 960.0}, { 992.0, 960.0},
        // Milieu bord gauche
        {  96.0, 512.0}, { 160.0, 512.0}, {  96.0, 576.0}, { 160.0, 576.0}
    };

    private GameConstants() {
    }
}
