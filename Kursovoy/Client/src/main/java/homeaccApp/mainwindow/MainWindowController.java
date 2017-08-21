package homeaccApp.mainwindow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import homeaccApp.api.DAO.*;
import homeaccApp.api.LocalDateReciever;
import homeaccApp.api.Sync.Syncronization;
import homeaccApp.cashes.Cashes;
import homeaccApp.cashes.CashesList;
import homeaccApp.categories.Categories;
import homeaccApp.categories.CategoriesList;
import homeaccApp.charts.Charts;
import homeaccApp.currencies.CurrenciesController;
import homeaccApp.currencies.CurrenciesList;
import homeaccApp.currencies.Currency;
import homeaccApp.mainwindow.bills.Bills;
import homeaccApp.mainwindow.bills.BillsList;
import homeaccApp.mainwindow.costsincomes.Costincomes;
import homeaccApp.mainwindow.costsincomes.CostincomesList;
import homeaccApp.mainwindow.costsincomes.IncomesController;
import homeaccApp.mainwindow.menuDialogs.miSettings.miSettings;
import homeaccApp.mainwindow.menuDialogs.miSettings.miSettingsController;
import homeaccApp.mainwindow.planning.Planning;
import homeaccApp.mainwindow.planning.PlansList;
import homeaccApp.measures.Measures;
import homeaccApp.measures.MeasuresList;
import homeaccApp.user.session.SessionController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import homeaccApp.Main;
import homeaccApp.mainwindow.bills.BillstabController;
import homeaccApp.mainwindow.costsincomes.CostsController;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.java_websocket.WebSocket;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller for main window
 */
public class MainWindowController {
    // Tabs. Communication between tabs.
    @FXML
    private Tab billsTab;
    @FXML
    private BillstabController billstabController;
    @FXML
    private Tab incomesTab;
    @FXML
    private Tab costsTab;
    @FXML
    private CostsController coststabController;
    @FXML
    private IncomesController incomestabController;

    // Menu and menu items.
    @FXML
    private MenuBar mainMenu;
    @FXML
    private MenuItem miCategories;
    @FXML
    private MenuItem miMeasures;
    @FXML
    private MenuItem miСurrencies;
    @FXML
    private MenuItem miReports;
    @FXML
    private MenuItem miSettingsItem;
    @FXML
    private MenuItem miSync;
    @FXML
    private MenuItem miBackup;
    @FXML
    private MenuItem miClose;
    @FXML
    private MenuItem miExit;
    @FXML
    private MenuItem miAbout;
    @FXML
    private MenuItem miCurrencies;

    private Main main;
    private Stage primaryStage;
    public static boolean isAuth = false;
    public static boolean isError = false;

    public MainWindowController() throws IOException {

    }

