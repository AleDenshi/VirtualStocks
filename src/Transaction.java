public class Transaction {
	private int transactionID;
	private String transactionDate;
	private double amount;
	private double sharesBought;
	private String ticker;
	private Stock stock;
	
	public Transaction(int transactionID, String transactionDate, double amount, double sharesBought, Stock stock) {
		this.transactionID = transactionID;
		this.transactionDate = transactionDate;
		this.amount = Math.abs(amount);
		if (sharesBought < 0) this.amount *= -1.0;
		
		this.sharesBought = sharesBought;
		this.stock = stock;
		this.ticker = stock.getTicker();
	}
	public int getTransactionID() {
		return transactionID;
	}

	public String getTransactionDate() {
		return transactionDate;
	}

	public double getAmount() {
		return amount;
	}

	public double getSharesBought() {
		return sharesBought;
	}

	public String getTicker() {
		return ticker;
	}
	
	public Stock getStock() {
		return stock;
	}
	
	public String toString() {
		String transaction = transactionID + " (" + transactionDate + "): ";
		if (sharesBought > 0) {
			transaction += " BUY ";
		} else {
			transaction += " SELL ";
		}
		transaction += ticker + " FOR $" + amount;
		return transaction;
	}
}
