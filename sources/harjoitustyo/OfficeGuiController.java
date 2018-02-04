package harjoitustyo;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * 
 */

/**
 * @author tkt
 *
 */
public class OfficeGuiController implements Initializable {
  @FXML
  private Button addToMapButton, createPackageBt, updatePackageBt, sendPackageBt,removePackageBt, modifyPackageBt, removePathBt, addShipmentBt, modShipmentBt, sendShipmentBt, removeShipmentBt;
  @FXML
  private Button logBt;
  @FXML
  private Button packageInfoBt, removeSentBt;
  @FXML
  private ComboBox<String> cityChoice; 
  @FXML
  private ComboBox<Package> packageChoice;
  @FXML
  private ComboBox<Shipment> shipmentChoice;
  @FXML
  private WebView webview;
  @FXML
  private TableView<Shipment> table;
  @FXML
  private TableColumn<Shipment, String> dateColumn;
  @FXML
  private TableColumn<Shipment, Integer> shipIdColumn;
  @FXML
  private TableColumn<Shipment, String> stateColumn;
  @FXML
  private TableColumn<Shipment, String> breakColumn;

  private DataBaseManager dbm;
  @FXML
  private TabPane root;

  private static OfficeGuiController officeController;

  private LogHandler lh = LogHandler.getInstance();;

  private Object currentChoice;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
      // Load the google maps into WebView
      webview.getEngine().load(getClass().getResource("index.html").toExternalForm());
      dbm = DataBaseManager.getInstance();
      Storage st = Storage.getInstance();
      // Load Database data
      dbm.createDB();
      ListManager lm = ListManager.getInstance();
      cityChoice.getItems().setAll(lm.getCities());
      // Populate shipment combobox
      shipmentChoice.getItems().setAll(st.getShipment());
      packageChoice.setValue(null);
      shipmentChoice.setValue(null);
      
      //TODO Add this event inside fxml
      addToMapButton.setOnAction(this::drawOfficeMarks);
      packageChoice.getItems().setAll(st.getPackages());
      removePathBt.setOnAction(this::removePaths);
      removePackageBt.setOnAction(this::removeSelectedPackage);

