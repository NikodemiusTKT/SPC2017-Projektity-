package harjoitustyo;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * 
 */

/**
 * @author tkt
 *
 */
public class PackageGuiController implements Initializable {
    @FXML
    private Button createNewItemBt, createPackageBt, modifyPackageBt, removePackageBt, CancelBt, addItemBt, removeItemBt, changePlacesBt, modifyItemBt, deleteItemBt;
    @FXML
    private ComboBox<Item> itemChoice;
    @FXML
    private ComboBox<String> startCityChoice, endCityChoice;
    @FXML
    private ComboBox<SmartPost> startPostChoice, endPostChoice;
    @FXML
    private ComboBox<Package> packageChoice;
    @FXML
    private TextField itemNameInput; 
    @FXML
    private TextField lengthInput; 
    @FXML
    private TextField widthInput; 
    @FXML
    private TextField heightInput;
    @FXML
    private TextField weightInput; 
    @FXML
    private RadioButton radioFirstClass, radioSecondClass, radioThirdClass; 
    @FXML
    private CheckBox fragileChoice;
    @FXML
    private WebView webview;
    @FXML
    private TableView<Item> table;
    @FXML
    private TableColumn<Item,String> nameColumn;
    @FXML
    private TableColumn<Item,String> sizeColumn;
    @FXML
    private TableColumn<Item,Double> weightColumn;
    @FXML
    private TableColumn<Item,Boolean> fragileColumn;
    @FXML
    private ToggleGroup classChoices;
    @FXML
    private Label classLabel, speedLabel, distLabel, sizeLabel, weightLabel, fragLabel, cuDistLabel;
    @FXML
    private Stage cuStage;
    private DataBaseManager dbm;
    private Storage st;
    private OfficeGuiController officeController = OfficeGuiController.getOfficeController();

    // Variable for storing packageClass
    private PackageClass classData;
    // Variables for class limits and current distance
    private double distanceLimit,speed, weightLimit, volumeLimit, cuDistance; 
    private boolean fragile;
    //
    // Variable for storing currently modified package
    private Package modifyPackage;
    //
    // Variables for storing controller instances
    private static PackageGuiController packageController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
      ListManager lm = ListManager.getInstance();
      dbm = DataBaseManager.getInstance();
      dbm.connect();
      Storage st = Storage.getInstance();

      itemNameInput.setText("");
      
      // Set radiobuttons data to Packageclasses
      radioFirstClass.setUserData(lm.searchClass(1));
      radioSecondClass.setUserData(lm.searchClass(2));
      radioThirdClass.setUserData(lm.searchClass(3));
      // Set up classLabels to first package class
      updateClassLabels(lm.searchClass(1));
      
      // Update class data labels and get PackageClass on radiobutton selection
     classChoices.selectedToggleProperty().addListener((ob,ol,ne) -> { 
         classData = (PackageClass)classChoices.getSelectedToggle().getUserData();
         updateClassLabels(classData);
         updateDistColor();
         updateClassLabelColors();
     }
      );
     // Set up default packageClass to first class to prevent null pointer errors
     classData = lm.searchClass(1);
      // Set ComboBox values to null for certain checks
      startCityChoice.setValue(null);
      endCityChoice.setValue(null);
      startPostChoice.setValue(null);
      endPostChoice.setValue(null);

      // Add KeyReleased events to inputFields to check user input
      lengthInput.textProperty().addListener((ob,o,n) -> isDouble(lengthInput));
      weightInput.setOnKeyReleased(we -> isDouble(weightInput));
      widthInput.setOnKeyReleased(wi -> isDouble(widthInput));
      heightInput.setOnKeyReleased(wi -> isDouble(heightInput));
      //
      // Populate Start and endCity comboboxes
      startCityChoice.getItems().setAll(lm.getCities());
      endCityChoice.getItems().setAll(lm.getCities());
      //
      // Populate items combobox
      itemChoice.getItems().setAll(st.getItems());
      // Populate package choice combobox with packages which are not yet sent to prevent errors
      packageChoice.getItems().setAll(st.getNotSentPackages());
      //
      // Add action event on startCity and endCity comboboxes so that Post offices comboboxes update on selection
      startCityChoice.setOnAction(this::updateOfficesChoices);
      endCityChoice.setOnAction(this::updateOfficesChoices);
      //
      // Remove selected postOffice from another postoffice combobox
      startPostChoice.setOnAction(this::updateDistanceOnSelection);
      endPostChoice.setOnAction(this::updateDistanceOnSelection);

