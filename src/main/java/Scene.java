import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Scene {

    private Camera cam;
    private List<Model> models = new ArrayList<>();
    private List<Light> lights = new ArrayList<>();

    private File driver;


    public Scene(File driver, boolean saveTransformedModels) throws FileNotFoundException {
        this.driver = driver;

        File dir = null;
        if (saveTransformedModels) {
            //Ensure driver folder is made
            String dirName = driver.getName();
            if (dirName.contains(".")) dirName = dirName.substring(0, dirName.lastIndexOf('.'));
            dir = new File(dirName);
            if (!dir.exists() || !dir.isDirectory()) dir.mkdir();
        }

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
                    Model model = new Model(parts);

                    if (saveTransformedModels) {
                        File file = new File(parts[9]);
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

                        //Save model to driver folder
                        model.save(output);
                    }

                    addModel(model);
                } else if (type.equalsIgnoreCase("light")) {
                    addLight(new Light(parts));
                } else if (type.equalsIgnoreCase("eye")) {
                    setCamera(new Camera(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
                } else if (type.equalsIgnoreCase("look")) {
                    cam.setLook(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
                } else if (type.equalsIgnoreCase("up")) {
                    cam.setUp(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
                } else if (type.equalsIgnoreCase("d")) {
                    cam.setD(Double.parseDouble(parts[1]));
                } else if (type.equalsIgnoreCase("bounds")) {
                    cam.setBounds(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), Double.parseDouble(parts[4]));
                } else if (type.equalsIgnoreCase("res")) {
                    cam.setResolution(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                } else if (type.equalsIgnoreCase("ambient")) {
                    cam.setAmbient(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
                } else {
                    System.out.println("Skipping line, unknown type: " + type);
                }
            }
        }
        scan.close();
    }

    private void addModel(Model model) {
        models.add(model);
        model.setScene(this);
    }

    private void addLight(Light light) {
        lights.add(light);
        light.setScene(this);
    }

    private void setCamera(Camera cam) {
        this.cam = cam;
        cam.setScene(this);
    }

    public Camera getCamera() {
        return cam;
    }

    public List<Model> getModels() {
        return models;
    }

    public List<Light> getLights() {
        return lights;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Scene:\n  driver_file: ");
        sb.append(driver);
        sb.append('\n');
        sb.append(cam);
        sb.append('\n');
        lights.forEach(light -> {
            sb.append(light);
            sb.append('\n');
        });
        models.forEach(model -> {
            sb.append(model);
            sb.append('\n');
        });

        return sb.toString();
    }

}
