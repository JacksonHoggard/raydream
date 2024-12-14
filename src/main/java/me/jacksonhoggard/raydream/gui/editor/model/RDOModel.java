package me.jacksonhoggard.raydream.gui.editor.model;

import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.math.Vector2F;
import me.jacksonhoggard.raydream.math.Vector3F;
import me.jacksonhoggard.raydream.util.io.SceneReader;
import me.jacksonhoggard.raydream.util.io.UnrecognizedTokenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RDOModel extends MeshModel {

    private final InputStream stream;

    public RDOModel(String path, InputStream stream) {
        super(path);
        this.stream = stream;
    }

    private void loadRDO(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        try {
            while((line = reader.readLine()) != null) {
                if(line.startsWith("+ mesh:")) {
                    addMesh(reader);
                }
            }
        } catch (IOException e) {
            throw new IOException("Could not read file: ", e);
        } catch (UnrecognizedTokenException e) {
            throw new RuntimeException(e);
        }
        try {
            reader.close();
        } catch (IOException e) {
            throw new IOException("Could not close stream: ", e);
        }
    }

    private void addMesh(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        List<Vector3F> vertices = new ArrayList<>();
        List<Vector3F> normals = new ArrayList<>();
        List<Vector2F> texCoords = new ArrayList<>();
        EditorObjectMaterial material = new EditorObjectMaterial();
        StringBuilder label = new StringBuilder();
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "label:":
                    label.append(line.substring(7));
                    break;
                case "material:":
                    SceneReader.parseObjectMaterial(reader, material, Paths.get(getPath()).getParent().toString());
                    break;
                case "triangles:":
                    while(!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
                        String[] triParams = line.trim().split("\\s+");
                        vertices.add(new Vector3F(
                                Float.parseFloat(triParams[1]),
                                Float.parseFloat(triParams[2]),
                                Float.parseFloat(triParams[3])
                        ));
                        vertices.add(new Vector3F(
                                Float.parseFloat(triParams[4]),
                                Float.parseFloat(triParams[5]),
                                Float.parseFloat(triParams[6])
                        ));
                        vertices.add(new Vector3F(
                                Float.parseFloat(triParams[7]),
                                Float.parseFloat(triParams[8]),
                                Float.parseFloat(triParams[9])
                        ));
                        normals.add(new Vector3F(
                                Float.parseFloat(triParams[10]),
                                Float.parseFloat(triParams[11]),
                                Float.parseFloat(triParams[12])
                        ));
                        normals.add(new Vector3F(
                                Float.parseFloat(triParams[13]),
                                Float.parseFloat(triParams[14]),
                                Float.parseFloat(triParams[15])
                        ));
                        normals.add(new Vector3F(
                                Float.parseFloat(triParams[16]),
                                Float.parseFloat(triParams[17]),
                                Float.parseFloat(triParams[18])
                        ));
                        texCoords.add(new Vector2F(
                                Float.parseFloat(triParams[19]),
                                1 - Float.parseFloat(triParams[20])
                        ));
                        texCoords.add(new Vector2F(
                                Float.parseFloat(triParams[21]),
                                1 - Float.parseFloat(triParams[22])
                        ));
                        texCoords.add(new Vector2F(
                                Float.parseFloat(triParams[23]),
                                1 - Float.parseFloat(triParams[24])
                        ));
                    }
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        getMeshes().add(new Mesh(label.toString(), material, vertices, normals, texCoords));
    }

    @Override
    public void create() {
        if(created)
            return;

        try {
            loadRDO(stream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load RDO model.", e);
        }

        for(Mesh mesh : getMeshes()) {
            mesh.create();
        }

        created = true;
    }

    @Override
    public void remove() {
        if(!created)
            return;
        super.remove();
    }
}
