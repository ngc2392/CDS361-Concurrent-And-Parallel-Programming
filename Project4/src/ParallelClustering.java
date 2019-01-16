import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@SuppressWarnings("Duplicates")
public class ParallelClustering {

    public static final int COMPUTE_CENTROIDS_THRESHOLD = 5;
    public static final int THRESHOLD = 10;

    public static final int K = 4;
    public static boolean oneHasChanged;

    // each cluster in its own thread
    public static void main(String[] args) {

        System.out.println("PARALLEL");

        List<College> colleges = readFile();

        Cluster[] clusters = new Cluster[K];

        // place centroids c1 ... ck at random locations

        for (int i = 0; i < clusters.length; i++) {

            // Choose a random college for the centroid
            Random rand = new Random();

            int index = rand.nextInt(colleges.size());

            College randomCollegeForCentroid = colleges.get(index);

            clusters[i] = new Cluster(i, randomCollegeForCentroid.getMedianIncome(), randomCollegeForCentroid.getPercentWhite(), randomCollegeForCentroid.getUnemploymentRate());

        }

        // add colleges to clusters
        for (int i = 0; i < colleges.size(); i++) {
            College collegeToAddToCluster = colleges.get(i);
            clusters[collegeToAddToCluster.getClusterIndex()].addCollege(collegeToAddToCluster);
        }

        ForkJoinPool pool = new ForkJoinPool();

        // compute the sum of the points in each cluster
        SumOfPointsInEachClusterTask sum = new SumOfPointsInEachClusterTask(0, clusters.length,clusters, colleges);
        pool.invoke(sum);

        int iterations = 0;

        do {

            System.out.println("Loop");

            oneHasChanged = false;

            DivideEachClusterTask divide = new DivideEachClusterTask(0, clusters.length,clusters, colleges);
            ReassignAndSumFusedTask task = new ReassignAndSumFusedTask(0, clusters.length, clusters, colleges);

            pool.invoke(divide);
            pool.invoke(task);

            // reset cluster sums
            for(Cluster c : clusters) {
                c.setSumOfX1(0);
                c.setSumOfX2(0);
                c.setSumOfX3(0);
            }

            iterations++;

        } while(oneHasChanged);


        for (int i = 0; i < clusters.length; i++) {
            System.out.println("Cluster number: " + clusters[i].getClusterNumber() + " has " + clusters[i].getCollegesInCluster().size() + " colleges in it");
        }

        System.out.println(iterations);

    }

    public static class ReassignAndSumFusedTask extends RecursiveAction {

        private int start;
        private int end;
        private Cluster[] clusters;
        private List<College> colleges;

        public ReassignAndSumFusedTask(int start, int end, Cluster[] clusters, List<College> colleges) {
            this.start = start;
            this.end = end;
            this.clusters = clusters;
            this.colleges = colleges;
        }

        @Override
        protected void compute() {

            System.out.println("ReassignAndSumFusedTask");


            if(end - start < THRESHOLD) {

                /*
                    for each point lo...hi
                        reassign;
                        add to correct entry;

                 */

                for(int i = start; i < end; i++) {

                    College tempCollege = colleges.get(i);

                    int indexOfClosestCentroid = getClosestCentroid(tempCollege, clusters);

                    // If centroids are different, switch clusters
                    if (tempCollege.getClusterIndex() != indexOfClosestCentroid) { // the index has changed
                        clusters[tempCollege.getClusterIndex()].remove(tempCollege); //remove tempCollege from its old cluster
                        tempCollege.setClusterIndex(indexOfClosestCentroid);   // assign the college to the new cluster
                        clusters[indexOfClosestCentroid].addCollege(tempCollege); //add tempCollege to its new cluster
                        oneHasChanged = true;


                        double sumOfX1 = 0;
                        double sumOfX2 = 0;
                        double sumOfX3 = 0;

                        for(int j = 0; j < clusters[tempCollege.getClusterIndex()].getCollegesInCluster().size(); j++) {

                            sumOfX1 += clusters[tempCollege.getClusterIndex()].getCollegesInCluster().get(j).getMedianIncome();
                            sumOfX2 += clusters[tempCollege.getClusterIndex()].getCollegesInCluster().get(j).getPercentWhite();
                            sumOfX3 += clusters[tempCollege.getClusterIndex()].getCollegesInCluster().get(j).getPercentWhite();
                        }

                        clusters[tempCollege.getClusterIndex()].setSumOfX1(clusters[tempCollege.getClusterIndex()].getSumOfX1() + sumOfX1);
                        clusters[tempCollege.getClusterIndex()].setSumOfX2(clusters[tempCollege.getClusterIndex()].getSumOfX2() + sumOfX2);
                        clusters[tempCollege.getClusterIndex()].setSumOfX3(clusters[tempCollege.getClusterIndex()].getSumOfX3() + sumOfX3);
                    }
                }

            } else {

                int mid = (start + end) / 2;
                ReassignAndSumFusedTask task1 = new ReassignAndSumFusedTask(start, mid, clusters, colleges);
                task1.fork();
                ReassignAndSumFusedTask task2 = new ReassignAndSumFusedTask(mid, start, clusters, colleges);
                task2.compute();
                task1.join();
            }

        }

    }

