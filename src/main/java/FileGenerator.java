import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

public class FileGenerator {

    private static final DecimalFormat format = new DecimalFormat("0.00");




    public static float f_x_2( float x) {
        return (float) (5 * Math.pow(x, 3)  - 2  * Math.pow(x, 2) + 3 * x);
    }

    public static float f_sin_cos(float x) {
            return (float) (Math.sin(x) + Math.cos(x));
    }

    public static float f_ln(float x) {
        return (float) (2 * Math.log(x + 1));
    }

    public static float f_x_y_2(Float[]  vars ) {
        float x = vars[0];
        float y = vars[1];
        return x + 2 * y;
    }

    public static float f_sin_x_cos_y( Float[]  vars ) {
        float x = vars[0];
        return (float) (Math.sin(x / 2) + Math.cos(x));
    }

    public static float f_x_2_y_3( Float[]  vars ) {
        float x = vars[0];
        float y = vars[1];
        return (float) (Math.pow(x,2)  + 3 * x * y - 7 * y + 1);
    }
    private void generate(String filename, int numOfVars, float fieldStart, float fieldEnd, int numOfFitts, Function<Float, Float> function) {
        File file = new File("DatFiles/" + filename);

        try {
            var fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(numOfVars + " " + "100 " + fieldStart + " " + fieldEnd + " " + numOfFitts);
            bw.newLine();

            int n = 0;
            int d = 0;
            for (int i =0; i< numOfFitts; ++i) {
                String x1 = d + "." + n;
                bw.write(x1 +  " " + format.format(function.apply(Float.parseFloat(x1))));
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

    private void generate2vars(String filename, int numOfVars, float fieldStart, float fieldEnd, int numOfFitts, Function<Float [], Float> function) {
        File file = new File("DatFiles/" + filename);

        try {
            var fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(numOfVars + " " + "100 " + fieldStart + " " + fieldEnd + " " + numOfFitts);
            bw.newLine();

            int n = 0;
            int d = 0;
            for (int i =0; i< numOfFitts; ++i) {
                String x1 = d + "." + n;
                String x2 = n + "." + d;
                bw.write(x1 +  " "+ x2 + " " + format.format(function.apply(new Float[] {Float.parseFloat(x1), Float.parseFloat(x2)})));
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
        File directory = new File("DatFiles/");
        if (! directory.exists()){
            directory.mkdir();
        }

        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        FileGenerator fileGenerator = new FileGenerator();
        fileGenerator.generate("1_1.dat", 1, -10, 10, 100, FileGenerator::f_x_2);
        fileGenerator.generate("1_2.dat", 1, 0, 100, 100, FileGenerator::f_x_2);
        fileGenerator.generate("1_3.dat", 1, -1, 1, 100, FileGenerator::f_x_2);
        fileGenerator.generate("1_4.dat", 1, -1000, 1000, 100, FileGenerator::f_x_2);

        fileGenerator.generate("2_1.dat", 1, -3.14f, 3.14f, 100, FileGenerator::f_sin_cos);
        fileGenerator.generate("2_2.dat", 1, 0, 7, 100, FileGenerator::f_sin_cos);
        fileGenerator.generate("2_3.dat", 1, 0, 100, 100, FileGenerator::f_sin_cos);
        fileGenerator.generate("2_4.dat", 1, -100, 100, 100, FileGenerator::f_sin_cos);

        fileGenerator.generate("3_1.dat", 1, 0, 4, 100, FileGenerator::f_ln);
        fileGenerator.generate("3_2.dat", 1, 0, 9, 100, FileGenerator::f_ln);
        fileGenerator.generate("3_3.dat", 1, 0, 99, 100, FileGenerator::f_ln);
        fileGenerator.generate("3_4.dat", 1, -100, 999, 100, FileGenerator::f_ln);

        fileGenerator.generate2vars("4_1.dat", 2, 0, 1, 100, FileGenerator::f_x_y_2);
        fileGenerator.generate2vars("4_2.dat", 2, -10, 10, 100, FileGenerator::f_x_y_2);
        fileGenerator.generate2vars("4_3.dat", 2, 0, 100, 100, FileGenerator::f_x_y_2);
        fileGenerator.generate2vars("4_4.dat", 2, -1000, 1000, 100, FileGenerator::f_x_y_2);

        fileGenerator.generate2vars("5_1.dat", 2, -3.14f, 3.14f, 100, FileGenerator::f_sin_x_cos_y);
        fileGenerator.generate2vars("5_2.dat", 2, 0, 7, 100, FileGenerator::f_sin_x_cos_y);
        fileGenerator.generate2vars("5_3.dat", 2, 0, 100, 100, FileGenerator::f_sin_x_cos_y);
        fileGenerator.generate2vars("5_4.dat", 2, -100, 100, 100, FileGenerator::f_sin_x_cos_y);

        fileGenerator.generate2vars("6_1.dat", 2, -10, 10, 100, FileGenerator::f_x_2_y_3);
        fileGenerator.generate2vars("6_2.dat", 2, 0, 100, 100, FileGenerator::f_x_2_y_3);
        fileGenerator.generate2vars("6_3.dat", 2, -1, 1, 100, FileGenerator::f_x_2_y_3);
        fileGenerator.generate2vars("6_4.dat", 2, -1000, 1000, 100, FileGenerator::f_x_2_y_3);

    }
}
