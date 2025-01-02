import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

class Grid {
    int width = 64;
    int height = 64;
    Cell Cells[][];

    public Grid(int width, int height) {
       this.width = width;
       this.height = height;
    }

    private void drawGrid() {
        for (int x = 0; x < this.width; x++) {
           for (int y = 0; y < this.height; y++) {
               // TODO: Draw cell
           }
        }
    }
}

class Cell {
    // false being dead, and true - alive
    private boolean state;
    private int x;
    private int y;

    public Cell(boolean state) {
        this.state = state;
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


// TODO: Add a grid
public class Main {
    private static long createWindow() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        long window = glfwCreateWindow(640, 480, "Hello Triangle!", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // Enable v-sync
        glfwShowWindow(window);

        return window;
    }

    private static int createShaderProgram() {
        String vertexShaderSource = """
                    #version 330 core
                    layout (location = 0) in vec3 position;
                    void main() {
                        gl_Position = vec4(position, 1.0);
                    }
                """;

        String fragmentShaderSource = """
                    #version 330 core
                    out vec4 FragColor;
                    void main() {
                        FragColor = vec4(0.5, 0.8, 0.4, 1.0); // Green color
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

    private static int createVAO() {
        float[] vertices = {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.0f, 0.5f, 0.0f
        };

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return vao;
    }

    public static void main(String[] args) {
        GLFWErrorCallback.createPrint(System.err).set();

        long window = createWindow();
        glfwMakeContextCurrent(window);

        GL.createCapabilities();

        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        glViewport(0, 0, 640, 480);

        int shaderProgram = createShaderProgram();
        int vao = createVAO();

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

            glUseProgram(shaderProgram);
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 3);
            glBindVertexArray(0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glDeleteVertexArrays(vao);
        glDeleteProgram(shaderProgram);
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}
