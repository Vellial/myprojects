package entities.lists;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.Bills;
import entities.deserializers.BillsDeserializer;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by vellial on 12.09.16.
 */

@JsonDeserialize(using = BillsDeserializer.class)
public class BillsList {

    private ObservableList<Bills> bills;
//    private String codeOp;
//    private String numSyn;

    public BillsList() {
        this(null);
    }

    public BillsList(ObservableList<Bills> billses) {
        this.bills = billses;

    }

    public ObservableList<Bills> getBills() {
        return bills;
    }

    @JsonCreator
    public static BillsList Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        BillsList module = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            module = mapper.readValue(jsonString, BillsList.class);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    public void setBills(ObservableList<Bills> bills) {
        this.bills = bills;
    }
//
//    public String getCodeOp() {
//        return codeOp;
//    }
//
//    public void setCodeOp(String codeOp) {
//        this.codeOp = codeOp;
//    }
//
//    public String getNumSyn() {
//        return numSyn;
//    }
//
//    public void setNumSyn(String numSyn) {
//        this.numSyn = numSyn;
//    }
}
