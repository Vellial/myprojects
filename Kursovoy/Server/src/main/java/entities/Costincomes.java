package entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.deserializers.CostincomesDeserializer;
import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Costincomes object.
 */
@JsonDeserialize(using = CostincomesDeserializer.class)
public class Costincomes {
    private SimpleBooleanProperty costincome;
    private LongProperty costincDate;
    private ObjectProperty<Bills> bill;
    private StringProperty costincBillName;
    private IntegerProperty costincBillId;
    private StringProperty costincNote;
    private IntegerProperty costincCount;
    private ObjectProperty<Measures> measure;
    private ObjectProperty<Categories> category;
    private DoubleProperty costincAmount;
    private IntegerProperty costincId;
    private StringProperty costincUUID;
    private IntegerProperty billId;
    private IntegerProperty measureId;
    private IntegerProperty categoryId;
    private IntegerProperty userId;
    private LongProperty deletedDate;
    private StringProperty uuidBills;
    private StringProperty uuidMeasure;
    private StringProperty uuidCategory;

    public Costincomes() {
        this(0, 0, 0, null, 0, false, 0, 0, 0,0,null);
    }

    public Costincomes(int moneyTurnId, long date, int count, String note, double amount, boolean costIncome, int measureId, int billId, int categoryId, long deletedDate, String uuid) {
        this.costincDate = new SimpleLongProperty(date);
        this.billId = new SimpleIntegerProperty(billId);
        this.costincNote = new SimpleStringProperty(note);
        this.costincCount = new SimpleIntegerProperty(count);
        this.measureId = new SimpleIntegerProperty(measureId);
        this.costincAmount = new SimpleDoubleProperty(amount);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.costincId = new SimpleIntegerProperty(moneyTurnId);
        this.costincome = new SimpleBooleanProperty(costIncome);
        this.deletedDate = new SimpleLongProperty(deletedDate);
        this.costincUUID = new SimpleStringProperty(uuid);
    }

    public Costincomes(String uuidMoneyTurn, long date, String uuidBills, int count, String uuidMeasure, String note, boolean costIntcome, double amount, String uuidCategory) {
        this.costincUUID = new SimpleStringProperty(uuidMoneyTurn);
        this.costincDate = new SimpleLongProperty(date);
        this.costincCount = new SimpleIntegerProperty(count);
        this.costincNote = new SimpleStringProperty(note);
        this.costincome = new SimpleBooleanProperty(costIntcome);
        this.costincAmount = new SimpleDoubleProperty(amount);
        this.uuidBills = new SimpleStringProperty(uuidBills);
        this.uuidMeasure = new SimpleStringProperty(uuidMeasure);
        this.uuidCategory = new SimpleStringProperty(uuidCategory);
    }


    public int getCostincCount() {
        return costincCount.get();
    }

    public IntegerProperty costincCountProperty() {
        return costincCount;
    }

    public void setCostincCount(int costincCount) {
        this.costincCount.set(costincCount);
    }

    public String getCostincNote() {
        return costincNote.get();
    }

    public StringProperty costincNoteProperty() {
        return costincNote;
    }

    public void setCostincNote(String costincNote) {
        this.costincNote.set(costincNote);
    }

    public double getCostincAmount() {
        return costincAmount.get();
    }

    public DoubleProperty costincAmountProperty() {
        return costincAmount;
    }

    public void setCostincAmount(double costincAmount) {
        this.costincAmount.set(costincAmount);
    }

    public int getCostincId() {
        return costincId.get();
    }

    public IntegerProperty costincIdProperty() {
        return costincId;
    }

    public void setCostincId(int costincId) {
        this.costincId.set(costincId);
    }

    public long getDeletedDate() {
        return deletedDate.get();
    }

    public LongProperty deletedDateProperty() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate.set(deletedDate);
    }

    public boolean isCostincome() {
        return costincome.get();
    }

    public SimpleBooleanProperty costincomeProperty() {
        return costincome;
    }

    public void setCostincome(boolean costincome) {
        this.costincome.set(costincome);
    }

    public long getCostincDate() {
        return costincDate.get();
    }

    public LongProperty costincDateProperty() {
        return costincDate;
    }

    public void setCostincDate(long costincDate) {
        this.costincDate.set(costincDate);
    }

    public String getCostincUUID() {
        return costincUUID.get();
    }

    public StringProperty costincUUIDProperty() {
        return costincUUID;
    }

    public void setCostincUUID(String costincUUID) {
        this.costincUUID.set(costincUUID);
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

    public int getMeasureId() {
        return measureId.get();
    }

    public IntegerProperty measureIdProperty() {
        return measureId;
    }

    public void setMeasureId(int measureId) {
        this.measureId.set(measureId);
    }

    public int getCategoryId() {
        return categoryId.get();
    }

    public IntegerProperty categoryIdProperty() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId.set(categoryId);
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

    public String getUuidBills() {
        return uuidBills.get();
    }

    public StringProperty uuidBillsProperty() {
        return uuidBills;
    }

    public void setUuidBills(String uuidBills) {
        this.uuidBills.set(uuidBills);
    }

    public String getUuidMeasure() {
        return uuidMeasure.get();
    }

    public StringProperty uuidMeasureProperty() {
        return uuidMeasure;
    }

    public void setUuidMeasure(String uuidMeasure) {
        this.uuidMeasure.set(uuidMeasure);
    }

    public String getUuidCategory() {
        return uuidCategory.get();
    }

    public StringProperty uuidCategoryProperty() {
        return uuidCategory;
    }

    public void setUuidCategory(String uuidCategory) {
        this.uuidCategory.set(uuidCategory);
    }
}
