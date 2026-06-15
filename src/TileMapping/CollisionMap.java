package TileMapping;

import java.io.*; // pour BufferedReader, FileReader, InputStream

public class CollisionMap {

    private static final int TILE_MUR  = 415;
    private static final int TILE_SLOW = 414;

    private final int[][] grid;
    private final int cols;
    private final int rows;

    public CollisionMap(String filename) {
        int[][] loadedGrid = new int[0][0];
        int loadedCols = 0;
        int loadedRows = 0;
        try (BufferedReader reader = openFile(filename)) {
            reader.readLine(); // ligne 1 : nom de la carte
            reader.readLine(); // ligne 2 : chemin du tileset
            String[] info = reader.readLine().trim().split(" ");
            loadedCols = Integer.parseInt(info[0]);
            loadedRows = Integer.parseInt(info[1]);
            loadedGrid = new int[loadedRows][loadedCols];
            for (int row = 0; row < loadedRows; row++) {
                String line = reader.readLine();
                if (line == null) break;
                String[] tokens = line.trim().split(" +");
                for (int col = 0; col < loadedCols && col < tokens.length; col++) {
                    loadedGrid[row][col] = Integer.parseInt(tokens[col]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.grid = loadedGrid;
        this.cols = loadedCols;
        this.rows = loadedRows;
    }

    public int[][] getGrid() {
        return grid;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    // Constructor for tests (no file I/O)
    public CollisionMap(int[][] grid) {
        this.grid  = grid;
        this.rows  = grid.length;
        this.cols  = grid.length > 0 ? grid[0].length : 0;
    }

    public boolean isMur(int col, int row) {
        if (col < 0 || row < 0 || row >= rows || col >= cols) return true;
        return grid[row][col] == TILE_MUR;
    }

    public double getSpeedFactor(int col, int row) {
        if (col < 0 || row < 0 || row >= rows || col >= cols) return 1.0;
        return grid[row][col] == TILE_SLOW ? 0.5 : 1.0;
    }

    private BufferedReader openFile(String filename) throws IOException {
        File local = new File(filename);
        if (local.exists()) return new BufferedReader(new FileReader(local));
        String resource = filename.replace('\\', '/');
        if (resource.startsWith("src/")) resource = resource.substring(4);
        InputStream stream = getClass().getResourceAsStream("/" + resource);
        if (stream != null) return new BufferedReader(new InputStreamReader(stream));
        throw new IOException("CollisionMap introuvable : " + filename);
    }
}
