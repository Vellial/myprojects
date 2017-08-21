package entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.deserializers.CategoriesDeserializer;

/**
 * Categories
 */
@JsonDeserialize(using = CategoriesDeserializer.class)
public class Categories {
    private String categoryName;
    private int categoryId;
    private boolean costincome;
    private long deletedDate;
    private String uuidCategory;

    public Categories() {

    }

    public Categories(Integer key, String value) {
        this.categoryId = key;
        this.categoryName = value;
    }

    // For parsing on server side.
    public Categories(int categoryId, String categoryName, boolean costIncome, long deletedDate, String catUUID) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.costincome = costIncome;
        this.deletedDate = deletedDate;
        this.uuidCategory = catUUID;
    }

    // For receive to client side.
    public Categories(String uuidCategory, String categoryName, boolean costIncome) {
        this.uuidCategory = uuidCategory;
        this.categoryName = categoryName;
        this.costincome = costIncome;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isCostincome() {
        return costincome;
    }

    public void setCostincome(boolean costincome) {
        this.costincome = costincome;
    }

    public long getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate = deletedDate;
    }

    public String getUuidCategory() {
        return uuidCategory;
    }

    public void setUuidCategory(String uuidCategory) {
        this.uuidCategory = uuidCategory;
    }
}
