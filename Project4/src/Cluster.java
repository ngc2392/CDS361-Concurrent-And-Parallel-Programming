import sun.awt.motif.X11Dingbats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Cluster {

    private int number;
    // These three points represent the location of the centroid (cluster center)
    private double x1; // medianIncome
    private double x2; // percentWhite
    private double x3; // unemploymentRate

    private double sumOfX1; // sum of all X'1s in this cluster
    private double sumOfX2;
    private double sumOfX3;

    private ArrayList<College> colleges; // List of colleges in this cluster.  These are the other points in the cluster

    public Cluster(int number, double x1, double x2, double x3) {

        this.x1 = x1;
        this.x2 = x2;
        this.x3 = x3;
        this.colleges = new ArrayList<>();
        this.number = number;
    }

    public Cluster(int number, double x1, double x2, double x3, ArrayList<College> collegesInThisCluster) {
        this.x1 = x1;
        this.x2 = x2;
        this.x3 = x3;
        this.colleges = collegesInThisCluster;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public void setX3(double x3) {
        this.x3 = x3;
    }




    public int getClusterNumber() {
        return this.number;
    }

    //we are comparing colleges to centroids
    public double computeDistance(College c) {

        double distance1 = Math.pow(x1 - c.getMedianIncome(), 2);
        double distance2 = Math.pow(x2 - c.getPercentWhite(), 2);
        double distance3 = Math.pow(x3 - c.getUnemploymentRate(), 2);

        return distance1 + distance2 + distance3;

    }

    public void remove(College c) {
            colleges.remove(c);
    }

    public void addCollege(College c) {
            colleges.add(c);
    }

    public ArrayList<College> getCollegesInCluster() {
        return this.colleges;
    }


    public void setSumOfX1(double i) {
        this.sumOfX1 = i;
    }


    public void setSumOfX2(double i) {
        this.sumOfX2 = i;
    }


    public void setSumOfX3(double i) {
        this.sumOfX3 = i;
    }


    public double getSumOfX1() {
        return this.sumOfX1;
    }

    public double getSumOfX2() {
        return this.sumOfX2;
    }

    public double getSumOfX3() {
        return this.sumOfX3;
    }
}
