import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

public class UnzipMain {

    static int[] parallelInput;
    static int[] parallelOutputX;
    static int[] parallelOutputY;

    public static void main(String[] args) {

        // array of x, y pairs.  Put x's in one array and y's in another

        /*
        int[] arr = new int[]{1, 5, 2, 5, 3, 5, 4, 5, 6, 7};

        Output out = unzip(arr);

        System.out.println("X ARRAY");

        for(int x : out.x) {
            System.out.print(x + ",");
        }

        System.out.println();
        System.out.println("Y ARRAY");

        for(int y : out.y) {
          System.out.print(y + ",");
        }

        System.out.println();

*/
        // --------- PARALLEL ------------

        /*
        System.out.println("THE RANDOM ARRAY IS ");

        for(int x : parallelInput) {
            System.out.print(x + ",");
        }
        */

        UnzipXYCoordinates();

        System.out.println();
        System.out.println("PARALLEL OUTPUT X COORDINATES");

        for(int x : parallelOutputX) {
            System.out.print(x + ",");
        }

        System.out.println();
        System.out.println("PARALLEL OUTPUT Y COORDINATES");

        for(int y : parallelOutputY) {
            System.out.print(y + ",");
        }

    }

    public static void UnzipXYCoordinates() {
        ForkJoinPool pool = new ForkJoinPool();

        //generate 100 random number between 100 to 200

        //parallelInput = IntStream.generate(() -> new Random().nextInt(100) + 100).limit(100).toArray();

         parallelInput = new int[]{1, 5, 2, 5, 3, 5, 4, 5, 6, 7};

        // intialize xCoordiantes and yCoordaintes

        parallelOutputX = new int[parallelInput.length/2];
        parallelOutputY = new int[parallelInput.length/2];

        UnzipTask ut = new UnzipTask(parallelInput);

        //pool.invoke(ut);

        pool.invoke(new UnzipTask(parallelInput));

    }



    /**
     *
     * @param n number of elements in data collection
     * @param m number of elements in index collection
     * @param a input data collection (n elements)
     * @param A output data collection (m elements)
     * @param index input index collection (m elements)
     */

    public static void gather(int n, int m, int[] a, int[] A, int[] index) {
        for(int i = 0; i < m; i++) {
            int j = index[i];
            assert (0 <= j && j < n); //if ((0 > j || j >= n)) throw new AssertionError();
            A[i] = a[j];
        }
    }

    public static class Output {

        int[] x;
        int[] y;

        public Output(int n) {
            this.x = new int[n/2];
            this.y = new int[n/2];
        }
    }

    // change to serial unzip.  Just make them global variables
    public static Output unzip(int[] input) {

        Output out = new Output(input.length);

        int index = 0;

        for(int i = 0; i < input.length/2; i++) {

            out.x[i] = input[index];
            index += 2;
            //System.out.println("i is " + i + " " + " input is " + input[i]);
        }

        index = 1;
        for(int i = 0; i < input.length/2; i++) {

            out.y[i] = input[index];
            index += 2;
            //System.out.println("i is " + i + " " + " input is " + input[i]);
        }

        return out;
    }

    public static class UnzipTask extends RecursiveAction {

        int[] input;
        int[] parallelOutputX;
        int[] parallelOutputY;

        public UnzipTask(int[] input) {
            this.input = input;
        }

        @Override
        public void compute() {

            // until we get down to two pairs
            if(input.length < 4) {
                // put in right spot
                for(int i = 0; i < input.length; i++) {
                    if(i % 2 == 0) {  // the index is even so it is an x value
                        parallelOutputX[i/2] = input[i];
                    } else { // the index is odd so it is an y value
                        parallelOutputY[(i/2)+1] = input[i];
                    }
                }

            } else {
                int[] firstHalf = Arrays.copyOfRange(input, 0, input.length/2);
                UnzipTask task1 = new UnzipTask(firstHalf);
                task1.fork();
                int[] secondHalf = Arrays.copyOfRange(input, input.length/2, input.length);
                UnzipTask task2 = new UnzipTask(secondHalf);
                task2.compute();
                task1.join();
            }
        }
    }





    //fork join
    // split into sections, need to find where to put the number.  Divide by 2 maybe add something

}
