package homeaccApp.mainwindow.costsincomes;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import homeaccApp.api.deserializers.CostincomesDeserializer;
import javafx.beans.property.*;
import homeaccApp.categories.Categories;
import homeaccApp.mainwindow.bills.Bills;
import homeaccApp.measures.Measures;

import java.time.LocalDate;

/**
 * Costincomes object.
 */
@JsonDeserialize (using = CostincomesDeserializer.class)
public class Costincomes {
    private SimpleBooleanProperty costincome;
    private LongProperty costincDate;
    private ObjectProperty<Bills> costincBill;
    private StringProperty costincBillName;
    private IntegerProperty costincBillId;
    private StringProperty costincNote;
    private IntegerProperty costincCount;
    private ObjectProperty<Measures> costincMeasure;
    private ObjectProperty<Categories> costincCategory;
    private DoubleProperty costincAmount;
    private IntegerProperty costincId;
    private IntegerProperty billId;
    private IntegerProperty measureId;
    private IntegerProperty categoryId;
//    private IntegerProperty userId;
    private LongProperty deletedDate;
    private StringProperty costincUUID;
    private StringProperty uuidBill;
    private StringProperty uuidMeasure;
    private StringProperty uuidCategory;

    public Costincomes() {
        this(0, 0, null, null, null, null, 0, 0, false);
    }

    /**
     * Конструктор с некоторыми начальными данными.
     *
     */
    public Costincomes(int cId, long cDate, Bills bills, String cNote, Measures cMeasure, Categories cCategory, int cCount, double cAmount, boolean cCostincome) {
        this.costincDate = new SimpleLongProperty(cDate);
        this.costincBill = new SimpleObjectProperty<Bills>(bills);
        this.costincNote = new SimpleStringProperty(cNote);
        this.costincCount = new SimpleIntegerProperty(cCount);
        this.costincMeasure = new SimpleObjectProperty<Measures>(cMeasure);
        this.costincAmount = new SimpleDoubleProperty(cAmount);
        this.costincCategory = new SimpleObjectProperty<Categories>(cCategory);
        this.costincId = new SimpleIntegerProperty(cId);
        this.costincBillName = bills == null ? new SimpleStringProperty() : new SimpleStringProperty(bills.getBillName());
        this.costincBillId = bills == null ? new SimpleIntegerProperty() : new SimpleIntegerProperty(bills.getBillId());
        this.costincome = new SimpleBooleanProperty(cCostincome);
    }

    /**
     * Constructor for syncronization
     * @param moneyTurnId
     * @param date
     * @param count
     * @param note
     * @param amount
     * @param costIncome
     * @param measureId
     * @param billId
     * @param categoryId
     * @param deletedDate
     * @param uuid
     */
    public Costincomes(int moneyTurnId, long date, int count, String note, double amount, boolean costIncome, int measureId, int billId, int categoryId, long deletedDate, String uuid) {
        this.costincDate = new SimpleLongProperty(date);
        this.costincNote = new SimpleStringProperty(note);
        this.costincCount = new SimpleIntegerProperty(count);
        this.measureId = new SimpleIntegerProperty(measureId);
        this.costincAmount = new SimpleDoubleProperty(amount);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.costincId = new SimpleIntegerProperty(moneyTurnId);
        this.costincome = new SimpleBooleanProperty(costIncome);
        this.deletedDate = new SimpleLongProperty(deletedDate);
        this.costincBill = new SimpleObjectProperty<>(new Bills());
        this.costincBillName = new SimpleStringProperty();
        this.costincBillId = new SimpleIntegerProperty(billId);
        this.billId = new SimpleIntegerProperty(billId);
        this.costincMeasure = new SimpleObjectProperty<>(new Measures());
        this.costincCategory = new SimpleObjectProperty<>(new Categories());
        this.costincUUID = new SimpleStringProperty(uuid);
    }

    public Costincomes(String uuidMoneyTurn, long date, String uuidBills, int count, String uuidMeasure, String note, boolean costIntcome, double amount, String uuidCategory, long deletedDate) {
        this.costincUUID = new SimpleStringProperty(uuidMoneyTurn);
        this.costincDate = new SimpleLongProperty(date);
        this.costincCount = new SimpleIntegerProperty(count);
        this.costincNote = new SimpleStringProperty(note);
        this.costincome = new SimpleBooleanProperty(costIntcome);
        this.costincAmount = new SimpleDoubleProperty(amount);
        this.uuidBill = new SimpleStringProperty(uuidBills);
        this.uuidMeasure = new SimpleStringProperty(uuidMeasure);
        this.uuidCategory = new SimpleStringProperty(uuidCategory);
        this.deletedDate = new SimpleLongProperty(deletedDate);
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

    public Bills getCostincBill() {
        return costincBill.get();
    }

    public ObjectProperty<Bills> costincBillProperty() {
        return costincBill;
    }

    public void setCostincBill(Bills costincBill) {
        this.costincBill.set(costincBill);
    }

    public Measures getCostincMeasure() {
        return costincMeasure.get();
    }

    public ObjectProperty<Measures> costincMeasureProperty() {
        return costincMeasure;
    }

    public void setCostincMeasure(Measures costincMeasure) {
        this.costincMeasure.set(costincMeasure);
    }

    public Categories getCostincCategory() {
        return costincCategory.get();
    }

    public ObjectProperty<Categories> costincCategoryProperty() {
        return costincCategory;
    }

    public void setCostincCategory(Categories costincCategory) {
        this.costincCategory.set(costincCategory);
    }

    public int getCostincBillId() {
        return costincBillId.get();
    }

    public IntegerProperty costincBillIdProperty() {
        return costincBillId;
    }

    public void setCostincBillId(int costincBillId) {
        this.costincBillId.set(costincBillId);
    }

    public String getCostincBillName() {
        return costincBillName.get();
    }

    public StringProperty costincBillNameProperty() {
        return costincBillName;
    }

    public void setCostincBillName(String costincBillName) {
        this.costincBillName.set(costincBillName);
    }

    public boolean getCostincome() {
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

    public long getDeletedDate() {
        return deletedDate.get();
    }

    public LongProperty deletedDateProperty() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate.set(deletedDate);
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

    public String getUuidBill() {
        return uuidBill.get();
    }

    public StringProperty uuidBillProperty() {
        return uuidBill;
    }

    public void setUuidBill(String uuidBill) {
        this.uuidBill.set(uuidBill);
    }
}
