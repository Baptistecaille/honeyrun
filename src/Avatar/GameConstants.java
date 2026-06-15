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


    public static final double[][] MONSTER_SPAWNS = {
        {192.0, 96.0},
        {96.0, 256.0},
        {288.0, 288.0},
        {1728.0, 96.0},
        {1824.0, 256.0},
        {1632.0, 288.0},
        {192.0, 992.0},
        {96.0, 832.0},
        {288.0, 800.0},
        {1728.0, 992.0},
        {1824.0, 832.0},
        {1632.0, 800.0},
        {96.0, 352.0},
        {352.0, 96.0},
        {320.0, 224.0},
        {1824.0, 352.0},
        {1568.0, 96.0},
        {1600.0, 224.0},
    };

    private GameConstants() {
    }
}
