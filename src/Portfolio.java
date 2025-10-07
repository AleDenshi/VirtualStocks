import java.util.ArrayList;

public class Portfolio {
	private int portfolioID;
	private double value;
	private String userID;
	// Values derived from relational DB
	private ArrayList<Transaction> transactions;
	private ArrayList<Stock> holds;
	

	public Portfolio(int portfolioID, String userID) {
		this.portfolioID = portfolioID;
		this.userID = userID;
		this.value = 0.0;
		this.transactions = new ArrayList<Transaction>();
		this.holds = new ArrayList<Stock>();
	}

	public int getPortfolioID() {
		return portfolioID;
	}

	public double getValue() {
		calcValue();
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getUserID() {
		return userID;
	}

	public void calcValue() {
		value = 0.0;
		for (int i = 0; i < getNumTransactions(); i++) {
			Transaction transaction = transactions.get(i);
			value += transaction.getSharesBought() * transaction.getStock().getCurrentPrice();
		}
	}
	
	public int getNumStocks() {
		return holds.size();
	}
	
	public String getTickerByIndex(int index) {
		return holds.get(index).getTicker();
	}

	// Return the index of a stock in the stocks array by its ticker
	public int getIndexByTicker(String ticker) {
		for (int i = 0; i < holds.size(); i++) {
			Stock stock = holds.get(i);
			if (stock.getTicker().equalsIgnoreCase(ticker)) return i;
		}
		return -1;
	}
	
	public void addTransaction(Transaction transaction) {
		// Do not add transaction if it already exists
		if (getTransactionByID(transaction.getTransactionID()) != null) return;
		transactions.add(transaction);
		
		// Add the stock if it is new
		Stock stock = transaction.getStock();
		String ticker = transaction.getTicker();
		if (getIndexByTicker(ticker) == -1) {
			holds.add(stock);
		}
		// Remove the stock if its share count is now 0
		if (getStocksOwnedByTicker(ticker) == 0.0) {
			holds.remove(stock);
		}
	}

	public int getNumTransactions() {
		return transactions.size();
	}
	
	public Transaction getTransactionByIndex(int index) {
		if (index >= transactions.size())
			return null;
		return transactions.get(index);
	}

	public Transaction getTransactionByID(int transactionID) {
		for (int i = 0; i < transactions.size(); i++) {
			Transaction transaction = transactions.get(i);
			if (transaction.getTransactionID() == transactionID)
				return transaction;
		}
		return null;
	}

	public ArrayList<Transaction> getTransactionsByTicker(String ticker) {
		ArrayList<Transaction> transactionsByTicker = new ArrayList<Transaction>();
		for (int i = 0; i < transactions.size(); i++) {
			Transaction transaction = transactions.get(i);
			if (transaction.getTicker().equals(ticker)) {
				transactionsByTicker.add(transaction);
			}
		}
		return transactionsByTicker;
	}

	// Turn the transaction list into a string
	public String getTransactionsToString() {
		String transactionsString = String.format("%-18s %-15s %-15s %-15s %-15s\n", "TransactionID", "Date", "Ticker",
				"Shares", "Amount");
		for (int i = 0; i < transactions.size(); i++) {
			Transaction transaction = transactions.get(i);
			transactionsString += String.format("%-18d %-15s %-15s %-15.2f $%-15.2f\n", transaction.getTransactionID(),
					transaction.getTransactionDate(), transaction.getTicker(), transaction.getSharesBought(),
					transaction.getAmount());
		}
		return transactionsString;
	}

	public double getStocksOwnedByTicker(String ticker) {
		double shares = 0.0;
		for (int i = 0; i < getNumTransactions(); i++) {
			Transaction transaction = getTransactionByIndex(i);
			String ticker2 = transaction.getTicker();
			if (ticker.equals(ticker2)) {
				double theseShares = transaction.getSharesBought();
				shares += theseShares;
			}
		}
		return shares;
	}
	
	public double getValueByTicker(String ticker) {
		Stock stock = holds.get(getIndexByTicker(ticker));
		double sharesBought = getStocksOwnedByTicker(ticker);
		return stock.getCurrentPrice() * sharesBought;
	}
	
	public double getInvestmentByTicker(String ticker) {
		double investment = 0.0;
		for (int i = 0; i < getNumTransactions(); i++) {
			Transaction transaction = getTransactionByIndex(i);
			String ticker2 = transaction.getTicker();
			if (ticker.equals(ticker2)) {
				double thisAmount = transaction.getAmount();
				investment += thisAmount;
			}
		}
		return investment;
	}
	
	public double getProfitByTicker(String ticker) {
		return getValueByTicker(ticker) - getInvestmentByTicker(ticker);
	}
	
	public double getProfitPercentageByTicker(String ticker) {
		return 100 * (getProfitByTicker(ticker) / getInvestmentByTicker(ticker));
	}

	// Get a summary of the portfolio
	public String toString() {
		String portfolio = "Portfolio " + portfolioID + ", owned by " +  userID +":\n";
		portfolio += String.format("Value: $%.2f\n", value);
		portfolio += "Transactions: " + getNumTransactions() + "\n";
		portfolio += String.format("%-8s %-15s %-15s %-15s %-15s\n", "Ticker", "Shares", "Value", "Profit/Loss", "%Change");
		for (int i = 0; i < getNumStocks(); i++) {
			String ticker = holds.get(i).getTicker();
			double stocksOwned = getStocksOwnedByTicker(ticker);
			double value = getValueByTicker(ticker);
			double profit = getProfitByTicker(ticker);
			double profitPercentage = getProfitPercentageByTicker(ticker);
			portfolio += String.format("%-8s %-15.2f $%-15.2f $%-15.2f %.2f%%\n", ticker, stocksOwned, value, profit, profitPercentage);
		}
		return portfolio;
	}

}
