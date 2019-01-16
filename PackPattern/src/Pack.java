import java.util.ArrayList;

public class Pack {

    //should combine map and scan operation in one fused operation.  That should be faster than doing a map and then a scan

    public static void main(String[] args) {

        // ---------------- SERIAL --------------

        System.out.println("----------- SERIAL -----------");

        int SIZE = 10;
        int[] input = new int[SIZE];

        for (int i = 0; i < SIZE; i++) {
            input[i] = i;
        }

        boolean[] includeInput = new boolean[SIZE];

        int sizeOfFinalArray = 0;

        for(int i = 0; i < includeInput.length; i++) {

            if(i % 2 == 0) {
                includeInput[i] = true;
                sizeOfFinalArray++;
            } else {
                includeInput[i] = false;
            }
        }

        System.out.println("PRINTING ORIGINAL ARRAY");
        printArray(input);

        System.out.println();
        System.out.println("PRINTING TRUE AND FALSE ARRAY ");
        printArray(includeInput);

        System.out.println();
        ArrayList<Integer> result = serialPack(input, includeInput);
        printArrayList(result);


        // ---------------- PARALLEL --------------

        System.out.println();
        System.out.println("----------- PARALLEL -----------");

        int[] results = parallelPack(input, includeInput, sizeOfFinalArray);

        printArray(results);

    }

    public static void printArrayList(ArrayList<Integer> in) {
        for(int i = 0; i < in.size(); i++) {
            if(i == in.size()) {
                System.out.println(in.get(i));
            } else {
                System.out.print(in.get(i) + ",");
            }
        }
    }

    public static void printArray(int[] arr) {

        for(int i = 0; i < arr.length; i++) {
            if(i == arr.length) {
                System.out.println(arr[i]);
            } else {
                System.out.print(arr[i] + ",");
            }
        }
    }

    public static void printArray(boolean[] arr) {

        for(int i = 0; i < arr.length; i++) {
            if(i == arr.length) {
                System.out.println(arr[i]);
            } else {
                System.out.print(arr[i] + ",");
            }
        }
    }

    public static ArrayList<Integer> serialPack(int[] input, boolean[] includeInput) {

        ArrayList<Integer> packedResult = new ArrayList<>();

        int index = 0;
        for(int i = 0; i < input.length; i++) {
            if(includeInput[i]) {
                packedResult.add(input[i]);
                index++;
            }
        }
        return packedResult;
    }


    public static int[] parallelPack(int[] input, boolean[] booleans, int sizeOfFinalArray) {

        // Map function
        int[] integers = Map.booleansToIntegers(booleans);

        // Use function to get destinations
        int[] destinations = Scan.getDestinationArray(integers);

        int[] result = Scatter.getFinalResult(input, destinations, integers, sizeOfFinalArray);

        return result;

    }

}
