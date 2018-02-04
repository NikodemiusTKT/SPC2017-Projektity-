package harjoitustyo;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.sqlite.SQLiteConfig;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author tkt
 * Modified: 16.07.17  
 */
public class DataBaseManager {
    // Final String variables to hold the database and it's sql instructions files
    private final String url = "database.sqlite3";
    private final String sqlPath = "database.sql";
    // There can be only one of DataBaseManager instance so it is initialized as Singleton
    public static DataBaseManager instance;
    private Connection conn = null;
    private DataBaseManager() {};

    /**
     * 
     * @return the instance
     * Lazy initalization of DataBaseManager instance for singleton
     */
    public static DataBaseManager getInstance() {
        if (instance == null)
            instance = new DataBaseManager();
        return instance;
    }

    /**
     * @return the conn
     */
    public Connection getConn() {
        return conn;
    }

    // Method for initilizing the database
    // If there is no currently created database it creates a new by readign the sql instruction defined inside database.sql file
    public void createDB() {
        Statement stmt = null;
        File db = new File(url);
        // Clearing the lists containing the class instances
        ListManager.getInstance().getOffices().clear();
        ListManager.getInstance().getPackageClasses().clear();
        ListManager.getInstance().getLogEvents().clear();
        Storage.getInstance().getPackages().clear();
        Storage.getInstance().getShipment().clear();
        Storage.getInstance().getItems().clear();
        LogHandler lh = LogHandler.getInstance();
        try  {
            if (!db.exists()) {
                conn = connect();
                if (isDbConnected(conn)) {
                    FileManager fm = new FileManager();
                    File sql = new File(sqlPath);
                    String orders = fm.readFile(sql);
                    stmt = conn.createStatement();
                    stmt.executeUpdate(orders);
                    stmt.close();
                    // Get data from xml and insert them into database and then inside java classes
                    insertData();
                    // Add packageClass and item data into java classes
                    getPackageClasses();
                    getDbItems();
                    // Save new database;
                    saveDatabase();
                }
            } else {
                conn = connect();
                if (isDbConnected(conn)) {
                    stmt = conn.createStatement();
                    // Restore previous database from db file
                    stmt.executeUpdate("restore from '" + db.getAbsolutePath() + "'");
                    stmt.close();
                    /* Add SmartPost data inside database into java classes */
                    addOfficesToClass();
                    /* get packageClass data from inside the database  */
                    getPackageClasses();
                    /* get items from the database and create Item instances inside Storage class list  */
                    getDbItems();
                    /* get package from the database and create Item instances inside Storage class list  */
                    getDbPackages();

                    // Get shipments from inside the dababase and load them into class instances
                    getShipmentsFromDatabase();

                    lh.getDbLogEvent();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + " " + e.getLocalizedMessage());
        }
    }
    // Method for saving the current in memory database to file
    public void saveDatabase() {
        File db = new File(url);
        try {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("backup to '" + db.getAbsolutePath() + "'"); 
                stmt.close();
            }
        }  catch (SQLException e) {
            System.out.format("%s: %s", e.getClass().getName(), e.getMessage());
        }
    }

