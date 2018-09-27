import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Modeltoworld {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid number of args, expected 1");
            System.exit(1);
        }
        String driver = args[0];

        try {
            loadDriver(new File(driver));
        } catch (FileNotFoundException e) {
            System.out.println("Driver file does not exist: " + driver);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void loadDriver(File driver) throws FileNotFoundException {

        //Ensure driver folder is made
        String dirName = driver.getName();
        if (dirName.contains(".")) dirName = dirName.substring(0, dirName.lastIndexOf('.'));
        File dir = new File(dirName);
        if (!dir.exists() || !dir.isDirectory()) dir.mkdir();

        //Read driver file line by line
        Scanner scan = new Scanner(driver);
        HashMap<String, Integer> objUsage = new HashMap<>();
        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            if (line == null || line.isEmpty() || line.startsWith("#")) {
                System.out.println("Skipping line: \"" + line + "\"");
            } else {
                String[] parts = line.split("\\s");
                final String type = parts[0];

                if (type.equalsIgnoreCase("model")) {
                    final double wx = Double.parseDouble(parts[1]);
                    final double wy = Double.parseDouble(parts[2]);
                    final double wz = Double.parseDouble(parts[3]);
                    final double theta = Double.parseDouble(parts[4]);
                    final double scale = Double.parseDouble(parts[5]);
                    final double tx = Double.parseDouble(parts[6]);
                    final double ty = Double.parseDouble(parts[7]);
                    final double tz = Double.parseDouble(parts[8]);
                    final File file = new File(parts[9]);

                    ObjMesh mesh = new ObjMesh(file);

                    //Apply transformations
                    mesh.rotate(wx, wy, wz, theta);
                    mesh.scale(scale);
                    mesh.translate(tx, ty, tz);

                    //Rebuild filename
                    String filename = file.getName();
                    filename = filename.substring(0, filename.lastIndexOf('.')) + "_mw";
                    int usage = objUsage.getOrDefault(file.getName(), 0);
                    if (usage < 10)
                        filename += "0";
                    filename += usage + ".obj";

                    //Construct new file path
                    String path = dir.getAbsolutePath();
                    if (!path.endsWith("/")) path += "/";
                    File output = new File(path + filename);

                    //Increment usage counter for obj
                    objUsage.put(file.getName(), objUsage.getOrDefault(file.getName(), 0) + 1);

                    //Save mesh to driver folder
                    mesh.save(output);
                } else {
                    System.out.println("Skipping line, unknown type: " + type);
                }
            }
        }
        scan.close();
    }

}
