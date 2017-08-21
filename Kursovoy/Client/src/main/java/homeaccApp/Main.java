package homeaccApp;

import homeaccApp.api.DAO.UserDAO;
import homeaccApp.api.DBapi;
import homeaccApp.mainwindow.MainWindowController;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import homeaccApp.user.api.chooseuser.ChooseUserController;
import homeaccApp.user.api.createuser.CreateUserController;
import homeaccApp.user.api.createuser.User;
import homeaccApp.user.api.edituser.EditUserController;
import homeaccApp.user.session.SessionController;

import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * homeaccApp.Main method. Here is start for application.
 */
public class Main extends Application {
    private Stage primaryStage;
    public static UUID uuidDevice;
    public static File dir = new File("src/main/java/homeaccApp");


    public static File sourceLightside;
    public static File destLightsideCopy;

    /**
     * Start method.
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(final Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Домашний бухучёт");
        boolean isOk = false;

        try {
            // If homeaccApp.user keeped authorize.

            byte[] authInfo = SessionController.readSmallBinaryFile("authInfo");
            if (authInfo != null && authInfo.length != 0) {
                int uid = UserDAO.authoriseUser((int) authInfo[1]);
                if (uid != 0) {
                    try {
                        showMainWindow();
                        isOk = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                ObservableList rs;
                try {
                    rs = UserDAO.selectAllUsersList(); // if any homeaccApp.user exists in database
                    if (rs.size() > 0) {
                        isOk = showLoginDialog();
                    } else {
                        showCreateUserDialog();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if (isOk) {
                // create uuid for device from mac-addr;
                InetAddress ip;
                ip = InetAddress.getLocalHost();
                NetworkInterface netInt = NetworkInterface.getByInetAddress(ip);
                byte[] macAddr = netInt.getHardwareAddress();

                uuidDevice = UUID.nameUUIDFromBytes(macAddr);

            } else {
                // homeaccApp.user choose "cancel" at login dialog
                showChooseUserDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public static void main(final String[] args) throws MalformedURLException {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        sourceLightside = new File(s + "/lightside2");
        destLightsideCopy = new File(s + "/lightside2_copy");

        // If database was lost, take exception.
        try {
            Connection c = DBapi.getConnection();

            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);

            if (!rs.next()) {
                copyFileUsingStream(destLightsideCopy, sourceLightside);
            }
            rs.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        launch(args);
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Show main window,
     */
    public void showMainWindow() {
        try {
            FXMLLoader mainLoader = new FXMLLoader(new File(dir, "mainwindow/mainwindow.fxml").toURI().toURL());
            Parent root = (Parent) mainLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            MainWindowController controller = mainLoader.getController();
            controller.setPrimaryStage(primaryStage);

            primaryStage.show();
            controller.showNotifications();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show "Create User" Dialog,
     */
    public void showCreateUserDialog() {
        try {
            // TODO: 26.07.16 May be create function with this 2 notes?
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(dir, "user/api/createuser/createuser.fxml").toURI().toURL();

            loader.setLocation(location);
            AnchorPane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Создание пользователя");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(scene);

            CreateUserController createUserController = loader.getController();
            createUserController.setDialogStage(dialogStage);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show "Edit User" Dialog,
     */
    public void showEditUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(dir, "user/api/edituser/edituser.fxml").toURI().toURL();
            loader.setLocation(location);
            AnchorPane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактирование пользователя");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(scene);

            EditUserController editUserController = loader.getController();
            editUserController.setDialogStage(dialogStage);
            editUserController.setUser(user);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show "Choose User" Dialog,
     */
    public static void showChooseUserDialog() {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(dir, "user/api/chooseuser/chooseuser.fxml").toURI().toURL();
            loader.setLocation(location);
            BorderPane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Выберите пользователя");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(primaryStage);
            dialogStage.setScene(scene);

            ChooseUserController chooseUserController = loader.getController();
            chooseUserController.setDialogStage(dialogStage);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Login dialog
     * @return true if authorization is ok.
     */
    public boolean showLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(dir, "user/session/login.fxml").toURI().toURL();
            loader.setLocation(location);
            AnchorPane rootLayout = loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Авторизация");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(rootLayout);
            dialogStage.setScene(scene);

            SessionController sessionController = loader.getController();
            sessionController.setDialogStage(dialogStage);
            // Даём контроллеру доступ к главному приложению.
            sessionController.setMainApp(this);

            dialogStage.showAndWait();

            return sessionController.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Login dialog with username (from "Choose homeaccApp.user" dialog).
     * @return true if authorization is ok.
     */
    public boolean showLoginDialog(String username) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(dir, "user/session/login.fxml").toURI().toURL();
            loader.setLocation(location);
            AnchorPane rootLayout = loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Авторизация");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(rootLayout);
            dialogStage.setScene(scene);

            SessionController sessionController = loader.getController();
            sessionController.setDialogStage(dialogStage);
            sessionController.setUsername(username);

            // Даём контроллеру доступ к главному приложению.
            sessionController.setMainApp(this);

            dialogStage.showAndWait();

            return sessionController.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
