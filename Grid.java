import java.util.Random;

import static org.lwjgl.opengl.GL30.*;

class Grid {
    private int vaoId, vboId;
    private float[] vertices;
    private final int width;
    private final int height;
    private Cell[][] cells;
    private Cell[][] nextGeneration;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        cells = new Cell[width][height];
        nextGeneration = new Cell[width][height];
        randomizeCellStates();
        initializeGrid();
    }

    private void randomizeCellStates() {
        Random random = new Random();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean state = random.nextBoolean();
                cells[x][y] = new Cell(state, x, y);
                nextGeneration[x][y] = new Cell(false, x, y);
            }
        }
    }

    private void initializeGrid() {
        float cellWidth = 2.0f / width;
        float cellHeight = 2.0f / height;
        vertices = new float[width * height * 6 * 5];

        int idx = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float startX = -1.0f + x * cellWidth;
                float startY = -1.0f + y * cellHeight;

                float[] color = cells[x][y].getState() ? new float[]{1.0f, 1.0f, 1.0f} : new float[]{0.0f, 0.0f, 0.0f};

                idx = addVertex(vertices, idx, startX, startY, color);
                idx = addVertex(vertices, idx, startX + cellWidth, startY, color);
                idx = addVertex(vertices, idx, startX + cellWidth, startY + cellHeight, color);

                idx = addVertex(vertices, idx, startX, startY, color);
                idx = addVertex(vertices, idx, startX + cellWidth, startY + cellHeight, color);
                idx = addVertex(vertices, idx, startX, startY + cellHeight, color);
            }
        }

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Position attribute
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Color attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 5 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private int addVertex(float[] vertices, int idx, float x, float y, float[] color) {
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = color[0];
        vertices[idx++] = color[1];
        vertices[idx++] = color[2];
        return idx;
    }

    public void updateGrid() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int liveNeighbors = countLiveNeighbors(x, y);
                boolean currentState = cells[x][y].getState();

                if (currentState && (liveNeighbors < 2 || liveNeighbors > 3)) {
                    nextGeneration[x][y].changeState(false);
                } else if (!currentState && liveNeighbors == 3) {
                    nextGeneration[x][y].changeState(true);
                } else {
                    nextGeneration[x][y].changeState(currentState);
                }
            }
        }

        // Swap current and next generation
        Cell[][] temp = cells;
        cells = nextGeneration;
        nextGeneration = temp;
        updateVertices();
    }

    private int countLiveNeighbors(int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && ny >= 0 && nx < width && ny < height && cells[nx][ny].getState()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void updateVertices() {
        float cellWidth = 2.0f / width;
        float cellHeight = 2.0f / height;
        int idx = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float startX = -1.0f + x * cellWidth;
                float startY = -1.0f + y * cellHeight;

                float[] color = cells[x][y].getState() ? new float[]{1.0f, 1.0f, 1.0f} : new float[]{0.0f, 0.0f, 0.0f};

                idx = addVertex(vertices, idx, startX, startY, color);
                idx = addVertex(vertices, idx, startX + cellWidth, startY, color);
                idx = addVertex(vertices, idx, startX + cellWidth, startY + cellHeight, color);

                idx = addVertex(vertices, idx, startX, startY, color);
                idx = addVertex(vertices, idx, startX + cellWidth, startY + cellHeight, color);
                idx = addVertex(vertices, idx, startX, startY + cellHeight, color);
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void drawGrid() {
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, width * height * 6);
        glBindVertexArray(0);
    }
}

