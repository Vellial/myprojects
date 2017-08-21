package entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.deserializers.PlanningDeserializer;
import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Planning notes.
 */
@JsonDeserialize(using = PlanningDeserializer.class)
public class Planning {
    private StringProperty billUUID;
    private StringProperty categoryUUID;
    private StringProperty measureUUID;
    private LongProperty planningDate;
    private ObjectProperty<Bills> planningBill;
    private StringProperty planningNote;
    private IntegerProperty planningCount;
    private ObjectProperty<Measures> planningMeasure;
    private ObjectProperty<Categories> planningCategory;
    private DoubleProperty planningAmount;
    private IntegerProperty planningId;
    private BooleanProperty planningCostincome;
    private IntegerProperty userId;
    private IntegerProperty measureId;
    private IntegerProperty billId;
    private StringProperty planStatus;
    private StringProperty planPeriod;
    private StringProperty planUUID;
    private long deletedDate;
    private IntegerProperty categoryId;

    public Planning() {
        this(0, 0, 0, null, 0, false, 0, 0, 0, 0, null, null, null, 0);
    }

    public Planning(int planId, long date, int count, String note, double amount, boolean costIncome, int measureId, int billId, int userId, long deletedDate, String status, String period, String uuidPlan, int categoryId) {
        this.planningId = new SimpleIntegerProperty(planId);
        this.planningDate = new SimpleLongProperty(date);
        this.planningCount = new SimpleIntegerProperty(count);
        this.planningNote = new SimpleStringProperty(note);
        this.planningAmount = new SimpleDoubleProperty(amount);
        this.planningCostincome = new SimpleBooleanProperty(costIncome);
        this.userId = new SimpleIntegerProperty(userId);
        this.measureId = new SimpleIntegerProperty(measureId);
        this.billId = new SimpleIntegerProperty(billId);
        this.deletedDate = deletedDate;
        this.planStatus = new SimpleStringProperty(status);
        this.planPeriod = new SimpleStringProperty(period);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.planUUID = new SimpleStringProperty(uuidPlan);
    }

    public Planning(String uuidPlanning, long date, String uuidBills, int count, String note, double amount, String uuidMeasure, boolean costIntcome, String uuidCategory) {
        this.planUUID = new SimpleStringProperty(uuidPlanning);
        this.planningDate = new SimpleLongProperty(date);
        this.billUUID = new SimpleStringProperty(uuidBills);
        this.planningCount = new SimpleIntegerProperty(count);
        this.planningNote = new SimpleStringProperty(note);
        this.planningAmount = new SimpleDoubleProperty(amount);
        this.measureUUID = new SimpleStringProperty(uuidMeasure);
        this.planningCostincome = new SimpleBooleanProperty(costIntcome);
        this.categoryUUID = new SimpleStringProperty(uuidCategory);
    }

    public Bills getPlanningBill() {
        return planningBill.get();
    }

    public ObjectProperty<Bills> planningBillProperty() {
        return planningBill;
    }

    public void setPlanningBill(Bills planningBill) {
        this.planningBill.set(planningBill);
    }

    public String getPlanningNote() {
        return planningNote.get();
    }

    public StringProperty planningNoteProperty() {
        return planningNote;
    }

    public void setPlanningNote(String planningNote) {
        this.planningNote.set(planningNote);
    }

    public int getPlanningCount() {
        return planningCount.get();
    }

    public IntegerProperty planningCountProperty() {
        return planningCount;
    }

    public void setPlanningCount(int planningCount) {
        this.planningCount.set(planningCount);
    }

    public Measures getPlanningMeasure() {
        return planningMeasure.get();
    }

    public ObjectProperty<Measures> planningMeasureProperty() {
        return planningMeasure;
    }

    public void setPlanningMeasure(Measures planningMeasure) {
        this.planningMeasure.set(planningMeasure);
    }

    public Categories getPlanningCategory() {
        return planningCategory.get();
    }

    public ObjectProperty<Categories> planningCategoryProperty() {
        return planningCategory;
    }

    public void setPlanningCategory(Categories planningCategory) {
        this.planningCategory.set(planningCategory);
    }

    public double getPlanningAmount() {
        return planningAmount.get();
    }

    public DoubleProperty planningAmountProperty() {
        return planningAmount;
    }

    public void setPlanningAmount(double planningAmount) {
        this.planningAmount.set(planningAmount);
    }

    public int getPlanningId() {
        return planningId.get();
    }

    public IntegerProperty planningIdProperty() {
        return planningId;
    }

    public void setPlanningId(int planningId) {
        this.planningId.set(planningId);
    }

    public boolean getPlanningCostincome() {
        return planningCostincome.get();
    }

    public BooleanProperty planningCostincomeProperty() {
        return planningCostincome;
    }

    public void setPlanningCostincome(boolean planningCostincome) {
        this.planningCostincome.set(planningCostincome);
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

    public long getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate = deletedDate;
    }

    public String getPlanUUID() {
        return planUUID.get();
    }

    public StringProperty planUUIDProperty() {
        return planUUID;
    }

    public void setPlanUUID(String planUUID) {
        this.planUUID.set(planUUID);
    }

    public long getPlanningDate() {
        return planningDate.get();
    }

    public LongProperty planningDateProperty() {
        return planningDate;
    }

    public void setPlanningDate(long planningDate) {
        this.planningDate.set(planningDate);
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

    public String getPlanStatus() {
        return planStatus.get();
    }

    public StringProperty planStatusProperty() {
        return planStatus;
    }

    public void setPlanStatus(String planStatus) {
        this.planStatus.set(planStatus);
    }

    public String getPlanPeriod() {
        return planPeriod.get();
    }

    public StringProperty planPeriodProperty() {
        return planPeriod;
    }

    public void setPlanPeriod(String planPeriod) {
        this.planPeriod.set(planPeriod);
    }

    public String getBillUUID() {
        return billUUID.get();
    }

    public StringProperty billUUIDProperty() {
        return billUUID;
    }

    public void setBillUUID(String billUUID) {
        this.billUUID.set(billUUID);
    }

    public String getCategoryUUID() {
        return categoryUUID.get();
    }

    public StringProperty categoryUUIDProperty() {
        return categoryUUID;
    }

    public void setCategoryUUID(String categoryUUID) {
        this.categoryUUID.set(categoryUUID);
    }

    public String getMeasureUUID() {
        return measureUUID.get();
    }

    public StringProperty measureUUIDProperty() {
        return measureUUID;
    }

    public void setMeasureUUID(String measureUUID) {
        this.measureUUID.set(measureUUID);
    }
}
