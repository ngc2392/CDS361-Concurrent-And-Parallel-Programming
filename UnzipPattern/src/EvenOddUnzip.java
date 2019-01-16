import sun.awt.geom.Crossings;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

public class EvenOddUnzip {

    public static int SIZE = 25_000_000;
    public static int THRESHOLD = SIZE / 256;

    static  int[] input;
    static  int[] serialOutputEvens;
    static  int[] serialOutputOdds;

    static int[] parallelOutputEvens;
    static int[] parallelOutputOdds;


    public static void main(String[] args) {




        //int[] input = IntStream.generate(() -> new Random().nextInt(100) + 100).limit(100).toArray();

        input = new int[SIZE];

        for(int i = 0; i < SIZE; i++) {
            input[i] = i;
        }

        System.out.println("----------- SERIAL ---------------");

        long start = System.currentTimeMillis();

        serialUnzip(input);

        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        System.out.println("Serial took " + elapsedTime + " milli seconds");

        System.out.println(isUnzippedCorrectForEvensAndOdds(serialOutputEvens, serialOutputOdds) ? "SERIAL IS CORRECT!" : "SERIAL IS NOT CORRECT!");


        /*
        System.out.println("SERIAL EVENS:");

        for(int i = 0; i < 100; i++) {
            int e = serialOutputEvens[i];
            System.out.print(e + ",");

        }
        */

        /*

        System.out.println();
        System.out.println("SERIAL ODDS:");
        for(int i = 0; i < 100; i++) {
            int o = serialOutputOdds[i];
            System.out.print(o + ",");

        }
        */


        /*
        System.out.println("EVENS ARRAY");

        for(int e : serialOutputEvens) {
            System.out.print(e + ",");
        }
        */

        System.out.println();
/*
        System.out.println("Odds ARRAY");

        for(int o : serialOutputOdds) {
            System.out.print(o + ",");
        }

*/

        System.out.println("----------- PARALLEL ---------------");

        System.out.println(Runtime.getRuntime().availableProcessors());

        ForkJoinPool pool = new ForkJoinPool();

        if(SIZE % 2 == 0) {
            parallelOutputEvens = new int[input.length/2];
            parallelOutputOdds = new int[input.length/2];
        } else {
            parallelOutputEvens = new int[input.length/2];
            parallelOutputOdds = new int[(input.length/2) + 1];
        }



        EvenOddTask task = new EvenOddTask(input, parallelOutputEvens, parallelOutputOdds, 0, input.length);

        start = System.currentTimeMillis();

        pool.invoke(task);

        end = System.currentTimeMillis();
        elapsedTime = end - start;
        System.out.println("Parallel took " + elapsedTime + " milliseconds");

        System.out.println(isUnzippedCorrectForEvensAndOdds(parallelOutputEvens, parallelOutputOdds) ? "PARALLEL IS CORRECT!" : "PARALLEL IS NOT CORRECT!");


        /*
        System.out.println("PARALLEL INPUT:");

        for(int i = 0; i < 100; i++) {
            int in = input[i];
            System.out.print(in + ",");

        }
        */

        /*
        System.out.println("PARALLEL EVENS:");
        System.out.println("ARRAY SIZE: " + parallelOutputEvens.length);

        for(int i = 0; i < parallelOutputEvens.length/2; i++) {
            int e = parallelOutputEvens[i];
            System.out.print(e + ",");

        }

        System.out.println();
        System.out.println("PARALLEL ODDS:");
        System.out.println("ARRAY SIZE: " + parallelOutputOdds.length);

        for(int i = 0; i < 100; i++) {
            int o = parallelOutputOdds[i];
            System.out.print(o + ",");

        }
        */

    }

    public static void serialUnzip(int[] in) {

        if(in.length % 2 == 0) {
            serialOutputEvens = new int[in.length/2];
            serialOutputOdds = new int[in.length/2];

        } else {
            serialOutputEvens = new int[in.length/2];
            serialOutputOdds = new int[(in.length/2)+1]; //there is going to be on more odd
        }

        for(int i = 0; i < in.length; i++) {
            if(i % 2 == 0) {
                serialOutputEvens[i/2] = input[i];
            } else {
                serialOutputOdds[i/2] = input[i];
            }
        }
    }

    public static class EvenOddTask extends RecursiveAction {

        int[] input;
        //int[] evens;
        int[] parallelOutputEvens;
        int[] parallelOutputOdds;
        //int[] odds;
        int start;
        int end;

        public EvenOddTask(int input[], int[] evens, int[] odds, int start, int end) {
            this.input = input;
            //this.evens = evens;
            //this.odds = odds;
            this.parallelOutputEvens = evens;
            this.parallelOutputOdds = odds;
            this.start = start;
            this.end = end;
        }

        @Override
        public void compute() {
            if(end - start < THRESHOLD) {
                for(int i = start; i < end; i++) {
                    if(i % 2 == 0) {
                       // evens[i/2] = input[i];
                        parallelOutputEvens[i/2] = input[i];

                    } else {
                        parallelOutputOdds[i/2] = input[i];
                        //odds[i/2] = input[i];
                    }
                }
            } else {
                int mid = (start + end) / 2;
                //EvenOddTask t1 = new EvenOddTask(input, evens, odds, start, mid);
                EvenOddTask t1 = new EvenOddTask(input, parallelOutputEvens, parallelOutputOdds, start, mid);
                t1.fork();
                //EvenOddTask t2 = new EvenOddTask(input, evens, odds, mid, start);
                EvenOddTask t2 = new EvenOddTask(input, parallelOutputEvens, parallelOutputOdds, mid, end);
                t2.compute();
                t1.join();
            }
        }
    }

    public static boolean isUnzippedCorrectForEvensAndOdds(int[] outputEvens, int[] outputOdds) {

        for(int i = 0; i < input.length; i++) {
            if(input[i] % 2 == 0) { //input[i] is even
                if (outputEvens[i/2] != input[i]) {
                    System.out.println("odd " + outputOdds[i/2] + " from input: " + input[i]);
                    return false;
                }
            } else { //input[i] is even
                if(outputOdds[i/2] != input[i]) {
                    return false;
                }
            }

        }

        /*

        for(int i = 0; i < serialOutputEvens.length; i++) {
            if(serialOutputEvens[i] != input[i*2]) {
                return false;
            }
        }

        for(int i = 0; i < serialOutputOdds.length; i++) {
            if(serialOutputOdds[i] != input[(i*2)+1]) {
                return false;
            }
        }
        */
        return true;
    }
}
