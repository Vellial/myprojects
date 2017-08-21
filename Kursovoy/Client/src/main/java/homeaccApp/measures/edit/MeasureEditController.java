package homeaccApp.measures.edit;

import homeaccApp.measures.Measures;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by vellial on 26.09.16.
 */
public class MeasureEditController {
    @FXML
    private TextField txtMeasure;
    @FXML
    private Button ok;
    @FXML
    private Button cancel;

    private Measures measure;
    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    private void initialize() {

    }

    /**
     * Вызывается, когда пользователь кликнул по кнопке OK.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            measure.setMeasureName(txtMeasure.getText());

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

        if (txtMeasure.getText() == null || txtMeasure.getText().length() == 0) {
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

    public void setMeasure(Measures measure) {
        this.measure = measure;
    }
}