      // Setting up tableview column values
      dateColumn.setCellValueFactory(new PropertyValueFactory<Shipment, String>("shipTime"));
      shipIdColumn.setCellValueFactory(new PropertyValueFactory<Shipment, Integer>("shipId"));
      stateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isSent() ? "lähetetty" : "ei lähetetty"));
      breakColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().hasBrokenItems() ? "kyllä" : "ei"));

      // Populate tableview with current shipments
      table.getItems().setAll(st.getShipment());

      // Set action to hightlight shipment inside tableview on combobox selection and the other way around too
      // Also update currentChoice object to currently selected combobox item value 
      shipmentChoice.getSelectionModel().selectedItemProperty().addListener((ob,ol,ne) -> {
          table.getSelectionModel().select(ne);
          currentChoice = ne;
      });
      table.getSelectionModel().selectedItemProperty().addListener((ob,ol,ne) -> {
          shipmentChoice.getSelectionModel().select(ne);
          currentChoice = ne;
      });
      packageChoice.getSelectionModel().selectedItemProperty().addListener((ob,ol,ne) -> {
          currentChoice = ne;
      });
      
      // Prevent external links to be opened in webview
      // Courtesy of https://stackoverflow.com/questions/15555510/javafx-stop-opening-url-in-webview-open-in-browser-instead
      webview.getEngine().setCreatePopupHandler( new Callback<PopupFeatures, WebEngine>() {
          @Override
          public WebEngine call(PopupFeatures config) {
              // grab the last hyperlink that has :hover pseudoclass
              webview.getEngine()
                  .executeScript(
                          "var list = document.querySelectorAll( ':hover' );"
                          + "for (i=list.length-1; i>-1; i--) "
                          + "{ if ( list.item(i).getAttribute('href') ) "
                          + "{ list.item(i).getAttribute('href'); break; } }");

              return null;
          }
      });

      Stage stage = Mainclass.getPrimaryStage();
      stage.setOnCloseRequest(e -> {
          lh.createLogEvent(LogType.PROG_END);
          Platform.exit();
      });

        modifyPackageBt.setOnAction(e -> loadPackageScene(e,modifyPackageBt,packageChoice.getValue()));
        

        createPackageBt.setOnAction(e -> loadPackageScene(e,modifyPackageBt,packageChoice.getValue())); 
  }

  // Method for loading the packageGuiScene. Takes pressed button and current package choice to check if user wants to modify package
  public void loadPackageScene(ActionEvent event, Button modbutton, Package pack) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PackageGui.fxml"));
            Parent root;
            root = loader.load();
            // Get controller instance from PackageGuiController
            PackageGuiController controller = (PackageGuiController) loader.getController();
            // Pass the webview for Package Gui
            controller.setWebview(webview);
            PackageGuiController.setPackageController(controller);
            // Check that scene can be loaded (true if event isn't from modifypackage button or package can be modified)
            if (canLoadPackageScene(event,modbutton,controller,pack)) 
                loadScene(root);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
  }

  // Method for checking if it's okay to load package scene
  // returns true if user has selected package to modify and it passes the check or if the user didn't press modify button
  public boolean canLoadPackageScene(ActionEvent event, Button source, PackageGuiController controller, Package pack) {
      if (event.getSource().equals(source)) {
          if (checkSelectedPackage(pack)) {
              // initiliaze mod mode inside packageScene
              controller.initModify(pack);
              return true;
          } else 
              return false;
      }
      return true;
  }

  // Method for loading the shipment scene
  // Also checks if user has selected shipment to modify and loads modify mode in shipmentgui if necessary
  public void loadShipmentScene(ActionEvent event) throws IOException {
      Shipment s = shipmentChoice.getValue();
      try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("ShipmentGui.fxml"));
          Parent root = loader.load();
          // Get controller instance from ShipmentGuiController
          ShipmentGuiController controller = (ShipmentGuiController) loader.getController();
          // check if user pressed the modshipment button and if current shipment choice passes the test load the modify mode inside shipment gui
          if (event.getSource().equals(modShipmentBt)) {
              if (checkSelectedShipment(s,event))
                  controller.initModifyShipment(shipmentChoice.getValue());
              else return;
          }
          ShipmentGuiController.setShipmentController(controller);
          loadScene(root);
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

  // General method to load the scene
  public void loadScene(Parent root) {
      Scene scene = new Scene(root);
      scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
      Stage stage = new Stage();
      stage.setScene(scene);
      stage.show();
  }
  // Method for loading the statistics scene
  public void loadStatisticsScene(ActionEvent event) throws IOException {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("StatisticsGui.fxml"));
      Parent root = (Parent) loader.load();
      StatisticsGuiController controller = (StatisticsGuiController) loader.getController();
      StatisticsGuiController.setStatController(controller);
      loadScene(root);
      // Select tab inside statistics scene depending on last selected shipment or package
      if (currentChoice != null) {
          if (currentChoice instanceof Package)
              controller.initPackageChoice((Package)currentChoice);
          else if (currentChoice instanceof Shipment)
              controller.initShipChoice((Shipment)currentChoice);
      }
  }
  // Method for loading the logGui scene
  public void loadLogScene(ActionEvent event) throws IOException {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("LogGui.fxml"));
      Parent root = (Parent) loader.load();
      LogGuiController controller = (LogGuiController) loader.getController();
      LogGuiController.setLogController(controller);
      loadScene(root);
  }

  // Method for getting all the smartposts with currently user selected city and call the drawMarkPoints method to draw the marks to the map
  public void drawOfficeMarks(ActionEvent event) {
      String city = cityChoice.getValue();
      ListManager lm = ListManager.getInstance();
      ArrayList<SmartPost> offices = lm.getPostOfficeData(city);
      drawMarkPoints(offices);
  }

  // Method for drawing the marks of given offices to the map
  public void drawMarkPoints(ArrayList<SmartPost> offices) {
      for (SmartPost office: offices) {
          String Address = String.format("%s, %s %s", office.getLocation().getStreetAddress(),office.getLocation().getPostNumber(),office.getLocation().getCity());
          String information = String.format("%s, %s", office.getOfficeName(),office.getAvailibility());
          String script = String.format("document.goToLocation('%s', '%s', '%s')",Address,information,"red");
          webview.getEngine().executeScript(script);
      }
  }

  // Method for sending and drawing the user selected shipment
  public void drawShipmentPath(ActionEvent event) {
      ArrayList<Package> packages = new ArrayList<Package>();
      ListManager lm = ListManager.getInstance();
      DataBaseManager dbm = DataBaseManager.getInstance();
      Shipment s = shipmentChoice.getValue();
      if (checkSelectedShipment(s,event)) {
          packages.addAll(s.getPackages());
          // Set shipment sent status to true
          s.setSent(true);
          for (Package pack: shipmentChoice.getValue().getPackages()) {
              SmartPost startOffice = lm.searchOffice(pack.getStartPoint().getGeoId());
              SmartPost endOffice = lm.searchOffice(pack.getEndPoint().getGeoId());
              // Get the patharray containing the office coordinates
              ArrayList<Double> pathArray = ListManager.getInstance().getPathArray(startOffice,endOffice);
              String color = "red";
              // Change the path color and break the items depending the on the package instance
              if (pack instanceof FirstClassPackage == true) {
                  pack.breakItems();
              }
              else if (pack instanceof SecondClassPackage == true)
                  color = "blue";
              else if (pack instanceof ThirdClassPackage) {
                  color = "green";
                  pack.breakItems();
              }
              // Set sent status of every package inside the shipment to true and update the package's status in the database
              pack.setSent(true);
              SmartPost start = lm.getOfficeWithGeoLocation(pack.getStartPoint());
              SmartPost end = lm.getOfficeWithGeoLocation(pack.getEndPoint());
              ArrayList<SmartPost> offices = new ArrayList<>(Arrays.asList(start,end));
              drawMarkPoints(offices);
              String script = String.format("document.createPath(%s,'%s','%d')",pathArray,color,(int)pack.getShipClass().getSpeedLimit());
              webview.getEngine().executeScript(script);
              dbm.updateDbPackageStatus(pack);
          }
          LogHandler.getInstance().createShipSentLogEvent(s);
          // Update shipment status in the database and save the changes to database
          dbm.updateShipmentDbStatus(s);
          dbm.saveDatabase();
          // Reload database and comboboxes
          updateWindows();
      }
  }
  // Method for removing all the marks and paths from the map
  public void removePaths(ActionEvent event) {
      webview.getEngine().executeScript("document.deletePaths()");
  }
  // Method for removing the user selected package from the storage and database
  public void removeSelectedPackage(ActionEvent event) {
      Package removedPackage = packageChoice.getValue();
      DataBaseManager dbm = DataBaseManager.getInstance();
      if (removedPackage != null) {
          //Remove package from database and from storage and then update packageChoice combobox
          lh.createLogEvent(LogType.PACKAGE_REMOVE,removedPackage);
          dbm.removeDbPackage(removedPackage);
          updateWindows();
      }

  }

  // Method for removing the user selected shipment from the storage and database
  public void removeSelectedShipment(ActionEvent even) {
      Shipment shipment = shipmentChoice.getValue();
      DataBaseManager dbm = DataBaseManager.getInstance();
      if (shipment != null) {
          LogHandler.getInstance().createLogEvent(LogType.SHIP_REMOVE,shipment);
          dbm.removeDbShipment(shipment);
          dbm.saveDatabase();
          updateWindows();
      }
  }
  // Method for reloading all the shipments and packages from the database and repopulate the comboboxes
  public void reloadShipmentsAndPackages() {
      Storage st = Storage.getInstance();
      DataBaseManager.getInstance().reloadDatabase();
      packageChoice.getItems().setAll(st.getPackages());
      shipmentChoice.getItems().setAll(st.getShipment());
      table.getItems().setAll(st.getShipment());
      packageChoice.setValue(null);
      shipmentChoice.setValue(null);
  }

  // Method for throwing warning dialogs
  public void throwWarningDialog(DialogTypes title, DialogTypes desc) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle(title.getDesc());
      alert.setHeaderText(desc.getDesc());
      alert.showAndWait();
  }

  // Method for removing all the sent packages and shipments from the storage and database and repopulate the comboboxes
  public void removeSentPackagesAndShipments(ActionEvent event) {
      Storage st = Storage.getInstance();
      DataBaseManager dbm = DataBaseManager.getInstance();
      ArrayList<Package> rPackages = new ArrayList<>();
      ArrayList<Shipment> rShipments = new ArrayList<>();
      for (Package pack: st.getPackages())
          if (pack.isSent()) {
              dbm.removeDbPackage(pack);
              rPackages.add(pack.clone());
          }
      for (Shipment ship: st.getShipment())
          if (ship.isSent()) {
              dbm.removeDbShipment(ship);
              rShipments.add(ship.clone());
          }
      LogHandler.getInstance().createLogEvent(rPackages,rShipments);
      reloadShipmentsAndPackages();
      updateWindows();
      dbm.saveDatabase();
    }

  // Method for checking currently selected package and also throw error dialogs if necessary
  public boolean checkSelectedPackage(Package p) {
      boolean status = true;
      DialogTypes error = null;
      DialogTypes title = DialogTypes.MODPACKAGERROR;
      if (p == null) {
          status = false;
          error = DialogTypes.NOMODPACK_ERROR;
      } else if (p.isSent()) {
          status = false;
          error = DialogTypes.MODSENTPACK_ERROR;
      }
      if (!status) {
          throwWarningDialog(title,error);
      }
      return status;
  }
  // Method for checking the currently selected shipment and throw error dialogs if necessary
  private boolean checkSelectedShipment(Shipment s, ActionEvent event) {
      boolean status = true;
      DialogTypes error = null;
      DialogTypes title = null;
      Object ob  = event.getSource();
      if (ob.equals(modShipmentBt))
          title = DialogTypes.SHIPMOD_ERROR;
      else if (ob.equals(sendShipmentBt))
          title = DialogTypes.SHIPSENT_ERROR;
      if (s == null) {
          status = false;
          if (ob.equals(modShipmentBt))
              error = DialogTypes.NOSHIPMOD_ERROR;
          else
              error = DialogTypes.NOSHIPSENT_ERROR;
      } else if (s.isSent()) {
          status = false;
          if (ob.equals(modShipmentBt))
              error = DialogTypes.SENTSHIPMOD_ERROR;
          else
              error = DialogTypes.SENTSHIPSEND_ERROR;
      }
      if (!status)
          throwWarningDialog(title,error);
      return status;
  }
  // Method for updating all the window fields whenever any changes are made
  // Is called from other classes through statistic controller instance variable
  public void updateWindows() {
      OfficeGuiController officeController = OfficeGuiController.getOfficeController();
      StatisticsGuiController infoController = StatisticsGuiController.getStatController();
      PackageGuiController packageController = PackageGuiController.getPackageController();
      ShipmentGuiController shipmentController = ShipmentGuiController.getShipmentController();
      LogGuiController logController = LogGuiController.getLogController();
      if (officeController != null)
          officeController.reloadShipmentsAndPackages();
      if (infoController != null) 
          infoController.reloadData();
      if (packageController != null) 
          packageController.reloadPackagesAndItems();
      if (shipmentController != null) 
          shipmentController.reloadShipments();
      if (logController != null) 
          logController.reloadLogs();
  }



    /**
    * @return the officeController
    */
    public static OfficeGuiController getOfficeController() {
        return officeController;
    }

    /**
     * @param officeController the officeController to set
     */
    public void setOfficeController(OfficeGuiController officeController) {
        OfficeGuiController.officeController = officeController;
    }


}
