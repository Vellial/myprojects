package homeaccApp.categories.edit;

import homeaccApp.categories.Categories;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by vellial on 26.09.16.
 */
public class CategoriesEditController {

    @FXML
    private TextField txtCatName;
    @FXML
    private ComboBox<String> cmbCostinc;
    @FXML
    private Button addCat;
    @FXML
    private Button cancel;

    private Categories category;
    private Stage dialogStage;
    private boolean okClicked = false;

    private static String cost = "Расход";
    private static String income = "Доход";

    @FXML
    private void initialize() {
        cmbCostinc.getItems().addAll(income, cost);
    }
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true, если пользователь кликнул OK, в другом случае false.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    public void setCategory(Categories category) {
        this.category = category;
    }

    /**
     * Вызывается, когда пользователь кликнул по кнопке OK.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            category.setCategoryName(txtCatName.getText());
            String s = cmbCostinc.getSelectionModel().getSelectedItem();
            boolean costinc = false;
            if (s.equals("Доход")) {
                costinc = true;
            }
            category.setCostincome(costinc);

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Вызывается, когда пользователь кликнул по кнопке Cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Проверяет пользовательский ввод в текстовых полях.
     *
     * @return true, если пользовательский ввод корректен
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (txtCatName.getText() == null || txtCatName.getText().length() == 0) {
            errorMessage += "Неверное название для единицы измерения\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Показываем сообщение об ошибке.
            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.initOwner(dialogStage);
            alert.setTitle("Поля заполнены неверно");
            alert.setHeaderText("Пожалуйста, проверьте и заполните поля правильно.");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

}
