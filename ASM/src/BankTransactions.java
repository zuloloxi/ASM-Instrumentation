
public class BankTransactions {

	public static void main(String[] args) {
		BankTransactions bank = new BankTransactions();
		for (int i = 0; i < 100; i++) {
		    String accountId = "account" + i;
			String passId = "Ashley"+i; 
		    bank.login("password", accountId, passId);
		    bank.unimportantProcessing(accountId);
		    bank.withdraw(accountId, Double.valueOf(i)+100.0);
		}
		System.out.println("Transactions completed");

	}
	
	private void unimportantProcessing(String accountId) {
		// Unimportant Processing
	}

	@ImportantLog(fields = { "1", "2" })
	public void login(String password, String accountId, String userName) {
	    // login logic
	}
	
	@ImportantLog(fields = { "0", "1" })
	public void withdraw(String accountId, Double moneyToRemove) {
	    // transaction logic
	}

}
