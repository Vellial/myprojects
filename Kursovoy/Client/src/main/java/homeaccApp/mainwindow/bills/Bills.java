package homeaccApp.mainwindow.bills;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import homeaccApp.api.Item;
import homeaccApp.api.deserializers.BillsDeserializer;
import javafx.beans.property.*;

import java.time.LocalDate;

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
    private StringProperty uuidBill;
    private StringProperty uuidUser;
    private LongProperty delDate;
    private ObjectProperty<Item> billCurrency;

    public Bills() {
        this(0, null, null, 0, 0, 0, 0, null);
    }

    /**
     * Конструктор с начальными данными.
     *
     */
    public Bills(long cdate, String cbillName, String cnote, Item ccurrency, double cstartBalance, double ccashAmount, int cuserId, int cbillId) {
        this.date = new SimpleLongProperty(cdate);
        this.billName = new SimpleStringProperty(cbillName);
        this.note = new SimpleStringProperty(cnote);
        this.startBalance = new SimpleDoubleProperty(cstartBalance);
        this.cashAmount = new SimpleDoubleProperty(ccashAmount);
        this.billCurrency = new SimpleObjectProperty<Item>(ccurrency);
        this.userId = new SimpleIntegerProperty(cuserId);
        this.billId = new SimpleIntegerProperty(cbillId);
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

    public Bills(long date, String billName, String note, double startBalance, int idBill, int idUser, long delDate, String uuid) {
        this.date = new SimpleLongProperty(date);
        this.billName = new SimpleStringProperty(billName);
        this.note = new SimpleStringProperty(note);
        this.startBalance = new SimpleDoubleProperty(startBalance);
        this.billId = new SimpleIntegerProperty(idBill);
        this.userId = new SimpleIntegerProperty(idUser);
        this.delDate = new SimpleLongProperty(delDate);
        this.cashAmount = new SimpleDoubleProperty();
        this.billCurrency = new SimpleObjectProperty();
        this.uuidUser = new SimpleStringProperty();
        this.uuidBill = uuid != "" ? new SimpleStringProperty(uuid) : new SimpleStringProperty();
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

    public Item getBillCurrency() {
        return billCurrency.get();
    }

    public ObjectProperty<Item> billCurrencyProperty() {
        return billCurrency;
    }

    public void setBillCurrency(Item billCurrency) {
        this.billCurrency.set(billCurrency);
    }

    @Override
    public String toString() {
        return getBillName();
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
}
