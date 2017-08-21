package entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.deserializers.CashesDeserializer;
import javafx.beans.property.*;

/**
 * Cashes for bills
 */
@JsonDeserialize(using = CashesDeserializer.class)
public class Cashes {
    private IntegerProperty cashId;
    private IntegerProperty billId;
    private IntegerProperty currencyId;
    private DoubleProperty amount;
    private LongProperty delDate;
    private StringProperty uuidCash;

    public Cashes() {
        this(0,0,0,0,0,null);
    }

    public Cashes(int cash, int bill, int cur, double am, long delDate, String uuid) {
        this.billId = new SimpleIntegerProperty(bill);
        this.cashId = new SimpleIntegerProperty(cash);
        this.currencyId = new SimpleIntegerProperty(cur);
        this.amount = new SimpleDoubleProperty(am);
        this.delDate = new SimpleLongProperty(delDate);
        this.uuidCash = new SimpleStringProperty(uuid);
    }

    public int getCashId() {
        return cashId.get();
    }

    public IntegerProperty cashIdProperty() {
        return cashId;
    }

    public void setCashId(int cashId) {
        this.cashId.set(cashId);
    }

    public int getBillId() {
        return billId.get();
    }

    public IntegerProperty billIdProperty() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId.set(billId);
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

    public double getAmount() {
        return amount.get();
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public long getDelDate() {
        return delDate.get();
    }

    public LongProperty delDateProperty() {
        return delDate;
    }

    public void setDelDate(long delDate) {
        this.delDate.set(delDate);
    }

    public String getUuidCash() {
        return uuidCash.get();
    }

    public StringProperty uuidCashProperty() {
        return uuidCash;
    }

    public void setUuidCash(String uuidCash) {
        this.uuidCash.set(uuidCash);
    }
}
