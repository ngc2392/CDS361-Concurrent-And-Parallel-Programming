public class College {

    private final int ID;
    private final String name;
    private int clusterIndex; //index in the cluster array that this college belongs to ... from 0 to k-1

    private double medianIncome; // MEDIAN_HH_INC; // Median household income
    private double percentWhite; //PCT_WHITE;   // Percent of the population from students' zip codes that is White, via Census data
    private double unemploymentRate;  // UNEMP_RATE; // Unemployment rate, via Census data

    public College(int ID, String name, double medianIncome, double percentWhite, double unRate, int clusterIndex) {
        this.ID = ID;
        this.name = name;
        this.medianIncome = medianIncome;
        this.percentWhite = percentWhite;
        this.unemploymentRate = unRate;
        this.clusterIndex = clusterIndex;
    }

    public void setClusterIndex(int index) {
        this.clusterIndex = index;
    }

    public int getCollegeID() {
        return this.ID;
    }

    public String getCollegeName() {
        return this.name;
    }

    public int getClusterIndex() {
        return this.clusterIndex;
    }

    public double getMedianIncome() {
        return this.medianIncome;
    }

    public double getPercentWhite() {
        return this.percentWhite;
    }

    public double getUnemploymentRate() {
        return this.unemploymentRate;
    }


    public String toString() {

        int ID = this.ID;
        String name = this.name;
        double medianIncome = this.medianIncome;
        double percentWhite = this.percentWhite;
        double unemploymentRate = this.unemploymentRate;

        return name + " has an ID of " + ID + ", with an Median Income of " + medianIncome + " and " + percentWhite + " % of the students are white.  The unemployment rate is " + unemploymentRate + "%";
    }

}
