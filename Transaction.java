class Transaction {
    private String description;
    private double amount;
    private String type;
    private String category;
    private java.util.Date date;

    public Transaction(String description, double amount, String type, String category) {
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = new java.util.Date();
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy ");
        return "Transaction{" +
                "description='" + description + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", date=" + sdf.format(date) +
            '}';
  }
}
