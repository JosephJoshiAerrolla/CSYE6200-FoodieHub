package application;

public abstract class AbstractPlannerController {
    protected int userId;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public abstract void saveToDatabase();
}
