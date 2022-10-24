import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.function.Function;

public class FileGenerator {

    private static final DecimalFormat format = new DecimalFormat("0.00");
    public static float x_3(float x) {
        return x*x*x;
    }
    private void generate(String filename, int numOfVars, float fieldStart, float fieldEnd, int numOfFitts, Function<Float, Float> function) {
        String path = "DatFiles\\" + filename;

        try {
            var fw = new FileWriter(path, false);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(numOfVars + " " + "100 " + fieldStart + " " + fieldEnd + " " + numOfFitts);
            bw.newLine();

            int n = 0;
            int d = 0;
            for (int i =0; i< numOfFitts; ++i) {
                String pattern = d + "." + n;
                String x = format.format(function.apply(Float.parseFloat(pattern)));
                bw.write(pattern + " " );
                bw.newLine();
                ++n;
                if (n == 10) {
                    n = 0;
                }

                if (i % 10 == 0 && i != 0) {
                    ++d;
                }

            }
            bw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String [] args) {
        FileGenerator fileGenerator = new FileGenerator();
        fileGenerator.generate("test.dat", 1, -10, 10, 100, FileGenerator::x_3);
    }
}
