package me.jacksonhoggard.raydream.gui.editor.model;

import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL43.GL_PRIMITIVE_RESTART_FIXED_INDEX;

public class SphereModel extends EditorModel {
    public SphereModel() {
        super();
    }

    @Override
    public void create() {
        if(created)
            return;

        ArrayList<Double> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        int indicator = 0;
        int lats = 40;
        int longs = 40;
        for(int i = 0; i <= lats; i++) {
            double lat0 = Math.PI * (-0.5D + (double) (i - 1) / lats);
            double z0 = Math.sin(lat0);
            double zr0 = Math.cos(lat0);

            double lat1 = Math.PI * (-0.5D + (double) i / lats);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);

            for(int j = 0; j <= longs; j++) {
                double lng = 2 * Math.PI * (double) (j - 1) / longs;
                double x = Math.cos(lng);
                double y = Math.sin(lng);

                Vector3D normal = new Vector3D(x * zr0, y * zr0, z0).normalize();
                Vector2D texCoord = new Vector2D((double) (j - 1) / longs,  (double) (i - 1) / lats);
                vertices.add(x * zr0);
                vertices.add(y * zr0);
                vertices.add(z0);
                vertices.add(normal.x);
                vertices.add(normal.y);
                vertices.add(normal.z);
                indices.add(indicator);
                indicator++;

                vertices.add(texCoord.x);
                vertices.add(texCoord.y);

                normal = new Vector3D(x * zr1, y * zr1, z1).normalize();
                texCoord = new Vector2D((double) (j - 1) / longs, (double) i / lats);
                vertices.add(x * zr1);
                vertices.add(y * zr1);
                vertices.add(z1);
                vertices.add(normal.x);
                vertices.add(normal.y);
                vertices.add(normal.z);
                indices.add(indicator);
                indicator++;

                vertices.add(texCoord.x);
                vertices.add(texCoord.y);
            }
            indices.add(GL_PRIMITIVE_RESTART_FIXED_INDEX);
        }

        double[] tx = {0.0f, 0.0f, -1.0f};    // x-axis (left)
        double[] ty = {1.0f, 0.0f, 0.0f};    // y-axis (up)
        double[] tz = {0.0f, 1.0f, 0.0f};    // z-axis (forward)

        double vx;
        double vy;
        double vz;
        double nx, ny, nz;
        int i;
        for(i = 0; i < vertices.size(); i += 8)
        {
            // transform vertices
            vx = vertices.get(i);
            vy = vertices.get(i+1);
            vz = vertices.get(i+2);
            vertices.set(i, tx[0] * vx + ty[0] * vy + tz[0] * vz);   // x
            vertices.set(i+1, tx[1] * vx + ty[1] * vy + tz[1] * vz);   // y
            vertices.set(i+2, tx[2] * vx + ty[2] * vy + tz[2] * vz);   // z

            // transform normals
            nx = vertices.get(i+3);
            ny = vertices.get(i+4);
            nz = vertices.get(i+5);
            vertices.set(i+3, tx[0] * nx + ty[0] * ny + tz[0] * nz);   // nx
            vertices.set(i+4, tx[1] * nx + ty[1] * ny + tz[1] * nz);   // ny
            vertices.set(i+5, tx[2] * nx + ty[2] * ny + tz[2] * nz);   // nz
        }

        i = 0;
        this.vertices = new float[vertices.size()];
        for(Double d : vertices) {
            this.vertices[i++] = d.floatValue();
        }
        vertexCount = this.vertices.length / 8;

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.size());
        vertexBuffer.put(this.vertices);
        vertexBuffer.flip();

        i = 0;
        this.indices = new int[indices.size()];
        for(Integer index : indices) {
            this.indices[i++] = index;
        }
        indicesCount = this.indices.length;

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.size());
        indexBuffer.put(this.indices);
        indexBuffer.flip();

        vertexArrayId = glGenVertexArrays();
        glBindVertexArray(vertexArrayId);

        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        // position attribute
        glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // normal attribute
        glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        // texture attribute
        glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        indicesBufferId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesBufferId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        glBindVertexArray(0);

        created = true;
    }

    @Override
    public void remove() {
        if(!created)
            return;
        glDeleteBuffers(vertexBufferId);
        glDeleteBuffers(indicesBufferId);
        glDeleteVertexArrays(vertexArrayId);
    }
}
