import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@SuppressWarnings("Duplicates")
public class Main {

    public static int WORKERS = 64;
    public static BufferedImage image;

    public static void main(String[] args) {
        try {
            image = ImageIO.read(new File("/Users/LoganPhillips/Desktop/colors.jpg"));
            final int THRESHOLD = image.getHeight() / WORKERS;
        } catch (IOException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
        JLabel label = new JLabel(new ImageIcon(image));

        ForkJoinPool pool = new ForkJoinPool();

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                long start = System.nanoTime();
                //greyScaleAllCloseColors(e.getX(), e.getY());
                final int THRESHOLD = image.getHeight() / WORKERS;
                GreyScaleWithThreads gs = new GreyScaleWithThreads(0, image.getHeight(), e.getX(), e.getY(), THRESHOLD);
                pool.invoke(gs);
                long end = System.nanoTime();
                long timeElapsed = end - start;
                System.out.println("Grey scale took " + timeElapsed / 1_000_000.0 + " miliseconds");
                frame.repaint();
            }
        });
        frame.getContentPane().add(label);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    // https://java-8-tips.readthedocs.io/en/stable/forkjoin.html
    public static class GreyScaleWithThreads extends RecursiveAction {

        private int start;
        private int end;
        private int clickedPixelX;
        private int clickedPixelY;
        private int THRESHOLD;

        GreyScaleWithThreads(int x, int y, int clickedPixelX, int clickedPixelY, int THRESHOLD) {
            this.start = x;
            this.end = y;
            this.clickedPixelX = clickedPixelX;
            this.clickedPixelY = clickedPixelY;
            this.THRESHOLD = THRESHOLD;
        }

        @Override
        protected void compute() {
            //If the task is small, we want to execute the task
            if (end - start < THRESHOLD) {
                greyScale(start, end);
            } else { // Split the task into smaller chunks
                int mid = (end + start) / 2;
                GreyScaleWithThreads gs = new GreyScaleWithThreads(start, mid, clickedPixelX, clickedPixelY, THRESHOLD);
                gs.fork();
                GreyScaleWithThreads gs2 = new GreyScaleWithThreads(mid, end, clickedPixelX, clickedPixelY, THRESHOLD);
                gs2.compute();
                gs.join();
            }
        }

        public void greyScale(int start, int end) {

            int rgbOfPixelClicked = image.getRGB(clickedPixelX, clickedPixelY);
            Color colorOfPixelClicked = new Color(rgbOfPixelClicked); //sRGB is default color space
            double[] clickedPixelLAB = sRGBToLAB(colorOfPixelClicked);

            for (int y = start; y < end; y++) {
                for (int x = 0; x < image.getWidth(); x++) { // go through the row
                    int tempRGB = image.getRGB(x, y); // get each pixel in the row
                    Color tempColor = new Color(tempRGB);
                    double[] temp = sRGBToLAB(tempColor);

                    double deltaE = computeDeltaE(clickedPixelLAB, temp);

                    if(deltaE < 20) {
                        Color c = new Color(image.getRGB(x, y));
                        int red = (int)(c.getRed() * 0.299);
                        int green = (int)(c.getGreen() * 0.587);
                        int blue = (int)(c.getBlue() * 0.114);
                        int combine = red + green + blue;
                        Color greyScale = new Color(combine, combine, combine);
                        image.setRGB(y, x, greyScale.getRGB());
                    }
                }
            }
        }
    }

    // FOR SERIAL VERSION
    public static void greyScaleAllCloseColors(int xCoordinateClicked, int yCoordinateClicked) {

        int width = image.getWidth();
        int height = image.getHeight();

        int rgbOfPixelClicked = image.getRGB(xCoordinateClicked, yCoordinateClicked);
        Color colorOfPixelClicked = new Color(rgbOfPixelClicked); //sRGB is default color space
        double[] clickedPixelLAB = sRGBToLAB(colorOfPixelClicked);

        int[][] pixels = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //.getRGB() returns the value of color at location (x,y)
                // it is a binary format
                int tempRGB = image.getRGB(i, j);
                Color tempColor = new Color(tempRGB);
                double[] temp = sRGBToLAB(tempColor);

                double deltaE = computeDeltaE(clickedPixelLAB, temp);

                System.out.println("DeltaE is: " + deltaE);

                if(deltaE < 20) {
                    Color c = new Color(image.getRGB(i,j));
                    int red = (int)(c.getRed() * 0.299);
                    int green = (int)(c.getGreen() * 0.587);
                    int blue = (int)(c.getBlue() * 0.114);
                    int combine = red + green + blue;
                    Color greyScale = new Color(combine, combine, combine);
                    image.setRGB(i, j, greyScale.getRGB());

                    //image.setRGB(i, j, Color.GRAY.getRGB());
                    // image.setRGB(i, j, greyScaleThePixel.getRGB());
                }
            }
        }

    }


    public static double[] sRGBToXYZ(Color c) {

        /*
         *  at this link (https://en.wikipedia.org/wiki/Color_difference), to calculate the 1994 equation, we need
         *  to have Lab coordinates.
         *
         *
         * */

        // https://www.image-engineering.de/library/technotes/958-how-to-convert-between-srgb-and-ciexyz
        /* first convert sRGB to XYZ.  According to the docs, (https://docs.oracle.com/javase/7/docs/api/java/awt/Color.html),
           the colors are already in sRGB.  If you look at this link (https://en.wikipedia.org/wiki/CIELAB_color_space#CIELAB),
           there is a sub heading of CIELAB - CIEXYZ conversions which tells you how to get each Lab coordinate.  To do this conversion,
           our colors have to be XYZ.  This link (https://www.image-engineering.de/library/technotes/958-how-to-convert-between-srgb-and-ciexyz),
           explains how to convert from sRGB to XYZ.  According to the java docs (https://docs.oracle.com/javase/7/docs/api/java/awt/Color.html),
           the Color class uses sRGB as default.*/


        //STEP 1 : CONVERT sRGB TO XYZ. https://www.image-engineering.de/library/technotes/958-how-to-convert-between-srgb-and-ciexyz
        // The link explains that we should get to a range of [0...1].  Thats why I divide by 255.

        // sRGB values
        int sRed = c.getRed();
        int sGreen = c.getGreen();
        int sBlue = c.getBlue();

        // we first scale the sRGB data into a range of [0...1].
        double sRedScaled = (double) sRed / 255.0;
        double sGreenScaled = (double) sGreen / 255.0;
        double sBlueScaled = (double) sBlue / 255.0;

        /* NOW WE HAVE TO LINEARIZE THE DATA BEFORE WE GET THE XYZ */

        double sRedScaledPrime = linearizeValue(sRedScaled);
        double sGreenScaledPrime = linearizeValue(sGreenScaled);
        double sBlueScaledPrime = linearizeValue(sBlueScaled);

        sRedScaledPrime *= 100;
        sGreenScaledPrime *= 100;
        sBlueScaledPrime *= 100;

        /* NOW CONVERT TO XYZ.  THIS LOOKS AWFUL, BUT I AM JUST USING THE FUNCTION ON THE WEBSITE */
        double X = (0.4124564 * sRedScaledPrime) + (0.3575761 * sGreenScaledPrime) + (0.1804375 * sBlueScaledPrime);
        double Y = (0.2126729 * sRedScaledPrime) + (0.7151522 * sGreenScaledPrime) + (0.0721750 * sBlueScaledPrime);
        double Z = (0.0193339 * sRedScaledPrime) + (0.1191920 * sGreenScaledPrime) + (0.9503041 * sBlueScaledPrime);


        double[] XYZ = new double[] {X, Y, Z};

        return XYZ;
    }

    public static double[] sRGBToLAB(Color c) {

        //STEP 1 : CONVERT sRGB TO XYZ
        double[] XYZ = sRGBToXYZ(c);


        //STEP 2: CONVERT XYZ TO LAB.  USE THE EQUATION HERE: https://en.wikipedia.org/wiki/CIELAB_color_space#CIELAB
                // UNDER THE HEADING: 'CIELAB - CIEXYZ conversions'

        /* Now convert XYZ to LAB */
        final double delta = (double) 6 / 29;

        // Illuminant D65
        final double X_n = 95.047;
        final double Y_n = 100.000;
        final double Z_n = 108.883;

        double f_L = f(XYZ[1]/Y_n);
        double f_a1 = f(XYZ[0] / X_n);
        double f_a2 = f(XYZ[1] / Y_n);
        double f_b1 = f(XYZ[1] / Y_n);
        double f_b2 = f(XYZ[2] / Z_n);

        //Calculate the Lab coordinates
        double L = (116 * f_L) - 16;
        double a = 500 * (f_a1 - f_a2);
        double b = 200 * (f_b1 - f_b2);

        // END OF GETTING THE 'b' Value

        //NOW WE SHOULD HAVE L, a, b.  I put them in an array to return.
        double[] Lab = new double[]{L, a, b};

        return Lab;
    }

    public static int[] LABtoRGB(double[] LAB) {

        int[] result = new int[3];

        //Illuminant D65
        final double X_n = 95.047;
        final double Y_n = 100.000;
        final double Z_n = 108.883;

        double X = X_n * fInverse((((LAB[0] + 16.0) / 116.0) + LAB[1] / 500.0));
        double Y = Y_n * fInverse(((LAB[0] + 16.0) / 116.0));
        double Z = Z_n * fInverse(((LAB[0] + 16.0) / 116.0) - (LAB[2] / 200.0));

        // XYZ to sRGB
        double x = X / 100.0;
        double y = Y / 100.0;
        double z = Z / 100.0;

        double sRPrime = (3.2404542 * x) + (-1.5371385 * y) + (-0.4985314 * z);
        double sGPrime = (-0.9692660 * x) + (1.8760108 * y) + (0.0415560 * z);
        double sBPrime = (0.0556434 * x) + (-0.2040259 * y) + (1.0572252 * z);

        double r = linearizeBack(sRPrime);
        double g = linearizeBack(sGPrime);
        double b = linearizeBack(sBPrime);

        r = (r < 0) ? 0 : r;
        g = (g < 0) ? 0 : g;
        b = (b < 0) ? 0 : b;

        // convert 0..1 into 0.255
        result[0] = (int) Math.round(r * 255);
        result[1] = (int) Math.round(g * 255);
        result[2] = (int) Math.round(b * 255);

        return result;

    }

    public static double linearizeBack(double value) {
        double r = 0;

        if(value > 0.0031308) {
            r = ((1.055 * Math.pow(value, 1.0 / 2.4)) - 0.55);
        } else {
            r = (value * 12.92);
        }

        return r;
    }

    public static double fInverse(double t) {
        double delta = 6/29;

        double inverse = 0;

        if(t > delta) {
            inverse = Math.pow(t, 3);
        } else {
            inverse = 3*(delta*delta) * (t - (4/29));
        }

        return inverse;
    }

    public static double linearizeValue(double scaledValue) {

        double linearize ;

        if (scaledValue <= 0.04045) {
            linearize = scaledValue / 12.92;
        } else {
            linearize = Math.pow((scaledValue + 0.055) / 1.055, 2.4);
        }
        return linearize;
    }

    //Now that we have the Lab coordinates, we will compute the distance here: https://en.wikipedia.org/wiki/Color_difference, under the heading 'CIE94'
    public static double computeDeltaE(double[] clickedPixelLAB, double[] tempPixelLAB) {

        final double k_L = 1.0;
        final double k_1 = 0.045;
        final double k_2 = 0.015;

        final double s_L = 1.0;
        final double k_C = 1.0; //The above article says that Kc and Kh are usually both unity.  Google says that unity is a synonym for '1'
        final double k_H = 1.0;

        //first computation
        double firstComputation = Math.pow((clickedPixelLAB[0] - tempPixelLAB[0]) / (k_L * s_L), 2);

        //second computation
        double c_1 = Math.sqrt(Math.pow(clickedPixelLAB[1], 2) + Math.pow(clickedPixelLAB[2], 2));
        double c_2 = Math.sqrt(Math.pow(tempPixelLAB[1], 2) + Math.pow(tempPixelLAB[2], 2));

        double s_C = 1 + (k_1 * c_1);

        double secondComputation = Math.pow((c_1 - c_2) / (k_C * s_C), 2);

        //third computation
        final double s_H = 1 + (k_2 * c_1);

        double deltaH = Math.sqrt((Math.pow(clickedPixelLAB[1] - tempPixelLAB[1], 2) + Math.pow(clickedPixelLAB[2] - tempPixelLAB[2], 2) - Math.pow(c_1 - c_2, 2)));

        double thirdComputation = Math.pow(deltaH / (k_H * s_H), 2);

        // add up the 3 parts and then square root it
        double colorDifference = Math.sqrt(firstComputation + secondComputation + thirdComputation);

        return colorDifference;
    }

    private static double f(double t) {
        final double delta = 9.0 / 29.0;

        double r = 0;
        if( t > Math.pow(delta, 3)) {
            r = Math.cbrt(t);
        } else {
            r = ((t/(3* Math.pow(delta, 2))) + 4.0/29.0);
        }
        return r;
    }
}