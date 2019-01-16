// http://farenda.com/algorithms/parallel-merge-sort-in-java/


import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

// use java fork join, open up 'ex' project
public class Main{

    public static int WORKERS = 1024;

    public static void main(String[] args) {

        //int[] a = new int[]{};

        //int[] a = createArrayWithRandomNumbers(50);
        int[] a = new int[]{56,12,8,3,4,99,4,8,1};

        System.out.println("Will be sorting");
        printNumbers(a);


        ForkJoinPool pool = new ForkJoinPool();
        ParallelMergeSort ms = new ParallelMergeSort(a, 0, a.length);

        pool.invoke(ms);

        System.out.println("SORTED RESULT: ");

        printNumbers(a);

    }

    public static class ParallelMergeSort extends RecursiveAction {

        int[] array;
        int start;
        int end;


        public ParallelMergeSort(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        protected void compute() {
            if(end-start<WORKERS) {

                Arrays.sort(array);
                //sort(array);
                //return;
            } else {
                int mid = array.length / 2;
                ParallelMergeSort leftOfMid = new ParallelMergeSort(array, start, mid);
                leftOfMid.fork();
                ParallelMergeSort rightOfMid = new ParallelMergeSort(array, mid, end);
                rightOfMid.compute();
                leftOfMid.join();
                merge(array,leftOfMid.array, rightOfMid.array);
                // merge(list, start, middle, end)
            }
        }
    }


    public static void merge(int[] array, int[] left, int[] right) {

        int leftIndex = 0;
        int rightIndex = 0;
        int indexOfArray = 0;
        // Put the values from 'left' and 'right' into the right spot in the input array
        while (leftIndex < left.length && rightIndex < right.length){
            if(left[leftIndex] < right[rightIndex]) {
                array[indexOfArray] = left[leftIndex];
                indexOfArray++;
                leftIndex++;
            } else {
                array[indexOfArray] = right[rightIndex];
                indexOfArray++;
                rightIndex++;
            }
        }

        //jump out of the loop when we are done with either the left sub array or right sub array


        //if there are any remainder in 'left' or 'right', we have to add it
        // When one list becomes empty, copy all values from the remaining array into the sorted array
        while(leftIndex < left.length) {
            array[indexOfArray] = left[leftIndex];
            indexOfArray++;
            leftIndex++;
        }

        while(rightIndex < right.length) {
            array[indexOfArray] = right[rightIndex];
            indexOfArray++;
            rightIndex++;
        }
    }


    public static void printNumbers(int[] a) {
        int w = 0;
        for(int i : a) {
            if(w == a.length - 1) {
                System.out.print(i);
            } else {
                System.out.print(i + ",");
            }
            w++;
        }
    }

    // n is the size of the array
    public static int[] createArrayWithRandomNumbers(int n) {

        int[] array = new int[n];
        Random rand = new Random();

        for(int i = 0; i < array.length; i++){
            array[i] = rand.nextInt();
        }

        return array;

    }

}


