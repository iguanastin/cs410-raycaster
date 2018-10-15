import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid number of args, expected 1");
            System.exit(1);
        }
        String driver = args[0];

        try {
            Scene scene = new Scene(new File(driver), false);

            System.out.println(scene);
        } catch (FileNotFoundException e) {
            System.out.println("Driver file does not exist: " + driver);
            e.printStackTrace();
            System.exit(1);
        }
    }

}
