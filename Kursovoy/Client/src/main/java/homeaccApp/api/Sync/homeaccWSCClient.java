package homeaccApp.api.Sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import homeaccApp.api.DAO.*;
import homeaccApp.cashes.Cashes;
import homeaccApp.categories.Categories;
import homeaccApp.currencies.Currency;
import homeaccApp.mainwindow.MainWindowController;
import homeaccApp.mainwindow.bills.Bills;
import homeaccApp.mainwindow.costsincomes.Costincomes;
import homeaccApp.mainwindow.planning.Planning;
import homeaccApp.measures.Measures;
import javafx.collections.ObservableList;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Websocket client for application.
 */
@ClientEndpoint
public class homeaccWSCClient extends WebSocketClient {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private Session session;
//    public static boolean isAuth = false;

    public homeaccWSCClient(URI uri) {
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("Connected ... " + serverHandshake.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        try {
            System.out.println("output" + message);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(message);
            JSONObject jsonObject = (JSONObject) obj;
            String codeOperation = jsonObject.get("key").toString();
            ObjectMapper mapper = new ObjectMapper();
            JSONArray ar = new JSONArray();

            switch (codeOperation) {
                case "message":
                    String uuidUser = jsonObject.get("value").toString();
                    UserDAO.updateUserUUID(uuidUser);
                    BillDAO.updateUserUUID(uuidUser);
                    break;
                case "billsUUIDs":
                    HashMap<Integer, String> billsUUIDs = (HashMap<Integer, String>) jsonObject.get("value");
                    for (Map.Entry entry: billsUUIDs.entrySet()) {
                        int billId = Integer.parseInt(entry.getKey().toString());
                        String billUUID = entry.getValue().toString();
                        BillDAO.editUUIDBill(billId, billUUID);
                        PlanningDAO.editBillUUID(billId, billUUID);
                        CostsIncomesDAO.editBillUUID(billId, billUUID);
                        BillDAO.editCashBillUUID(billId, billUUID);
                    }
                    break;
                case "cursUUIDs":
                    HashMap<Integer, String> cursUUIDs = (HashMap<Integer, String>) jsonObject.get("value");
                    for (Map.Entry entry: cursUUIDs.entrySet()) {
                        int curId = Integer.parseInt(entry.getKey().toString());
                        String curUUID = entry.getValue().toString();
                        CurrencyDAO.editCurrencyUUID(Integer.parseInt(entry.getKey().toString()), entry.getValue().toString());
                        BillDAO.editCashCurUUID(curId, curUUID);
                    }
                    break;
                case "cashUUIDs":
                    HashMap<Integer, String> cashUUIDs = (HashMap<Integer, String>) jsonObject.get("value");
                    for (Map.Entry entry: cashUUIDs.entrySet()) {
                        BillDAO.editCashUUID(Integer.parseInt(entry.getKey().toString()), entry.getValue().toString());
                    }
                    break;
                case "categoriesUUIDs":
                    HashMap<Integer, String> categoriesUUIDs = (HashMap<Integer, String>) jsonObject.get("value");
                    for (Map.Entry entry: categoriesUUIDs.entrySet()) {
                        int catId = Integer.parseInt(entry.getKey().toString());
                        String catUUID = entry.getValue().toString();
                        CategoryDAO.updateCategoryUUID(Integer.parseInt(entry.getKey().toString()), entry.getValue().toString());
                        PlanningDAO.editCategoryUUID(catId, catUUID);
                        CostsIncomesDAO.editCategoryUUID(catId, catUUID);
                    }
                    break;
                case "measuresUUIDs":
                    HashMap<Integer, String> measuresUUIDs = (HashMap<Integer, String>) jsonObject.get("value");
                    for (Map.Entry entry: measuresUUIDs.entrySet()) {
                        int measureId = Integer.parseInt(entry.getKey().toString());
                        String measureUUID = entry.getValue().toString();

                        MeasureDAO.updateMeasureUUID(Integer.parseInt(entry.getKey().toString()), entry.getValue().toString());
                        PlanningDAO.editMeasureUUID(measureId, measureUUID);
                        CostsIncomesDAO.editMeasureUUID(measureId, measureUUID);
                    }
                    break;
                case "costincUUIDs":
                    HashMap<Integer, String> costincUUIDs = (HashMap<Integer, String>) jsonObject.get("value");
                    for (Map.Entry entry: costincUUIDs.entrySet()) {
                        CostsIncomesDAO.editCostincomeUUID(Integer.parseInt(entry.getKey().toString()), entry.getValue().toString());
                    }
                    break;
                case "plansUUIDs":
                    HashMap<Integer, String> plansUUIDs = (HashMap<Integer, String>) jsonObject.get("value");
                    for (Map.Entry entry: plansUUIDs.entrySet()) {
                        PlanningDAO.editPlansUUID(Integer.parseInt(entry.getKey().toString()), entry.getValue().toString());
                    }
                    break;
                case "date":
                    long date = Long.parseLong(jsonObject.get("value").toString());
                    CommonDAO.setLastSyncDate(date);
                    break;
                case "error":
                    MainWindowController.isError = true;
//                    throw new RuntimeException();
                    break;
                case "token":
                    MainWindowController.isAuth = true;
//                    UserDAO.editUser(jsonObject.get("value").toString(), UserDAO.authUserId);
                    String result = "Пользователь успешно авторизован";
                    break;
                case "oldBills" :
                    ar = (JSONArray) jsonObject.get("value");
                    if (ar.size() > 0) {
                        String billses = jsonObject.get("value").toString();
                        try {
                            ObservableList<Bills> billsList = (ObservableList<Bills>) mapper.readValue(billses, Bills.class);
                            billsList.forEach(element -> {
                                BillDAO.editBill(element.getDate(), element.getBillName(), element.getNote(), element.getStartBalance(), element.getUserId(), element.getBillId(), element.getUuidBill(), element.getUuidUser());
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                case "oldCashes":
                    ar = (JSONArray) jsonObject.get("value");
                    if (ar.size() > 0) {
                        String cashes = jsonObject.get("value").toString();
                        try {
                            ObservableList<Cashes> cashesList = (ObservableList<Cashes>) mapper.readValue(cashes, Cashes.class);
                            cashesList.forEach(element -> {
                                BillDAO.editCashes(element.getAmount(), element.getCurrencyId(), element.getUuidCash(), element.getBillId(), element.getBillUuid(), element.getCurrencyUuid());
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                case "oldCategories":
                    ar = (JSONArray) jsonObject.get("value");
                    if (ar.size() > 0) {
                        String categories = jsonObject.get("value").toString();
                        try {
                            ObservableList<Categories> categoriesList = (ObservableList<Categories>) mapper.readValue(categories, Categories.class);
                            categoriesList.forEach(element -> {
                                CategoryDAO.updateCategoryFromServer(element.getCategoryName(), element.getCostincome(), element.getUuidCategory(), element.getCategoryId());
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                case "oldCurrencies":
                    ar = (JSONArray) jsonObject.get("value");
                    if (ar.size() > 0) {
                        String currencies = jsonObject.get("value").toString();
                        try {
                            ObservableList<Currency> currenciesList = (ObservableList<Currency>) mapper.readValue(currencies, Currency.class);
                            currenciesList.forEach(element -> {
                                CurrencyDAO.editCurrencyFromServer(element.getCurrencyName(), element.getCurrencyShortName(), element.getCurrencyUUID(), element.getCurrencyId());
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                case "oldMeasures":
                    ar = (JSONArray) jsonObject.get("value");
                    if (ar.size() > 0) {
                        String measures = jsonObject.get("value").toString();
                        try {
                            ObservableList<Measures> measuresList = (ObservableList<Measures>) mapper.readValue(measures, Measures.class);
                            measuresList.forEach(element -> {
                                MeasureDAO.updateFromServer(element.getMeasureName(), element.getUuidMeasure(), element.getMeasureId());
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                case "oldCostincomes":
                    ar = (JSONArray) jsonObject.get("value");
                    if (ar.size() > 0) {
                        String costincomes = jsonObject.get("value").toString();
                        try {
                            ObservableList<Costincomes> costincomesList = (ObservableList<Costincomes>) mapper.readValue(costincomes, Costincomes.class);
                            costincomesList.forEach(element -> {
                                CostsIncomesDAO.editCostincomeFromServer(element.getCostincDate(), element.getBillId(), element.getCostincCount(), element.getMeasureId(), element.getCategoryId(), element.getCostincNote(), element.getCostincome(), element.getCostincAmount(), element.getCostincUUID(), element.getBillId());
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                case "oldPlannings":
                    ar = (JSONArray) jsonObject.get("value");
                    if (ar.size() > 0) {
                        String plannings = jsonObject.get("value").toString();
                        try {
                            ObservableList<Planning> planningsList = (ObservableList<Planning>) mapper.readValue(plannings, Planning.class);
                            planningsList.forEach(element -> {
                                PlanningDAO.editPlansFromServer(element.getPlanningDate(), element.getBillId(), element.getMeasureId(), element.getPlanningCount(), element.getCategoryId(), element.getPlanningNote(), element.getPlanningAmount(), element.getPlanningCostincome(), element.getPlanUUID(), element.getPlanStatus(), element.getPlanPeriod(), element.getPlanningId());
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

            }
            logger.info("Received ...." + message);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        return "";
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.info(String.format("Session %s close because of %s", session.getId(), s));
    }

    @Override
    public void onError(Exception e) {

    }

    public void setSession(Session session) {
        this.session = session;
    }
}
