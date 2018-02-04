/**
 * tkt
 * Jul 28, 2017
 * LogWriter.java
 * 
 */
package harjoitustyo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;

/**
 * @author tkt
 *
 */
public class LogHandler {

    private static LogHandler instance;

    private LogHandler() {}

    private DataBaseManager dbm = DataBaseManager.getInstance();
    private ListManager lm = ListManager.getInstance();
    private Connection conn = dbm.connect();

    private final String packGeneral = "(%d) %d.luokka %s --> %s: %s";

    private final String itemTemp = "(%d) '%s' pituus: %.2f cm leveys: %.2f cm korkeus: %.2f cm paino: %.2f kg särkyvä: %s";
    private final String itemModTemp = "(%d) '%s' %s särkyvä: %s ---> muokkauksen jälkeen '%s' %s särkyvä: %s"; 

    private final String shipModTemp = "paketit ennen muokkausta %s ---> paketit muokkauksen jälkeen %s";




    /**
     *
     * @param e
     * Method for adding LogEvent to Database
     */
    public void addDbLogEvent(LogEvent e) {
        String sql = "INSERT INTO LogTable(LogType,Description) VALUES (?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,e.getLogType());
            stmt.setString(2,e.getDesciption());
            stmt.executeUpdate();
            conn.commit();
            dbm.saveDatabase();
        } catch (SQLException ex) {
            System.out.println("Something went wrong with inserting data into LogTable");
            System.out.println(ex.getLocalizedMessage());
            if (conn != null) {
                try {
                    System.err.println("LogEvent transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    System.err.println(excep.getMessage());
                }
            }
        }
    }

    // Method for getting logEvent from the database and adding logEvent instances inside listmanager logEvents list
    public void getDbLogEvent() {
        String logQuery = "SELECT * FROM LogTable;";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(logQuery);
            while(rs.next()) {
                int logId = rs.getInt(1);
                String logTime = rs.getString(2);
                String logType = rs.getString(3);
                String logDesc = rs.getString(4);
                LogEvent event = new LogEvent(logId,logTime,logType,logDesc);
                lm.addLogToList(event);
            }
        } catch(Exception e) {
            System.out.println("Something went wrong with reading LogEvents from the database");
            System.out.println(e.getLocalizedMessage());
        }
    }
    public void createLogEvent(LogType event) {
        LogEvent log = new LogEvent(event.getType(),event.getDesc());
        addDbLogEvent(log);
    }
    // Log event for adding and removing items
    public void createLogEvent(LogType event, Item item) {
        String desc = String.format(event.getDesc(),formatItemTemp(item));
        LogEvent log = new LogEvent(event.getType(),desc);
        addDbLogEvent(log);
    }
    public void createLogEvent(LogType event, Item org, Item mod) {
        String desc = String.format(event.getDesc(),formatModItemTemp(org,mod));
        addDbLogEvent(new LogEvent(event.getType(),desc));
    }
    // Log event for adding and removing packages
    public void createLogEvent(LogType event, Package pack) {
        addDbLogEvent(new LogEvent(event.getType(),String.format(event.getDesc(),formatPackTemp(pack))));
    }
    // Log event for modifying packages
    public void createLogEvent(LogType event, Package org, Package mod) {
        String desc = String.format(event.getDesc() + " ---> muokkauksen jälkeen %s",formatPackTemp(org),formatPackTemp(mod));
        addDbLogEvent(new LogEvent(event.getType(),desc));
    }

    public String formatPackTemp(Package p) {
        return String.format(packGeneral,p.getPackageId(),p.getShipClass().getClassNumber(),p.getStartPoint().getCity(),p.getEndPoint().getCity(),p.getItems().toString());
    }
    public String formatItemTemp(Item i) {
        return String.format(itemTemp,i.getItemNumber(),i.getItemName(),i.getLenght(),i.getWidth(),i.getHeight(),i.getWeight(),i.isFragile() ? "kyllä" : "ei");
    }
    public String formatModItemTemp(Item o, Item m) {
        String ofragile = o.isFragile() == true ? "kyllä" : "ei";
        String mfragile = m.isFragile() == true ? "kyllä" : "ei";
        return String.format(itemModTemp,o.getItemNumber(),o.getItemName(),o.getDimensions(),ofragile,m.getItemName(),m.getDimensions(),mfragile);
    }
    // Log event for shipments
    public void createLogEvent(LogType event, Shipment ship) {
        ArrayList<Integer> packages = ship.getPackages().stream().map(p -> p.getPackageId()).collect(Collectors.toCollection(ArrayList::new));

        String desc = String.format(event.getDesc(),ship.getShipId(),packages);
        addDbLogEvent(new LogEvent(event.getType(),desc));
    }
    public void createShipSentLogEvent(Shipment ship) {
        String info;
        ArrayList<Integer> packages = ship.getPackages().stream().map(p -> p.getPackageId()).collect(Collectors.toCollection(ArrayList::new));
        if (ship.hasBrokenItems())
            info = String.format("lähetyksessä hajosi esineet %s",ship.getBrokenItems().toString());
        else 
            info = "lähetyksessä ei hajonnut esineitä";
        String desc = String.format(LogType.SHIP_SEND.getDesc(),ship.getShipId(),packages.toString(),info);
        addDbLogEvent(new LogEvent(LogType.SHIP_SEND.getType(),desc));

    }
    public void createLogEvent(LogType event, Shipment org, Shipment mod) {
        ArrayList<Integer> oPackages = org.getPackages().stream().map(p -> p.getPackageId()).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Integer> mPackages = mod.getPackages().stream().map(p -> p.getPackageId()).collect(Collectors.toCollection(ArrayList::new));
        String desc = String.format(event.getDesc(),org.getShipId(),String.format(shipModTemp,oPackages,mPackages));
        addDbLogEvent(new LogEvent(event.getType(),desc));
    }
    public void createLogEvent(ArrayList<Package> packs, ArrayList<Shipment> ships) {
        ArrayList<Integer> shipInts = ships.stream().map(Shipment::getShipId).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Integer> packInts = packs.stream().map(Package::getPackageId).collect(Collectors.toCollection(ArrayList::new));
        String desc = String.format(LogType.REMOVE_SENT.getDesc(),shipInts.toString(),packInts.toString());
        LogEvent log = new LogEvent(LogType.REMOVE_SENT.getType(),desc);
        addDbLogEvent(log);
    }



    // Log event for statiscs
    public <T> void createStatLogEvent(T object) {
        String desc = "";
        if (object instanceof Shipment) {
            Shipment ship = (Shipment)object;
            desc = String.format(LogType.STAT_SHIP.getDesc(),ship.getShipId());
        } else if (object instanceof Package) {
            Package pack = (Package)object;
            desc = String.format(LogType.STAT_PACK.getDesc(),pack.getPackageId());
        } 
        else if (object instanceof LocalDate){
            String date = ((LocalDate)object).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            desc = String.format(LogType.STAT_DATE.getDesc(),date);
        }
        LogEvent log = new LogEvent(LogType.STAT_DATE.getType(),desc);
        addDbLogEvent(log);
    }

    public void reloadLogEvent() {
        lm.getLogEvents().clear();
        this.getDbLogEvent();
    }
    /**
     * @return the instance
     */
    public static LogHandler getInstance() {
        if (instance == null) 
            instance = new LogHandler();
        return instance;
    }
}
    
