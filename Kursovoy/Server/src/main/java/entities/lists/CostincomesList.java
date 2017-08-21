package entities.lists;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.Costincomes;
import entities.deserializers.CostincomesDeserializer;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by vellial on 12.09.16.
 */

@JsonDeserialize(using = CostincomesDeserializer.class)
public class CostincomesList {

    private ObservableList<Costincomes> costincomes;
    private String codeOp;
    private String numSyn;

    public CostincomesList() {
        this(null, null, null);
    }

    public CostincomesList(ObservableList<Costincomes> billses, String code, String num) {
        this.costincomes = billses;
        this.codeOp = code;
        this.numSyn = num;
    }

    public ObservableList<Costincomes> getCostincomes() {
        return costincomes;
    }

    @JsonCreator
    public static CostincomesList Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        CostincomesList module = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            module = mapper.readValue(jsonString, CostincomesList.class);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    public void setCostincomes(ObservableList<Costincomes> costincomes) {
        this.costincomes = costincomes;
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
