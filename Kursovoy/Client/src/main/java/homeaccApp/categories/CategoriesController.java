package homeaccApp.categories;

import homeaccApp.Main;
import homeaccApp.api.DAO.CategoryDAO;
import homeaccApp.api.LocalDateReciever;
import homeaccApp.categories.edit.CategoriesEditController;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by vellial on 06.06.16.
 */
public class CategoriesController {
    @FXML
    private TableView<Categories> categoriesView;
    @FXML
    private Button addCategory;
    @FXML
    private Button deleteCategory;
    @FXML
    private TableColumn<Categories, String> categoryName;
    @FXML
    private TableColumn<Categories, String> categoryCostincome;

    private ObservableList<Categories> categories;

    @FXML
    public void initialize() {
        categoryName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCategoryName()));
        categoryCostincome.setCellValueFactory(cellData -> {
            String s = cellData.getValue().getCostincome() ? "Доход" : "Расход";
            return new SimpleStringProperty(s);
        });
        buildData();
    }

    private void buildData() {

        try {
            categories = CategoryDAO.selectCategoriesSyncData();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        categoriesView.setItems(categories);

    }

    @FXML
    private void handleCreate() {
        Categories category = new Categories();
        boolean okClicked = showCategoryEditDialog(category);

        if (okClicked) {
            long date = LocalDateReciever.getDateOfNow();

            // Adding new bill to database.
            try {
                CategoryDAO.createCategory(category.getCategoryName(), category.getCostincome());
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            categoriesView.getItems().add(category);
        }
    }

    @FXML
    private void handleDelete() throws SQLException, ClassNotFoundException {
        int categoryId = categoriesView.getSelectionModel().getSelectedItem().getCategoryId();
        int selectedIndex = categoriesView.getSelectionModel().getSelectedIndex();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Подтвердите действие");
        alert.setContentText("Вы действительно хотите удалить запись?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK){
            CategoryDAO.deleteCategory(categoryId);
            categoriesView.getItems().remove(selectedIndex);
        }
    }

    /**
     * Открывает диалоговое окно для изменения деталей указанной записи.
     * Если пользователь кликнул OK, то изменения сохраняются в предоставленном
     * объекте адресата и возвращается значение true.
     *
     * @return true, если пользователь кликнул OK, в противном случае false.
     */
    public boolean showCategoryEditDialog(Categories category) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            URL location = new File(Main.dir, "categories/edit/CategoriesEditView.fxml").toURI().toURL();
            loader.setLocation(location);
            GridPane page = (GridPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Создать запись");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            CategoriesEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCategory(category);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
