package entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.deserializers.CurrenciesDeserializer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Currencies object.
 */
@JsonDeserialize(using = CurrenciesDeserializer.class)
public class Currencies {

    private StringProperty currencyName;
    private IntegerProperty currencyId;
    private StringProperty currencyShortName;
    private String currencyUUID;
    private long deletedDate;

    /**
     * Constructor.
     */
    public Currencies() {
        this(null, 0, null, null, 0);
    }

    /**
     * Constructor.
     */
    public Currencies(String name, int id, String shortName, String uuid, long delDate) {
        this.currencyName = new SimpleStringProperty(name);
        this.currencyId = new SimpleIntegerProperty(id);
        this.currencyShortName = new SimpleStringProperty(shortName);
        this.currencyUUID = uuid;
        this.deletedDate = delDate;
    }

    public Currencies(String uuidCurrency, String currencyName, String nameShort) {
        this.currencyUUID = uuidCurrency;
        this.currencyName = new SimpleStringProperty(currencyName);
        this.currencyShortName = new SimpleStringProperty(nameShort);
    }

    /**
     * Get short name of currency.
     * @return short name.
     */
    public String getCurrencyShortName() {
        return currencyShortName.get();
    }

    /**
     * Set short name of currency.
     * @param currencyShortName new short name for currency
     */
    public void setCurrencyShortName(String currencyShortName) {
        this.currencyShortName.set(currencyShortName);
    }

    /**
     * Get currency name.
     * @return currency name.
     */
    public String getCurrencyName() {
        return currencyName.get();
    }

    /**
     * Set currency name.
     * @param currencyName new currency name.
     */
    public void setCurrencyName(String currencyName) {
        this.currencyName.set(currencyName);
    }

    public int getCurrencyId() {
        return currencyId.get();
    }

    public IntegerProperty currencyIdProperty() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId.set(currencyId);
    }

    public String getCurrencyUUID() {
        return currencyUUID;
    }

    public void setCurrencyUUID(String currencyUUID) {
        this.currencyUUID = currencyUUID;
    }

    public long getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate = deletedDate;
    }
}
