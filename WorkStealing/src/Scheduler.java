import java.util.Random;

/*
    - constructor - start all workers
    - launch(FJAction) - start a f/j.  wait until done
    - Steal from Random
 */

public class Scheduler {

    private Worker[] workers;
    private Random r;

    public Scheduler(int n) {

        r = new Random();

        workers = new Worker[n];
        for(int i = 0; i < n; ++i) {
            workers[i] = new Worker(this);
        }

        // start all the workers
        for(int i = 0; i < workers.length; i++) {
           // System.out.println("Starting worker");
            workers[i].startWorker();
        }
    }

    public void launch(FJAction actionToRun) {

        //System.out.println("Launch commencing.  Who gave trump the nuclear codes?");

        FJInitAction action = new FJInitAction(actionToRun, this);

        workers[0].pushFJAction(action);

        for(int i = 0; i < workers.length; i++) {
            Worker currentWorker = workers[i];
            synchronized (currentWorker) {
                currentWorker.notify();
            }
        }

        action.join();
    }

    public FJAction stealFromRandom(Worker worker) {

        //System.out.println("Stealing");

        while(true) {

            int index = r.nextInt(workers.length);

            Worker randomWorker = workers[index];

            FJAction action;

            if(randomWorker != worker) {
                //System.out.println("STEALING: The size of the deck is " + workers[index].getTasks().size());
                action = workers[index].getTasks().pollFirst();
            } else { // we got ourselves, so pick another random and go back to the beginning
                //System.out.println("CONTINUTING: WORKER: " + worker.getIndex());
                continue;
            }

            if(action == null) { // our steal attempt failed
                //System.out.println("ACTION IS NULL");
            } else { // our attempt succeeded
                //System.out.println("RETURNING STOLEN ACTION");
            }
            return action;
        }
    }
}
