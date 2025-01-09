import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

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

        int height = 480;
        int width = 640;

        long window = createWindow();
        glfwMakeContextCurrent(window);

        GL.createCapabilities();

        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        glViewport(0, 0, width, height);

        int shaderProgram = createShaderProgram();
        Grid grid = new Grid(width / 5, height / 5);

        glfwSetKeyCallback(window, (_, key, _, action, _) -> {
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
