package harjoitustyo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Shipment {
    private static int lastId = 1;
    private int shipId;
    private boolean sent;
    private String shipTime;
    private ArrayList<Package> packages;
    private boolean hasBrokenItems = false;


    /**
     * @param shipId
     * @param sent
     * @param shipTime
     * @param packages
     */
    public Shipment(int shipId, boolean sent, String shipTime, ArrayList<Package> packages) {
        this.shipId = shipId;
        Shipment.lastId = shipId;
        this.sent = sent;
        this.shipTime = shipTime;
        this.packages = packages;
    }
    // Increment shipId if id is not given
    public Shipment(boolean sent, String shipTime, ArrayList<Package> packages) {
        this.shipId = ++Shipment.lastId;
        this.sent = sent;
        this.shipTime = shipTime;
        this.packages = packages;
    }
    public Shipment(Shipment copy) {
        this.shipId = copy.shipId;
        this.sent =  copy.sent;
        this.shipTime = copy.shipTime;
        this.packages =  new ArrayList<Package>(copy.packages);
        this.hasBrokenItems = copy.hasBrokenItems;
    }

    public Shipment clone() {
        return new Shipment(this);
    }

    @Override
    public String toString() {
        int size = this.packages.size();
        return String.format("Lähetys %d: %d %s", this.shipId,size,size == 1 ? "paketti" : "pakettia" );
    }

    /**
     * @return the shipId
     */
    public int getShipId() {
        return shipId;
    }

    /**
     * @param shipId the shipId to set
     */
    public void setShipId(int shipId) {
        this.shipId = shipId;
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
     * @return the shipTime
     */
    public String getShipTime() {
        String date = null;
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat of = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try {
            Date source = dt.parse(this.shipTime);
            date = of.format(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * @param shipTime the shipTime to set
     */
    public void setShipTime(String shipTime) {
        this.shipTime = shipTime;
    }

    /**
     * @return the packages
     * returns alls packages insides the Shipment
     */
    public ArrayList<Package> getPackages() {
        return packages;
    }

    /**
     * @param packages the packages to set
     */
    public void setPackages(ArrayList<Package> packages) {
        this.packages = packages;
    }

    // Method for checking if shipment contains any package which has broken Item
    public boolean hasBrokenItems() {
        for (Package pack: packages) {
            for (Item item: pack.getItems()) {
                if (item.isBroken()) {
                    hasBrokenItems = true;
                    return hasBrokenItems;
                }
            }
        }
        return hasBrokenItems;
    }
    public ArrayList<Item> getBrokenItems() {
        ArrayList<Item> brokenItems = new ArrayList<>();
       for (Package pack: packages) {
           for (Item item: pack.getItems()) {
               if (item.isBroken())
                   brokenItems.add(item);
           }
       }
       return brokenItems;
    }

    // Method for counting the all items inside the shipment
    public int getItemAmount() {
        return packages.stream().map(Package::getItems).mapToInt(List::size).sum();
    }


}
