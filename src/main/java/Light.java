import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class Light {

    private Scene scene;

    private RealVector pos;
    private RealVector color;
    private boolean infinite = false;

    public Light(double x, double y, double z, int w, double r, double g, double b) {
        pos = new ArrayRealVector(new double[]{x, y, z});
        color = new ArrayRealVector(new double[]{r, g, b});

        if (w == 0) infinite = true;
    }

    public Light(String[] a) {
        this(Double.parseDouble(a[1]), Double.parseDouble(a[2]), Double.parseDouble(a[3]), Integer.parseInt(a[4]), Double.parseDouble(a[5]), Double.parseDouble(a[6]), Double.parseDouble(a[7]));
    }

    public RealVector getPos() {
        return pos;
    }

    public RealVector getColor() {
        return color;
    }

    public boolean isInfinite() {
        return infinite;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    @Override
    public String toString() {
        return "Light:\n" +
                "  pos: " + getPos() + "\n" +
                "  color: " + getColor() + "\n" +
                "  infinite: " + isInfinite();
    }

}
