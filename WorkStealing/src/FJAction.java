
/*
    - constructor - pass in parameters
    - compute - recursive
    - fork() - enqueue on current worker
    - join() - wait to complete

 */

public abstract class FJAction {

    private volatile boolean isDone;

    public FJAction() {
        isDone = false;
    }

    // recursive
    public abstract void compute();

    /**
     * push this FJAction object onto the deque of the ActiveObject that is runs fork()
     */

    public void fork() {
        Worker worker = (Worker) Thread.currentThread();
        worker.pushFJAction(this); //stick on correct end of the deque
    }

    /**
     * Wait until this action is completed.  Allow the worker that calls join to continue executing (other) tasks while we wait.
     */
    public void join() {
        Worker worker = (Worker) Thread.currentThread();

        // do other tasks until this task is completed
        while (!this.isDone) {
            worker.doSomethingWillYou(this);
        }
    }

    public void setDone() {
        this.isDone = true;
    }

    public boolean getIsDone() {
        return this.isDone;
    }

}
