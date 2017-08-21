package homeaccApp.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import homeaccApp.currencies.Currency;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by vellial on 16.09.16.
 */
public class CurrenciesDeserializer extends JsonDeserializer<ObservableList<Currency>> {
    public CurrenciesDeserializer() {
        this(Currency.class);
    }

    public CurrenciesDeserializer(Class<Currency> t) {

    }

    @Override
    public ObservableList<Currency> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObservableList<Currency> currenciesList = FXCollections.observableArrayList();

        try {

            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            JsonNode currencies = node.get("currencies");

            ArrayNode slaidsNode = (ArrayNode) currencies;
            Iterator<JsonNode> slaidsIterator = slaidsNode.elements();
            while (slaidsIterator.hasNext()) {
                Currency currency = new Currency();
                JsonNode slaidNode = slaidsIterator.next();
                String curName = slaidNode.get("currencyName").asText();
                String curShortName = slaidNode.get("currencyShortName").asText();
                long delDate = slaidNode.get("deletedDate") != null ? slaidNode.get("deletedDate").numberValue().longValue() : 0;
                String curUUID = slaidNode.get("currencyUUID").isNull() ? "" : slaidNode.get("currencyUUID").asText();
                int curId = slaidNode.get("currencyId").numberValue().intValue();

                currency.setCurrencyId(curId);
                currency.setCurrencyName(curName);
                currency.setCurrencyShortName(curShortName);
                currency.setDeletedDate(delDate);
                currency.setCurrencyUUID(curUUID);
                currenciesList.add(currency);
            }

            return currenciesList;
        } catch (Exception ez) {
            ez.printStackTrace();
        }

        return currenciesList;

    }

}
