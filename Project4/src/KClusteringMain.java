import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("Duplicates")
public class KClusteringMain {

    static final int K = 4; // number of clusters

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


    public static Cluster[] placeCentroidsAtRandomLocations(List<College> colleges) {
        Cluster[] clusters = new Cluster[K];

        for(int i = 0; i < clusters.length; i++) {

            // Choose a random college for the centroid
            Random rand = new Random();

            int index = rand.nextInt(colleges.size());

            College randomCollegeForCentroid = colleges.get(index);

            clusters[i] = new Cluster(i, randomCollegeForCentroid.getMedianIncome(), randomCollegeForCentroid.getPercentWhite(), randomCollegeForCentroid.getUnemploymentRate());

            //clusters[i] = new Cluster(clusterNumber, Math.random() * 100_000 + 1_000, (int) (Math.random() * (100 - 0)) + 0, (int) (Math.random() * (6 - 0)) + 0);

        }

        // add colleges to clusters
        for(int i = 0; i < colleges.size(); i++) {
            College collegeToAddToCluster = colleges.get(i);
            clusters[collegeToAddToCluster.getClusterIndex()].addCollege(collegeToAddToCluster);
        }

        return clusters;
    }

    public static void main(String[] args) {

        System.out.println("SERIAL");

        List<College> colleges = readFile();

        // place centroids c1 ... ck at random locations
        Cluster[] clusters = placeCentroidsAtRandomLocations(colleges);

        // Repeat until convergence:
        //      for each point x_i
        //          find nearest centroid c_j

        int iterations = 0;

        boolean oneHasChanged = true;

        long start = System.nanoTime();

        while(oneHasChanged) {

            oneHasChanged = false;

            // For each point x, find nearest centroid cluster center.  Move point to new cluster if necessary
            for(int i = 0; i < colleges.size(); i++) {

                College tempCollege = colleges.get(i);

                int indexOfClosestCentroid = getClosestCentroid(tempCollege, clusters);

                // If centroids are different, switch clusters
                if(tempCollege.getClusterIndex() != indexOfClosestCentroid) { // the index has changed
                    clusters[tempCollege.getClusterIndex()].getCollegesInCluster().remove(tempCollege); //remove tempCollege from its old cluster
                    tempCollege.setClusterIndex(indexOfClosestCentroid);   // assign the college to the new cluster
                    clusters[indexOfClosestCentroid].addCollege(tempCollege); //add tempCollege to its new cluster
                    oneHasChanged = true;
                } else {
                    //System.out.println("Cluster of college didn't change");
                    //System.out.println("tempCollege Index: " + tempCollege.getClusterIndex() + " " + "closestCentroidIndex: " + indexOfClosestCentroid);
                }
            }

            // For each centroid, recompute its position: Find the averages of each cluster and use that as new centroid location
            for(int i = 0; i < clusters.length; i++) {

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

                x1 /= currentCluster.getCollegesInCluster().size();
                x2 /= currentCluster.getCollegesInCluster().size();
                x3 /= currentCluster.getCollegesInCluster().size();

                currentCluster.setX1(x1);
                currentCluster.setX2(x2);
                currentCluster.setX3(x3);
            }
            iterations++;
        }

        for(int i = 0; i < clusters.length; i++) {
            System.out.println("Cluster number: " + clusters[i].getClusterNumber() + " has " + clusters[i].getCollegesInCluster().size() + " colleges in it");
        }

        long end = System.nanoTime();
        long elaspsedTime = end - start;

        System.out.println("K clustering took " + elaspsedTime + " nano seconds");


        System.out.println("Completed " + iterations + " iterations of K-means clustering");

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
}

