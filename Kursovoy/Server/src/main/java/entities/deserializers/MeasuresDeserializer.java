package entities.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import entities.Costincomes;
import entities.Measures;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by vellial on 16.09.16.
 */
public class MeasuresDeserializer extends JsonDeserializer<ObservableList<Measures>> {
    public MeasuresDeserializer() {
        this(Measures.class);
    }

    public MeasuresDeserializer(Class<Measures> t) {

    }

    @Override
    public ObservableList<Measures> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObservableList<Measures> measuresList = FXCollections.observableArrayList();

        try {

            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            JsonNode measures = node.get("measures");

            ArrayNode slaidsNode = (ArrayNode) measures;
            Iterator<JsonNode> slaidsIterator = slaidsNode.elements();
            while (slaidsIterator.hasNext()) {
                Measures measure = new Measures();
                JsonNode slaidNode = slaidsIterator.next();
                int measureId = slaidNode.get("measureId").numberValue().intValue();
                String measureName = slaidNode.get("measureName").asText();
                String uuidMeasure = slaidNode.get("uuidMeasure").isNull() ? "" : slaidNode.get("uuidMeasure").asText();
                long delDate = slaidNode.get("deletedDate") != null ? slaidNode.get("deletedDate").numberValue().longValue() : 0;

                measure.setMeasureId(measureId);
                measure.setMeasureName(measureName);
                measure.setUuidMeasure(uuidMeasure);
                measure.setDeletedDate(delDate);
                measuresList.add(measure);
            }

            return measuresList;
        } catch (Exception ez) {
            ez.printStackTrace();
        }

        return measuresList;

    }

}
