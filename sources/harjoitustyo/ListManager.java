package harjoitustyo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ListManager {
    public static ListManager instance = null;
    private ArrayList<SmartPost> offices;
    private ArrayList<PackageClass> packageClasses;
    private ArrayList<LogEvent> logEvents;

    private ListManager() {
        offices = new ArrayList<>();
        packageClasses = new ArrayList<PackageClass>();
        logEvents = new ArrayList<>();
    }

    /**
     * @return the instance
     */
    public static ListManager getInstance() {
        if (instance == null) {
            instance = new ListManager();
        }
        return instance;
    }

    /**
     * @return the offices
     */
    public ArrayList<SmartPost> getOffices() {
        return offices;
    }

    /**
     * @param offices the offices to set
     */
    public void setOffices(ArrayList<SmartPost> offices) {
        this.offices = offices;
    }

    /**
     * @return the packageClasses
     */
    public ArrayList<PackageClass> getPackageClasses() {
        return packageClasses;
    }

    /**
     * @param packageClasses the packageClasses to set
     */
    public void setPackageClasses(ArrayList<PackageClass> packageClasses) {
        this.packageClasses = packageClasses;
    }

    /**
     * @return the logEvents
     * Returns the list of logevents in the reverse order
     */
    public ArrayList<LogEvent> getLogEvents() {
        ArrayList<LogEvent> result = new ArrayList<>(logEvents.size());
        for (int i = logEvents.size() -1; i >= 0; i--) {
            result.add(logEvents.get(i));
        }
        return result;
    }

    // Method for adding logEvent to logEvents list
    public void addLogToList(LogEvent e) {
        logEvents.add(e);
    }
    //Method for getting list of distinct cities from smartpost offices
    public List<String> getCities() {
        return offices.stream().map(e -> e.getLocation().getCity()).distinct().collect(Collectors.toList());
    }

    //Method for adding SmartPost element to offices list
    public void makeOfficesList(SmartPost office) {
        offices.add(office);
    }

    //Method for adding PackageClass element to packageClasses list
    public void addPackageClass(PackageClass pc) {
        packageClasses.add(pc);
    }
        

    // Method for returning list containing all SmartPost offices with given String city 
    public ArrayList<SmartPost> getPostOfficeData(String city) {
        return offices.stream().filter(e -> e.getLocation().getCity().equals(city)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    // Methon for searching and return PackageClass with given classId integer
    public PackageClass searchClass (int classId) {
        return packageClasses.stream().filter(e -> e.getClassNumber() == classId).findFirst().get();
    }
    // Method for searching and getting SmartPost with given officeId
    public SmartPost searchOffice(int officeId) {
        return offices.stream().filter(e -> e.getOfficeId() == officeId).findFirst().get();
    }
    // Method for getting GeoLocation with given officeId
    // Created: 18.07.17 
    public GeoLocation getLocation(int officeId) {
        return offices.stream().filter(o -> o.getOfficeId() == officeId).map(e -> e.getLocation()).findFirst().get();
    }
    // Method for getting SmartPost's officeId with given GeoLocation's geoId
    // Created: 18.07.17 
    public int getOfficeId(int geoId) {
        return offices.stream().filter(p -> p.getLocation().getGeoId() == geoId).map(e -> e.getOfficeId()).findFirst().get();
    }

    //Method for getting particular Smartpost instance with given Geolocation
    public SmartPost getOfficeWithGeoLocation(GeoLocation loc) {
        return offices.stream().filter(p -> p.getLocation() == loc).findFirst().get();
    }

    // Method for creating patharray containing the office coordinates with the given start and end point SmartPost offices
    public ArrayList<Double> getPathArray(SmartPost start, SmartPost end) {
        ArrayList<Double> pathArray = new ArrayList<>();
        pathArray.add(start.getLocation().getLatitude());
        pathArray.add(start.getLocation().getLongitude());
        pathArray.add(end.getLocation().getLatitude());
        pathArray.add(end.getLocation().getLongitude());
        return pathArray;
    }
}


