import javax.naming.NamingException;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.SQLException;

import DAO.CommonDAO;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.*;
import javafx.collections.ObservableList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;

/**
 * WebSocket Server Endpoint.
 */
@ServerEndpoint(value = "/end")
public class HomeAccServerEndpoint {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private static long lastSyncDate = 0;

    @OnOpen
    public void onOpen(Session session) {
        logger.info("Connected ... " + session.getId());
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        JSONParser parser = new JSONParser();
        try {
            System.out.println(message);
            Object obj = parser.parse(message);
            JSONObject jsonObject = (JSONObject) obj;
            String codeOperation = jsonObject.get("codeOperation").toString();

            ObjectMapper mapper = new ObjectMapper();
            String username, password, uuidDevice = "";
            int isLogged;
            JSONObject response;
            JSONObject responseWithData = new JSONObject();
            int counter = 0;

            switch (codeOperation) {
                case "register":
                    username = jsonObject.get("username").toString();
                    password = jsonObject.get("pass").toString();
                    isLogged = 0;
                    uuidDevice = jsonObject.get("UUIDDevice").toString();

                    CommonDAO.setSession(session);
                    response = CommonDAO.authorizeUser(username, password, isLogged, uuidDevice);
                    CommonDAO.sendMessage(response);

                    break;
                case "authorize":
                    username = jsonObject.get("username").toString();
                    password = jsonObject.get("pass").toString();
                    isLogged = 1;
                    uuidDevice = jsonObject.get("UUIDDevice").toString();

                    CommonDAO.setSession(session);
                    response = CommonDAO.authorizeUser(username, password, isLogged, uuidDevice);
                    CommonDAO.sendMessage(response);

                    break;
                case "bills" :
                    try {
                        // we get uuidUser and uuidDevice, if it's not first sync.
                        String uuidUser = jsonObject.get("uuidUser").toString();
                        uuidDevice = jsonObject.get("uuidDevice").toString();
                        // we get lastSyncDate;
                        long lsd = Long.valueOf(jsonObject.get("lastSyncDate").toString());
                        // we get data from client to server database
                        ObservableList<Bills> billses = (ObservableList<Bills>) mapper.readValue(message, Bills.class);
                        // we insert or update this data
                        CommonDAO.insertNewBills(billses, uuidDevice, responseWithData);
                        counter += 1;
                        // if lastSyncDate > 0, then we get updates from server to client
                        if (lsd > 0) {
                            CommonDAO.deleteOldBills(billses, uuidDevice, uuidUser);
                            ObservableList<Bills> oldBills = CommonDAO.getOldBillsFromServer(lsd);
                            responseWithData.put("key", "oldBills");
                            responseWithData.put("value", oldBills);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "categories" :
                    try {
                        // we get uuidUser and uuidDevice, if it's not first sync.
                        String uuidUser = jsonObject.get("uuidUser").toString();
                        uuidDevice = jsonObject.get("uuidDevice").toString();
                        long lsd = Long.valueOf(jsonObject.get("lastSyncDate").toString());
                        ObservableList<Categories> categories = (ObservableList<Categories>) mapper.readValue(message, Categories.class);
                        CommonDAO.insertNewCategories(categories, uuidDevice, responseWithData);
                        counter += 1;
                        if (lsd > 0) {
                            CommonDAO.deleteOldCategories(categories, uuidDevice, uuidUser);
                            ObservableList<Categories> oldCategories = CommonDAO.getOldCategoriesFromServer(lsd);
                            responseWithData.put("key", "oldCategories");
                            responseWithData.put("value", oldCategories);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "measures" :
                    try {
                        // we get uuidUser and uuidDevice, if it's not first sync.
                        String uuidUser = jsonObject.get("uuidUser").toString();
                        uuidDevice = jsonObject.get("uuidDevice").toString();
                        long lsd = Long.valueOf(jsonObject.get("lastSyncDate").toString());
                        ObservableList<Measures> measures = (ObservableList<Measures>) mapper.readValue(message, Measures.class);
                        CommonDAO.insertNewMeasures(measures, uuidDevice, responseWithData);
                        counter += 1;
                        if (lsd > 0) {
                            CommonDAO.deleteOldMeasures(measures, uuidUser, uuidDevice);
                            ObservableList<Measures> oldMeasures = CommonDAO.getOldMeasuressFromServer(lsd);
                            responseWithData.put("key", "oldMeasures");
                            responseWithData.put("value", oldMeasures);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "currencies" :
                    try {
                        // we get uuidUser and uuidDevice, if it's not first sync.
                        String uuidUser = jsonObject.get("uuidUser").toString();
                        uuidDevice = jsonObject.get("uuidDevice").toString();
                        // we get lastSyncDate;
                        long lsd = Long.valueOf(jsonObject.get("lastSyncDate").toString());
                        // we get data from client to server database
                        ObservableList<Currencies> currencies = (ObservableList<Currencies>) mapper.readValue(message, Currencies.class);
                        // we insert or update this data
                        CommonDAO.insertNewCurrencies(currencies, uuidDevice, responseWithData);
                        counter += 1;
                        // if lastSyncDate > 0, then we get updates from server to client
                        if (lsd > 0) {
                            CommonDAO.deleteOldCurrencies(currencies, uuidUser, uuidDevice);
                            ObservableList<Currencies> oldCurrencies = CommonDAO.getOldCurrenciesFromServer(lsd);
                            responseWithData.put("key", "oldCurrencies");
                            responseWithData.put("value", oldCurrencies);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "cashes" :
                    try {
                        // we get uuidUser and uuidDevice, if it's not first sync.
                        String uuidUser = jsonObject.get("uuidUser").toString();
                        uuidDevice = jsonObject.get("uuidDevice").toString();
                        long lsd = Long.valueOf(jsonObject.get("lastSyncDate").toString());
                        ObservableList<Cashes> cashes = (ObservableList<Cashes>) mapper.readValue(message, Cashes.class);
                        CommonDAO.insertNewCashes(cashes, uuidDevice, responseWithData);
                        counter += 1;
                        if (lsd > 0) {
                            CommonDAO.deleteOldCashes(cashes, uuidUser, uuidDevice);
                            ObservableList<Cashes> oldCashes = CommonDAO.getOldCashesFromServer(lsd);
                            responseWithData.put("key", "oldCashes");
                            responseWithData.put("value", oldCashes);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "costincomes" :
                    try {
                        // we get uuidUser and uuidDevice, if it's not first sync.
                        String uuidUser = jsonObject.get("uuidUser").toString();
                        uuidDevice = jsonObject.get("uuidDevice").toString();
                        long lsd = Long.valueOf(jsonObject.get("lastSyncDate").toString());
                        ObservableList<Costincomes> costincomes = (ObservableList<Costincomes>) mapper.readValue(message, Costincomes.class);
                        CommonDAO.insertNewCostincomes(costincomes, uuidDevice, responseWithData);
                        counter += 1;
                        if (lsd > 0) {
                            CommonDAO.deleteOldCostincomes(costincomes, uuidUser, uuidDevice);
                            ObservableList<Costincomes> oldCostincomes = CommonDAO.getOldCostincomesFromServer(lsd);
                            responseWithData.put("key", "oldCostincomes");
                            responseWithData.put("value", oldCostincomes);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "plans" :
                    try {
                        // we get uuidUser and uuidDevice, if it's not first sync.
                        String uuidUser = jsonObject.get("uuidUser").toString();
                        uuidDevice = jsonObject.get("uuidDevice").toString();
                        long lsd = Long.valueOf(jsonObject.get("lastSyncDate").toString());
                        ObservableList<Planning> plannings = (ObservableList<Planning>) mapper.readValue(message, Planning.class);
                        CommonDAO.insertNewPlannings(plannings, uuidDevice, responseWithData);
                        counter += 1;
                        if (lsd > 0) {
                            CommonDAO.deleteOldPlans(plannings, uuidUser, uuidDevice);
                            ObservableList<Planning> oldPlanning = CommonDAO.getOldPlanningFromServer(lsd);
                            responseWithData.put("key", "oldPlanning");
                            responseWithData.put("value", oldPlanning);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }

        if (!responseWithData.isEmpty() && counter == 7) {
            responseWithData.put("key", "date");
            responseWithData.put("value", CommonDAO.getDateOfNow());
            CommonDAO.insertLastSyncDate();
            CommonDAO.sendMessage(responseWithData);
        }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return message;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
    }

}
