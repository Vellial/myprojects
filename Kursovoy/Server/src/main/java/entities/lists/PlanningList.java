package entities.lists;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.Planning;
import entities.deserializers.PlanningDeserializer;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by vellial on 12.09.16.
 */

@JsonDeserialize(using = PlanningDeserializer.class)
public class PlanningList {

    private ObservableList<Planning> plans;
    private String codeOp;
    private String numSyn;

    public PlanningList() {
        this(null, null, null);
    }

    public PlanningList(ObservableList<Planning> billses, String code, String num) {
        this.plans = billses;
        this.codeOp = code;
        this.numSyn = num;
    }

    public ObservableList<Planning> getPlans() {
        return plans;
    }

    @JsonCreator
    public static PlanningList Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        PlanningList module = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            module = mapper.readValue(jsonString, PlanningList.class);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    public void setPlans(ObservableList<Planning> plans) {
        this.plans = plans;
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
