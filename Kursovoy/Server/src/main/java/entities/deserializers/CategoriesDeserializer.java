package entities.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import entities.Categories;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by vellial on 16.09.16.
 */
public class CategoriesDeserializer extends JsonDeserializer<ObservableList<Categories>> {
    public CategoriesDeserializer() {
        this(Categories.class);
    }

    public CategoriesDeserializer(Class<Categories> t) {

    }

    @Override
    public ObservableList<Categories> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObservableList<Categories> categoriesList = FXCollections.observableArrayList();

        try {

            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            JsonNode categories = node.get("categories");

            ArrayNode slaidsNode = (ArrayNode) categories;
            Iterator<JsonNode> slaidsIterator = slaidsNode.elements();
            while (slaidsIterator.hasNext()) {
                Categories category = new Categories();
                JsonNode slaidNode = slaidsIterator.next();
                int categoryId = slaidNode.get("categoryId").intValue();
                long delDate = slaidNode.get("deletedDate") != null ? slaidNode.get("deletedDate").numberValue().longValue() : 0;
                boolean costincome = slaidNode.get("costincome").booleanValue();
                String categoryName = slaidNode.get("categoryName").asText();
                String uuidCategory = slaidNode.get("uuidCategory").isNull() ? "" : slaidNode.get("uuidCategory").asText();

                category.setCategoryId(categoryId);
                category.setCategoryName(categoryName);
                category.setCostincome(costincome);
                category.setDeletedDate(delDate);
                category.setUuidCategory(uuidCategory);
                categoriesList.add(category);
            }

            return categoriesList;
        } catch (Exception ez) {
            ez.printStackTrace();
        }

        return categoriesList;

    }

}
