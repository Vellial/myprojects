package homeaccApp.mainwindow.planning;

import javafx.collections.ObservableList;

/**
 * Created by vellial on 12.09.16.
 */

public class PlansList {
    private ObservableList<Planning> plannings;
    private String codeOperation;
    private String numSync;
    private long lastSyncDate;
    private String uuidUser;
    private String uuidDevice;

    public ObservableList<Planning> getPlannings() {
        return plannings;
    }

    public void setPlannings(ObservableList<Planning> plannings) {
        this.plannings = plannings;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public String getNumSync() {
        return numSync;
    }

    public void setNumSync(String numSync) {
        this.numSync = numSync;
    }

    public long getLastSyncDate() {
        return lastSyncDate;
    }

    public void setLastSyncDate(long lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }

    public String getUuidUser() {
        return uuidUser;
    }

    public void setUuidUser(String uuidUser) {
        this.uuidUser = uuidUser;
    }

    public String getUuidDevice() {
        return uuidDevice;
    }

    public void setUuidDevice(String uuidDevice) {
        this.uuidDevice = uuidDevice;
    }
}