    public static class SumOfPointsInEachClusterTask extends RecursiveAction {

        private int start;
        private int end;
        private Cluster[] clusters;
        private List<College> colleges;

        public SumOfPointsInEachClusterTask(int start, int end, Cluster[] clusters, List<College> colleges) {

            this.start = start;
            this.end = end;
            this.clusters = clusters;
            this.colleges = colleges;

        }

        @Override
        protected void compute() {

            System.out.println("SumOfPointsInEachClusterTask");

            if ((end - start) < THRESHOLD) {

                for (int i = start; i < end; i++) {

                    Cluster currentCluster = clusters[i];

                    ArrayList<College> collegesInCluster = currentCluster.getCollegesInCluster();

                    double x1 = 0;
                    double x2 = 0;
                    double x3 = 0;

                    for(int j = 0; j < collegesInCluster.size(); j++) {

                        College collegeInCluster = currentCluster.getCollegesInCluster().get(j);

                        x1 += collegeInCluster.getMedianIncome();
                        x2 += collegeInCluster.getPercentWhite();
                        x3 += collegeInCluster.getUnemploymentRate();

                    }

                    currentCluster.setSumOfX1(x1);
                    currentCluster.setSumOfX2(x2);
                    currentCluster.setSumOfX3(x3);

                }

            } else {
                int mid = (start + end) / 2;
                SumOfPointsInEachClusterTask left = new SumOfPointsInEachClusterTask(start, mid, clusters, colleges);
                left.fork();
                SumOfPointsInEachClusterTask right = new SumOfPointsInEachClusterTask(mid, end, clusters, colleges);
                right.compute();
                left.join();
            }
        }
    }

    // Divide each cluster sum by the number of points in that cluster.
    public static class DivideEachClusterTask extends RecursiveAction {

        private int start;
        private int end;
        private Cluster[] clusters;
        private List<College> colleges;

        public DivideEachClusterTask(int start, int end, Cluster[] clusters, List<College> colleges) {
            this.start = start;
            this.end = end;
            this.clusters = clusters;
            this.colleges = colleges;
        }

        @Override
        protected void compute() {

            System.out.println("DivideEachClusterTask");

            if((end - start) < COMPUTE_CENTROIDS_THRESHOLD) {

                for(int i = start; i < end; i++) {

                    Cluster currentCluster = clusters[i];

                    int CLUSTER_SIZE = currentCluster.getCollegesInCluster().size();

                    double newX1 = currentCluster.getSumOfX1() / CLUSTER_SIZE;
                    double newX2 = currentCluster.getSumOfX2() / CLUSTER_SIZE;
                    double newX3 = currentCluster.getSumOfX3() / CLUSTER_SIZE;

                    currentCluster.setX1(newX1);
                    currentCluster.setX2(newX2);
                    currentCluster.setX3(newX3);
                }

            } else {

                int mid = (start + end) / 2;
                DivideEachClusterTask nct1 = new DivideEachClusterTask(start, mid, clusters, colleges);
                nct1.fork();
                DivideEachClusterTask nct2 = new DivideEachClusterTask(mid, end, clusters, colleges);
                nct2.compute();
                nct1.join();
            }
        }

    }

    // Gives the index of the closest c
    public static int getClosestCentroid(College c, Cluster[] centroids) {

        double minDistance = Integer.MAX_VALUE;
        int indexToReturn = 0;

        for(int i = 0; i < centroids.length; i++) {

            double tempDistance = centroids[i].computeDistance(c);

            if(tempDistance < minDistance) {
                indexToReturn = i;
                minDistance = tempDistance;
            }
        }
        return indexToReturn;
    }


    public static List<College> readFile() {

        List<College> points = new ArrayList<>();

        String line = " ";

        Random rand = new Random();

        try {
            BufferedReader br = new BufferedReader(new FileReader("colleges.csv"));

            br.readLine(); //skip headers

            while((line = br.readLine()) != null) {

                String[] row = line.split(",");

                int collegeID = Integer.parseInt(row[0]);
                String collegeName = row[3];

                String medianIncome = row[13];
                String unemploymentRate = row[15];
                String percentWhite = row[6];

                if(!(medianIncome.equals("NULL") || medianIncome.equals("PrivacySuppressed")) && !(percentWhite.equals("NULL") || percentWhite.equals("PrivacySuppressed")) && !(unemploymentRate.equals("NULL") || unemploymentRate.equals("PrivacySuppressed"))) {
                    double medianIncomeParsed = Double.parseDouble(medianIncome);
                    double percentWhiteParsed = Double.parseDouble(percentWhite);
                    double unemploymentRateParsed = Double.parseDouble(unemploymentRate);

                    int clusterIndexOfCollege = rand.nextInt(K);

                    College c = new College(collegeID, collegeName, medianIncomeParsed, percentWhiteParsed, unemploymentRateParsed, clusterIndexOfCollege);
                    points.add(c);
                }
            }

        } catch(IOException e) {

        }

        return points;
    }

}
