import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Map {

    //start the recursion and return the result
    public static int[] booleansToIntegers(boolean[] include) {

        int[] result = new int[include.length];

        ForkJoinPool pool = new ForkJoinPool();
        BooleansToIntegersTask task = new BooleansToIntegersTask(0, include.length, include, result);

        pool.invoke(task);

        return result;
    }

    public static class BooleansToIntegersTask extends RecursiveAction {
        private final int THRESHOLD = 100;

        private int start;
        private int end;
        private boolean[] include;
        private int[] result;

        public BooleansToIntegersTask(int start, int end, boolean[] include, int[] result) {
            this.start = start;
            this.end = end;
            this.include = include;
            this.result = result;
        }

        @Override
       protected void compute() {

            if((end-start) < THRESHOLD) {

               for(int i = start; i < end; i++) {
                   if(include[i]) {
                       result[i] = 1;
                   } else {
                       result[i] = 0;
                   }
               }

            } else {
                int mid = (start+end) / 2;
                BooleansToIntegersTask task1 = new BooleansToIntegersTask(start, mid, include, result);
                task1.fork();
                BooleansToIntegersTask task2 = new BooleansToIntegersTask(mid, end, include, result);
                task2.compute();
                task1.join();
            }
        }
    }
}
