package co.il.safevote;

public class User
{
    public String azurePersonId;
    public boolean isVoted;
    public boolean isBlocked;

    public User() {
        //required
    }

    public User(String nameFromFirebase, String azurePersonId)
    {
        this.azurePersonId = azurePersonId;
        this.isVoted = false;
        this.isBlocked = false;
    }

    public String getAzurePersonId() {
        return azurePersonId;
    }

    public void setAzurePersonId(String azurePersonId) {
        this.azurePersonId = azurePersonId;
    }

    public boolean getIsVoted() {
        return this.isVoted;
    }

    public void setIsVoted(boolean isVoted) {
        this.isVoted = isVoted;
    }

    public boolean getIsBlocked() {
        return this.isBlocked;
    }

    public void setIsBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}
