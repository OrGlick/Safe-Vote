package co.il.safevote;

public class User
{
    public String databaseKey;
    public String firebaseUid;
    public String nameFromFirebase;
    public String azurePersonId;
    public boolean isVoted;
    public boolean isBlocked;

    public User(String databaseKey, String firebaseUid, String nameFromFirebase, String azurePersonId, boolean isVoted, boolean isBlocked)
    {
        this.databaseKey = databaseKey;
        this.firebaseUid = firebaseUid;
        this.nameFromFirebase = nameFromFirebase;
        this.azurePersonId = azurePersonId;
        this.isVoted = isVoted;
        this.isBlocked = isBlocked;
    }

    public User()
    {}//required

    public String getDatabaseKey() {
        return databaseKey;
    }

    public void setDatabaseKey(String databaseKey) {
        this.databaseKey = databaseKey;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getNameFromFirebase() {
        return nameFromFirebase;
    }

    public void setNameFromFirebase(String nameFromFirebase) {
        this.nameFromFirebase = nameFromFirebase;
    }

    public String getAzurePersonId() {
        return azurePersonId;
    }

    public void setAzurePersonId(String azurePersonId) {
        this.azurePersonId = azurePersonId;
    }

    public boolean isVoted() {
        return isVoted;
    }

    public void setVoted(boolean voted) {
        isVoted = voted;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
