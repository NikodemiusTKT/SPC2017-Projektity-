/**
 * 
 */
package harjoitustyo;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * @author tkt
 *
 */
public class LogGuiController implements Initializable {

    @FXML
    private Button closeBt, saveBt;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableView<LogEvent> logTable;
    @FXML
    private TableColumn<LogEvent,String> dateColumn;
    @FXML
    private TableColumn<LogEvent, String> typeColumn;
    @FXML
    private TableColumn<LogEvent, String> descColumn;
    @FXML
    private TextField searchField;
    private ListManager lm = ListManager.getInstance();
    private LogHandler lh = LogHandler.getInstance();

    private static LogGuiController logController;
    private final String dateFormat = "dd.MM.yyyy";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
       dateColumn.setCellValueFactory(new PropertyValueFactory<LogEvent,String>("logTime"));
       typeColumn.setCellValueFactory(new PropertyValueFactory<LogEvent,String>("logType"));
       descColumn.setCellValueFactory(new PropertyValueFactory<LogEvent,String>("desciption"));
       setUpTable();
       logTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
       logTable.setTableMenuButtonVisible(true);
       closeBt.setOnAction(e -> ((Stage)((Node)e.getSource()).getScene().getWindow()).close());
    }

    public void reloadLogs() {
        lm.getLogEvents().clear();
        lh.getDbLogEvent();
        setUpTable();
    }

    /**
     * @return the logController
     */
    public static LogGuiController getLogController() {
        return logController;
    }

    /**
     * @param logController the logController to set
     */
    public static void setLogController(LogGuiController logController) {
        LogGuiController.logController = logController;
    }

    public void setUpTable() {

        // Add listener to searchField to filter the tableview results
       ObservableList<LogEvent> logs = FXCollections.observableArrayList(lm.getLogEvents());
       FilteredList<LogEvent> filteredLogs = new FilteredList<>(logs, p -> true);
       searchField.textProperty().addListener((ob,ol,ne) -> {
           filteredLogs.setPredicate(logEvent -> {
               if (ne == null || ne.isEmpty()) {
                   return true;
               }
               if (logEvent.getDesciption().toLowerCase().contains(ne.toLowerCase()))
                       return true;
               return false;
           });
       });
        // Add listener to datepicker to filter the tableview results
       datePicker.setPromptText(dateFormat);
       datePicker.valueProperty().addListener((ob,ol,ne) -> {
           filteredLogs.setPredicate(logEvent -> {
               if (ne == null || ne.toString().isEmpty()) {
                   return true;
               }
               String date = ne.format(DateTimeFormatter.ofPattern(dateFormat));
               if (logEvent.getLogTime().matches(date+".*"))
                   return true;
               else return false;
           });
       });



       SortedList<LogEvent> sortedLogs = new SortedList<>(filteredLogs);
       sortedLogs.comparatorProperty().bind(logTable.comparatorProperty());

       logTable.setItems(sortedLogs);
    }

}
