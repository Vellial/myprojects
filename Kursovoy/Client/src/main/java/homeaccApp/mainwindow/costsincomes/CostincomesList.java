package homeaccApp.mainwindow.costsincomes;

import javafx.collections.ObservableList;

/**
 * Created by vellial on 12.09.16.
 */

public class CostincomesList {
    private ObservableList<Costincomes> costincomes;
    private String codeOperation;
    private String numSync;
    private long lastSyncDate;
    private String uuidUser;
    private String uuidDevice;

    public ObservableList<Costincomes> getCostincomes() {
        return costincomes;
    }

    public void setCostincomes(ObservableList<Costincomes> costincomes) {
        this.costincomes = costincomes;
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

    public String getUuidDevice() {
        return uuidDevice;
    }

    public void setUuidDevice(String uuidDevice) {
        this.uuidDevice = uuidDevice;
    }

    public String getUuidUser() {
        return uuidUser;
    }

    public void setUuidUser(String uuidUser) {
        this.uuidUser = uuidUser;
    }
}
