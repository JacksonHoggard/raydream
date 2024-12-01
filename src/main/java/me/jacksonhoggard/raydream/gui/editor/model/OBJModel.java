package me.jacksonhoggard.raydream.gui.editor.model;

import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.material.Texture;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Vector2F;
import me.jacksonhoggard.raydream.math.Vector3F;
import me.jacksonhoggard.raydream.util.Util;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class OBJModel extends EditorModel {

    private final String path;
    private final List<Mesh> meshes = new ArrayList<>();

    public OBJModel(String path) {
        super();
        this.path = path;
    }

    private void loadMTL(String path, Map<String, EditorObjectMaterial> materials) throws IOException {
        path = path.trim();
        if(path.startsWith("./")) path = path.substring(2);
        String parentDir = Paths.get(this.path).getParent().toAbsolutePath().toString() + File.separator;
        List<String> lines;
        try {
            lines = Util.readAllLines(parentDir + path);
        } catch (RuntimeException e) {
            throw new IOException(e);
        }

        EditorObjectMaterial currentMaterial = null;
        String mtlName = null;

        for(String line : lines) {
            line = line.trim();
            String[] tokens = line.split("\\s+");
            switch(tokens[0]) {
                case "newmtl":
                    if(currentMaterial != null)
                        materials.put(mtlName, currentMaterial);
                    currentMaterial = new EditorObjectMaterial(
                        new float[3],
                        0.3f,
                        0.6f,
                        0.5f,
                        32,
                        1.0f,
                        0.5f,
                        0.f,
                        Material.Type.OTHER,
                        1.f
                    );
                    mtlName = tokens[1];
                    break;
                case "Kd":
                    currentMaterial.setColor(parseColor(tokens));
                    break;
                case "Ns":
                    currentMaterial.setSpecularExponent(Float.parseFloat(tokens[1]));
                    break;
                case "Ni":
                    currentMaterial.setIndexOfRefraction(Float.parseFloat(tokens[1]));
                    if(currentMaterial.getType() == Material.Type.OTHER)
                        currentMaterial.setType(Material.Type.REFLECT);
                    break;
                case "d":
                    float d = Float.parseFloat(tokens[1]);
                    if(d < 1.f)
                        currentMaterial.setType(Material.Type.REFLECT_REFRACT);
                    break;
                case "map_Kd":
                    String texturePath = line.substring(line.indexOf(tokens[1]));
                    for(int i = tokens.length - 1; i >= 0; i--) {
                        if(tokens[i].startsWith("-")) {
                            i += 4;
                            texturePath = line.substring(line.indexOf(tokens[i]));
                            break;
                        }
                    }
                    currentMaterial.setTexture(new Texture(parentDir + texturePath.replace("\\\\", File.separator)));
                    break;
            }
        }
        if(currentMaterial != null)
            materials.put(mtlName, currentMaterial);
    }

    private float[] parseColor(String[] tokens) {
        return new float[] {
                Float.parseFloat(tokens[1]),
                Float.parseFloat(tokens[2]),
                Float.parseFloat(tokens[3])
        };
    }

    private void makeMesh(String label, List<Vector3F> vertices, List<Vector3F> normals, List<Vector2F> textures, EditorObjectMaterial material, Map<Integer[], Vector3F[]> triangles, List<Vector3F[]> triangleNormals, List<Vector2F[]> triangleTexCoords) {
        List<Vector3F> tempNormals = new ArrayList<>(normals);
        List<Vector2F> tempTextures = new ArrayList<>(textures);
        List<Vector3F> tempVertices = new ArrayList<>(vertices);
        if(!normals.isEmpty())
            formatLists(tempVertices, tempNormals, triangles, tempTextures, triangleTexCoords, triangleNormals);
        else
            calculateSmoothNormals(tempVertices, tempNormals, triangles, tempTextures, triangleTexCoords);
        meshes.add(new Mesh(label, material, tempVertices, tempNormals, tempTextures));
    }

    private void loadOBJ(String path) throws IOException {
        List<String> lines;
        try {
            lines = Util.readAllLines(path);
        } catch (RuntimeException e) {
            throw new IOException(e);
        }

        List<Vector3F> vertices = new ArrayList<>();
        List<Vector3F> normals = new ArrayList<>();
        List<Vector2F> textures = new ArrayList<>();
        Map<Integer[], Vector3F[]> triangles = new LinkedHashMap<>();
        List<Vector2F[]> triangleTexCoords = new ArrayList<>();
        List<Vector3F[]> triangleNormals = new ArrayList<>();
        Map<String, EditorObjectMaterial> materials = new LinkedHashMap<>();
        EditorObjectMaterial currentMaterial = null;

        for(String line : lines) {
            String[] tokens = line.split("\\s+");
            switch(tokens[0]) {
                case "mtllib":
                    loadMTL(line.substring(tokens[0].length()), materials);
                    break;
                case "usemtl":
                    if(!triangles.isEmpty()) {
                        EditorObjectMaterial finalCurrentMaterial = new EditorObjectMaterial(currentMaterial);
                        makeMesh(materials.entrySet().stream()
                                .filter(entry -> finalCurrentMaterial.equals(entry.getValue()))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse("Mesh"), vertices, normals, textures, finalCurrentMaterial, triangles, triangleNormals, triangleTexCoords);
                        triangles.clear();
                        triangleNormals.clear();
                        triangleTexCoords.clear();
                    }
                    currentMaterial = materials.get(tokens[1]);
                    break;
                case "v":
                    Vector3F v = new Vector3F(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    );
                    vertices.add(v);
                    break;
                case "vn":
                    Vector3F n = new Vector3F(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    );
                    normals.add(n);
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
                            if(tokens[1].split("/").length > 1 && !tokens[1].split("/")[1].isEmpty()) {
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
                            if(tokens[1].split("/").length > 2 && !tokens[1].split("/")[2].isEmpty()) {
                                Integer[] normalIndices = new Integer[]{
                                        Integer.parseInt(tokens[1].split("/")[2]) - 1,
                                        Integer.parseInt(tokens[i + 2].split("/")[2]) - 1,
                                        Integer.parseInt(tokens[i + 3].split("/")[2]) - 1
                                };
                                triangleNormals.add(new Vector3F[]{
                                        normals.get(normalIndices[0]),
                                        normals.get(normalIndices[1]),
                                        normals.get(normalIndices[2])
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
                        if(tokens[1].split("/").length > 1 && !tokens[1].split("/")[1].isEmpty()) {
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
                        if(tokens[1].split("/").length > 2 && !tokens[1].split("/")[2].isEmpty()) {
                            Integer[] normalIndices = new Integer[]{
                                    Integer.parseInt(tokens[1].split("/")[2]) - 1,
                                    Integer.parseInt(tokens[2].split("/")[2]) - 1,
                                    Integer.parseInt(tokens[3].split("/")[2]) - 1
                            };
                            triangleNormals.add(new Vector3F[]{
                                    normals.get(normalIndices[0]),
                                    normals.get(normalIndices[1]),
                                    normals.get(normalIndices[2])
                            });
                        }
                    }
                    break;
            }
        }

        if(currentMaterial == null)
            currentMaterial = new EditorObjectMaterial(
                    new float[3],
                    0.3f,
                    0.6f,
                    0.5f,
                    32,
                    1.0f,
                    0.5f,
                    0.f,
                    Material.Type.OTHER,
                    1.f
            );
        if(!triangles.isEmpty()) {
            EditorObjectMaterial finalCurrentMaterial = new EditorObjectMaterial(currentMaterial);
            makeMesh(materials.entrySet().stream()
                    .filter(entry -> finalCurrentMaterial.equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("Group"), vertices, normals, textures, finalCurrentMaterial, triangles, triangleNormals, triangleTexCoords);
        }

    }

    private void formatLists(List<Vector3F> vertices, List<Vector3F> normals, Map<Integer[], Vector3F[]> triangles, List<Vector2F> textures, List<Vector2F[]> triangleTexCoords, List<Vector3F[]> triangleNormals) {
        vertices.clear();
        normals.clear();
        textures.clear();
        AtomicInteger j = new AtomicInteger(0);
        AtomicInteger k = new AtomicInteger(0);
        triangles.forEach((_, v) -> {
            vertices.add(v[0]);
            vertices.add(v[1]);
            vertices.add(v[2]);
            normals.add(triangleNormals.get(k.get())[0]);
            normals.add(triangleNormals.get(k.get())[1]);
            normals.add(triangleNormals.get(k.get())[2]);
            if (!triangleTexCoords.isEmpty()) {
                textures.add(triangleTexCoords.get(j.get())[0]);
                textures.add(triangleTexCoords.get(j.get())[1]);
                textures.add(triangleTexCoords.get(j.get())[2]);
            }
            j.incrementAndGet();
            k.incrementAndGet();
        });
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

    @Override
    public void create() {
        if(created)
            return;

        try {
            loadOBJ(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Mesh mesh : meshes) {
            mesh.create();
        }

        created = true;
    }

    @Override
    public void remove() {
        for(Mesh mesh : meshes)
            mesh.delete();
    }

    public String getPath() {
        return path;
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public class Mesh {

        private final String label;
        private final EditorObjectMaterial material;
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

        public String getLabel() {
            return label;
        }
    }

}
