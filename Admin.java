import java.util.ArrayList;
import java.util.List;

public class Admin {
    private String adminname;
    private String password;
    private String mobileNumber;
    private String securityAnswer;
    private LinkedList<Transaction> transactions;
    private List<Investment> investments;

    public Admin(String adminname, String password, String securityAnswer, String mobileNumber) {
        this.adminname = adminname;
        this.password = password;
        this.mobileNumber = mobileNumber;
        this.securityAnswer = securityAnswer;
        this.transactions = new LinkedList<>();
        this.investments = new ArrayList<>();
    }

    public List<Investment> getInvestments() {
        return this.investments;
    }

    public LinkedList<Transaction> getTransactions() {
        return transactions;
    }

    public String getAdminname() {
        return adminname;
    }

    public String getPassword() {
        return password;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

}
