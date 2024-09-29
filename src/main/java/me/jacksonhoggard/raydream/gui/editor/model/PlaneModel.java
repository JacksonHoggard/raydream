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

    private final int sizeX, sizeZ;

    public PlaneModel(int sizeX, int sizeZ) {
        super();
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
    }

    @Override
    public void create() {
        if(created)
            return;

        ArrayList<Float> vertices = new ArrayList<>();

        for (int x = -sizeX / 2; x < sizeX / 2; x++) {
            for (int z = -sizeZ / 2; z < sizeZ / 2; z++) {
                vertices.add((float) x);
                vertices.add(0.f);
                vertices.add((float) z);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);
                vertices.add(0.f);
                vertices.add(0.f);

                vertices.add((float) x);
                vertices.add(0.f);
                vertices.add((float) z + 1);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);
                vertices.add(0.f);
                vertices.add(1.f);

                vertices.add((float) (x + 1));
                vertices.add(0.f);
                vertices.add((float) (z + 1));
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(1.f);


                vertices.add((float) x);
                vertices.add(0.f);
                vertices.add((float) z);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);
                vertices.add(0.f);
                vertices.add(0.f);

                vertices.add((float) (x + 1));
                vertices.add(0.f);
                vertices.add((float) z);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);

                vertices.add((float) (x + 1));
                vertices.add(0.f);
                vertices.add((float) (z + 1));
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(0.f);
                vertices.add(1.f);
                vertices.add(1.f);
            }
        }

        int i = 0;
        this.vertices = new float[vertices.size()];
        for(Float f : vertices) {
            this.vertices[i++] = f;
        }
        vertexCount = this.vertices.length / 8;

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.size());
        vertexBuffer.put(this.vertices).flip();

        vertexArrayId = glGenVertexArrays();
        GL30.glBindVertexArray(vertexArrayId);
        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        // position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // normal attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        // texture attribute
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
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
