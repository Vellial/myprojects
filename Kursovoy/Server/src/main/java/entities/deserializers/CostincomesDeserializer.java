package entities.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import entities.Costincomes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by vellial on 16.09.16.
 */
public class CostincomesDeserializer extends JsonDeserializer<ObservableList<Costincomes>> {
    public CostincomesDeserializer() {
        this(Costincomes.class);
    }

    public CostincomesDeserializer(Class<Costincomes> t) {

    }

    @Override
    public ObservableList<Costincomes> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObservableList<Costincomes> costincomesList = FXCollections.observableArrayList();

        try {

            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            JsonNode costincomes = node.get("costincomes");

            ArrayNode slaidsNode = (ArrayNode) costincomes;
            Iterator<JsonNode> slaidsIterator = slaidsNode.elements();
            while (slaidsIterator.hasNext()) {
                Costincomes costincome = new Costincomes();
                JsonNode slaidNode = slaidsIterator.next();
                boolean costinc = slaidNode.get("costincome").booleanValue();
                int costincDate = slaidNode.get("costincDate").numberValue().intValue();
                String note = slaidNode.get("costincNote").asText();
                int count = slaidNode.get("costincCount").asInt();
                double amount = slaidNode.get("costincAmount").asDouble();
                int costincId = slaidNode.get("costincId").asInt();
                String costincUUID = slaidNode.get("costincUUID").asText();
                String costincUUIDText = (costincUUID.equals("")) ? "" : costincUUID;
                long delDate = slaidNode.get("deletedDate") != null ? slaidNode.get("deletedDate").numberValue().longValue() : 0;

                costincome.setCostincId(costincId);
                costincome.setCostincNote(note);
                costincome.setCostincAmount(amount);
                costincome.setDeletedDate(delDate);
//                costincome.setBillId();
//                costincome.setCategoryId();
                costincome.setCostincCount(count);
                costincome.setCostincDate(costincDate);
                costincome.setCostincome(costinc);
                costincome.setCostincUUID(costincUUIDText);
                costincomesList.add(costincome);
            }

            return costincomesList;
        } catch (Exception ez) {
            ez.printStackTrace();
        }

        return costincomesList;

    }

}
