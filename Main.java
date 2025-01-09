import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.util.Random;

class Grid {
    private int vaoId, vboId;
    private float[] vertices;
    private int width, height;
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

class Cell {
    private boolean state;
    private int x;
    private int y;

    public Cell(boolean state, int x, int y) {
        this.state = state;
        this.x = x;
        this.y = y;
    }

    public void changeState(boolean newState) {
        this.state = newState;
    }

    public int[] getPosition() {
        return new int[]{this.x, this.y};
    }

    public boolean getState() {
        return this.state;
    }
}

public class Main {
    private static boolean isPaused = true;

    private static long createWindow() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        long window = glfwCreateWindow(640, 480, "Conway's Game of Life", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        return window;
    }

    private static int createShaderProgram() {
        String vertexShaderSource = """
                #version 330 core
                layout (location = 0) in vec2 position;
                layout (location = 1) in vec3 color;

                out vec3 fragColor;

                void main() {
                    gl_Position = vec4(position, 0.0, 1.0);
                    fragColor = color;
                }
                """;

        String fragmentShaderSource = """
                #version 330 core
                in vec3 fragColor;
                out vec4 FragColor;

                void main() {
                    FragColor = vec4(fragColor, 1.0);
                }
                """;

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        checkShaderCompileStatus(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        checkShaderCompileStatus(fragmentShader);

        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        checkProgramLinkStatus(shaderProgram);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return shaderProgram;
    }

    private static void checkShaderCompileStatus(int shader) {
        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(shader);
            throw new RuntimeException("Shader compilation failed: " + infoLog);
        }
    }

    private static void checkProgramLinkStatus(int program) {
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(program);
            throw new RuntimeException("Program linking failed: " + infoLog);
        }
    }

    public static void main(String[] args) {
        GLFWErrorCallback.createPrint(System.err).set();

        long window = createWindow();
        glfwMakeContextCurrent(window);

        GL.createCapabilities();

        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        glViewport(0, 0, 640, 480);

        int shaderProgram = createShaderProgram();
        Grid grid = new Grid(64, 64);

        glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
                isPaused = !isPaused;
            }
        });

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            glUseProgram(shaderProgram);

            if (!isPaused) {
                grid.updateGrid();
            }

            grid.drawGrid();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glDeleteProgram(shaderProgram);
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}
