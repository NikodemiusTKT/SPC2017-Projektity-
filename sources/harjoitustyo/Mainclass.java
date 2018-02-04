package harjoitustyo;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 
 */

/**
 * @author tkt
 *
 */
public class Mainclass extends Application {
    private static Stage primaryStage;
    private void setPrimaryStage(Stage stage) {
        Mainclass.primaryStage = stage;
    }
    static public Stage getPrimaryStage() {
        return Mainclass.primaryStage;
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
      launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        setPrimaryStage(primaryStage);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("OfficeGui.fxml"));
        Parent root = loader.load();
        OfficeGuiController controller = (OfficeGuiController) loader.getController();
        primaryStage.setTitle("Postiautomaatti");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setOnCloseRequest(e -> {
            LogHandler.getInstance().createLogEvent(LogType.PROG_END);
            DataBaseManager.getInstance().saveDatabase();
            try {
                DataBaseManager.getInstance().connect().close();
            } catch (SQLException ex) {
                System.err.println("Something went wrong on closing the connection.");
            }
            Platform.exit();
        });
        primaryStage.show();
        LogHandler.getInstance().createLogEvent(LogType.PROG_START);
        primaryStage.setScene(scene);
        // Set the controller instance
        controller.setOfficeController(controller);

    }
}