    // Method for getting  and creating the connection to the database
    public Connection connect() {
        try {
            // Return new connection only if current connection is null
            if (conn == null) {
                try {
                Class.forName(".org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                }
                // Turn on the foreign keys in the database
                SQLiteConfig config = new SQLiteConfig();
                config.enforceForeignKeys(true);
                conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                // Set autocommit to false
                conn.setAutoCommit(false);
            }
        } catch (SQLException e ) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    // Method for testing if connection is open to the database
    public boolean isDbConnected(Connection conn) {
        boolean status = false;
        try {
            if (!conn.isClosed())
                status =  true;
        } catch (SQLException e) {
            System.out.println("Connection to the database was unsuccessful.");
        }
        return status;
    }

    // 10.07.17
    /*
     * Method which inserts PostOffice data into database and SmartPost class.
     * Created smartPost instances are added to offices list inside of ListManager class.
     * Data is found from external xml file which is parsed inside XmlParser class.
     *
     */
    public void insertData() {
        XmlParser parser = new XmlParser();
        ListManager lm = ListManager.getInstance();
        // Get nodelist which contains all postoffice data from xml
        NodeList node = parser.parseData();
        if (node != null && node.getLength() > 0) {
            // Loop every node in nodelist and get wanted data from element
            for (int i=0; i < node.getLength(); i++) {
                Element el = (Element)node.item(i);
                String code = el.getElementsByTagName("code").item(0).getTextContent();
                String city = el.getElementsByTagName("city").item(0).getTextContent();
                // Uppercase the first letter and lowercase the rest in city
                city = city.substring(0,1).toUpperCase()+city.substring(1).toLowerCase();
                String address = el.getElementsByTagName("address").item(0).getTextContent();
                String availability = el.getElementsByTagName("availability").item(0).getTextContent();
                String postoffice = el.getElementsByTagName("postoffice").item(0).getTextContent();
                String latitude = el.getElementsByTagName("lat").item(0).getTextContent();
                String longitude = el.getElementsByTagName("lng").item(0).getTextContent();
                // try-with-resources style exception handling automatically closes the resources so no need to close statement manually
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("INSERT OR IGNORE INTO PostNumberTable(PostNumber,City) VALUES('"+code+"','"+city+"');");
                    // Get last added PostId Primary key id
                    int PostId = stmt.getGeneratedKeys().getInt(1);
                    stmt.executeUpdate("INSERT INTO Coordinates(Longitude,Latitude) VALUES('"+longitude+"','"+latitude+"');");
                    // Get last added CoorId Primary key id
                    int CoordId = stmt.getGeneratedKeys().getInt(1);
                    stmt.executeUpdate("INSERT INTO Location(StreetAddress,PostId,CoordId) VALUES('"+address+"','"+PostId+"','"+CoordId+"');");
                    // Get last added LocId Primary key id
                    int LocId = stmt.getGeneratedKeys().getInt(1);
                    stmt.executeUpdate("INSERT INTO smartPost(OfficeName,LocId,Availability) VALUES('"+postoffice+"','"+LocId+"','"+availability+"');");
                    int officeId = stmt.getGeneratedKeys().getInt(1);
                    // Change coordinates into Doubles
                    double lat = Double.parseDouble(latitude);
                    double longi = Double.parseDouble(longitude);
                    GeoLocation location =  new GeoLocation(officeId,lat,longi,address,code,city);
                    SmartPost office = new SmartPost(officeId,postoffice,availability,location);
                    // Create new SmartPost instance with given data and add them into offices list inside of ListManager class.
                    lm.makeOfficesList(office);
                    conn.commit();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    public void addOfficesToClass() {
        ListManager lm = ListManager.getInstance();
        String sql = 
            "SELECT smartPost.OfficeId, OfficeName, Availability, StreetAddress, PostNumber, City, Longitude, Latitude FROM smartPost "
            +"JOIN Location ON smartPost.LocId = Location.LocId "
            +"JOIN PostNumberTable ON Location.PostId = PostNumberTable.PostId "
            +"JOIN Coordinates ON Location.CoordId = Coordinates.CoordId;";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int officeId = rs.getInt("OfficeId");
                String officeName = rs.getString("OfficeName");
                String availability = rs.getString("Availability");
                String streetaddress = rs.getString("StreetAddress");
                String postnumber = rs.getString("PostNumber");
                String city = rs.getString("City");
                Double longi = rs.getDouble("Longitude");
                Double lat = rs.getDouble("Latitude");
                GeoLocation location =  new GeoLocation(officeId,lat,longi,streetaddress,postnumber,city);
                SmartPost office = new SmartPost(officeId,officeName,availability,location);
                lm.makeOfficesList(office);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /*
     * Get package classes data from database and create new PackageClass instance from each class
     * and add them into ListManager packageClasses list.
     * Created: 16.07.17 
     */
    public void getPackageClasses() {
        ListManager lm = ListManager.getInstance();
        String query = "SELECT * FROM Class;";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int classNumber = rs.getInt("ClassId");
                int speed = rs.getInt("Speed");
                double weight = rs.getDouble("WeightLimit");
                double distance = rs.getDouble("DistanceLimit");
                double volume = rs.getDouble("VolumeLimit");
                boolean breakable = rs.getInt("Breakable") == 1 ? true: false;
                PackageClass c = new PackageClass(classNumber,speed,weight,distance,volume,breakable);
                // Add packageClasses into PackageClasses list inside ListManager class
                lm.addPackageClass(c);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }



    /*
     * Method for getting item data from database and insert Item instances 
     * insideStorage class items list.
     * Created: 16.07.17 
     */
    public void getDbItems() {
        Storage st = Storage.getInstance();
        String query = "SELECT * FROM Item;";
        try (Statement stmt = conn.createStatement()){
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int itemNumber = rs.getInt("ItemId");
                String itemName = rs.getString("ItemName");
                double length = rs.getDouble("Length");
                double width = rs.getDouble("Width");
                double height = rs.getDouble("Height");
                double weight = rs.getDouble("Weight");
                boolean fragile = rs.getInt("Fragile") == 1 ? true : false;
                Item item = new Item(itemNumber,itemName,weight,width,height,length,fragile); 
                st.addItem(item);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Method for getting package data from database and insert Package instances inside Storage class packages list.
    // Requires that items list inside Storage class has already been populated with items contained in the database.
    // and offices list inside ListManager class has been populated with SmartPost offices contained in the database.
    // Created 16.07.17
    public void getDbPackages() {
        Storage st = Storage.getInstance();
        ListManager lm = ListManager.getInstance();
        String query = "SELECT * FROM Package "
            + "JOIN Road ON Package.RoadId = Road.RoadId;";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Package pack = null;
                int packageId = rs.getInt("PackageId");
                String createTime = rs.getString("createTime");
                int classId = rs.getInt("ClassId");
                double distance = rs.getDouble("Distance");
                int stPoint = rs.getInt("StartPoint");
                int endPoint = rs.getInt("EndPoint");
                boolean sent = rs.getBoolean("Sent");
                ArrayList<Item> items = getItemsInsidePackage(packageId);
                // Get GeoLocation from offices list inside ListManager class by OfficeIds
                GeoLocation startOffice = lm.getLocation(stPoint);
                GeoLocation endOffice = lm.getLocation(endPoint);
                switch (classId) {
                    case 1:
                        pack = new FirstClassPackage(packageId,items,createTime,startOffice,endOffice,distance,sent);
                        break;
                    case 2:
                        pack = new SecondClassPackage(packageId,items,createTime,startOffice,endOffice,distance,sent);
                        break;
                    case 3:
                        pack = new ThirdClassPackage(packageId,items,createTime, startOffice,endOffice,distance,sent);
                        break;
                    default:
                        break;
                }
                // Add package inside packages list inside Storage class
                st.addPackage(pack);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage() +" " +e.getStackTrace()[1].getMethodName());
        }
    }

    // Method for getting list of items inside packages from database
    // requires that items list inside Storage class has already been populated with items contained in database
    // Created: 16.07.17 
    public ArrayList<Item> getItemsInsidePackage(int packageId) {
        String query = "SELECT ItemId,ItemIsBroken FROM Items WHERE PackageId = '"+packageId+"';";
        Storage st = Storage.getInstance();
        ArrayList<Item> items = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int itemNumber = rs.getInt(1);
                boolean isBroken = rs.getInt(2) == 1 ? true : false;
                Item item = st.searchItem(itemNumber);
                Item clone = item.clone();
                clone.setBroken(isBroken);
                items.add(clone);

            }
        } catch (SQLException e) {
            System.err.println("Errors in getting items inside package.");
            System.err.println(e.getMessage() + " " + e.getStackTrace()[1].getMethodName());
        }
        return items;
    }

    // Method for inserting items into database
    // Takes item's name String, weight, lenght, width, height doubles and fragile boolean values as parameters
    // Created 17.07.17 
    void addItemToDatabase(Item item) {
        String insertion = "INSERT INTO ITEM(itemName,Weight,Length,Width,Height,Fragile) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement insertItem = conn.prepareStatement(insertion)){
            conn.setAutoCommit(false);
            insertItem.setString(1,item.getItemName());
            insertItem.setDouble(2,item.getWeight());
            insertItem.setDouble(3,item.getLenght());
            insertItem.setDouble(4,item.getWidth());
            insertItem.setDouble(5,item.getHeight());
            insertItem.setBoolean(6,item.isFragile());
            insertItem.executeUpdate();
            // Commit the changes to database
            conn.commit();
        } catch (SQLException e) {
            String method = e.getStackTrace()[1].getMethodName();
            String classname = e.getStackTrace()[1].getClassName();
            System.err.format("%s %s: %s\n", classname,method,e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }
    }



    //Method for inserting Package into database
    // Created 17.07.17
    void addPackageToDatabase(Package p) {
        ListManager lm = ListManager.getInstance();
        String packageInsertion = "INSERT INTO Package(ClassId,RoadId) VALUES(?, ?);";
        String itemsInsertion = "INSERT INTO Items(PackageId,ItemId,ItemIsBroken) VALUES(?, ?, ?);";
        String roadInsertion = "INSERT INTO Road(Distance,startPoint,endPoint) VALUES(?, ?, ?);"; 
        try (
                PreparedStatement insertPackage = conn.prepareStatement(packageInsertion); 
                PreparedStatement insertPackageItems = conn.prepareStatement(itemsInsertion); 
                PreparedStatement insertRoad = conn.prepareStatement(roadInsertion); 
            ) {

            // Insert packages data first into Road table
            insertRoad.setDouble(1,p.getDistance());
            // Get startPoint and endPoint ids from office's OfficeId by using ListManagers getOfficeId method which takes GeoId as parameter
            insertRoad.setInt(2,lm.getOfficeId(p.getStartPoint().getGeoId()));
            insertRoad.setInt(3,lm.getOfficeId(p.getEndPoint().getGeoId()));
            insertRoad.executeUpdate();
            // Get last inserted RoadId
            int roadId = insertRoad.getGeneratedKeys().getInt(1);

            // Insert package data into packaga table
            // Get package's shipment class from inside Package class and insert it as ClassId into Package table inside database
            insertPackage.setInt(1,p.getShipClass().getClassNumber());
            insertPackage.setInt(2,roadId);
            insertPackage.executeUpdate();
            // Get last inserted packageId
            int packageId = insertPackage.getGeneratedKeys().getInt(1);
            
            // Begin inserting item data into database
            // Insert every item inside package to database's Items table
            for (Item item: p.getItems()) {
                insertPackageItems.setInt(1,packageId);
                insertPackageItems.setInt(2,item.getItemNumber());
                insertPackageItems.setInt(3,item.isBroken() == true ? 1 : 0);
                insertPackageItems.executeUpdate();
            }
            // commit the changes to database
            conn.commit();
        } catch (SQLException e) {
            String method = e.getStackTrace()[1].getMethodName();
            String classname = e.getStackTrace()[1].getClassName();
            System.err.format("%s %s: %s\n", classname,method,e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }
    }
    
    //Method for getting all shipments inside database.
    // Makes shipment class instance from every shipment and add them inside storage shipments list
    // Requires that packages inside database have already been added into Storage classes packages list
    // Created: 17.07.17
    void getShipmentsFromDatabase() {
        Storage st = Storage.getInstance();
        String query = "SELECT Shipment.ShipId, shipTime, hasBeenSent FROM Shipment;";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int shipId = rs.getInt(1);
                String shipTime = rs.getString(2);
                boolean sentStatus = rs.getInt(3) == 1 ? true : false;
                ArrayList<Package> packages = this.getDbPackagesInsideShipment(shipId);
                Shipment shipment = new Shipment(shipId,sentStatus,shipTime,packages); 
                st.addShipment(shipment);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Method for returning package list with all the packages included in Shipment table inside database
    // Takes ShipmentId number as parameter
    // Requires that packages list inside Storage class has already been populated with packages inside Database
    // Created: 17.07.17
    public ArrayList<Package> getDbPackagesInsideShipment(int shipId) {
        String query = "SELECT * FROM Packages WHERE ShipId = '"+shipId+"';";
        Storage st = Storage.getInstance();
        ArrayList<Package> packages = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int packageId = rs.getInt("PackageId");
                Package pack = st.searchPackage(packageId);
                packages.add(pack);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return packages;
    }

    // Method for adding new shipment and its packages into database
    public void addShipmentToDatabase(Shipment s) {
        String shipmentInsertion = "INSERT INTO Shipment(hasBeenSent) VALUES (?);"; 
        String packagesInsertion = "INSERT INTO Packages(PackageId,shipId) VALUES (?,?);"; 
        try (
                PreparedStatement insertShipment = conn.prepareStatement(shipmentInsertion);
                PreparedStatement insertPackages = conn.prepareStatement(packagesInsertion))
        {
            insertShipment.setBoolean(1,s.isSent());
            insertShipment.executeUpdate();
            // Get last inserted ShipId
            int shipId = insertShipment.getGeneratedKeys().getInt(1);
            for (Package pack: s.getPackages()) {
                insertPackages.setInt(1,pack.getPackageId());
                insertPackages.setInt(2,shipId);
                insertPackages.executeUpdate();
            }
            // Commit the changes
            conn.commit();
        }  catch(SQLException e) {
            String method = e.getStackTrace()[1].getMethodName();
            String classname = e.getStackTrace()[1].getClassName();
            System.err.format("%s %s: %s\n", classname,method,e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }
    }


    //Method for updating items on database
    public void updateDbItem(Item item) {
        String itemUpdate = "UPDATE Item SET ItemName = ?, Weight = ?, Length = ?, Width = ?, Height = ?, Fragile = ? WHERE ItemId = ?;";
        try(PreparedStatement pstmt = conn.prepareStatement(itemUpdate)){
            pstmt.setString(1, item.getItemName());
            pstmt.setDouble(2, item.getWeight());
            pstmt.setDouble(3, item.getLenght());
            pstmt.setDouble(4, item.getWidth());
            pstmt.setDouble(5, item.getHeight());
            pstmt.setBoolean(6, item.isFragile());
            pstmt.setInt(7, item.getItemNumber());
            pstmt.executeUpdate();
            conn.commit();
        }catch(Exception e){
            System.err.print( e.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    // Method for updating package on database
    public void updateDbPackage(Package p) {
        ListManager lm = ListManager.getInstance();
        String packageUpdate = "UPDATE Package SET ClassId = ?, RoadId = ?, Sent = ? WHERE PackageId = ?;";
        String RoadIdQuery = "SELECT RoadId FROM Package WHERE PackageId = ?;";
        String itemsInsertion = "INSERT INTO Items(PackageId,ItemId,ItemIsBroken) VALUES(?, ?, ?);";
        String roadUpdate = "UPDATE Road SET Distance = ?, startPoint = ?, endPoint = ? WHERE RoadId = ?;"; 
        String removeOldItems = "DELETE FROM Items WHERE PackageId = ?;";
        try (
                PreparedStatement getPackageRoadId = conn.prepareStatement(RoadIdQuery);
                PreparedStatement updatePackage = conn.prepareStatement(packageUpdate); 
                PreparedStatement insertItems = conn.prepareStatement(itemsInsertion); 
                PreparedStatement updateRoad = conn.prepareStatement(roadUpdate); 
                PreparedStatement oldItemsRemoval = conn.prepareStatement(removeOldItems); 
            ) {
            conn.setAutoCommit(false);
            int packageId = p.getPackageId();
            // Get package's roadId from database;
            getPackageRoadId.setInt(1,packageId);
            ResultSet rs = getPackageRoadId.executeQuery();
            int roadId = rs.getInt(1);

            // Update Road table
            updateRoad.setDouble(1,p.getDistance());
            // Get startPoint and endPoint ids from office's OfficeId by using ListManagers getOfficeId method which takes GeoId as parameter
            updateRoad.setInt(2,lm.getOfficeId(p.getStartPoint().getGeoId()));
            updateRoad.setInt(3,lm.getOfficeId(p.getEndPoint().getGeoId()));
            updateRoad.setInt(4,roadId);
            updateRoad.executeUpdate();

            // Update package data in packaga table
            // Get package's shipment class from inside Package class and update it as new ClassId in Package table
            updatePackage.setInt(1,p.getShipClass().getClassNumber());
            updatePackage.setInt(2,roadId);
            updatePackage.setBoolean(3,p.isSent());
            updatePackage.setInt(4,p.getPackageId());
            updatePackage.executeUpdate();

            //Remove old items from Items table before inserting the new items
            oldItemsRemoval.setInt(1,packageId);
            oldItemsRemoval.executeUpdate();
            
            // Begin inserting new package items into database
            // Insert every item inside package to database's Items table
            for (Item item: p.getItems()) {
                insertItems.setInt(1,packageId);
                insertItems.setInt(2,item.getItemNumber());
                insertItems.setInt(3,item.isBroken() == true ? 1 : 0);
                insertItems.executeUpdate();
            }
            // commit the changes to database
            conn.commit();
        } catch (SQLException e) {
            String method = e.getStackTrace()[1].getMethodName();
            String classname = e.getStackTrace()[1].getClassName();
            System.err.format("%s %s: %s\n", classname,method,e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }
    }
    // Method for updating the package's sent and items broken status inside the database
    public void updateDbPackageStatus(Package p) {
        String packageSql = "UPDATE Package SET Sent = ? WHERE PackageId = ?;";
        String itemsSql = "UPDATE Items SET ItemIsBroken = ? WHERE (PackageId = ? AND ItemId = ?);";
        try (PreparedStatement packageUpdate = conn.prepareStatement(packageSql);
                PreparedStatement itemsUpdate = conn.prepareStatement(itemsSql)) {
            packageUpdate.setInt(1,p.isSent() == true ? 1 : 0);
            packageUpdate.setInt(2,p.getPackageId());
            packageUpdate.executeUpdate();
            for (Item item: p.getItems()) {
                 int itemId = item.getItemNumber();
                 int status = item.isBroken() == true ? 1 : 0;
                 itemsUpdate.setInt(1,status);
                 itemsUpdate.setInt(2,p.getPackageId());
                 itemsUpdate.setInt(3,itemId);
                 itemsUpdate.executeUpdate();
             }
            conn.commit();
            saveDatabase();
        } catch (SQLException e) {
            String method = e.getStackTrace()[1].getMethodName();
            String classname = e.getStackTrace()[1].getClassName();
            System.err.format("%s %s: %s\n", classname,method,e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }
    }

    
    

    // Method for removing item from the database
    public void removeDbItem(Item item) {
        String sql = "DELETE FROM Item WHERE ItemId = ?;";
        try (PreparedStatement removal = conn.prepareStatement(sql)) {
            removal.setInt(1,item.getItemNumber());
            removal.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            String method = e.getStackTrace()[1].getMethodName();
            String classname = e.getStackTrace()[1].getClassName();
            System.err.format("%s %s: %s\n", classname,method,e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }


    }
    //Method for removing package from the database
    public void removeDbPackage(Package p) {
        String sql = "DELETE FROM Package WHERE PackageId = ?;";
        try (PreparedStatement removal = conn.prepareStatement(sql)) {
            removal.setInt(1,p.getPackageId());
            removal.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            String method = e.getStackTrace()[1].getMethodName();
            String classname = e.getStackTrace()[1].getClassName();
            System.err.format("%s %s: %s\n", classname,method,e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }
    }
    //Method for removin shipment on database
    public void removeDbShipment(Shipment s) {
        String sql = "DELETE FROM Shipment WHERE ShipId = ?;";
        String packageRemove = "DELETE FROM Packages WHERE ShipId = ?;";
        try (PreparedStatement removal = conn.prepareStatement(sql);
                PreparedStatement packageRemoval = conn.prepareStatement(packageRemove)) {
            int shipId = s.getShipId();
            packageRemoval.setInt(1,shipId);
            packageRemoval.executeUpdate();
            removal.setInt(1,shipId);
            removal.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            String method = e.getStackTrace()[1].getMethodName();
            String classname = e.getStackTrace()[1].getClassName();
            System.err.format("%s %s: %s\n", classname,method,e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }
    }

    //Method for updating shipment's packages in database
    // Requires to be updated Shipment instance as parameter
    public void updateDbShipmentPackages(Shipment s) {
        String shipmentSql = "INSERT INTO Shipment(shipId,hasBeenSent) VALUES (?,?);";
        String packagesSql = "INSERT INTO Packages(PackageId,ShipId) VALUES (?,?);";
        // Before making changes the old shipment needs to be removed from the database
        removeDbShipment(s);
        try ( 
                PreparedStatement shipmentInsertion = conn.prepareStatement(shipmentSql);
                PreparedStatement packagesInsertion = conn.prepareStatement(packagesSql)
            ) {
            int shipId = s.getShipId();
            shipmentInsertion.setInt(1,shipId);
            shipmentInsertion.setBoolean(2,s.isSent());
            shipmentInsertion.executeUpdate();
            // Get last inserted ShipId
            shipId = shipmentInsertion.getGeneratedKeys().getInt(1);
            for (Package pack: s.getPackages()) {
                packagesInsertion.setInt(1,pack.getPackageId());
                packagesInsertion.setInt(2,shipId);
                packagesInsertion.executeUpdate();
            }
            // Commit the changes
            conn.commit();
        }  catch(SQLException e) {
            String method = e.getStackTrace()[1].getMethodName();
            String classname = e.getStackTrace()[1].getClassName();
            System.err.format("%s %s: %s\n", classname,method,e.getMessage());
            if (conn != null) {
                try {
                    System.err.println("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }
    }



    public void updateShipmentDbStatus(Shipment s) {
        String updateSql = "UPDATE Shipment SET hasBeenSent = ?, shipTime = datetime('now','localtime') WHERE ShipId = ?;";
        try ( 
                PreparedStatement shipmentUpdate = conn.prepareStatement(updateSql);
            ) {
            int shipId = s.getShipId();
            System.out.println(shipId);
            shipmentUpdate.setBoolean(1,s.isSent());
            shipmentUpdate.setInt(2,shipId);
            shipmentUpdate.executeUpdate();
            // commit the changes to database
            conn.commit();
            } catch (SQLException e) {
                String method = e.getStackTrace()[1].getMethodName();
                String classname = e.getStackTrace()[1].getClassName();
                System.err.format("%s %s: %s\n", classname,method,e.getMessage());
                if (conn != null) {
                    try {
                        System.err.println("Transaction is being rolled back");
                        conn.rollback();
                    } catch(SQLException excep) {
                        System.err.println(excep.getMessage());
                    }
                }
            }
    }

    // Method for clearing storage lists and reloading new items,packages and shipments from database 
    public void reloadDatabase() {
        Storage st = Storage.getInstance();
        // Clear all the storage lists
        st.getItems().clear();
        st.getPackages().clear();
        st.getShipment().clear();
        // Reload items,packages and shipments from database and add them to storage
        getDbItems();
        getDbPackages();
        getShipmentsFromDatabase();

    }
}




