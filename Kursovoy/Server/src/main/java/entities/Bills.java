package entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.deserializers.BillsDeserializer;
import javafx.beans.property.*;

import java.io.IOException;

/**
 * Bills object.
 */
@JsonDeserialize(using = BillsDeserializer.class)
public class Bills {
    private LongProperty date;
    private StringProperty billName;
    private StringProperty note;
    private DoubleProperty startBalance;
    private DoubleProperty cashAmount;
    private IntegerProperty userId;
    private IntegerProperty billId;
    private ObjectProperty<Item> billCurrency;
    private StringProperty uuidBill;
    private StringProperty uuidUser;
    private LongProperty delDate;

    public Bills() {
        this(0, null, null, 0, 0, 0, 0, null, null);
    }

    /**
     * Конструктор для combobox.
     * @param billId - bill id.
     * @param billName - bill name.
     */
    public Bills(int billId, String billName) {
        this.billName = new SimpleStringProperty(billName);
        this.billId = new SimpleIntegerProperty(billId);
    }

    // For parsing bills on server
    public Bills(long date, String billName, String note, double startBalance, int idBill, int idUser, long delDate, String userUUid, String billUUid) {
        this.date = new SimpleLongProperty(date);
        this.billName = new SimpleStringProperty(billName);
        this.note = new SimpleStringProperty(note);
        this.startBalance = new SimpleDoubleProperty(startBalance);
        this.billId = new SimpleIntegerProperty(idBill);
        this.userId = new SimpleIntegerProperty(idUser);
        this.delDate = new SimpleLongProperty(delDate);
        this.cashAmount = new SimpleDoubleProperty();
        this.uuidUser = new SimpleStringProperty(userUUid);
        this.uuidBill = new SimpleStringProperty(billUUid);
    }

    // For reseived data to client
    public Bills(long date, String billName, String note, double startBalance, String uuidBills, String uuidUser) {
        this.date = new SimpleLongProperty(date);
        this.billName = new SimpleStringProperty(billName);
        this.note = new SimpleStringProperty(note);
        this.startBalance = new SimpleDoubleProperty(startBalance);
        this.uuidBill = new SimpleStringProperty(uuidBills);
        this.uuidUser = new SimpleStringProperty(uuidUser);
    }

    @JsonCreator
    public static Bills Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        Bills module = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            module = mapper.readValue(jsonString, Bills.class);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    public String getBillName() {
        return billName.get();
    }

    public StringProperty billNameProperty() {
        return billName;
    }

    public void setBillName(String billName) {
        this.billName.setValue(billName);
    }

    public String getNote() {
        return note.get();
    }

    public StringProperty noteProperty() {
        return note;
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public double getStartBalance() {
        return startBalance.get();
    }

    public DoubleProperty startBalanceProperty() {
        return startBalance;
    }

    public void setStartBalance(double startBalance) {
        this.startBalance.set(startBalance);
    }

    public int getUserId() {
        return userId.get();
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
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

    public double getCashAmount() {
        return cashAmount.get();
    }

    public DoubleProperty cashAmountProperty() {
        return cashAmount;
    }

    public void setCashAmount(double cashAmount) {
        this.cashAmount.set(cashAmount);
    }

//    public Item getBillCurrency() {
//        return billCurrency.get();
//    }
//
//    public ObjectProperty<Item> billCurrencyProperty() {
//        return billCurrency;
//    }
//
//    public void setBillCurrency(Item billCurrency) {
//        this.billCurrency.set(billCurrency);
//    }

    @Override
    public String toString() {
        return getBillName();
    }

    public String getUuidBill() {
        return uuidBill.get();
    }

    public StringProperty uuidBillProperty() {
        return uuidBill;
    }

    public void setUuidBill(String uuidBill) {
        this.uuidBill.set(uuidBill);
    }

    public String getUuidUser() {
        return uuidUser.get();
    }

    public StringProperty uuidUserProperty() {
        return uuidUser;
    }

    public void setUuidUser(String uuidUser) {
        this.uuidUser.set(uuidUser);
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

    public long getDate() {
        return date.get();
    }

    public LongProperty dateProperty() {
        return date;
    }

    public void setDate(long date) {
        this.date.set(date);
    }
}
