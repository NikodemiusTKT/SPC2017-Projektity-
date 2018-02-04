package harjoitustyo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public abstract class Package {
    private int packageId;
    private static int lastId = 0;
    private ArrayList<Item> items;
    private String createTime;
    private GeoLocation startPoint;
    private GeoLocation endPoint;
    private double distance;
    private PackageClass shipClass;
    private boolean sent = false;

    /**
     *
     */
    public Package(int classNumber) {
        this.packageId = ++Package.lastId;
        this.shipClass = ListManager.getInstance().searchClass(classNumber);
    }





     /* Constructor when packageId is not given so packageId is one number larger than last inserted packageId.  */


    /**
     * @param packageId
     * @param items
     * @param createTime
     * @param startPoint
     * @param endPoint
     * @param distance
     * @param sent
     */
    public Package(int packageId,ArrayList<Item> items, String createTime, GeoLocation startPoint, GeoLocation endPoint, double distance, boolean sent, int classNumber) {
        this.packageId = packageId;
        Package.lastId = packageId;
        this.items = items;
        this.createTime = createTime;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.distance = distance;
        this.sent = sent;
        PackageClass pClass = ListManager.getInstance().searchClass(classNumber);
        this.shipClass = pClass;
    }

    /**
     * @param items
     * @param createTime
     * @param startPoint
     * @param endPoint
     * @param distance
     * @param sent
     */
    public Package(ArrayList<Item> items, String createTime, GeoLocation startPoint, GeoLocation endPoint, double distance, boolean sent, int classNumber) {
        this.packageId = ++Package.lastId;
        this.items = items;
        this.createTime = createTime;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.distance = distance;
        this.sent = sent;
        PackageClass pClass = ListManager.getInstance().searchClass(classNumber);
        this.shipClass = pClass;
    }

    public Package(Package clone) {
        this.packageId = clone.packageId;
        this.items = new ArrayList<>(clone.items);
        this.createTime = clone.createTime;
        this.startPoint = new GeoLocation(clone.startPoint);
        this.endPoint = new GeoLocation(clone.endPoint);
        this.distance = clone.distance;
        this.shipClass = new PackageClass(clone.shipClass);
        this.sent = clone.sent;
    }

    public abstract Package clone();

    /**
     * @return the packageId
     */
    public int getPackageId() {
        return packageId;
    }

    /**
     * @param packageId the packageId to set
     */
    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    /**
     * @return the items
     */
    public ArrayList<Item> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }


    /**
     * @return the volume
     * Calculate the volume of the package by summing all the sizes of items inside items list.
     */
    public double getVolume() {
        double volume = 0;
        for (Item item: items) {
            volume = item.getHeight()+item.getWidth()+item.getLenght();
        }
        return volume;
    }


    /**
     * @return the weight
     * Get the weight of the package by summing all the item weights inside the package.
     * Created: 19.07.17 
     */
    public double getWeight() {
       return items.stream().mapToDouble(Item::getWeight).sum(); 
    }

    /**
     * @return the createTime
     */
    public String getCreateTime() {
        String date = null;
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat of = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        try {
            Date source = dt.parse(this.createTime);
            date = of.format(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    public abstract void breakItems();

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the startPoint
     */
    public GeoLocation getStartPoint() {
        return startPoint;
    }

    /**
     * @param startPoint the startPoint to set
     */
    public void setStartPoint(GeoLocation startPoint) {
        this.startPoint = startPoint;
    }

    /**
     * @return the endPoint
     */
    public GeoLocation getEndPoint() {
        return endPoint;
    }

    /**
     * @param endPoint the endPoint to set
     */
    public void setEndPoint(GeoLocation endPoint) {
        this.endPoint = endPoint;
    }

    /**
     * @return the shipClass
     */
    public PackageClass getShipClass() {
        return shipClass;
    }

    /**
     * @param shipClass the shipClass to set
     */
    public void setShipClass(PackageClass shipClass) {
        this.shipClass = shipClass;
    }

    /**
     * @return the sent
     */
    public boolean isSent() {
        return sent;
    }

    /**
     * @param sent the sent to set
     */
    public void setSent(boolean sent) {
        this.sent = sent;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return String.format("Paketti %d %d.luokka %s->%s", this.packageId,this.shipClass.getClassNumber(),this.startPoint.getCity(),this.endPoint.getCity());
    }

    public boolean hasBrokenItems() {
        return items.stream().anyMatch(e -> e.isBroken());
    }


}


class FirstClassPackage extends Package {
    FirstClassPackage () {
        super(1);
    }
    public FirstClassPackage (int packageId, ArrayList<Item> items,String createTime, GeoLocation startPoint, GeoLocation endPoint, double distance, boolean sent) {
        super(packageId,items,createTime,startPoint,endPoint,distance,sent,1);
    }
    public FirstClassPackage (ArrayList<Item> items, String createTime, GeoLocation startPoint, GeoLocation endPoint, double distance, boolean sent) {
        super(items,createTime,startPoint,endPoint,distance,sent,1);
    }
    // Method for breaking every item inside the package if the item is fragile.
    @Override
    public void breakItems() {
        for (Item item: this.getItems()) {
            if (item.isFragile() == true)
                item.setBroken(true);
            else item.setBroken(false);
        }
    }

    public FirstClassPackage(FirstClassPackage clone) {
        super(clone);
    }

    public FirstClassPackage clone() {
        return new FirstClassPackage(this);
    }

}

class SecondClassPackage extends Package {
    SecondClassPackage () {
        super(2);
    }
    public SecondClassPackage (int packageId,ArrayList<Item> items,String createTime, GeoLocation startPoint, GeoLocation endPoint, double distance, boolean sent) {
        super(packageId,items,createTime,startPoint,endPoint,distance,sent,2);
    }
    public SecondClassPackage (ArrayList<Item> items,String createTime, GeoLocation startPoint, GeoLocation endPoint, double distance, boolean sent) {
        super(items,createTime,startPoint,endPoint,distance,sent,2);
    }
    public SecondClassPackage(SecondClassPackage clone) {
        super(clone);
    }

    public SecondClassPackage clone() {
        return new SecondClassPackage(this);
    }

    @Override
    public void breakItems() {};
}

class ThirdClassPackage extends Package {
    ThirdClassPackage () {
        super(3);
    }
    public ThirdClassPackage (int packageId, ArrayList<Item> items, String createTime, GeoLocation startPoint, GeoLocation endPoint, double distance,boolean sent) {
        super(packageId,items,createTime,startPoint,endPoint,distance,sent,3);
    }
    public ThirdClassPackage (ArrayList<Item> items,String createTime, GeoLocation startPoint, GeoLocation endPoint, double distance, boolean sent) {
        super(items,createTime,startPoint,endPoint,distance,sent,3);
    }
    @Override
    // Method for breaking every fragile inside package if package's weight is less that 50 kilograms and volume less than 41040 cm3
    public void breakItems() {
        for (Item item: this.getItems()) {
            if (item.isFragile() == true && this.getWeight() < 50 && this.getVolume() < 41040)
                item.setBroken(true);
            else item.setBroken(false);
        }
    }
    public ThirdClassPackage(ThirdClassPackage clone) {
        super(clone);
    }

    public ThirdClassPackage clone() {
        return new ThirdClassPackage(this);
    }
}
