public class UpdateRange {

    private int minRangeP;
    private int maxRangeP;
    private int minRangeS;
    private int maxRangeS;

    public UpdateRange(int minRange, int maxRange){

        this.minRangeP = minRange;
        this.maxRangeS = maxRange;
        this.maxRangeP = ((maxRange - minRange)/2) + minRange;
        this.minRangeS = maxRangeP + 1;
    }

    public int getMinRangeP() {
        return minRangeP;
    }

    public int getMaxRangeP() {
        return maxRangeP;
    }

    public int getMinRangeS() {
        return minRangeS;
    }

    public int getMaxRangeS() {
        return maxRangeS;
    }

}
