package util;

import math.Vector3D;
import object.Mesh;
import object.Triangle;

import java.io.*;
import java.util.ArrayList;

public class OBJLoader {

    private final ArrayList<Vector3D> vertices;
    private final ArrayList<Triangle> triangles;

    public OBJLoader() {
        this.vertices = new ArrayList<>();
        this.triangles = new ArrayList<>();
    }

    public Mesh meshFromOBJ(String path) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        try {
            while((line = reader.readLine()) != null) {
                //String lastWord = line.substring(line.lastIndexOf(" ") + 1);
                parseLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Triangle[] t = new Triangle[triangles.size()];
        return new Mesh(triangles.toArray(t));
    }

    private void parseLine(String line) {
        String[] tokens = line.split("\\s+");
        switch(tokens[0]) {
            case "v":
                vertices.add(
                        new Vector3D(
                                Double.parseDouble(tokens[1]),
                                Double.parseDouble(tokens[2]),
                                Double.parseDouble(tokens[3])
                        )
                );
                break;
            case "f":
                triangles.add(
                        new Triangle(
                                vertices.get(Integer.parseInt(tokens[1]) - 1),
                                vertices.get(Integer.parseInt(tokens[2]) - 1),
                                vertices.get(Integer.parseInt(tokens[3]) - 1)
                        )
                );
                break;
        }
    }

}
