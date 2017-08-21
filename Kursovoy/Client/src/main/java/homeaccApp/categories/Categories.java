package homeaccApp.categories;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import homeaccApp.api.deserializers.CategoriesDeserializer;
import javafx.beans.property.*;

/**
 * Categories
 */
@JsonDeserialize(using = CategoriesDeserializer.class)
public class Categories {
    private StringProperty categoryName;
    private IntegerProperty categoryId;
    private BooleanProperty costincome;
    private long deletedDate;
    private StringProperty uuidCategory;

    public Categories() {
        this(0, null, false, 0, null);
    }

    public Categories(Integer key, String value) {
        this.categoryId = new SimpleIntegerProperty(key);
        this.categoryName = new SimpleStringProperty(value);
    }

    public Categories(int categoryId, String categoryName, boolean costIncome, long deletedDate, String uuid) {
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.categoryName = new SimpleStringProperty(categoryName);
        this.costincome = new SimpleBooleanProperty(costIncome);
        this.deletedDate = deletedDate;
        this.uuidCategory = new SimpleStringProperty(uuid);
    }

    public long getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate = deletedDate;
    }

    @Override
    public String toString() {
        return categoryName.get();
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

    public String getCategoryName() {
        return categoryName.get();
    }

    public StringProperty categoryNameProperty() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName.set(categoryName);
    }

    public boolean getCostincome() {
        return costincome.get();
    }

    public BooleanProperty costincomeProperty() {
        return costincome;
    }

    public void setCostincome(boolean costincome) {
        this.costincome.set(costincome);
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
