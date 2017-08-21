package entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import entities.deserializers.MeasuresDeserializer;

/**
 * Measures
 */
@JsonDeserialize(using = MeasuresDeserializer.class)
public class Measures {
    private int measureId;
    private String measureName;
    private String uuidMeasure;
    private long deletedDate;

    public Measures() {
        this(0, null, null, 0);
    }

    public Measures(int measureId, String measureName) {
        this.measureId = measureId;
        this.measureName = measureName;
    }

    public Measures(int measureId, String measureName, String uuidMeasure, long deletedDate) {
        this.measureId = measureId;
        this.measureName = measureName;
        this.uuidMeasure = uuidMeasure;
        this.deletedDate = deletedDate;
    }

    public Measures(String uuidMeasures, String measureName) {
        this.uuidMeasure = uuidMeasures;
        this.measureName = measureName;
    }


    public int getMeasureId() {
        return measureId;
    }

    public void setMeasureId(int measureId) {
        this.measureId = measureId;
    }

    public String getMeasureName() {
        return measureName;
    }

    public void setMeasureName(String measureName) {
        this.measureName = measureName;
    }

    public String getUuidMeasure() {
        return uuidMeasure;
    }

    public void setUuidMeasure(String uuidMeasure) {
        this.uuidMeasure = uuidMeasure;
    }

    public long getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate = deletedDate;
    }
}
