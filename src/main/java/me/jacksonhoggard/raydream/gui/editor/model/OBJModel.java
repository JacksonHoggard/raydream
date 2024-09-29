package me.jacksonhoggard.raydream.gui.editor.model;

import me.jacksonhoggard.raydream.math.Vector2F;
import me.jacksonhoggard.raydream.math.Vector3F;
import me.jacksonhoggard.raydream.util.Util;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
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
    private final boolean isSmooth;

    public OBJModel(String path, boolean isSmooth) {
        super();
        this.path = path;
        this.isSmooth = isSmooth;
    }

    private void loadOBJ(String path) {
        List<String> lines = Util.readAllLines(path);

        List<Vector3F> vertices = new ArrayList<>();
        List<Vector3F> normals = new ArrayList<>();
        List<Vector2F> textures = new ArrayList<>();
        Map<Integer[], Vector3F[]> triangles = new LinkedHashMap<>();
        List<Vector2F[]> triangleTexCoords = new ArrayList<>();

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
                case "f":
                    if(tokens.length > 4) {
                        int numTrisInFace = tokens.length - 3;
                        for(int i = 0; i < numTrisInFace; i++) {
                            Integer[] indices = new Integer[]{
                                    Integer.parseInt(tokens[1].split("/")[0]) - 1,
                                    Integer.parseInt(tokens[i + 2].split("/")[0]) - 1,
                                    Integer.parseInt(tokens[i + 3].split("/")[0]) - 1
                            };
                            if(tokens[1].split("/").length > 1) {
                                Integer[] texIndices = new Integer[]{
                                        Integer.parseInt(tokens[1].split("/")[1]) - 1,
                                        Integer.parseInt(tokens[i + 2].split("/")[1]) - 1,
                                        Integer.parseInt(tokens[i + 3].split("/")[1]) - 1
                                };
                                triangleTexCoords.add(new Vector2F[]{
                                        textures.get(texIndices[0]),
                                        textures.get(texIndices[1]),
                                        textures.get(texIndices[2])
                                });
                            }
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
                        if(tokens[1].split("/").length > 1) {
                            Integer[] texIndices = new Integer[]{
                                    Integer.parseInt(tokens[1].split("/")[1]) - 1,
                                    Integer.parseInt(tokens[2].split("/")[1]) - 1,
                                    Integer.parseInt(tokens[3].split("/")[1]) - 1
                            };
                            triangleTexCoords.add(new Vector2F[]{
                                    textures.get(texIndices[0]),
                                    textures.get(texIndices[1]),
                                    textures.get(texIndices[2])
                            });
                        }
                    }
                    break;
            }
        }
        if(isSmooth)
            // Store shared vertices (smooth shading)
            calculateSmoothNormals(vertices, normals, triangles, textures, triangleTexCoords);
        else
            // Store non-shared vertices (flat shading)
            calculateFlatNormals(vertices, normals, triangles, textures, triangleTexCoords);
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
        for(Vector2F t : textures) {
            this.vertices[(i * 8) + 6] = t.x;
            this.vertices[(i * 8) + 7] = 1 - t.y;
            i++;
        }

        // Store indices
        List<Integer> indices = new ArrayList<>();
        triangles.forEach((k, _) -> {
            indices.add(k[0]);
            indices.add(k[1]);
            indices.add(k[2]);
        });
        this.indices = indices.stream().mapToInt((Integer v) -> v).toArray();
        indicesCount = this.indices.length;
    }

    private void calculateSmoothNormals(List<Vector3F> vertices, List<Vector3F> normals, Map<Integer[], Vector3F[]> triangles, List<Vector2F> textures, List<Vector2F[]> triangleTexCoords) {
        List<Vector3F> newNormals = new ArrayList<>();
        // Initialize normals
        for(int i = 0; i < vertices.size(); i++)
            newNormals.add(new Vector3F());
        vertices.clear();
        normals.clear();
        textures.clear();
        // Compute the cross product and add it to each vertex
        AtomicInteger j = new AtomicInteger(0);
        triangles.forEach((i, v) -> {
            Vector3F bMinA = Vector3F.sub(v[1], v[0]);
            Vector3F cMinA = Vector3F.sub(v[2], v[0]);
            Vector3F p = bMinA.cross(cMinA);
            vertices.add(v[0]);
            vertices.add(v[1]);
            vertices.add(v[2]);
            newNormals.set(i[0], newNormals.get(i[0]).add(p));
            newNormals.set(i[1], newNormals.get(i[1]).add(p));
            newNormals.set(i[2], newNormals.get(i[2]).add(p));
            if(!triangleTexCoords.isEmpty()) {
                textures.add(triangleTexCoords.get(j.get())[0]);
                textures.add(triangleTexCoords.get(j.get())[1]);
                textures.add(triangleTexCoords.get(j.get())[2]);
            }
            j.incrementAndGet();
        });
        // Normalize the vertex normals
        for(Vector3F n : newNormals)
            n.normalize();
        triangles.forEach((i, _) -> {
            normals.add(newNormals.get(i[0]));
            normals.add(newNormals.get(i[1]));
            normals.add(newNormals.get(i[2]));
        });
    }

    private void calculateFlatNormals(List<Vector3F> vertices, List<Vector3F> normals, Map<Integer[], Vector3F[]> triangles, List<Vector2F> textures, List<Vector2F[]> triangleTexCoords) {
        vertices.clear();
        normals.clear();
        textures.clear();
        // Compute the cross product, normalize it, and add it to the list of normals
        AtomicInteger i = new AtomicInteger();
        triangles.forEach((_, v) -> {
            Vector3F bMinA = Vector3F.sub(v[1], v[0]);
            Vector3F cMinA = Vector3F.sub(v[2], v[0]);
            Vector3F p = bMinA.cross(cMinA).normalize();
            vertices.add(v[0]);
            vertices.add(v[1]);
            vertices.add(v[2]);
            normals.add(p);
            normals.add(p);
            normals.add(p);
            if(!triangleTexCoords.isEmpty()) {
                textures.add(triangleTexCoords.get(i.get())[0]);
                textures.add(triangleTexCoords.get(i.get())[1]);
                textures.add(triangleTexCoords.get(i.get())[2]);
            }
            i.getAndIncrement();
        });
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
        glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // normal attribute
        glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        // texture attribute
        glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        //indicesBufferId = glGenBuffers();
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesBufferId);
        //glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
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
