package harjoitustyo;

import java.util.ArrayList;
import java.util.stream.Collectors;

public final class Storage {
    public static Storage instance;
    private ArrayList<Item> items;
    private ArrayList<Package> packages;
    private ArrayList<Shipment> shipments;

    /**
     *
     */
    private Storage() {
        items = new ArrayList<Item>();
        packages = new ArrayList<Package>();
        shipments= new ArrayList<Shipment>();
    }

    /**
     * @return the instance
     */
    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    /**
     * @return the items
     */
    public ArrayList<Item> getItems() {
        return items;
    }

    /**
     * @return the packages
     */
    public ArrayList<Package> getPackages() {
        return packages;
    }
    
    /**
     * @return the shipment
     */
    public ArrayList<Shipment> getShipment() {
        return shipments;
    }

    // Method for adding new item inside items list
    public void addItem(Item item) {
        items.add(item);
    }
    // Method for removing item from the items list
    public void removeItem(Item item) {
        for (Item it: items) {
            if (it.equals(item)) {
                items.remove(item);
                break;
            }
        }
    }
    // Method for removing given package from the packages list
    public void removePackage(Package pack) {
        packages.remove(packages.stream().filter(e -> e.equals(pack)).findFirst().get());
    }
    //
    // Method for adding new package inside packages list
    public void addPackage(Package p) {
        packages.add(p);
    }
    // Method for returning Item from items list with given itemId
    public Item searchItem(int itemId) {
       return items.stream().filter(e -> e.getItemNumber() == itemId).findFirst().get(); 
    }
    // Method for adding package into shipment list
    public void addShipment(Shipment s) {
        shipments.add(s);
    }
    // Method for returning Shipment with given shipId
    public Shipment searchShipments(int shipId) {
        return shipments.stream().filter(e -> e.getShipId() ==  shipId).findFirst().get();
    }
    // Method for returning package from packages list with given packageId
    public Package searchPackage(int packageId) {
        return packages.stream().filter(p -> p.getPackageId() == packageId).findFirst().get();
    }
    // Method for finding out sentStatus of given package inside shipments
    // Returns true if package is inside shipments and shipment has boolean sentStatus set to true else returns false
    public boolean getSentStatus(Package pack) {
        return shipments.stream().anyMatch(e -> e.getPackages().contains(pack) && e.isSent() == true);
    }

    // Method for returning arraylist of packages which are not yet sent
    public ArrayList<Package> getNotSentPackages() {
        return packages.stream().filter(e -> !e.isSent()).collect(Collectors.toCollection(ArrayList::new)); 
    }

    // Method for returning arraylist of not Sent shipments
    public ArrayList<Shipment> getNotSentShipments() {
        return shipments.stream().filter(e -> !e.isSent()).collect(Collectors.toCollection(ArrayList::new)); 
    }


    // Method for getting all the packages which are not inside any of the shipments
    public ArrayList<Package> getNotShipPackage() {
        ArrayList<Package> shipPackages = new ArrayList<>();
        this.shipments.stream().map(Shipment::getPackages).forEach(e -> shipPackages.addAll(e));
        ArrayList<Package> uncommon = new ArrayList<>(this.packages);
        uncommon.removeAll(shipPackages);
        return uncommon;
    }

        
        
    
}
