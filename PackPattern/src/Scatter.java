import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Scatter {

    public static int[] getFinalResult(int[] input, int[] destinations, int[] binary, int sizeOfFinalArray) {

        /*

        last index is the sum of all 1's

        int resultSize;

        if(binary[binary.length-1] == 0) {
            resultSize = destinations[destinations.length-1];
        } else {
            resultSize = destinations[destinations.length-1]+1;

        }

        int results[] = new int[resultSize];
        */

        int[] results = new int[sizeOfFinalArray];

        ForkJoinPool pool = new ForkJoinPool();

        FinalResultTask task = new FinalResultTask(0, input.length, input,destinations, binary, results);

        pool.invoke(task);

        return results;
    }

    public static class FinalResultTask extends RecursiveAction {

        private final int THRESHOLD = 100;

        private int start;
        private int end;
        private int[] input;
        private int[] destinations; //indices of where the value is in input, if binary is 1
        private int[] binary;
        private int[] results;

        public FinalResultTask(int start, int end, int[] input, int[] destinations, int[] binary, int[] results) {
            this.start = start;
            this.end = end;
            this.input = input;
            this.destinations = destinations;
            this.binary = binary;
            this.results = results;
        }

        @Override
        protected void compute() {
            if((end - start) < THRESHOLD) {
                for(int i = start; i < end; i++) {
                    if(binary[i] == 1) { //binary[i] = 1 means that we want to keep input[i].  destinations[i] is the index of where to write input[i] in the results array
                        results[destinations[i]-1] = input[i];
                    }
                }
            } else {
                int mid = (start+end) / 2;
                FinalResultTask task1 = new FinalResultTask(start, end, input, destinations, binary, results);
                task1.fork();
                FinalResultTask task2 = new FinalResultTask(mid, end, input, destinations, binary, results);
                task2.compute();
            }
        }
    }
}
