package harjoitustyo;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class ShipmentGuiController implements Initializable {
    @FXML
    private ComboBox<Shipment> shipmentChoice; 
    @FXML
    private ComboBox<Package> packageChoice;
    @FXML
    private Button addPackageBt, modPackageBt, newPackageBt, removePackBt, newShipmentBt, modShipmentBt, CancelBt, zeroChoiceBt;
    @FXML
    private TableView<Package> shipTable;
    @FXML
    private TableColumn<Package,String> DtColum;
    @FXML
    private TableColumn<Package,Integer> IdColumn;
    @FXML
    private TableColumn<Package,Integer> classColumn;
    @FXML
    private TableColumn<Package,String> startPointColumn;
    @FXML
    private TableColumn<Package,String> endPointColumn;
    @FXML
    private TableColumn<Package,Double> distanceColumn;
    @FXML
    private VBox rootPane;
    @FXML
    private HBox shipHbox;
    
    private Shipment modShipment;

    private static ShipmentGuiController shipmentController;
    private static OfficeGuiController officeController = OfficeGuiController.getOfficeController();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Storage st = Storage.getInstance();
        // Setting up combobox values
        packageChoice.getItems().setAll(st.getNotShipPackage());
        shipmentChoice.getItems().setAll(st.getNotSentShipments());
        // Setting up tableviews columns values
        DtColum.setCellValueFactory(new PropertyValueFactory<Package,String>("createTime"));
        IdColumn.setCellValueFactory(new PropertyValueFactory<Package,Integer>("packageId"));
        distanceColumn.setCellValueFactory(new PropertyValueFactory<Package,Double>("distance"));

        endPointColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndPoint().getCity()));
        startPointColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartPoint().getCity()));
        classColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getShipClass().getClassNumber()).asObject());

        addPackageBt.setOnAction(this::addPackageAction);
        shipTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        removePackBt.setOnAction(this::removePackage);
        newShipmentBt.setOnAction(this::createNewShipment);


        shipmentChoice.setOnAction(this::updateShipmentOnSelection);

        CancelBt.setOnAction(this::closeWindow);

        modShipmentBt.setOnAction(this::modifyShipment);

        zeroChoiceBt.setOnAction(this::zeroChoice);




    }
    // Method for adding the selected package to the tableview
    public void addPackageAction(ActionEvent event) {
        Package pack = packageChoice.getValue();
        if (pack != null) {
            shipTable.getItems().add(pack);
            packageChoice.getItems().remove(pack);
        }
    }
    // Method for removing the selected package from the tableview
    public void removePackage(ActionEvent even) {
      shipTable.getSelectionModel().getSelectedItems().forEach(i -> {packageChoice.getItems().add(i); shipTable.getItems().remove(i);});
    }

    // Method for creating a new shipment and updating the database and all the data
    public void createNewShipment(ActionEvent event) {
        if (checkShipment(event)) {
            java.util.Date now = new java.util.Date();
            SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
            String createTime = df.format((new java.sql.Timestamp(now.getTime())));
            ArrayList<Package> packages = shipTable.getItems().stream().collect(Collectors.toCollection(ArrayList::new));
            Shipment newShipment = new Shipment(false,createTime,packages);
            Storage.getInstance().addShipment(newShipment);
            DataBaseManager.getInstance().addShipmentToDatabase(newShipment);
            DataBaseManager.getInstance().saveDatabase();
            officeController.updateWindows();
            LogHandler.getInstance().createLogEvent(LogType.SHIP_ADD,newShipment);
        }

    }
    // Method for updating the tableview with currently selected shipment
    public void updateShipmentOnSelection(ActionEvent event) {
        Shipment ship = shipmentChoice.getValue();
        if (ship != null) {
            shipTable.getItems().setAll(ship.getPackages());
            packageChoice.getItems().setAll(Storage.getInstance().getNotShipPackage());
            packageChoice.getItems().removeAll(ship.getPackages());
            modShipment = ship;
            newShipmentBt.setDisable(true);

        }
    }
    // Method for closing the window
    public void closeWindow(ActionEvent event) {
        ((Stage)((Node)event.getTarget()).getScene().getWindow()).close();
    }
    // Method for initialize the mod mode in the gui
    public void initModifyShipment(Shipment ship) {
        modShipment = ship;
        shipmentChoice.getSelectionModel().select(modShipment);
        shipTable.getItems().setAll(modShipment.getPackages());
        newShipmentBt.setDisable(true);
        packageChoice.getItems().removeAll(ship.getPackages());

    }
    // Method for loading the package scene calls the method from the officegui
    public void loadPackageScene(ActionEvent event) throws IOException {
        officeController.loadPackageScene(event,modPackageBt,packageChoice.getValue());
    }

    // Method for modify the currently selected shipment and update the changes in the database
    public void modifyShipment(ActionEvent event) {
        DataBaseManager dbm = DataBaseManager.getInstance();
        if(checkShipment(event)) {
            Shipment org = modShipment.clone();
            ArrayList<Package> packages = shipTable.getItems().stream().collect(Collectors.toCollection(ArrayList::new));
            modShipment.setPackages(packages);
            modShipment.setSent(false);
            dbm.updateDbShipmentPackages(modShipment);
            officeController.updateWindows();
            LogHandler.getInstance().createLogEvent(LogType.SHIP_MOD,org,modShipment);
        }
    }
    // Method for reseting the modify shipment choice
    public void zeroChoice(ActionEvent event) {
        shipmentChoice.setValue(null);
        shipTable.getItems().clear();
        modShipment = null;
        newShipmentBt.setDisable(false);
        shipmentChoice.setDisable(false);
        newShipmentBt.setDisable(false);
    }
    // Method for reloading the shipments from the database and repopulate the fields
    public void reloadShipments() {
        //Reload storage and database
        DataBaseManager.getInstance().reloadDatabase();
        Storage st = Storage.getInstance();
        //Repopulate shipmentChoice combobox
        shipmentChoice.getItems().setAll(st.getNotSentShipments());
        packageChoice.getItems().setAll(st.getNotShipPackage());
        shipmentChoice.setValue(null);
    }

    // Method for checking the currently selected shipment
    public boolean checkShipment(ActionEvent event) {
        if (event.getSource().equals(modShipmentBt)) {
            if (modShipment == null) {
                if (shipmentChoice.getValue() == null)
                    return false;
                else { 
                    modShipment = shipmentChoice.getValue();
                }
            }
            if (modShipment.isSent()) {
                return false;
            }
        } 
        else if (event.getSource().equals(newShipmentBt) && modShipment != null)
            return false;
        if (shipTable.getItems().isEmpty())
            return false;
        return true;
    }


    /**
     * @return the shipmentController
     */
    public static ShipmentGuiController getShipmentController() {
        return shipmentController;
    }

    /**
     * @param shipmentController the shipmentController to set
     */
    public static void setShipmentController(ShipmentGuiController shipmentController) {
        ShipmentGuiController.shipmentController = shipmentController;
    }

}
