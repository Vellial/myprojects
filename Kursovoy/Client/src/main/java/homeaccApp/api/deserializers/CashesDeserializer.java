package homeaccApp.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import homeaccApp.cashes.Cashes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by vellial on 16.09.16.
 */
public class CashesDeserializer extends JsonDeserializer<ObservableList<Cashes>> {
    public CashesDeserializer() {
        this(Cashes.class);
    }

    public CashesDeserializer(Class<Cashes> t) {

    }

    @Override
    public ObservableList<Cashes> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObservableList<Cashes> cashesList = FXCollections.observableArrayList();

        try {

            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            JsonNode cashes = node.get("cashes");

            ArrayNode slaidsNode = (ArrayNode) cashes;
            Iterator<JsonNode> slaidsIterator = slaidsNode.elements();
            while (slaidsIterator.hasNext()) {
                Cashes cash = new Cashes();
                JsonNode slaidNode = slaidsIterator.next();
                long delDate = slaidNode.get("delDate") != null ? slaidNode.get("delDate").numberValue().longValue() : 0;
                double cashAmount = slaidNode.get("amount").numberValue().doubleValue();
                int billId = slaidNode.get("billId").numberValue().intValue();
                int currencyId = slaidNode.get("currencyId").numberValue().intValue();
                int cashId = slaidNode.get("cashId").numberValue().intValue();

                String uuidCash = slaidNode.get("uuidCash").isNull() ? "" : slaidNode.get("uuidCash").asText();
//                String uuidUser = slaidNode.get("uuidUser") != null ? slaidNode.get("uuidUser").asText() : null;
                String uuidBill = slaidNode.get("uuidBill").isNull() ? "" : slaidNode.get("uuidBill").asText();
                String uuidCur = slaidNode.get("uuidCurrency").isNull() ? "" : slaidNode.get("uuidCurrency").asText();

                cash.setDelDate(delDate);
                cash.setAmount(cashAmount);
                cash.setBillId(billId);
                cash.setCashId(cashId);
                cash.setCurrencyId(currencyId);
                cash.setUuidCash(uuidCash);
                cash.setBillUuid(uuidBill);
                cash.setCurrencyUuid(uuidCur);

                cashesList.add(cash);
            }

            return cashesList;
        } catch (Exception ez) {
            ez.printStackTrace();
        }

        return cashesList;

    }

}
