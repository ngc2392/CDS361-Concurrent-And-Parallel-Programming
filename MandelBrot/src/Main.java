import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Main {

    private static int[][] pixels;
    public static BufferedImage image;
    public static final int THRESHOLD = 50;
    private static final int DEPTH = 1000;
    private static final int HEIGHT = 64;

    public static void main(String[] args) throws InterruptedException {

        image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        pixels = new int[300][300];

        ForkJoinPool pool = new ForkJoinPool();
        MandelTask mt = new MandelTask(0, pixels.length);

        pool.invoke(mt);

       // MandelTask mandelTask = new MandelTask(0, image.getWidth());


/*
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++) {
                System.out.print(pixels[i][j] + " ");
            }
            System.out.println();
        }
*/

        System.out.println("end");

        drawImage();

        displaysImage();

    }

    public static void drawImage() {
        Random rand = new Random();


        System.out.println("In draw image");
        for(int i = 0; i < pixels.length; i++) {
            for(int j = 0; j < pixels[i].length; j++) {

                if(pixels[i][j] < DEPTH) {
                    image.setRGB(i, j, 0xFFFFFF);
                } else {
                    image.setRGB(i, j, 0x000000);
                }
            }
        }
    }

    public static void displaysImage() {
        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public static int mandel(Complex c) {
        int count = 0;
        Complex z = new Complex(); //initialize to 0
        for(int k = 0; k < DEPTH; k++) {
            if(z.abs() >= 2.0) {
                break;
            }
            z = z.times(z).plus(c);
            count++;

            //System.out.println("z is " + z.toString());
            //System.out.println("count is " + count);
        }
        return count;
    }
    // book does scale function?
    public static void serial_mandel(int pixels[][], int startRow, int endRow) {
        for(int i = startRow; i < endRow; ++i) {
            for(int j = 0; j < pixels[i].length; ++j) {
                //Complex c = new Complex(i, j);

                int max = pixels.length;

                // convert pixels into complex coordinates between -2, 2
                double c_re = (j - pixels.length/2.0)*4.0/pixels[i].length;
                double c_im = (i - pixels[i].length/2.0)*4.0/pixels[i].length;

                pixels[i][j] = mandel(new Complex(c_re, c_im));

                //System.out.println(pixels[i][j]);
            }
        }
        System.out.println("Done");
    }

    public static class MandelTask extends RecursiveAction {

        int startRow;
        int endRow;

        public MandelTask(int x, int y) {
            this.startRow = x;
            this.endRow = y;
        }

        @Override
        protected void compute() {
            if(endRow - startRow < THRESHOLD) {
                serial_mandel(pixels, startRow, endRow);
            } else {
                int mid = (startRow + endRow) / 2;
                MandelTask left = new MandelTask(startRow, mid);
                left.fork();
                MandelTask right = new MandelTask(mid, endRow);
                right.compute();
                left.join();
            }
        }
    }
}
