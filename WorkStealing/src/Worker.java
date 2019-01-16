import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/*
    - A thread, a deque
    - run() - process the deqeue
    - add() (private!) - enqeue a task
    - StealFromMe() - attempt to steal
 */

public class Worker extends Thread {

    static int index = 0;

    private ConcurrentLinkedDeque<FJAction> deque; // nothing ever gets added from the other front.  We steal from this end
    private AtomicBoolean running;
    private Scheduler s;

    public final int THRESHOLD = 20; // If we try to steal and fail 20 times in a row, then we will wait.
    private int count;

    public FJAction stealFromAnotherWorker() {
        return s.stealFromRandom(this);
    }

    // enqueue a task
    public FJAction pushFJAction(FJAction action) {

        if(action == null)
            return null;

        // push the FJAction onto the deck
        deque.add(action); //add to tail

        return action;
    }

    /**
     * create a thread that will run tasks we supply to it via the add() method.
     */

    public Worker(Scheduler s) {
        deque = new ConcurrentLinkedDeque();
        running = new AtomicBoolean(false);
        this.s = s;
        this.count = 0; //keep track so that if all workers fail at stealing we wait
        this.index++;
    }

    public boolean idle() {

        //System.out.println("IN IDLE METHOD");
        boolean isIdle = true;
        if (this.count < THRESHOLD)
            return false;
        // count > THRESHOLD so we wait
        synchronized (this) {
            while (isIdle) {
                //System.out.println("Going to sleep. night night");
                try {
                    //System.out.println("Waiting...");
                    this.wait(2);
                }
                catch (InterruptedException e) { }
                isIdle = false;
            }

            return false; // if isIdle is true, then we are just waiting.  Once we wake up, we jump out of the loop and return false
        }
    }

    public void run() {

        while(isRunning() && !idle()) {

            //System.out.println("Running...");

            FJAction fja = deque.poll();

            if(fja == null) {
                fja = s.stealFromRandom(this);
            }

            if(fja != null) {
                this.count = 0;
                fja.compute();
                fja.setDone();
            }
            else {
                // steal failed
                this.count++;
            }

        }
    }

    // we only push tasks when forking.  When we steal we just execute
    public void doSomethingWillYou(FJAction initial) {
        FJAction actionToDo;

        if(this.getTasks().isEmpty()) {
            actionToDo = this.stealFromAnotherWorker();

            if(actionToDo == null) {
                this.count++;
            } else {
                this.count = 0;
            }

        } else {
            actionToDo = this.pop();
        }

        /*
        if(this.getTasks().isEmpty())
            actionToDo = this.stealFromAnotherWorker(); // if nothing in the deque, try stealing
        else
            actionToDo = this.pop(); // if the worker has something to do, do it
        */
        if(actionToDo != null) {
            if(!actionToDo.getIsDone()) {
                actionToDo.compute();
                actionToDo.setDone();

                if(actionToDo == initial) //we just did 'this' task, so our join is successful and we can exit
                    return;
            }
        }
    }

    public FJAction pop() {
       return deque.pop();
    }

    /**
     * start the object's thread running
     */
    public void startWorker() {
        running.set(true);
        this.start();
    }

    /**
     * stop the thread from running. Any remaining tasks in the thread are lost, although if
     * one is currently executing it will be completed.
     */
    public void stopWorker() {
        running.set(false);
    }

    /**
     * is the thread running right now?
     * @return true if so
     */
    public boolean isRunning() {
        return running.get();
    }

    public ConcurrentLinkedDeque<FJAction> getTasks() {
        return deque;
    }

    public void incrementCount() {
        this.count++;
    }

    public void resetCount() {
        this.count = 0;
    }

    public int getIndex() {
        return index;
    }
}