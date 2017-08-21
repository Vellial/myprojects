package homeaccApp.currencies;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import homeaccApp.api.deserializers.CurrenciesDeserializer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Currency object.
 */
@JsonDeserialize(using = CurrenciesDeserializer.class)
public class Currency {

    private StringProperty currencyName;
    private IntegerProperty currencyId;
    private StringProperty currencyShortName;
    private StringProperty currencyUUID;
    private long deletedDate;

    /**
     * Constructor.
     */
    public Currency() {
        this(null, 0, null, null, 0);
    }

    /**
     * Constructor.
     */
    public Currency(String name, int id, String shortName, String uuid, long delDate) {
        this.currencyName = new SimpleStringProperty(name);
        this.currencyId = new SimpleIntegerProperty(id);
        this.currencyShortName = new SimpleStringProperty(shortName);
        this.currencyUUID = new SimpleStringProperty(uuid);
        this.deletedDate = delDate;
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

    public long getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate = deletedDate;
    }

    public String getCurrencyUUID() {
        return currencyUUID.get();
    }

    public StringProperty currencyUUIDProperty() {
        return currencyUUID;
    }

    public void setCurrencyUUID(String currencyUUID) {
        this.currencyUUID.set(currencyUUID);
    }
}
