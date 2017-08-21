package entities.lists;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.Categories;
import entities.deserializers.CategoriesDeserializer;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by vellial on 12.09.16.
 */

@JsonDeserialize(using = CategoriesDeserializer.class)
public class CategoriesList {

    private ObservableList<Categories> categories;
    private String codeOp;
    private String numSyn;

    public CategoriesList() {
        this(null, null, null);
    }

    public CategoriesList(ObservableList<Categories> billses, String code, String num) {
        this.categories = billses;
        this.codeOp = code;
        this.numSyn = num;
    }

    public ObservableList<Categories> getCategories() {
        return categories;
    }

    @JsonCreator
    public static CategoriesList Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        CategoriesList module = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            module = mapper.readValue(jsonString, CategoriesList.class);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    public void setCategories(ObservableList<Categories> categories) {
        this.categories = categories;
    }

    public String getCodeOp() {
        return codeOp;
    }

    public void setCodeOp(String codeOp) {
        this.codeOp = codeOp;
    }

    public String getNumSyn() {
        return numSyn;
    }

    public void setNumSyn(String numSyn) {
        this.numSyn = numSyn;
    }
}
