package me.jacksonhoggard.raydream.gui.editor.model;

import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.math.Vector2F;
import me.jacksonhoggard.raydream.math.Vector3F;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public abstract class MeshModel extends EditorModel {

    private final String path;
    private final List<Mesh> meshes = new ArrayList<>();

    public MeshModel(String path) {
        super();
        this.path = path;
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public String getPath() {
        return path;
    }

    @Override
    public void remove() {
        for(Mesh mesh : getMeshes())
            mesh.delete();
    }

    public class Mesh {

        private final String label;
        private EditorObjectMaterial material;
        private final float[] vertices;
        private int vertexArrayId;
        private int vertexBufferId;
        private int vertexCount;

        public Mesh(String label, EditorObjectMaterial material, List<Vector3F> vertices, List<Vector3F> normals, List<Vector2F> texCoords) {
            this.label = label;
            this.material = material;
            // Store vertices
            this.vertices = new float[vertices.size() * 8];
            int i = 0;
            for(Vector3F pos : vertices) {
                this.vertices[i * 8] = pos.x;
                this.vertices[(i * 8) + 1] = pos.y;
                this.vertices[(i * 8) + 2] = pos.z;
                i++;
            }
            vertexCount = this.vertices.length / 8;

            // Store normals
            i = 0;
            for(Vector3F n : normals) {
                this.vertices[(i * 8) + 3] = n.x;
                this.vertices[(i * 8) + 4] = n.y;
                this.vertices[(i * 8) + 5] = n.z;
                i++;
            }

            // Store texture coords
            i = 0;
            for(Vector2F t : texCoords) {
                this.vertices[(i * 8) + 6] = t.x;
                this.vertices[(i * 8) + 7] = 1 - t.y;
                i++;
            }
        }

        public void create() {
            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
            vertexBuffer.put(vertices).flip();

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

            glBindVertexArray(0);
        }

        public void draw() {
            if(material.getTexture() != null) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, getMaterial().getTexture().getId());
            }
            glBindVertexArray(vertexArrayId);
            glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        }

        public void delete() {
            glDeleteBuffers(vertexBufferId);
            glDeleteVertexArrays(vertexArrayId);
            if(material.getTexture() != null) {
                material.getTexture().remove();
            }
            if(material.getBumpMap() != null) {
                material.getBumpMap().remove();
            }
        }

        public int getVertexCount() {
            return vertexCount;
        }

        public float[] getVertices() {
            return vertices;
        }

        public EditorObjectMaterial getMaterial() {
            return material;
        }

        public void setMaterial(EditorObjectMaterial material) {
            this.material = material;
        }

        public String getLabel() {
            return label;
        }
    }

}
