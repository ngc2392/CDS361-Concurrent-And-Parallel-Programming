
// The input to your scan is an array of doubles.
// The output is an array of the partial sums of the input array.

import java.awt.*;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ScanMain {

    private static final int THRESHOLD = 64;

    public static void main(String[] args) {

        System.out.println(split(0));

        ForkJoinPool pool = new ForkJoinPool();

        //double[] r = getRandomArray(30);
        double input[] = new double[]{2.5,7.1,1.8,15.6,1.2,11.1};

        double r[] = new double[input.length];
        double s[] = new double[input.length];

        int n = input.length;

        Upsweep upsweep = new Upsweep(0,n,input, r);
        pool.invoke(upsweep);

        Downsweep downsweep = new Downsweep(0, n, s, r, input,0.0);
        pool.invoke(downsweep);

        for(double d : s) {
            System.out.print(d + " ,");
        }

    }

    public static double[] getRandomArray(int size) {

        double[] a = new double[size];

        for(int i = 0; i < size; i++) {
            Random r = new Random();
            a[i] = r.nextDouble();
        }

        return a;
    }

    //get the highest power of 2 less then n
    public static int split(int n) {

        //find left most 1 and make everything to the right 0

        return Integer.highestOneBit(n-1);
    }

    public static class Upsweep extends RecursiveAction {

        private int i;
        private int m;
        private double[] input;
        private double[] r;

        public  Upsweep(int i, int m, double[] input, double[] r) {
            this.i = i;
            this.m = m;
            this.r = r;
            this.input = input;
        }

        @Override
        protected void compute() {

            if(m == 1) {
               r[i] = input[i];
            } else {
                int k = split(m);
                Upsweep us = new Upsweep(i, k, input, r);
                us.fork();
                Upsweep us2 = new Upsweep(i+k, m-k, input, r);
                us2.compute();
                us.join();

                if(m==2*k) {
                    r[i+m-1] = r[i+k-1] + r[i+m-1];
                }
            }
        }
    }

    public static class Downsweep extends RecursiveAction {
            private int i;
            private int m;
            private double[] r;
            private double[] s; //what you are ultimately going to end up
            private double[] input;
            private double initial;


        public Downsweep(int i, int m, double[] s, double[] r, double[] input,  double initial) {
            this.i = i;
            this.m = m;
            this.r = r;
            this.initial = initial;
            this.s = s;
            this.input = input;
        }

        @Override
        protected void compute() {

            if(m == 1) {
                s[i] = initial + input[i];
            } else {
                int k = split(m);
                Downsweep ds = new Downsweep(i, k, s, r, input, initial);
                ds.fork();
                initial += r[i+k-1];
                Downsweep ds2 = new Downsweep(i+k, m-k, s, r, input, initial);
                ds2.compute();
                ds.join();
            }
        }
    }
}