    @FXML
    private void initialize() throws IOException, SQLException, ClassNotFoundException {
        billstabController.init(this);
        coststabController.init(this);
        incomestabController.init(this);

        // Menu initialization
        miCategories.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                showCategoriesWindow();
            }
        });
        miMeasures.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                showMeasuresWindow();
            }
        });
        miReports.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                Charts.showChart(true, "Доходы");
                Charts.showChart(false, "Расходы");
            }
        });
        // Set settings
        miSettingsItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                miSettings settings = new miSettings();
                showSettingsDialog(settings);
            }
        });
        miBackup.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();
                File source = new File(s + "/lightside2");
                File dest = new File(s + "/lightside2_copy");
                try {
                    copyDatabaseFile(source, dest);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Сообщение");
                    alert.setHeaderText("Операция прошла успешно");
                    alert.setContentText("База данных скопирована");
                    alert.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        miSync.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) throws RuntimeException {
                miSettings settings = CommonDAO.selectSettings();
                if (settings != null) {
                    // get LastSyncDate - if not exist: get all notes from db and send to server; else: get notes with date after lastSyncDate
                    LocalDate lastSyncDate = CommonDAO.getLastSyncDate();
                    // Settings exists but lastSyncDate = 0 -> it's firts time syncronization
                    // get all notes and send to server
                    if (lastSyncDate == null) {
                        try {
                            List<String> resultJson = getSyncData();


                            Timer time = new Timer();

                            time.schedule(new TimerTask() {
                                int i = 0;
                                @Override
                                public void run() { //ПЕРЕЗАГРУЖАЕМ МЕТОД RUN В КОТОРОМ ДЕЛАЕТЕ ТО ЧТО ВАМ НАДО
                                    Syncronization.getInstance();

                                    if (Syncronization.getInstance().getReadyState() == WebSocket.READYSTATE.CLOSED) {
                                        Syncronization.getInstance().connectToWebServer();
                                    }

                                    if (Syncronization.getInstance().getReadyState() == WebSocket.READYSTATE.OPEN) {
                                        // auth
                                        CommonDAO.authorizeOnServer(settings.getRemotePassword(), settings.getRemoteEmail());
                                        boolean isAll = false;
                                        if (isError) {
                                            time.cancel();
                                            return;
                                        }

                                        if (isAuth) {
                                            // messages
                                            resultJson.forEach((temp) -> Syncronization.getInstance().sendMessage(temp));
                                            isAll = true;
                                        }
                                        if (isAll) {
                                            time.cancel();
                                            return;
                                        }
                                    }
                                }
                            }, 0, 3000); //(0 - ПОДОЖДАТЬ ПЕРЕД НАЧАЛОМ В МИЛИСЕК, ПОВТОРЯТСЯ каждую секунду (1 СЕК = 1000 МИЛИСЕК))

//                            if (isError) {
//                                Alert al = new Alert(Alert.AlertType.ERROR);
//                                al.setTitle("Ошибка авторизации");
//                                al.setHeaderText("Пожалуйста, проверьте данные авторизации или зарегистрируйтесь на сервере через настройки");
//                                al.showAndWait();
//                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        // get notes > lastSyncDate
                        try {
                            List<String> resultJson = getSyncDataFromLastSync(lastSyncDate);
                            Syncronization.getInstance();
                            // auth
                            CommonDAO.authorizeOnServer(settings.getRemotePassword(), settings.getRemoteEmail());
                            // messages
                            resultJson.forEach((temp) -> Syncronization.getInstance().sendMessage(temp));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(main.getPrimaryStage());
                    alert.setTitle("Авторизация на сервере");
                    alert.setHeaderText("Пожалуйста, укажите в настройках имя пользователя и пароль для авторизации.");
                    alert.setContentText("");

                    alert.showAndWait();

                    showSettingsDialog(settings);
                }
            }
        });

        // just close program
        miClose.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                primaryStage.close();
            }
        });

        // delete auth user (logout)
        miExit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                byte[] arr = new byte[0];
                try {
                    SessionController.writeSmallBinaryFile(arr, "authInfo");
                    Main.showChooseUserDialog();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // "about program" window
        miAbout.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("О программе");
                alert.setHeaderText("Домашняя бухгалтерия");
                alert.setContentText("Управление и контроль над финансами. \n\nВерсия программы: 1.0 \nВерсия jre: 8.0\nВерсия sqlite: 3.8\nРазработчик: Кузина Светлана");
                alert.showAndWait();
            }
        });
        miCurrencies.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showCurrenciesWindow();
            }
        });
    }

    public void showNotifications() throws SQLException, ClassNotFoundException {
        long dateOfNow = LocalDateReciever.getDateOfNow();
        ObservableList<Planning> oldNotes = PlanningDAO.selectOldPlans(dateOfNow);

        // get plans to dateOfNow
        ObservableList<Planning> plansForNow = PlanningDAO.selectPlansForNow(dateOfNow);
        ObservableList<Planning> periodicalPlans = FXCollections.observableArrayList();
        ObservableList<Planning> completedPlans = FXCollections.observableArrayList();

        if (oldNotes != null) {

            // check period
            for (Planning note : oldNotes) {
                String notePeriod = note.getPlanPeriod();
                switch (notePeriod) {
                    case "Каждый день":
                        LocalDate oldDateDay = LocalDateReciever.getLocalDateFromLong(note.getPlanningDate());
                        LocalDate newDateDay = oldDateDay.plusDays(1);
                        // create new note with new date
                        PlanningDAO.createPlan(LocalDateReciever.getLongTimeFromLocalDate(newDateDay), note.getPlanningBill().getBillId(), note.getPlanningCount(), note.getPlanningMeasure().getMeasureId(), note.getPlanningCategory().getCategoryId(), note.getPlanningNote(), note.getPlanningCostincome(), note.getPlanningAmount(), note.getPlanStatus(), note.getPlanPeriod());
                        // postpone to costs/incomes
                        CostsIncomesDAO.createCostIncome(LocalDateReciever.getLongTimeFromLocalDate(oldDateDay), note.getPlanningBill().getBillId(), note.getPlanningCount(), note.getPlanningMeasure().getMeasureId(), note.getPlanningCategory().getCategoryId(), note.getPlanningNote(), note.getPlanningCostincome(), note.getPlanningAmount());
                        // update date
                        PlanningDAO.updateDate(LocalDateReciever.getLongTimeFromLocalDate(newDateDay), note.getPlanningId());
                        // add to list of periodicalPlans
                        periodicalPlans.add(note);
                    case "Каждую неделю":
                        LocalDate oldDateWeek = LocalDateReciever.getLocalDateFromLong(note.getPlanningDate());
                        LocalDate newDateWeek = oldDateWeek.plusWeeks(1);
                        // create new note with new date
                        PlanningDAO.createPlan(LocalDateReciever.getLongTimeFromLocalDate(newDateWeek), note.getPlanningBill().getBillId(), note.getPlanningCount(), note.getPlanningMeasure().getMeasureId(), note.getPlanningCategory().getCategoryId(), note.getPlanningNote(), note.getPlanningCostincome(), note.getPlanningAmount(), note.getPlanStatus(), note.getPlanPeriod());
                        // postpone to costs/incomes
                        CostsIncomesDAO.createCostIncome(LocalDateReciever.getLongTimeFromLocalDate(oldDateWeek), note.getPlanningBill().getBillId(), note.getPlanningCount(), note.getPlanningMeasure().getMeasureId(), note.getPlanningCategory().getCategoryId(), note.getPlanningNote(), note.getPlanningCostincome(), note.getPlanningAmount());
                        // update date
                        PlanningDAO.updateDate(LocalDateReciever.getLongTimeFromLocalDate(newDateWeek), note.getPlanningId());
                        // add to list of periodicalPlans
                        periodicalPlans.add(note);
                    case "Каждый месяц":
                        LocalDate oldDateMonth = LocalDateReciever.getLocalDateFromLong(note.getPlanningDate());
                        LocalDate newDateMonth = oldDateMonth.plusMonths(1);
                        // create new note with new date
                        PlanningDAO.createPlan(LocalDateReciever.getLongTimeFromLocalDate(newDateMonth), note.getPlanningBill().getBillId(), note.getPlanningCount(), note.getPlanningMeasure().getMeasureId(), note.getPlanningCategory().getCategoryId(), note.getPlanningNote(), note.getPlanningCostincome(), note.getPlanningAmount(), note.getPlanStatus(), note.getPlanPeriod());
                        // postpone to costs/incomes
                        CostsIncomesDAO.createCostIncome(LocalDateReciever.getLongTimeFromLocalDate(oldDateMonth), note.getPlanningBill().getBillId(), note.getPlanningCount(), note.getPlanningMeasure().getMeasureId(), note.getPlanningCategory().getCategoryId(), note.getPlanningNote(), note.getPlanningCostincome(), note.getPlanningAmount());
                        // update date
                        PlanningDAO.updateDate(LocalDateReciever.getLongTimeFromLocalDate(newDateMonth), note.getPlanningId());
                        // add to list of periodicalPlans
                        periodicalPlans.add(note);
                    case "Каждый год":
                        LocalDate oldDateYear = LocalDateReciever.getLocalDateFromLong(note.getPlanningDate());
                        LocalDate newDateYear = oldDateYear.plusYears(1);
                        // create new note with new date
                        PlanningDAO.createPlan(LocalDateReciever.getLongTimeFromLocalDate(newDateYear), note.getPlanningBill().getBillId(), note.getPlanningCount(), note.getPlanningMeasure().getMeasureId(), note.getPlanningCategory().getCategoryId(), note.getPlanningNote(), note.getPlanningCostincome(), note.getPlanningAmount(), note.getPlanStatus(), note.getPlanPeriod());
                        // postpone to costs/incomes
                        CostsIncomesDAO.createCostIncome(LocalDateReciever.getLongTimeFromLocalDate(oldDateYear), note.getPlanningBill().getBillId(), note.getPlanningCount(), note.getPlanningMeasure().getMeasureId(), note.getPlanningCategory().getCategoryId(), note.getPlanningNote(), note.getPlanningCostincome(), note.getPlanningAmount());
                        // update date
                        PlanningDAO.updateDate(LocalDateReciever.getLongTimeFromLocalDate(newDateYear), note.getPlanningId());
                        // add to list of periodicalPlans
                        periodicalPlans.add(note);
                    case "Однократно":
                        // update status
                        note.setPlanStatus("Выполнено");
                        PlanningDAO.updateStatus(note.getPlanStatus(), note.getPlanningId());
                        // add to list of periodicalPlans
                        completedPlans.add(note);
                }
            }
        }

        Label popupLabel = new Label();
        String labelText = "";

        // show notification about plans for now.
        if (plansForNow.size() != 0) {
            labelText += "Планы на сегодня:\n";
            for (Planning plan : plansForNow) {
                labelText += plan.getPlanningNote() + "\n";
            }
        }

        // show notification about period plans.
        if (periodicalPlans.size() != 0) {
            labelText += "Выполненные периодические задачи:\n";
            for (Planning plan : periodicalPlans) {
                labelText += plan.getPlanningNote() + "\n";
            }
        }

        // show notification about old plans.
        if (completedPlans.size() != 0) {
            labelText += "Выполненные задачи:\n";
            for (Planning plan : completedPlans) {
                labelText += plan.getPlanningNote() + "\n";
            }
        }

        if (labelText.length() != 0) {
            popupLabel.setText(labelText);

            FlowPane flowPane = new FlowPane();
            flowPane.setStyle("-fx-background-color:white;-fx-padding:10px;");
            flowPane.getChildren().add(popupLabel);
            Popup pop = createPopup(flowPane);
            pop.setX(primaryStage.getMaxWidth());
            pop.setY(primaryStage.getMaxHeight());
            pop.show(primaryStage);
        }
    }

    private Popup createPopup(FlowPane label) {
        final Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(label);
        return popup;
    }

    public void showCurrenciesWindow() {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File(Main.dir, "currencies/currencies.fxml").toURI().toURL());
            AnchorPane page = (AnchorPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Валюта");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(mainMenu.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            CurrenciesController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void showMeasuresWindow() {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File(Main.dir, "measures/MeasureView.fxml").toURI().toURL());
            AnchorPane page = (AnchorPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Единицы измерения");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showCategoriesWindow() {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File(Main.dir, "categories/CategoriesView.fxml").toURI().toURL());
            AnchorPane page = (AnchorPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Категории");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            //dialogStage.initOwner(mainMenu.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyDatabaseFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Открывает диалоговое окно для изменения деталей указанного адресата.
     * Если пользователь кликнул OK, то изменения сохраняются в предоставленном
     * объекте адресата и возвращается значение true.
     *
     * @param settings - настройки, если они есть
     * @return true, если пользователь кликнул OK, в противном случае false.
     */
    public boolean showSettingsDialog(miSettings settings) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File(Main.dir, "mainwindow/menuDialogs/miSettings/miSettings.fxml").toURI().toURL());
            AnchorPane page = (AnchorPane) loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Настройки синхронизации");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainMenu.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            miSettingsController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<String> getSyncData() throws SQLException, ClassNotFoundException {
        // Bills
        ObservableList<Bills> bills = BillDAO.selectBillsSyncData();
        // Cashes
        ObservableList<Cashes> cashes = BillDAO.selectCashedSyncData();
        // Measures
        ObservableList<Measures> measures = MeasureDAO.selectMeasuresSyncData();
        // Currencies
        ObservableList<Currency> currencies = CurrencyDAO.selectCurrenciesSyncData();
        // Categories
        ObservableList<Categories> categories = CategoryDAO.selectCategoriesSyncData();
        // Planning
        ObservableList<Planning> plans = PlanningDAO.selectPlansSyncData();
        // MoneyTurn
        ObservableList<Costincomes> costincomes = CostsIncomesDAO.selectCostincomesSyncData();

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonObjList = new ArrayList<>();

        try {
            ObjectMapper maps = new ObjectMapper();
            BillsList billsList = new BillsList();
            billsList.setCodeOperation("bills");
            billsList.setNumSync("first");
            billsList.setBills(bills);
            String billsString = maps.writeValueAsString(billsList);
            jsonObjList.add(billsString);

            //---
            MeasuresList measuresList = new MeasuresList();
            measuresList.setCodeOperation("measures");
            measuresList.setNumSync("first");
            measuresList.setMeasures(measures);
            String measureStr = maps.writeValueAsString(measuresList);
            jsonObjList.add(measureStr);

            CurrenciesList currenciesList = new CurrenciesList();
            currenciesList.setCodeOperation("currencies");
            currenciesList.setNumSync("first");
            currenciesList.setCurrencies(currencies);
            String currenciesString = maps.writeValueAsString(currenciesList);
            jsonObjList.add(currenciesString);

            CategoriesList categoriesList = new CategoriesList();
            categoriesList.setCodeOperation("categories");
            categoriesList.setNumSync("first");
            categoriesList.setCategories(categories);
            String categoriesString = maps.writeValueAsString(categoriesList);
            jsonObjList.add(categoriesString);

            // TODO: 20.09.16 create generic class for lists
            CashesList cashesList = new CashesList();
            cashesList.setCodeOperation("cashes");
            cashesList.setNumSync("first");
            cashesList.setCashes(cashes);
            String cashesString = maps.writeValueAsString(cashesList);
            jsonObjList.add(cashesString);

            PlansList plansList = new PlansList();
            plansList.setCodeOperation("plans");
            plansList.setNumSync("first");
            plansList.setPlannings(plans);
            String plansString = maps.writeValueAsString(plansList);
            jsonObjList.add(plansString);

            CostincomesList costincomesList = new CostincomesList();
            costincomesList.setCodeOperation("costincomes");
            costincomesList.setNumSync("first");
            costincomesList.setCostincomes(costincomes);
            String costincomesString = maps.writeValueAsString(costincomesList);
            jsonObjList.add(costincomesString);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonObjList;
    }

    private List<String> getSyncDataFromLastSync(LocalDate lastSyncDate) throws SQLException, ClassNotFoundException {
        String uuidUser = UserDAO.getUserUUID(UserDAO.authUserId);
        // Bills
        ObservableList<Bills> bills = BillDAO.selectBillsSyncDataFromLastDate(lastSyncDate);
        // Cashes
        ObservableList<Cashes> cashes = BillDAO.selectCashesSyncDataFromLastDate(lastSyncDate);
        // Measures
        ObservableList<Measures> measures = MeasureDAO.selectMeasuresFromLastDate(lastSyncDate);
        // Currencies
        ObservableList<Currency> currencies = CurrencyDAO.selectCurrenciesFromLastDate(lastSyncDate);
        // Categories
        ObservableList<Categories> categories = CategoryDAO.selectCategoriesSyncDataFromLastDate(lastSyncDate);
        // Planning
        ObservableList<Planning> plans = PlanningDAO.selectPlansSyncDataFromLastDate(lastSyncDate);
        // MoneyTurn
        ObservableList<Costincomes> costincomes = CostsIncomesDAO.selectCostincomesSyncDataFromLastDate(lastSyncDate);

        // how to send all data to server?
        // TODO: 22.10.16 repeated code.
        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonObjList = new ArrayList<>();
        long lsd = LocalDateReciever.getLongTimeFromLocalDate(lastSyncDate);

        try {
            ObjectMapper maps = new ObjectMapper();
            BillsList billsList = new BillsList();
            billsList.setCodeOperation("bills");
            billsList.setUuidUser(uuidUser);
            billsList.setUuiddevice(Main.uuidDevice.toString());
            billsList.setLastSyncDate(lsd);
            billsList.setBills(bills);
            String billsString = maps.writeValueAsString(billsList);
            jsonObjList.add(billsString);

            // TODO: 20.09.16 create generic class for lists
            CashesList cashesList = new CashesList();
            cashesList.setCodeOperation("cashes");
            cashesList.setUuidUser(uuidUser);
            cashesList.setUuiddevice(Main.uuidDevice.toString());
            cashesList.setLastSyncDate(lsd);
            cashesList.setCashes(cashes);
            String cashesString = maps.writeValueAsString(cashesList);
            jsonObjList.add(cashesString);

            //---
            MeasuresList measuresList = new MeasuresList();
            measuresList.setCodeOperation("measures");
            measuresList.setUuidUser(uuidUser);
            measuresList.setUuiddevice(Main.uuidDevice.toString());
            measuresList.setLastSyncDate(lsd);
            measuresList.setMeasures(measures);
            String measureStr = maps.writeValueAsString(measuresList);
            jsonObjList.add(measureStr);

            CurrenciesList currenciesList = new CurrenciesList();
            currenciesList.setCodeOperation("currencies");
            currenciesList.setUuidUser(uuidUser);
            currenciesList.setUuiddevice(Main.uuidDevice.toString());
            currenciesList.setLastSyncDate(lsd);
            currenciesList.setCurrencies(currencies);
            String currenciesString = maps.writeValueAsString(currenciesList);
            jsonObjList.add(currenciesString);

            CategoriesList categoriesList = new CategoriesList();
            categoriesList.setCodeOperation("categories");
            categoriesList.setUuidUser(uuidUser);
            categoriesList.setUuiddevice(Main.uuidDevice.toString());
            categoriesList.setLastSyncDate(lsd);
            categoriesList.setCategories(categories);
            String categoriesString = maps.writeValueAsString(categoriesList);
            jsonObjList.add(categoriesString);

            PlansList plansList = new PlansList();
            plansList.setCodeOperation("plans");
            plansList.setUuidUser(uuidUser);
            plansList.setUuidDevice(Main.uuidDevice.toString());
            plansList.setLastSyncDate(lsd);
            plansList.setPlannings(plans);
            String plansString = maps.writeValueAsString(plansList);
            jsonObjList.add(plansString);

            CostincomesList costincomesList = new CostincomesList();
            costincomesList.setCodeOperation("costincomes");
            costincomesList.setUuidUser(uuidUser);
            costincomesList.setUuidDevice(Main.uuidDevice.toString());
            costincomesList.setLastSyncDate(lsd);
            costincomesList.setCostincomes(costincomes);
            String costincomesString = maps.writeValueAsString(costincomesList);
            jsonObjList.add(costincomesString);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonObjList;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
