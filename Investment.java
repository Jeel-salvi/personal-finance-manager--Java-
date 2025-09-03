import java.util.Date;

public class Investment {
    private String description;
    private double amount;
    private String category;
    private Date date;

    public Investment(String description, double amount, String category) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = new Date();
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format("Investment [description=%s, amount=%.2f, category=%s, date=%s]",
                description, amount, category, date);
    }
}
