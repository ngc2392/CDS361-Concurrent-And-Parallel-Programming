public class FJInitAction extends FJAction {

    private FJAction action;
    private Scheduler s;

    public FJInitAction(FJAction action, Scheduler s) {
        this.action = action;
        this.s = s;
    }

    public void compute() {
        action.compute();

        synchronized (s) {
            action.setDone();
            s.notifyAll();
        }
    }

    public void join() {
        synchronized (s) {
            while(!action.getIsDone()) {
                try {
                    s.wait();
                } catch (InterruptedException e) {

                }
            }
        }
    }
}