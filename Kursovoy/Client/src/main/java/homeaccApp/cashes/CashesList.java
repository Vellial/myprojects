package homeaccApp.cashes;

import javafx.collections.ObservableList;

/**
 * Created by vellial on 12.09.16.
 */

public class CashesList {
    private ObservableList<Cashes> cashes;
    private String codeOperation;
    private String numSync;
    private long lastSyncDate;
    private String uuidUser;
    private String uuiddevice;

    public ObservableList<Cashes> getCashes() {
        return cashes;
    }

    public void setCashes(ObservableList<Cashes> cashes) {
        this.cashes = cashes;
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

    public String getUuiddevice() {
        return uuiddevice;
    }

    public void setUuiddevice(String uuiddevice) {
        this.uuiddevice = uuiddevice;
    }

    public String getUuidUser() {
        return uuidUser;
    }

    public void setUuidUser(String uuidUser) {
        this.uuidUser = uuidUser;
    }
}
