package entities.lists;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.Measures;
import entities.deserializers.MeasuresDeserializer;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by vellial on 12.09.16.
 */

@JsonDeserialize(using = MeasuresDeserializer.class)
public class MeasuresList {

    private ObservableList<Measures> measures;
    private String codeOp;
    private String numSyn;

    public MeasuresList() {
        this(null, null, null);
    }

    public MeasuresList(ObservableList<Measures> billses, String code, String num) {
        this.measures = billses;
        this.codeOp = code;
        this.numSyn = num;
    }

    public ObservableList<Measures> getMeasures() {
        return measures;
    }

    @JsonCreator
    public static MeasuresList Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        MeasuresList module = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            module = mapper.readValue(jsonString, MeasuresList.class);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    public void setMeasures(ObservableList<Measures> measures) {
        this.measures = measures;
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
