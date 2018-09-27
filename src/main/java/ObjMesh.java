import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class ObjMesh {

    private ArrayList<RealVector> geoVerts = new ArrayList<>();
    private ArrayList<RealVector> texCoords = new ArrayList<>();
    private ArrayList<RealVector> vecNorms = new ArrayList<>();
    private ArrayList<String> faces = new ArrayList<>();


    public ObjMesh(File file) throws FileNotFoundException {
        Scanner scan = new Scanner(file);

        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            if (line != null && !line.isEmpty() && !line.startsWith("#")) {
                String[] parts = line.split("\\s");
                String id = parts[0];

                if (id.equalsIgnoreCase("v")) {
                    parseGeometricVertex(parts);
                } else if (id.equalsIgnoreCase("vt")) {
                    parseTextureCoordinate(parts);
                } else if (id.equalsIgnoreCase("vn")) {
                    parseVectorNormal(parts);
                } else if (id.equalsIgnoreCase("f")) {
                    parseFace(parts);
                }
                //TODO: Other id parsing https://en.wikipedia.org/wiki/Wavefront_.obj_file
            }
        }

        scan.close();
    }

    public void scale(double factor) {
        RealMatrix scaleMatrix = new Array2DRowRealMatrix(new double[][]{
                {factor, 0, 0, 0},
                {0, factor, 0, 0},
                {0, 0, factor, 0},
                {0, 0, 0, 1}
        }, false);

        operateMatrixOnMesh(scaleMatrix);
    }

    public void translate(double x, double y, double z) {
        RealMatrix translateMatrix = new Array2DRowRealMatrix(new double[][]{
                {1, 0, 0, x},
                {0, 1, 0, y},
                {0, 0, 1, z},
                {0, 0, 0, 1}
        }, false);

        operateMatrixOnMesh(translateMatrix);
    }

    public void rotate(double x, double y, double z, double theta) {
        RealVector axis = new ArrayRealVector(new double[]{x, y, z}, false);
        axis.unitize();
        x = axis.getEntry(0);
        y = axis.getEntry(1);
        z = axis.getEntry(2);

        double cos = Math.cos(Math.toRadians(theta));
        double sin = Math.sin(Math.toRadians(theta));

        RealMatrix m1 = new Array2DRowRealMatrix(new double[][]{
                {cos, 0, 0, 0},
                {0, cos, 0, 0},
                {0, 0, cos, 0},
                {0, 0, 0, 1}
        }, false);
        RealMatrix m2 = new Array2DRowRealMatrix(new double[][]{
                {0, -z, y, 0},
                {z, 0, -x, 0},
                {-y, x, 0, 0},
                {0, 0, 0, 0}
        }, false).scalarMultiply(sin);
        RealMatrix m3 = new Array2DRowRealMatrix(new double[][]{
                {x * x, x * y, x * z, 0},
                {x * y, y * y, y * z, 0},
                {x * z, y * z, z * z, 0},
                {0, 0, 0, 0}
        }, false).scalarMultiply(1 - cos);

        operateMatrixOnMesh(m1.add(m2).add(m3));
    }

    private void operateMatrixOnMesh(RealMatrix translateMatrix) {
        for (int i = 0; i < geoVerts.size(); i++) {
            geoVerts.set(i, translateMatrix.operate(geoVerts.get(i)));
        }

        //TODO: Add functionality to operate on other important vectors?
    }

    public void save(File file) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(file);

        geoVerts.forEach(vec -> {
            if (vec.getEntry(3) == 1)
                writer.println("v " + vec.getEntry(0) + " " + vec.getEntry(1) + " " + vec.getEntry(2));
            else
                writer.println("v " + vec.getEntry(0) + " " + vec.getEntry(1) + " " + vec.getEntry(2) + " " + vec.getEntry(3));
        });

        //TODO: Enable these again when they're needed
//        texCoords.forEach(coord -> {
//            if (coord.getEntry(2) == 0)
//                writer.println("vt " + coord.getEntry(0) + " " + coord.getEntry(1));
//            else
//                writer.println("vt " + coord.getEntry(0) + " " + coord.getEntry(1) + " " + coord.getEntry(2));
//        });

//        vecNorms.forEach(vec -> writer.println("vn " + vec.getEntry(0) + " " + vec.getEntry(1) + " " + vec.getEntry(2)));

        //TODO: Fix this once I start loading in faces properly
        faces.forEach(writer::println);

        //TODO: Write others

        writer.close();
    }

    private void parseFace(String[] parts) {
        //TODO: Actually load in faces in a usable state instead of a cheap workaround
        faces.add(String.join(" ", parts));
    }

    private void parseVectorNormal(String[] parts) {
        RealVector vec = new ArrayRealVector(new double[]{Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])}, false);
        vecNorms.add(vec);
    }

    private void parseTextureCoordinate(String[] parts) {
        double w = 0;
        if (parts.length == 4) w = Double.parseDouble(parts[3]);
        RealVector vec = new ArrayRealVector(new double[]{Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), w}, false);
        texCoords.add(vec);
    }

    private void parseGeometricVertex(String[] parts) {
        double w = 1;
        if (parts.length == 5) w = Double.parseDouble(parts[4]);
        RealVector vec = new ArrayRealVector(new double[]{Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), w}, false);
        geoVerts.add(vec);
    }

}
