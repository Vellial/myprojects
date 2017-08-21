package homeaccApp.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import homeaccApp.mainwindow.bills.Bills;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by vellial on 16.09.16.
 */
public class BillsDeserializer extends JsonDeserializer<ObservableList<Bills>> {
    public BillsDeserializer() {
        this(Bills.class);
    }

    public BillsDeserializer(Class<Bills> t) {

    }

    @Override
    public ObservableList<Bills> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObservableList<Bills> billses = FXCollections.observableArrayList();

        try {

            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            JsonNode bills = node.get("bills");

            ArrayNode slaidsNode = (ArrayNode) bills;
            Iterator<JsonNode> slaidsIterator = slaidsNode.elements();
            while (slaidsIterator.hasNext()) {
                Bills bill = new Bills();
                JsonNode slaidNode = slaidsIterator.next();
                long locDate = slaidNode.get("date").numberValue().longValue();
                long delDate = slaidNode.get("deletedDate") != null ? slaidNode.get("deletedDate").numberValue().longValue() : 0;
                double stBalance = slaidNode.get("startBalance").numberValue().doubleValue();
                double cashAmount = slaidNode.get("cashAmount").numberValue().doubleValue();
                int userId = slaidNode.get("userId").numberValue().intValue();
                int billId = slaidNode.get("billId").numberValue().intValue();
                String note = slaidNode.get("note").asText();
                String billName = slaidNode.get("billName").asText();
                String uuidBill = slaidNode.get("uuidBill") != null ? slaidNode.get("uuidBill").asText() : "";
                String uuidUser = slaidNode.get("uuidUser") != null ? slaidNode.get("uuidUser").asText() : "";

                bill.setDate(locDate);
                bill.setBillName(billName);
                bill.setNote(note);
                bill.setStartBalance(stBalance);
                bill.setCashAmount(cashAmount);
                bill.setUserId(userId);
                bill.setUuidBill(uuidBill);
                bill.setUuidUser(uuidUser);
                bill.setDelDate(delDate);
                bill.setBillId(billId);
                billses.add(bill);
            }

            return billses;
        } catch (Exception ez) {
            ez.printStackTrace();
        }

        return billses;

    }

}
