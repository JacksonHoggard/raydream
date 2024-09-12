package me.jacksonhoggard.raydream.gui.editor.model;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class AreaLightModel extends EditorModel {

    public AreaLightModel() {
        super();
        vertices = new float[]{
                -0.5f, 0.5f, 0,
                -0.5f, -0.5f, 0,
                0.5f, -0.5f, 0,
                0.5f, -0.5f, 0,
                0.5f, 0.5f, 0,
                -0.5f, 0.5f, 0
        };
        vertexCount = vertices.length / 3;
    }

    @Override
    public void create() {
        if(created)
            return;
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();
        vertexArrayId = glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayId);
        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        // position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
        created = true;
    }

    @Override
    public void remove() {
        if(!created)
            return;
        glDeleteVertexArrays(vertexArrayId);
        glDeleteBuffers(vertexBufferId);
    }
}
