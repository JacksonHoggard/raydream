package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.gui.editor.material.Texture;
import me.jacksonhoggard.raydream.util.Util;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;

    public Shader(String vertexShaderFile, String fragmentShaderFile) {
        // Compile and attach shaders
        try {
            vertexShaderId = createShader(Paths.get(ClassLoader.getSystemResource(vertexShaderFile).toURI()).toString(), GL_VERTEX_SHADER);
            fragmentShaderId = createShader(Paths.get(ClassLoader.getSystemResource(fragmentShaderFile).toURI()).toString(), GL_FRAGMENT_SHADER);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not load glsl file.");
        }
        programId = createProgram();

        // Link the shader program
        link();
    }

    private int createProgram() {
        int id = glCreateProgram();
        if(id == 0)
            throw new RuntimeException("Could create shader program.");
        return id;
    }

    private int createShader(String shaderPath, int shaderType) {
        String shaderSource = Util.loadShader(shaderPath);
        int shaderId = glCreateShader(shaderType);
        if(shaderType == 0)
            throw new RuntimeException("Could not create shader.");

        glShaderSource(shaderId, shaderSource);
        glCompileShader(shaderId);

        return shaderId;
    }

    private void link() {
        if(programId == 0)
            throw new RuntimeException("Trying to link an invalid/released program");

        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);

        // Check for linking errors
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        // Validate the shader program
        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    public void use() {
        glUseProgram(programId);
    }

    public void unuse() {
        glUseProgram(0);
    }

    public void cleanup() {
        unuse();
        if (programId != 0) {
            if(vertexShaderId != 0) {
                glDetachShader(programId, vertexShaderId);
                glDeleteShader(vertexShaderId);
            }
            if(fragmentShaderId != 0) {
                glDetachShader(programId, fragmentShaderId);
                glDeleteShader(fragmentShaderId);
            }
            glDeleteProgram(programId);
        }
    }

    public void setMatrix4(String name, float[] matrix) {
        int location = glGetUniformLocation(programId, name);
        glUniformMatrix4fv(location, false, matrix);
    }

    public void setVec3(String name, float[] vector) {
        int location = glGetUniformLocation(programId, name);
        glUniform3fv(location, vector);
    }

    public void setInt(String name, int value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1i(location, value);
    }

    public void setFloat(String name, float value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1f(location, value);
    }

    public void setBool(String name, boolean value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1i(location, value ? 1 : 0);
    }
}
