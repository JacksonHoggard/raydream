package me.jacksonhoggard.raydream.gui.editor.model;

import me.jacksonhoggard.raydream.math.Vector2F;
import me.jacksonhoggard.raydream.math.Vector3F;
import me.jacksonhoggard.raydream.util.Util;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class OBJModel extends EditorModel {

    private final String path;

    public OBJModel(String path) {
        super();
        this.path = path;
    }

    private void loadOBJ(String path) {
        List<String> lines = Util.readAllLines(path);

        List<Vector3F> vertices = new ArrayList<>();
        List<Vector3F> normals = new ArrayList<>();
        List<Vector2F> textures = new ArrayList<>();
        List<Vector3F> faces = new ArrayList<>();
        Map<Integer[], Vector3F[]> triangles = new HashMap<>();

        for(String line : lines) {
            String[] tokens = line.split("\\s+");
            switch(tokens[0]) {
                case "v":
                    Vector3F v = new Vector3F(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    );
                    vertices.add(v);
                    break;
                case "vt":
                    Vector2F t = new Vector2F(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    );
                    textures.add(t);
                    break;
                case "vn":
                    Vector3F n = new Vector3F(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    );
                    normals.add(n);
                    break;
                case "f":
                    if(tokens.length > 4) {
                        int numTrisInFace = tokens.length - 3;
                        for(int i = 0; i < numTrisInFace; i++) {
                            Integer[] indices = new Integer[]{
                                    Integer.parseInt(tokens[1].split("/")[0]) - 1,
                                    Integer.parseInt(tokens[i + 2].split("/")[0]) - 1,
                                    Integer.parseInt(tokens[i + 3].split("/")[0]) - 1
                            };
                            triangles.put(indices, new Vector3F[]{
                                    vertices.get(indices[0]),
                                    vertices.get(indices[1]),
                                    vertices.get(indices[2])
                            });
                        }
                    } else {
                        Integer[] indices = new Integer[]{
                                Integer.parseInt(tokens[1].split("/")[0]) - 1,
                                Integer.parseInt(tokens[2].split("/")[0]) - 1,
                                Integer.parseInt(tokens[3].split("/")[0]) - 1
                        };
                        triangles.put(indices, new Vector3F[]{
                                vertices.get(indices[0]),
                                vertices.get(indices[1]),
                                vertices.get(indices[2])
                        });
                    }
                    for(int i = 1; i < tokens.length; i++) {
                        processFace(tokens[i], faces);
                    }
                    break;
            }
        }
        this.vertices = new float[vertices.size() * 6];
        int i = 0;
        for(Vector3F pos : vertices) {
            this.vertices[i * 6] = pos.x;
            this.vertices[i * 6 + 1] = pos.y;
            this.vertices[i * 6 + 2] = pos.z;
            i++;
        }
        vertexCount = this.vertices.length / 6;

        if(normals.isEmpty()) {
            calculateNormals(normals, triangles);
        }

        i = 0;
        for(Vector3F n : normals) {
            this.vertices[(i * 6) + 3] = n.x;
            this.vertices[(i * 6) + 4] = n.y;
            this.vertices[(i * 6) + 5] = n.z;
            i++;
        }

        List<Integer> indices = new ArrayList<>();
        triangles.forEach((k, v) -> {
            indices.add(k[0]);
            indices.add(k[1]);
            indices.add(k[2]);
        });

        this.indices = indices.stream().mapToInt((Integer v) -> v).toArray();
        indicesCount = this.indices.length;
    }

    private void calculateNormals(List<Vector3F> normals, Map<Integer[], Vector3F[]> triangles) {
        // Initialize normals
        for(int i = 0; i < vertexCount; i++) {
            normals.add(new Vector3F());
        }
        // Compute the cross product and add it to each vertex
        triangles.forEach((i, v) -> {
            Vector3F bMinA = Vector3F.sub(v[1], v[0]);
            Vector3F cMinA = Vector3F.sub(v[2], v[0]);
            Vector3F p = bMinA.cross(cMinA);
            normals.set(i[0], normals.get(i[0]).add(p));
            normals.set(i[1], normals.get(i[1]).add(p));
            normals.set(i[2], normals.get(i[2]).add(p));
        });
        // Normalize the vertex normals
        for(Vector3F n : normals)
            n.normalize();
    }

    private static void processFace(String token, List<Vector3F> faces) {
        String[] lineToken = token.split("/");
        int length = lineToken.length;
        int pos = Integer.parseInt(lineToken[0]) - 1;
        int coords = -1;
        int normal = -1;
        if(length > 1) {
            String texCoord = lineToken[1];
            coords = !texCoord.isEmpty() ? Integer.parseInt(texCoord) - 1 : -1;
            if(length > 2)
                normal = Integer.parseInt(lineToken[2]) - 1;
        }
        Vector3F f = new Vector3F(pos, coords, normal);
        faces.add(f);
    }

    @Override
    public void create() {
        if(created)
            return;

        loadOBJ(path);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();

        vertexArrayId = glGenVertexArrays();
        glBindVertexArray(vertexArrayId);

        vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        // position attribute
        glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // normal attribute
        glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

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

    public String getPath() {
        return path;
    }
}
