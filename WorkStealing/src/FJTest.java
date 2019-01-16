public class FJTest {

    /*
    static class DotProd extends FJAction {

        public final static int SIZE = 200_000_000;
        public final static int THRESHOLD = SIZE / 32;

        int low;
        int hi;
        double[] x;
        double[] y;
        static long r;


        public DotProd(int low, int hi, double[] x, double[] y) {
            this.low = low;
            this.hi = hi;
            this.x = x;
            this.y = y;
        }

        public void compute() {
            if(hi - low < THRESHOLD) {
                for (int i=low; i<hi; ++i) {
                    //r += x[i] * y[i];

                    //r += (long) Math.tan(x[i] * y[i]);
                }
            } else {
                int mid = (hi+low) / 2;
                DotProd left = new DotProd(low, mid, x, y);
                left.fork();
                DotProd right = new DotProd(mid, hi, x, y);
                right.compute();
                left.join();
            }
        }

        public static long getR() {
            return r;
        }

        public static void main(String[] args) {
            Scheduler s = new Scheduler(4);

           // double[] x = DoubleStream.generate(ThreadLocalRandom.current()::nextDouble).limit(100_000).toArray();
            //double[] y = DoubleStream.generate(ThreadLocalRandom.current()::nextDouble).limit(100_000).toArray();

            double[] x = new double[100];
            double[] y = new double[100];

            for(int i = 0; i < x.length; i++) {
                x[i] = 1;
                y[i] = 2;
            }

            s.launch(new DotProd(0, x.length, x, y));

            System.out.println("DOTPROD IS: " + getR());
        }
    }
    */

}
