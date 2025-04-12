package application;

public abstract class UserControllerBase {
    protected int userId;

    public void setUserId(int id) {
        this.userId = id;
    }

    public int getUserId() {
        return userId;
    }

    public abstract void loadUserData();
}
