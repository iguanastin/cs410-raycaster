import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Material {

    private int phong = 16;

    private String name;
    private RealVector ka, kd, ks, kr;


    public Material(String name, RealVector ka, RealVector kd, RealVector ks, RealVector kr) {
        this.name = name;
        this.ka = ka;
        this.kd = kd;
        this.ks = ks;
        this.kr = kr;
    }

    public Material(File file) throws FileNotFoundException {
        Scanner scan = new Scanner(file);

        // TODO: Multiple materials in one material file?

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line != null && !line.isEmpty() && !line.startsWith("#")) {
                String[] parts = line.split("\\s");
                String id = parts[0];

                if (id.equalsIgnoreCase("newmtl")) {
                    name = parts[1];
                } else if (id.equalsIgnoreCase("ka")) {
                    ka = new ArrayRealVector(new double[]{Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])});
                } else if (id.equalsIgnoreCase("kd")) {
                    kd = new ArrayRealVector(new double[]{Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])});
                }
                // TODO: Other material parts (if necessary)
            }
        }

        scan.close();
    }

    public String getName() {
        return name;
    }

    public RealVector getKa() {
        return ka;
    }

    public RealVector getKd() {
        return kd;
    }

    public RealVector getKs() {
        return ks;
    }

    public RealVector getKr() {
        return kr;
    }

    public int getPhong() {
        return phong;
    }

    @Override
    public String toString() {
        return "Material:\n" +
                "  name: " + name + "\n" +
                "  Ka: " + ka + "\n" +
                "  Kd: " + kd + "\n";
    }
}
