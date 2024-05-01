package util;

import math.Vector3D;
import object.Mesh;
import object.Triangle;

import java.io.*;
import java.util.ArrayList;

public class OBJLoader {

    private static ArrayList<Vector3D> vertices = new ArrayList<Vector3D>();
    private static ArrayList<Triangle> triangles = new ArrayList<Triangle>();

    public static Mesh meshFromOBJ(String path) {
        vertices.clear();
        triangles.clear();
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

    private static void parseLine(String line) {
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
                if(tokens.length > 4) {
                    int numTrisInFace = tokens.length - 3;
                    for(int i = 0; i < numTrisInFace; i++) {
                        triangles.add(
                                new Triangle(
                                        vertices.get(Integer.parseInt(tokens[1]) - 1),
                                        vertices.get(Integer.parseInt(tokens[i + 2]) - 1),
                                        vertices.get(Integer.parseInt(tokens[i + 3]) - 1)
                                )
                        );
                    }
                } else {
                    triangles.add(
                            new Triangle(
                                    vertices.get(Integer.parseInt(tokens[1]) - 1),
                                    vertices.get(Integer.parseInt(tokens[2]) - 1),
                                    vertices.get(Integer.parseInt(tokens[3]) - 1)
                            )
                    );
                }
                break;
        }
    }

}
