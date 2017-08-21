package entities.lists;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.Cashes;
import entities.deserializers.CashesDeserializer;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by vellial on 12.09.16.
 */

@JsonDeserialize(using = CashesDeserializer.class)
public class CashesList {

    private ObservableList<Cashes> cashes;
    private String codeOp;
    private String numSyn;

    public CashesList() {
        this(null, null, null);
    }

    public CashesList(ObservableList<Cashes> billses, String code, String num) {
        this.cashes = billses;
        this.codeOp = code;
        this.numSyn = num;
    }

    public ObservableList<Cashes> getCashes() {
        return cashes;
    }

    @JsonCreator
    public static CashesList Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        CashesList module = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            module = mapper.readValue(jsonString, CashesList.class);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    public void setCashes(ObservableList<Cashes> cashes) {
        this.cashes = cashes;
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
