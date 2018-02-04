package harjoitustyo;

public class PackageClass {
    private String className;
    private int classNumber;
    private double speedLimit;
    private double weightLimit;
    private double distanceLimit;
    private double volumeLimit;
    private boolean breakable;

    /**
     * @param speedLimit
     * @param weightLimit
     * @param distanceLimit
     * @param volumeLimit
     * @param breakable
     */
    public PackageClass(String className, int classNumber, double speedLimit, double weightLimit, double distanceLimit, double volumeLimit, boolean breakable) {
        this.className = className;
        this.classNumber = classNumber;
        this.speedLimit = speedLimit;
        this.weightLimit = weightLimit;
        this.distanceLimit = distanceLimit;
        this.volumeLimit = volumeLimit;
        this.breakable = breakable;
    }
    public PackageClass(int classNumber, double speedLimit, double weightLimit, double distanceLimit, double volumeLimit, boolean breakable) {
        this.classNumber = classNumber;
        this.className = String.format("%d.luokka",classNumber);
        this.speedLimit = speedLimit;
        this.weightLimit = weightLimit;
        this.distanceLimit = distanceLimit;
        this.volumeLimit = volumeLimit;
        this.breakable = breakable;
    }

    public PackageClass(PackageClass copy) {
         this.className = copy.className;
         this.classNumber = copy.classNumber;
         this.speedLimit = copy.speedLimit;
         this.weightLimit = copy.weightLimit;
         this.distanceLimit = copy.distanceLimit;
         this.volumeLimit = copy.volumeLimit;
         this.breakable = copy.breakable;
    }

    public PackageClass clone() {
        return new PackageClass(this);
    }

    /**
     *
     */
    public PackageClass() {
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the classNumber
     */
    public int getClassNumber() {
        return classNumber;
    }

    /**
     * @param classNumber the classNumber to set
     */
    public void setClassNumber(int classNumber) {
        this.classNumber = classNumber;
    }

    /**
     * @return the speedLimit
     */
    public double getSpeedLimit() {
        return speedLimit;
    }

    /**
     * @param speedLimit the speedLimit to set
     */
    public void setSpeedLimit(double speedLimit) {
        this.speedLimit = speedLimit;
    }

    /**
     * @return the weightLimit
     */
    public double getWeightLimit() {
        return weightLimit;
    }

    /**
     * @param weightLimit the weightLimit to set
     */
    public void setWeightLimit(double weightLimit) {
        this.weightLimit = weightLimit;
    }

    /**
     * @return the distanceLimit
     */
    public double getDistanceLimit() {
        return distanceLimit;
    }

    /**
     * @param distanceLimit the distanceLimit to set
     */
    public void setDistanceLimit(double distanceLimit) {
        this.distanceLimit = distanceLimit;
    }

    /**
     * @return the volumeLimit
     */
    public double getVolumeLimit() {
        return volumeLimit;
    }

    /**
     * @param volumeLimit the volumeLimit to set
     */
    public void setVolumeLimit(double volumeLimit) {
        this.volumeLimit = volumeLimit;
    }

    /**
     * @return the breakable
     */
    public boolean isBreakable() {
        return breakable;
    }

    /**
     * @param breakable the breakable to set
     */
    public void setBreakable(boolean breakable) {
        this.breakable = breakable;
    }

}

