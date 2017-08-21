package entities.lists;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.Currencies;
import entities.deserializers.CurrenciesDeserializer;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by vellial on 12.09.16.
 */

@JsonDeserialize(using = CurrenciesDeserializer.class)
public class CurrenciesList {

    private ObservableList<Currencies> currencies;
    private String codeOp;
    private String numSyn;

    public CurrenciesList() {
        this(null, null, null);
    }

    public CurrenciesList(ObservableList<Currencies> billses, String code, String num) {
        this.currencies = billses;
        this.codeOp = code;
        this.numSyn = num;
    }

    public ObservableList<Currencies> getCurrencies() {
        return currencies;
    }

    @JsonCreator
    public static CurrenciesList Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        CurrenciesList module = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            module = mapper.readValue(jsonString, CurrenciesList.class);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    public void setCurrencies(ObservableList<Currencies> currencies) {
        this.currencies = currencies;
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
