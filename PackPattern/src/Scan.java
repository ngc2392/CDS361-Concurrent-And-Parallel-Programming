import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Scan {

    public static int[] getDestinationArray(int[] input) {

        System.out.println(split(0));

        ForkJoinPool pool = new ForkJoinPool();

        int r[] = new int[input.length];
        int s[] = new int[input.length];

        int n = input.length;

        Upsweep upsweep = new Upsweep(0,n,input, r);
        pool.invoke(upsweep);
        Downsweep downsweep = new Downsweep(0, n, s, r, input,0);
        pool.invoke(downsweep);

        return s;

    }

    //get the highest power of 2 less then n
    public static int split(int n) {

        //find left most 1 and make everything to the right 0
        return Integer.highestOneBit(n-1);
    }

    public static class Upsweep extends RecursiveAction {

        private int i;
        private int m;
        private int[] input;
        private int[] r;

        public  Upsweep(int i, int m, int[] input, int[] r) {
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
        private int[] r;
        private int[] s; //where you are ultimately going to end up
        int[] input;
        private int initial;


        public Downsweep(int i, int m, int[] s, int[] r, int[] input,  int initial) {
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

