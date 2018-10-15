import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class Camera {

    private Scene scene;

    private RealVector eye;
    private RealVector look;
    private RealVector up;
    private double d;
    private double xmin, ymin, xmax, ymax;
    private int width, height;
    private RealVector ambient;


    public Camera(double x, double y, double z) {
        eye = new ArrayRealVector(new double[]{x, y, z});
    }

    public void setLook(double x, double y, double z) {
        look = new ArrayRealVector(new double[]{x, y, z});
    }

    public void setUp(double x, double y, double z) {
        up = new ArrayRealVector(new double[]{x, y, z});
    }

    public void setD(double d) {
        this.d = d;
    }

    public void setBounds(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setAmbient(double r, double g, double b) {
        ambient = new ArrayRealVector(new double[]{r, g, b});
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

}
