package me.jacksonhoggard.raydream.gui.editor.model;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class PlaneModel extends EditorModel {

    public PlaneModel() {
        super();
    }

    @Override
    public void create() {
        if(created)
            return;

        ArrayList<Float> vertices = new ArrayList<>();

        for (int x = -500; x < 500; x++) {
            for (int z = -500; z < 500; z++) {
                vertices.add((float) x);
                vertices.add(0.f);
                vertices.add((float) z);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);

                vertices.add((float) x);
                vertices.add(0.f);
                vertices.add((float) z + 1);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);

                vertices.add((float) (x + 1));
                vertices.add(0.f);
                vertices.add((float) (z + 1));
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);


                vertices.add((float) x);
                vertices.add(0.f);
                vertices.add((float) z);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);

                vertices.add((float) (x + 1));
                vertices.add(0.f);
                vertices.add((float) z);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);

                vertices.add((float) (x + 1));
                vertices.add(0.f);
                vertices.add((float) (z + 1));
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);
            }
        }

        int i = 0;
        this.vertices = new float[vertices.size()];
        for(Float f : vertices) {
            this.vertices[i++] = f;
        }
        vertexCount = this.vertices.length / 6;

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.size());
        vertexBuffer.put(this.vertices).flip();

        vertexArrayId = glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayId);
        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        // position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // normal attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
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
