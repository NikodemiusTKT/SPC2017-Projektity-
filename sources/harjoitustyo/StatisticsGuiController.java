package harjoitustyo;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class StatisticsGuiController implements Initializable {
    @FXML
    private ComboBox<Package> packageChoice;
    @FXML
    private ComboBox<Shipment> shipChoice;
    @FXML
    private ComboBox<Item> itemChoice;
    @FXML
    private TableView<Package> packageTable;
    @FXML
    private TableView<Item> itemTable;
    @FXML
    private TableColumn<Package,String> dateColumn;
    @FXML
    private TableColumn<Package,Integer> pIdColumn;
    @FXML
    private TableColumn<Package,Integer> classColumn;
    @FXML
    private TableColumn<Package,String> startColumn;
    @FXML
    private TableColumn<Package,String> endColumn;
    @FXML
    private TableColumn<Package,Double> distanceColumn;
    @FXML
    private TableColumn<Package,String> stateColumn;
    @FXML
    private TableColumn<Package,String> packBreakColumn;
    // Item tableview columns
    @FXML
    private TableColumn<Item,Integer> itemIdColumn;
    @FXML
    private TableColumn<Item,String> nameColumn;
    @FXML
    private TableColumn<Item,Double> lengthColumn;
    @FXML
    private TableColumn<Item,Double> widthColumn;
    @FXML
    private TableColumn<Item,Double> heightColumn;
    @FXML
    private TableColumn<Item,Double> weightColumn;
    @FXML
    private TableColumn<Item,String> fragileColumn;
    @FXML
    private TableColumn<Item,String> brokenColumn;
    
    // Shipment table columns
    @FXML
    private TableView<Shipment> shipTable;
    @FXML
    private TableColumn<Shipment,String> shipDateColumn;
    @FXML
    private TableColumn<Shipment,Integer> shipIdColumn;
    @FXML
    private TableColumn<Shipment,String> shipSentColumn;
    @FXML
    private TableColumn<Shipment,Integer> shipPackColumn;
    @FXML
    private TableColumn<Shipment,Integer> shipItemColumn;
    @FXML
    private TableColumn<Shipment,String> shipBreakColumn;
    @FXML
    private Button closeButton1;
    @FXML
    private Button closeButton2; 
    @FXML
    private Button closeButton3; 
    @FXML
    private Button showAllPackBt; 
    @FXML
    private Button showShipPackBt; 
    @FXML
    private Button showPackItemBt;
    @FXML
    private DatePicker datepicker;
    @FXML
    private TabPane tabpane;
    private Storage st = Storage.getInstance();
    private static StatisticsGuiController statController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
      Storage st = Storage.getInstance();

      // Set up package table column values
      dateColumn.setCellValueFactory(new PropertyValueFactory<Package,String>("createTime"));
      pIdColumn.setCellValueFactory(new PropertyValueFactory<Package,Integer>("packageId"));
      classColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getShipClass().getClassNumber()).asObject());
      startColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartPoint().getCity()));
      endColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndPoint().getCity()));
      distanceColumn.setCellValueFactory(new PropertyValueFactory<Package,Double>("distance"));
      stateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isSent() ? "lähetetty" : "ei lähetetty"));
      packBreakColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().hasBrokenItems() ? "kyllä" : "ei"));

      // Set up item table column values
      itemIdColumn.setCellValueFactory(new PropertyValueFactory<Item,Integer>("itemNumber"));
      nameColumn.setCellValueFactory(new PropertyValueFactory<Item,String>("itemName"));
      lengthColumn.setCellValueFactory(new PropertyValueFactory<Item,Double>("Lenght"));
      widthColumn.setCellValueFactory(new PropertyValueFactory<Item,Double>("Width"));
      heightColumn.setCellValueFactory(new PropertyValueFactory<Item,Double>("Height"));
      weightColumn.setCellValueFactory(new PropertyValueFactory<Item,Double>("Weight"));
      fragileColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isFragile() ? "kyllä" : "ei"));
      brokenColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isBroken() ? "kyllä" : "ei"));

      // Set up shipment table column values
      shipDateColumn.setCellValueFactory(new PropertyValueFactory<Shipment, String>("shipTime"));
      shipIdColumn.setCellValueFactory(new PropertyValueFactory<Shipment, Integer>("shipId"));
      shipSentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isSent() ? "lähetetty" : "ei lähetetty"));
      shipPackColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPackages().size()).asObject());
      shipItemColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getItemAmount()).asObject());
      shipBreakColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().hasBrokenItems() ? "kyllä" : "ei"));

      // Populate tableview with current shipments
      shipTable.getItems().setAll(st.getShipment());
      packageTable.setItems(FXCollections.observableArrayList(st.getPackages()));
      // Populate all the comboboxes
      packageChoice.getItems().setAll(st.getPackages());
      shipChoice.getItems().setAll(st.getShipment());

      packageChoice.setOnAction(this::updateItemsOnSelection);
      showAllPackBt.setOnAction(this::showAllPackages);
      shipChoice.setOnAction(this::updatePackagesOnSelection);
      datepicker.setOnAction(this::updateShipmentsOnDate);

      tabpane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
  }

  // Method for updating item table items depending on user selected package on combobox
  public void updateItemsOnSelection(ActionEvent event) {
      Package pack = packageChoice.getValue();
      if (pack != null) {
          packageTable.getItems().setAll(pack);
          itemTable.getItems().setAll(pack.getItems());
      }
  }
  
  // Methods for selecting either package or shipment on corresponding combobox

  public void initPackageChoice(Package p) {
      tabpane.getSelectionModel().select(2);
      packageChoice.getSelectionModel().select(p);
      LogHandler.getInstance().createStatLogEvent(p);
  }
  public void initShipChoice(Shipment s) {
      tabpane.getSelectionModel().select(1);
      shipChoice.getSelectionModel().select(s);
      LogHandler.getInstance().createStatLogEvent(s);
  }

  //Method for reloading all the table and combobox values
  public void reloadData() {
      packageChoice.setItems(FXCollections.observableArrayList(st.getPackages()));
      shipChoice.setItems(FXCollections.observableArrayList(st.getShipment()));
      shipTable.setItems((FXCollections.observableArrayList(st.getShipment())));
      if (shipChoice.getValue() != null)
          packageTable.setItems(FXCollections.observableArrayList(shipChoice.getValue().getPackages()));
      else
          packageTable.setItems(FXCollections.observableArrayList(st.getPackages()));
      if (packageChoice.getValue() != null)
          itemTable.setItems(FXCollections.observableArrayList(packageChoice.getValue().getItems()));
  }


  // Method for populating packageTable with all the packages from storage
  public void showAllPackages(ActionEvent event) {
      packageTable.getItems().setAll(Storage.getInstance().getPackages());
  }

  //Method for updating packataTable packages depending on shipment choice from combobox
  public void updatePackagesOnSelection(ActionEvent event) {
      if (shipChoice.getValue() != null) 
          packageTable.getItems().setAll(shipChoice.getValue().getPackages());
  }

  // Method for updating shipement table depending on date selected on datapicker component
  public void updateShipmentsOnDate(ActionEvent event) {
      if (datepicker.getValue() != null) {
          // Need to format datepicker to same format as the one contained inside package class before filtering the results
          String date = datepicker.getValue().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
          // Get all the shipments from storage which contain the same date string and store them inside Arraylist and populate table. Uses Java 8 Stream API
          shipTable.getItems().setAll(st.getShipment().stream().filter(e -> e.getShipTime().matches(date+".*")).collect(Collectors.toCollection(ArrayList::new)));
          LogHandler.getInstance().createStatLogEvent(datepicker.getValue());
      } else {
          // If datepicker is empty populate table with all the shipments inside storage
          shipTable.getItems().setAll(st.getShipment());
      }
  }

  //Method for jumping to package tab and select shipment which corresponds to user selected shipment from Shipment table
  public void showShipmentPackage(ActionEvent event) {
      Shipment ship = shipTable.getSelectionModel().getSelectedItem();
      if (ship != null) { 
          tabpane.getSelectionModel().select(1);
          initShipChoice(ship);
      }
  }
  //Method for jumping to items tab and select package which corresponds to user selected package from Package table
  public void showPackageItems(ActionEvent event) {
      Package pack = packageTable.getSelectionModel().getSelectedItem();
      if (pack != null) { 
          tabpane.getSelectionModel().select(2);
          initPackageChoice(pack);
      }
  }

  //Method for closing window
  public void closeWindow(ActionEvent event) {
      ((Stage)((Node)event.getTarget()).getScene().getWindow()).close();
    }

    /**
     * @return the packageController
     */
    public static StatisticsGuiController getStatController() {
        return statController;
    }

    /**
     * @param packageController the packageController to set
     */
    public static void setStatController(StatisticsGuiController statController) {
        StatisticsGuiController.statController = statController;
    }
}
