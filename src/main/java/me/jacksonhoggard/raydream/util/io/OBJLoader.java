package me.jacksonhoggard.raydream.util.io;

import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Mesh;
import me.jacksonhoggard.raydream.object.Triangle;

import java.io.*;
import java.util.ArrayList;

public class OBJLoader {

    private static final ArrayList<Vector3D> vertices = new ArrayList<Vector3D>();
    private static final ArrayList<Triangle> triangles = new ArrayList<Triangle>();
    private static Vector3D min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
    private static Vector3D max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

    public static Mesh meshFromOBJ(String path) {
        vertices.clear();
        triangles.clear();
        min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
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
        return new Mesh(triangles.toArray(t), min, max);
    }

    private static void parseLine(String line) {
        String[] tokens = line.split("\\s+");
        switch(tokens[0]) {
            case "v":
                Vector3D v = new Vector3D(
                        Double.parseDouble(tokens[1]),
                        Double.parseDouble(tokens[2]),
                        Double.parseDouble(tokens[3])
                );
                vertices.add(v);
                min.x = Math.min(min.x, v.x);
                min.y = Math.min(min.y, v.y);
                min.z = Math.min(min.z, v.z);
                max.x = Math.max(max.x, v.x);
                max.y = Math.max(max.y, v.y);
                max.z = Math.max(max.z, v.z);
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