      // Switch city selection on startcity and endcity comboboxes when changePlacesBt is pressed
      changePlacesBt.setOnAction(this::switchPlaces);

      // Populate fields when item is selected on itemChoice combobox
      itemChoice.setOnAction(this::populateItemOnSelection);


      // Setting up tableviews columns values
      nameColumn.setCellValueFactory(new PropertyValueFactory<Item,String>("itemName"));
      weightColumn.setCellValueFactory(new PropertyValueFactory<Item,Double>("Weight"));
      sizeColumn.setCellValueFactory(new PropertyValueFactory<Item,String>("dimensions"));
      fragileColumn.setCellValueFactory(new PropertyValueFactory<Item,Boolean>("fragile"));
      // Action for button to add new Item into tableview
      addItemBt.setOnAction(this::addItemToTable);
      // Action for creating package
      createPackageBt.setOnAction(this::createPackage);

      // Action for removing item from the tableview
      removeItemBt.setOnAction(this::removeItemfromTable);

      // Turn on multiple selections on tableview
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

      // Action for creating new Item and updating itemChoice combobox
      createNewItemBt.setOnAction(this::createNewItem);

      // / Action for monifying selected items attributes inside class and database
      modifyItemBt.setOnAction(this::modifyItem);

      // Action for deleting item from Storage and database
      deleteItemBt.setOnAction(this::deleteItem);

