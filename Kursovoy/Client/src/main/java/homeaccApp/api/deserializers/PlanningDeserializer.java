package homeaccApp.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import homeaccApp.mainwindow.planning.Planning;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by vellial on 16.09.16.
 */
public class PlanningDeserializer extends JsonDeserializer<ObservableList<Planning>> {
    public PlanningDeserializer() {
        this(Planning.class);
    }

    public PlanningDeserializer(Class<Planning> t) {

    }

    @Override
    public ObservableList<Planning> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObservableList<Planning> plansList = FXCollections.observableArrayList();

        try {

            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            JsonNode plannings = node.get("plannings");

            ArrayNode slaidsNode = (ArrayNode) plannings;
            Iterator<JsonNode> slaidsIterator = slaidsNode.elements();
            while (slaidsIterator.hasNext()) {
                Planning plan = new Planning();
                JsonNode slaidNode = slaidsIterator.next();
                int measureId = slaidNode.get("measureId").intValue();
                int billId = slaidNode.get("billId").intValue();
                int categoryId = slaidNode.get("categoryId").intValue();
                long locDate = slaidNode.get("planningDate").longValue();
                String note = slaidNode.get("planningNote").asText();
                int count = slaidNode.get("planningCount").intValue();
                double amount = slaidNode.get("planningAmount").doubleValue();
                int id = slaidNode.get("planningId").intValue();
                boolean costincome = slaidNode.get("planningCostincome").booleanValue();
                int userId = slaidNode.get("userId").intValue();
                String planStatus = slaidNode.get("planStatus").asText();
                String planPeriod = slaidNode.get("planPeriod").asText();
                long delDate = slaidNode.get("deletedDate") != null ? slaidNode.get("deletedDate").numberValue().longValue() : 0;

                String planUUID = slaidNode.get("planUUID").isNull() ? "" : slaidNode.get("planUUID").asText();
                String measureUUID = slaidNode.get("uuidMeasure").isNull() ? "" : slaidNode.get("uuidMeasure").asText();
                String categoryUUID = slaidNode.get("uuidCategory").isNull() ? "" : slaidNode.get("uuidCategory").asText();
                String billUUID = slaidNode.get("uuidBill").isNull() ? "" : slaidNode.get("uuidBill").asText();

                plan.setMeasureId(measureId);
                plan.setBillId(billId);
                plan.setCategoryId(categoryId);
                plan.setPlanningDate(locDate);
                plan.setPlanningNote(note);
                plan.setPlanningCount(count);
                plan.setPlanningAmount(amount);
                plan.setPlanningId(id);
                plan.setPlanningCostincome(costincome);
                plan.setUserId(userId);
                plan.setPlanStatus(planStatus);
                plan.setPlanPeriod(planPeriod);
                plan.setDeletedDate(delDate);
                plan.setPlanUUID(planUUID);
                plan.setPlanUUID(planUUID);
                plan.setMeasureUUID(measureUUID);
                plan.setCategoryUUID(categoryUUID);
                plan.setBillUUID(billUUID);

                plansList.add(plan);
            }

            return plansList;
        } catch (Exception ez) {
            ez.printStackTrace();
        }

        return plansList;

    }

}
