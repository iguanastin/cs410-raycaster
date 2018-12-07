import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Model extends Obj {

    private File file;
    private double theta, scale;
    private RealVector rotate, translate;
    private Material material;
    private boolean smooth;

    private ArrayList<RealVector> geoVerts = new ArrayList<>();
    private ArrayList<RealVector> texCoords = new ArrayList<>();
    private ArrayList<RealVector> vecNorms = new ArrayList<>();
    private ArrayList<int[]> faces = new ArrayList<>();


    public Model(double wx, double wy, double wz, double theta, double scale, double tx, double ty, double tz, boolean smooth, File file) throws FileNotFoundException {
        this.file = file;
        this.rotate = new ArrayRealVector(new double[]{wx, wy, wz});
        this.theta = theta;
        this.scale = scale;
        this.translate = new ArrayRealVector(new double[]{tx, ty, tz});
        this.smooth = smooth;

        // Load object data from file
        Scanner scan = new Scanner(file);
        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            if (line != null && !line.isEmpty() && !line.startsWith("#")) {
                String[] parts = line.split("\\s");
                String id = parts[0];

                if (id.equalsIgnoreCase("v")) {
                    double w = 1;
                    if (parts.length == 5) w = Double.parseDouble(parts[4]);
                    RealVector vec = new ArrayRealVector(new double[]{Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), w}, false);
                    geoVerts.add(vec);
                } else if (id.equalsIgnoreCase("vt")) {
                    double w = 0;
                    if (parts.length == 4) w = Double.parseDouble(parts[3]);
                    RealVector vec = new ArrayRealVector(new double[]{Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), w}, false);
                    texCoords.add(vec);
                } else if (id.equalsIgnoreCase("vn")) {
                    RealVector vec = new ArrayRealVector(new double[]{Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])}, false);
                    vecNorms.add(vec);
                } else if (id.equalsIgnoreCase("f")) {
                    int[] face = {Integer.parseInt(parts[1].substring(0, parts[1].indexOf('/'))), Integer.parseInt(parts[2].substring(0, parts[2].indexOf('/'))), Integer.parseInt(parts[3].substring(0, parts[3].indexOf('/')))};
                    faces.add(face);
                } else if (id.equalsIgnoreCase("mtllib")) {
                    this.material = new Material(new File(parts[1]));
                } else {
                    System.out.println("Unable to parse unknown type in .obj: " + id);
                }
                //TODO: Other id parsing https://en.wikipedia.org/wiki/Wavefront_.obj_file
            }
        }
        scan.close();

        rotate(wx, wy, wz, theta);
        scale(scale);
        translate(tx, ty, tz);
    }

    public Model(String[] parts) throws FileNotFoundException {
        this(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Double.parseDouble(parts[4]), Double.parseDouble(parts[5]), Double.parseDouble(parts[6]), Double.parseDouble(parts[7]), Double.parseDouble(parts[8]), parts[9].equalsIgnoreCase("smooth"), new File(parts[10]));
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

        faces.forEach(face -> {
            writer.println("f " + face[0] + " " + face[1] + " " + face[2]);
        });

        //TODO: Write others

        writer.close();
    }

    public ArrayList<int[]> getFaces() {
        return faces;
    }

    public RealVector getVertex(int i) {
        return geoVerts.get(i - 1);
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public String toString() {
        return "Model: " + "\n" +
                "  file: " + file + "\n" +
                "  rotate: " + rotate + "\n" +
                "  theta: " + theta + "\n" +
                "  scale: " + scale + "\n" +
                "  translate: " + translate + "\n" +
                "  vertex_count: " + geoVerts.size() + "\n" +
                "  face_count: " + faces.size() + "\n" +
                material;
    }

}