      // Action to update data on package selection
      packageChoice.setOnAction(this::updateDataOnPackageSelection);
      // Action for modifying package
      modifyPackageBt.setOnAction(this::modifySelectedPackage);
      // Action for removing package
      removePackageBt.setOnAction(this::removeSelectedPackage);

  }


  // Method for switching startpoint and endpoint choices in the comboboxes
  private void switchPlaces(ActionEvent event) {
      // Check that startCity and endCity choices are not empty before making the switch
      if (startCityChoice.getValue() != null && endCityChoice.getValue() != null)
      {
          // Get currently selected choices from the destination comboboxes
          String startCityIndex = startCityChoice.getValue();
          SmartPost startPostIndex = startPostChoice.getValue();
          String endCityIndex = endCityChoice.getValue();
          SmartPost endPostIndex = endPostChoice.getValue();

          startCityChoice.getSelectionModel().select(endCityIndex);
          startPostChoice.getSelectionModel().select(endPostIndex);
          endCityChoice.getSelectionModel().select(startCityIndex);
          endPostChoice.getSelectionModel().select(startPostIndex);
      }
  }
  public void closeWindow(ActionEvent event) {
      ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
  }


  // Method for initializing modify package state in window 
  public void initModify(Package modpackage) {
      modifyPackage = modpackage;
      createPackageBt.setDisable(true);
      populatePackageFields(modifyPackage);
      packageChoice.getSelectionModel().select(modifyPackage);
  }

  // Method for returning distance between two points from Javascript getDistance function
  // Takes array containing the start and endpoints as parameter
  public double getDistance(ArrayList<Double> pathArray) {
      return Double.parseDouble(webview.getEngine().executeScript("document.getDistance("+pathArray+")").toString());
  }

  // Method for checking if input field can be parsed as double, takes to be checked inputfield as parameter
  // Also changes inputfields border color to red if current input isn't double
  private boolean isDouble(TextField in) {
      boolean value = false;
      if (!in.getText().isEmpty() && in.getText() != null) {
          try {
              Double.parseDouble(in.getText());
              in.setStyle(null);
              value = true;
          } catch (NumberFormatException ne) {
              in.setStyle("-fx-border-color: red;");
          }
      }
      else
          in.setStyle(null);
      return value;
  }

  // Method for adding selected item on the itemChoice combobox into tableview
  private void addItemToTable(ActionEvent event) {
      Item item = itemChoice.getValue();
      if (item != null && checkItemClassLimits(item))
      {
          table.getItems().add(item);
          updateClassLabelColors();
          
      }

  }
  
  // Method for creating new package instance and adding it inside storage list and database
  private void createPackage(ActionEvent event) {
      ListManager lm = ListManager.getInstance();
      st = Storage.getInstance();
      if (checkPackageInputs()) {
          GeoLocation startPoint = startPostChoice.getValue().getLocation();
          GeoLocation endPoint = endPostChoice.getValue().getLocation();
          ArrayList<Item> packageItems = table.getItems().stream().map(e -> e.clone()).collect(Collectors.toCollection(ArrayList::new));
          // Create sql timestamp for creating package
          java.util.Date now = new java.util.Date();
          SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
          String createTime = df.format((new java.sql.Timestamp(now.getTime())));
          //Get pathArray containing the start and endpoints coordinates
          ArrayList<Double> pathArray = lm.getPathArray(startPostChoice.getValue(),endPostChoice.getValue()); 
          //Get distance from javascript
          double distance = getDistance(pathArray);
          Package pack;
          // Create new package instance depending on currently selected class number
          switch(classData.getClassNumber()) {
              case 1:
                  pack = new FirstClassPackage(packageItems,createTime,startPoint,endPoint,distance,false);
                  break;
              case 2:
                  pack = new SecondClassPackage(packageItems,createTime,startPoint,endPoint,distance,false);
                  break;
              case 3:
                  pack = new ThirdClassPackage(packageItems,createTime,startPoint,endPoint,distance,false);
                  break;
              default:
                  pack = new FirstClassPackage(packageItems,createTime,startPoint,endPoint,distance,false);
                  break;
          }
          st.addPackage(pack);
          dbm.addPackageToDatabase(pack);
          dbm.saveDatabase();
          LogHandler.getInstance().createLogEvent(LogType.PACKAGE_ADD,pack);
          officeController.updateWindows();
      }
  }

  // Method for populating Item data textfields and checkboxes depending on the currently selected item on the itemchoice combobox
  private void populateItemOnSelection(ActionEvent event) {
      Item item = itemChoice.getValue();
      if (item != null) {
          // Update class label colors on Item selection 
          updateClassLabelColors();

          // Populate textfields and choiceBox with Item's values
          weightInput.setText(String.valueOf(item.getWeight()));
          heightInput.setText(String.valueOf(item.getHeight()));
          lengthInput.setText(String.valueOf(item.getLenght()));
          widthInput.setText(String.valueOf(item.getWidth()));
          itemNameInput.setText(item.getItemName());
          fragileChoice.setSelected(item.isFragile() == true ? true : false); 
      }
  }
  
  // Method for udating postOffice choice comboboxes depending on currently selected city on CityChoice comboboxes
  private void updateOfficesChoices(ActionEvent event) {
      ListManager lm = ListManager.getInstance();
      if (event.getSource().equals(startCityChoice)) {
          ObservableList<SmartPost> list = FXCollections.observableArrayList(lm.getPostOfficeData(startCityChoice.getValue()));
          startPostChoice.getItems().setAll(list);
          startPostChoice.setValue(null);
      }
      else if (event.getSource().equals(endCityChoice)) {
          ObservableList<SmartPost> list = FXCollections.observableArrayList(lm.getPostOfficeData(endCityChoice.getValue()));
          endPostChoice.getItems().setAll(list);
          endPostChoice.setValue(null);
      }
  }
  // Method for updating classdata labels and setting values to class limit variables
  private void updateClassLabels(PackageClass pc) {
      // Set packageClass limits to variables
      distanceLimit = pc.getDistanceLimit();
      speed = pc.getSpeedLimit();
      volumeLimit = pc.getVolumeLimit();
      weightLimit = pc.getWeightLimit();
      fragile = pc.isBreakable();
      // update class limit data on labels
      distLabel.setText(String.valueOf(distanceLimit));
      speedLabel.setText(String.valueOf(speed));
      sizeLabel.setText(String.valueOf(volumeLimit));
      weightLabel.setText(String.valueOf(weightLimit));
      fragLabel.setText(fragile ? "kyllä" : "ei");
  }

  
  // Method for updating distanceStatus label depending on the current distance between the selected post offices
  private void updateDistanceOnSelection(ActionEvent event) {
      ListManager lm = ListManager.getInstance();
      if (startPostChoice.getValue() != null && endPostChoice.getValue() != null)  {
          //Get pathArray containing the start and endpoints coordinates
          ArrayList<Double> pathArray = lm.getPathArray(startPostChoice.getValue(),endPostChoice.getValue()); 
          //Get distance from javascript
          cuDistance = getDistance(pathArray);
          cuDistLabel.setText(String.valueOf(cuDistance) + " km");
          // Update distance label colors
          updateDistColor();
      }
      else 
          cuDistLabel.setText("");
  }

  // Method for updating distanceStatus label colors depending on the current distance between the selected post offices and current selected packageClass's distance limit
  private void updateDistColor() {
      if (startPostChoice != null && endPostChoice != null) {
          if (cuDistance > distanceLimit)
              // Set distanceLabel color to red if the distance is larger than class limit
              cuDistLabel.setTextFill(Color.DARKRED);
          else 
              // Else set color back to black
              cuDistLabel.setTextFill(Color.BLACK);
      }
  }
  
  // Method for updating class label colors depending on the selected items data and current package's volume and weight
  private void updateClassLabelColors() {
      // Calculate current package's volume and weight from items currently in tableview
      double packageVolume = table.getItems().stream().mapToDouble(Item::getVolume).sum();
      double packageWeight = table.getItems().stream().mapToDouble(Item::getWeight).sum();
      // Check that the added item's volume isn't too big to satisfy current packageclass volume limit
      if (itemChoice.getValue() != null) {
          // get the current selected item's weight,volume and fragile state
          double cWeight = itemChoice.getValue().getWeight();
          double cVolume = itemChoice.getValue().getVolume();
          boolean cFragile = itemChoice.getValue().isFragile();
          if (cWeight > weightLimit-packageWeight && cVolume > volumeLimit-packageVolume) {
              sizeLabel.setTextFill(Color.DARKRED);
              weightLabel.setTextFill(Color.DARKRED);
          }
          else if (cWeight > weightLimit-packageWeight && cVolume < volumeLimit-packageVolume)
          {
              weightLabel.setTextFill(Color.DARKRED);
              sizeLabel.setTextFill(Color.BLACK);
          }
          else if (cWeight < weightLimit-packageWeight && cVolume > volumeLimit-packageVolume)
          {
              weightLabel.setTextFill(Color.BLACK);
              sizeLabel.setTextFill(Color.DARKRED);
          }
          else {
              weightLabel.setTextFill(Color.BLACK);
              sizeLabel.setTextFill(Color.BLACK);
          }
          if (cFragile && !classData.isBreakable())
              fragLabel.setTextFill(Color.DARKRED);
          else
              fragLabel.setTextFill(Color.BLACK);
      }

  }
  // Method for checking if currently selected item satisfies the current packageclass limits
  // Returns true only if all the requirements are fulfilled else returns false
  // Throws warning dialog if any of the requirements isn't fulfilled
  public boolean checkItemClassLimits(Item item) {
      boolean status = true;
      DialogTypes error = null;
      // Calculate current package's volume and weight from items currently in tableview
      double packageVolume = table.getItems().stream().mapToDouble(Item::getVolume).sum();
      double packageWeight = table.getItems().stream().mapToDouble(Item::getWeight).sum();
      // Check that the added item's volume isn't too big to satisfy current packageclass volume limit
      if (item.getVolume() > volumeLimit-packageVolume) {
          error = DialogTypes.TOOBIG_ERROR;
          status = false;
      }
      // Check that the added item's weight isn't too big to satisfy current packageclass weight limit
      if (item.getWeight() > weightLimit-packageWeight) {
          error = DialogTypes.TOOHEAVY_ERROR;
          status = false;
      }
      if (item.getWeight() > weightLimit-packageWeight && item.getVolume() > volumeLimit-packageVolume)
       {
          error = DialogTypes.BIGHEAVY_ERROR;
          status = false;
      }
      if (!status)
          throwWarningDialog(DialogTypes.NEWITEMERROR.toString(), error.getDesc());

      else if (item.isFragile() && !classData.isBreakable()) {
          if (throwConfirmationDialog(DialogTypes.ITEM_WARNING.getDesc(), DialogTypes.FRAG_CONFIRM.getDesc()))
              status = true;
          else
              status = false;
      }
      return status;

  }
  // Method for modifying selected Item's data inside class and database
  private void modifyItem(ActionEvent event) {
      Item modifyItem = itemChoice.getValue();
      String itemName = itemNameInput.getText();
      String length = lengthInput.getText();
      String width = widthInput.getText();
      String height = heightInput.getText();
      String weight = weightInput.getText();
      boolean fragile = fragileChoice.selectedProperty().getValue();
      st = Storage.getInstance();
      if (checkEmptyItemValues()) {
          if (checkItemInputFormats()) {
              // Clone original item for logEvent
              Item org = modifyItem.clone();
              // Convert input strings into doubles
              double dWeight = Double.parseDouble(weight);
              double dWidth = Double.parseDouble(width);
              double dLength = Double.parseDouble(length);
              double dHeight = Double.parseDouble(height);
              //
              // Modify Item instance attributes with setters
              modifyItem.setItemName(itemName);
              modifyItem.setLenght(dLength);
              modifyItem.setWidth(dWidth);
              modifyItem.setHeight(dHeight);
              modifyItem.setWeight(dWeight);
              modifyItem.setFragile(fragile);
              // finally update Item's new data in the database
              dbm.updateDbItem(modifyItem);
              itemChoice.getItems().setAll(Storage.getInstance().getItems());
              itemChoice.getSelectionModel().select(modifyItem);
              updateClassLabelColors();
              reloadPackagesAndItems();
              officeController.updateWindows();
              LogHandler.getInstance().createLogEvent(LogType.ITEM_MOD,org,modifyItem);

          }
      }
  }
  // Method for removing selected items from the tableview
  private void removeItemfromTable(ActionEvent event) {
      table.getSelectionModel().getSelectedItems().forEach(i -> table.getItems().remove(i));
      // Also update class labels when items are removed from the table
      updateClassLabelColors();
  }
  
  
  // Method for creating new Item object
  private void createNewItem(ActionEvent event) {
      String itemName = itemNameInput.getText();
      String length = lengthInput.getText();
      String width = widthInput.getText();
      String height = heightInput.getText();
      String weight = weightInput.getText();
      st = Storage.getInstance();
      boolean fragile = fragileChoice.selectedProperty().getValue();
      if (checkEmptyItemValues()) {
          if (checkItemInputFormats()) {
              // Convert strings to doubles
              double dWeight = Double.parseDouble(weight);
              double dWidth = Double.parseDouble(width);
              double dLength = Double.parseDouble(length);
              double dHeight = Double.parseDouble(height);
              Item it = new Item(itemName,dWeight,dWidth,dHeight,dLength,fragile);
              st.addItem(it);
              dbm.addItemToDatabase(it);
              itemChoice.getItems().clear();
              itemChoice.getItems().setAll(st.getItems());
              officeController.updateWindows();
              LogHandler.getInstance().createLogEvent(LogType.ITEM_ADD,it);
          }
      }
  }
  public void throwWarningDialog(String title, String header) {
      Alert alert = null;
      alert = new Alert(AlertType.WARNING);
      alert.setTitle(title);
      alert.setHeaderText(header);
      alert.showAndWait();
  }
  public boolean throwConfirmationDialog(String title, String header) {
      boolean status = true;
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setHeaderText(header);
      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK)
          status = true;
      else
          status = false;
      return status;
  }

  // Method for deleting selected combobox item from storage and database
  public void deleteItem(ActionEvent event) {
      Item item = itemChoice.getValue();
      if (item != null) {
          dbm.removeDbItem(item);
          Storage.getInstance().removeItem(item);
          itemChoice.getItems().setAll(Storage.getInstance().getItems());
          itemChoice.getSelectionModel().clearAndSelect(0);
          reloadPackagesAndItems();
          officeController.updateWindows();
          LogHandler.getInstance().createLogEvent(LogType.ITEM_REMOVE,item);
      }

  }

  // Method for checking all the empty item inputfields
  // Returns true only if all the inputfields are not empty and throws warning dialog if necessary
  public boolean checkEmptyItemValues() {
      boolean status = true;
      DialogTypes dialog = null;
      String name = itemNameInput.getText();
      String length = lengthInput.getText();
      String width = widthInput.getText();
      String height = heightInput.getText();
      String weight = weightInput.getText();
      ArrayList<String> emptyFields = new ArrayList<>();
      if (name.isEmpty()) {
          emptyFields.add("nimikenttä");
          status = false;
      }
      if (length.isEmpty()) {
          emptyFields.add("pituuskenttä");
          status = false;
      }
      if (width.isEmpty()) {
          emptyFields.add("leveyskenttä");
          status = false;
      }
      if (height.isEmpty()) {
          emptyFields.add("korkeuskenttä");
          status = false;
      }
      if (weight.isEmpty()) {
          emptyFields.add("painokenttä");
          status = false;
      }
      if (!status) {
          if ( emptyFields.size() == 1 )
          {
              dialog = DialogTypes.EMPTYFIELD;
          }
          else
              dialog = DialogTypes.EMPTYFIELDS; 
          dialog.setFields(emptyFields);
          throwWarningDialog(DialogTypes.NEWITEMERROR.toString(),dialog.getEmptyFields());
      }
      return status;
  }

  public boolean checkItemInputFormats() {
      boolean status = true;
      ArrayList<String> formatFields = new ArrayList<>();
      DialogTypes dialog;
      if (!isDouble(lengthInput)) {
          formatFields.add("pituuskenttä");
          status = false;
      }
      if (!isDouble(widthInput)) {
          formatFields.add("leveyskenttä");
          status = false;
      }
      if (!isDouble(heightInput)) {
          formatFields.add("korkeuskenttä");
          status = false;
      }
      if (!isDouble(weightInput)) {
          formatFields.add("painokenttä");
          status = false;
      }
      if (!status) {
          if ( formatFields.size() == 1 )
              dialog = DialogTypes.DPARSE_ERROR;
          else
              dialog = DialogTypes.DPARSE_ERRORS;
          dialog.setFields(formatFields);
          throwWarningDialog(DialogTypes.NEWITEMERROR.toString(),dialog.getEmptyFields());
      }
      return status;

  }

  public void updateDataOnPackageSelection(ActionEvent event) {
      modifyPackage = packageChoice.getValue();
      populatePackageFields(modifyPackage);
  } 

  public void populatePackageFields(Package p) {
      ListManager lm = ListManager.getInstance();
      if (modifyPackage != null) {
          table.getItems().setAll(modifyPackage.getItems());
          classData = modifyPackage.getShipClass();
          if (classData.getClassNumber() == 1)
              radioFirstClass.setSelected(true);
          else if (classData.getClassNumber() == 2)
              radioSecondClass.setSelected(true);
          else
              radioThirdClass.setSelected(true);
          startCityChoice.getSelectionModel().select(modifyPackage.getStartPoint().getCity());
          endCityChoice.getSelectionModel().select(modifyPackage.getEndPoint().getCity());
          startPostChoice.getSelectionModel().select(lm.getOfficeWithGeoLocation(modifyPackage.getStartPoint()));
          endPostChoice.getSelectionModel().select(lm.getOfficeWithGeoLocation(modifyPackage.getEndPoint()));
      }
  }

  // Method for reloading items and packages choiceboxes
  public void reloadPackagesAndItems() {
      Storage st = Storage.getInstance();
      DataBaseManager.getInstance().reloadDatabase();
      packageChoice.getItems().setAll(st.getNotSentPackages());
      itemChoice.getItems().setAll(st.getItems());
      packageChoice.setValue(null);
      itemChoice.setValue(null);
      itemNameInput.setText("");
      lengthInput.setText("");
      widthInput.setText("");
      heightInput.setText("");
      weightInput.setText("");
      fragileChoice.setSelected(false);
      table.getItems().clear();
      startCityChoice.setValue(null);
      endCityChoice.setValue(null);
      startPostChoice.setValue(null);
      endPostChoice.setValue(null);
  }

  // Method for modifying selected package in the database and storage
  public void modifySelectedPackage(ActionEvent event) {
     DataBaseManager dbm = DataBaseManager.getInstance();
     if (checkModifyPackage()) {
         if (checkPackageInputs()) {
             // Get the items inside the tableview
             ArrayList<Item> items = table.getItems().stream().map(e -> e.clone()).collect(Collectors.toCollection(ArrayList::new));
             double distance = cuDistance;
             // Get package's end and startpoint from currently selected post choices
             GeoLocation startPoint = startPostChoice.getValue().getLocation();
             GeoLocation endPoint = endPostChoice.getValue().getLocation();
             java.util.Date now = new java.util.Date();
             SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
             String createTime = df.format((new java.sql.Timestamp(now.getTime())));
             // Clone the original package for log event comparison
             Package original = modifyPackage.clone();
             modifyPackage.setShipClass(classData);
             modifyPackage.setStartPoint(startPoint);
             modifyPackage.setEndPoint(endPoint);
             modifyPackage.setCreateTime(createTime);
             modifyPackage.setItems(items);
             modifyPackage.setDistance(distance);
             dbm.updateDbPackage(modifyPackage);
             dbm.saveDatabase();
             LogHandler.getInstance().createLogEvent(LogType.PACKAGE_MOD,original,modifyPackage);
             officeController.updateWindows();
         }
     }
  }

  // Method for removing currently selected package from database and storage
  public void removeSelectedPackage(ActionEvent event) {
      Package toRemoved = packageChoice.getValue();
      DataBaseManager dbm = DataBaseManager.getInstance();
      if (toRemoved != null) {
          LogHandler.getInstance().createLogEvent(LogType.PACKAGE_REMOVE,toRemoved);
          dbm.removeDbPackage(toRemoved);
          officeController.updateWindows();
      }
  }

  // Method for checking currently to be modified or added package's value inputs
  // Returns false if any of the checks fails and throws proper warning dialog depending on the error
  public boolean checkPackageInputs() {
      boolean status = true;
      StringBuilder sb = new StringBuilder(); 
      if (table.getItems().isEmpty()) {
          sb.append(DialogTypes.ITEMS_ERROR);
          status = false;
      }
      if (startPostChoice.getValue() == null && endPostChoice.getValue() == null) {
          sb.append(DialogTypes.PATH_ERROR);
          status = false;
      }
      else if (startPostChoice.getValue() == null && endPostChoice.getValue() != null) {
          sb.append(DialogTypes.STARTPOINT_ERROR);
          status = false;
      }
      else if (startPostChoice.getValue() != null && endPostChoice.getValue() == null){
          sb.append(DialogTypes.ENDPOINT_ERROR);
          status = false;
      }

      else if (startPostChoice.getValue() == endPostChoice.getValue()) {
          sb.append(DialogTypes.SAMEPOINT_ERROR);
          status = false;
      }
      if (cuDistance > distanceLimit) {
          sb.append(DialogTypes.DISTANCE_ERROR);
          status = false;
      }
      if (!status)
          throwWarningDialog(DialogTypes.NEWPACKAGERROR.toString(),sb.toString());

      return status;

  }

  // Method for checking currently tobe modified package
  // Returns true only if any of the checks doesn't fail
  // Throws warning dialog if any of the checks fails
  public boolean checkModifyPackage() {
      boolean status = true;
      DialogTypes error = null;
      if (modifyPackage == null) {
          error = DialogTypes.NOMODPACK_ERROR;
          status = false;
      } else if (modifyPackage.isSent()) {
          error = DialogTypes.MODSENTPACK_ERROR;
          status = false;
      }
      if (!status) {
          throwWarningDialog(DialogTypes.MODPACKAGERROR.toString(),error.getDesc());
      }
      return status;
    }



    /**
     * @param webview the webview to set
     */
    public void setWebview(WebView webview) {
        this.webview = webview;
    }

    /**
     * @return the packageController
     */
    public static PackageGuiController getPackageController() {
        return packageController;
    }

    /**
     * @param packageController the packageController to set
     */
    public static void setPackageController(PackageGuiController packageController) {
        PackageGuiController.packageController = packageController;
    }

}
