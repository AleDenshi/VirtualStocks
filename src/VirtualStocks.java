import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class VirtualStocks {

	public static void main(String[] args) {
		// Connection arguments, TODO: PLACE IN ENVIRONMENT FILE!
		// TODO: make a user for this rather than root!!
		String DBurl = "jdbc:mariadb://localhost:3306/";
		String DBuser = "root";
		String DBpswd = "OrionessRed";

		// Create app object instance
		VirtualStocks app = new VirtualStocks();

		// Create the database interface
		SQL db = new SQL(DBurl, DBuser, DBpswd);

		// TODO actually get today's date,
		// Stop using this placeholder for testing
		Date today = Date.valueOf("2025-04-10");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String todayString = formatter.format(today);

		// Parse data from files
		db.parseStockInfo("stockInfo.csv");
		db.parseDayPrices("dayPrices.csv");
		
		// Obtain all stocks
		ArrayList<Stock> stocks = db.getStocks(today);

		// Create scanner
		Scanner scan = new Scanner(System.in);

		// Ask the user to log in
		User user = app.loginUser(db, scan);

		// Get the user's existing portfolios
		ArrayList<Portfolio> portfolios = db.getPortfolios(user.getUserID());
		Portfolio portfolio;

		// Create new portfolio if the user has 0
		if (portfolios.size() == 0) {
			portfolio = app.createNewPortfolio(scan, db.getUniquePortfolioID(), user.getUserID());
			db.addPortfolio(portfolio);
		} else {
			// Prompt the user to pick one of their portfolio
			portfolio = app.promptPortfolios(portfolios, scan);
		}

		// Begin prompting the user with options
		int request;
		do {
			System.out.printf("Welcome, %s %s.\n\n", user.getFirstName(), user.getLastName());
			request = app.menu(scan);
			app.parseRequest(request, db, user, scan, todayString, stocks, portfolio);
		} while (request != 8);

		// Small message when the user quits
		System.out.println("Thanks for using VirtualStocks!");
	}

	// Prompt the user for login info
	public User loginUser(SQL db, Scanner scan) {
		System.out.print("Please enter a username:\n> ");
		String userID = scan.nextLine();
		User user = db.findUser(userID);
		// Look for the user
		if (user == null) {
			// Prompt to create a new user
			boolean answer = promptConfirm(
					"UserID " + userID + " not found.\n" + "Do you want to create this user?\n> ", scan);
			if (!answer) {
				System.out.println("Not creating any new users. Quitting...");
				System.exit(0);
			}
			// Create a new user and add it to the database
			user = createNewUser(scan, userID);
			if (user == null) {
				System.out.println("Cancelling attempt, quitting!");
				return null;
			} else {
				db.addUser(user);
				return user;
			}

		} else {
			// Prompt for the password if the user exists
			passwdPrompt(scan, user);
			return user;
		}
	}

	// Method to prompt the user through a new DB user creation process
	public User createNewUser(Scanner scan, String userID) {

		System.out.print("Please enter a first name:\n> ");
		String firstName = scan.nextLine();

		System.out.print("Please enter a last name:\n> ");
		String lastName = scan.nextLine();

		// Keep asking for a new password until they're the same
		String confirmPasswd, passwd;
		do {
			System.out.print("Please enter a password:\n> ");
			passwd = scan.nextLine();
			System.out.print("Please confirm password:\n> ");
			confirmPasswd = scan.nextLine();
		} while (!passwd.equals(confirmPasswd));

		// Hash the password
		String passwdHash = sha256sum(passwd);
		User user = new User(userID, firstName, lastName, passwdHash);

		// Confirmation from the user
		boolean result = promptConfirm("Confirm user info:\n" + user + "\n> ", scan);

		if (result) {
			return user;
		} else {
			return null;
		}
	}

	public void passwdPrompt(Scanner scan, User user) {
		String passwdHash;
		do {
			System.out.print("Please enter the password for " + user.getUserID() + "\n> ");
			String passwd = scan.nextLine();
			passwdHash = sha256sum(passwd);
		} while (!passwdHash.equals(user.getPasswdHash()));
	}

	public void printStocks(ArrayList<Stock> stocks) {
		System.out.printf("%-4s %-8s %-15s %-15s %-15s\n", "No.", "Ticker", "Name", "Price", "Capitalization");
		for (int i = 0; i < stocks.size(); i++) {
			Stock stock = stocks.get(i);
			System.out.printf("%-4s %-8s %-15s $%-15.2f $%-15.2f\n", i + 1, stock.getTicker(), stock.getStockName(),
					stock.getCurrentPrice(), stock.getCapitalization());
		}
	}

	// Code to prompt user to pick a portfolio
	public Portfolio promptPortfolios(ArrayList<Portfolio> portfolios, Scanner scan) {
		System.out.printf("%-8s %-15s %-15s\n", "Index", "ID", "Value");
		for (int i = 0; i < portfolios.size(); i++) {
			Portfolio portfolio = portfolios.get(i);
			System.out.printf("%-8s %-15d $%-15.2f\n", i + 1, portfolio.getPortfolioID(), portfolio.getValue());
		}
		int index = promptInt("Please pick a portfolio:\n> ", scan, 1, portfolios.size());
		return portfolios.get(index - 1);
	}

	// Code to prompt user to create new portfolio
	public Portfolio createNewPortfolio(Scanner scan, int portfolioID, String userID) {
		// Ask user to confirm the creation of this portfolio
		boolean result = promptConfirm("Create new portfolio for " + userID + " with ID " + portfolioID + "?", scan);
		if (result) {
			System.out.println("Creating new portfolio with ID: " + portfolioID);
			Portfolio portfolio = new Portfolio(portfolioID, userID);
			return portfolio;
		} else {
			System.out.println("Not creating any new portfolios. Quitting...");
			return null;
		}
	}

	// Prompt user to create a new transaction
	public Transaction createNewTransaction(Scanner scan, ArrayList<Stock> stocks, Portfolio portfolio,
			String transactionDate, int transactionID) {
		printStocks(stocks);
		int index = promptInt("Please pick a stock:\n> ", scan, 1, stocks.size());
		Stock stock = stocks.get(index - 1);
		String ticker = stock.getTicker();
		double stocksOwned = portfolio.getStocksOwnedByTicker(ticker);
		System.out.println("You currently own " + stocksOwned + " of " + ticker);
		double stocksToBuy = promptDouble("Please enter an amount to buy/sell:\n>", scan, -1 * stocksOwned, 9001);
		System.out.printf("Value of %.2f %s: $%.2f.\n", stocksToBuy, ticker, stocksToBuy*stock.getCurrentPrice());
		double amount = promptDouble("Please enter the amount you bought/sold for:\n>", scan, -900001, 900001);
		Transaction transaction = new Transaction(transactionID, transactionDate, amount, stocksToBuy, stock);
		return transaction;
	}

	// Method to hash a string and return the hash as a string
	// Especially useful for passwords in this app.
	public String sha256sum(String string) {
		String hashString = "";
		try {
			MessageDigest digest;
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(string.getBytes(StandardCharsets.UTF_8));
			// Loop through, mask the bytes to get a string
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hashString += "0";
				hashString += hex;
			}
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Couldn't find hashing algorithm, quitting!");
			System.out.println(e.getMessage());
			System.exit(0);
		}
		return hashString;
	}

	public int menu(Scanner scan) {
		System.out.println("""
				Select from the following options:
				1) View portfolio overview
				2) View available stocks
				3) View all transactions
				4) Create new portfolio
				5) Delete portfolio
				6) New transaction
				7) Change user info
				8) Quit
				""");
		System.out.print("> ");
		String input = scan.nextLine();
		try {
			int request = Integer.parseInt(input);
			return request;
		} catch (Exception e) {
			System.out.println("Invalid value!");
			return -1;
		}
	}

	// Prompt the user for an integer
	public int promptInt(String message, Scanner scan, int min, int max) {
		int num = min - 1;
		while (num < min || num > max) {
			try {
				System.out.print(message);
				num = Integer.parseInt(scan.nextLine());
			} catch (Exception e) {
				System.out.println("Invalid selection.");
			}
		}
		return num;
	}

	// Prompt the user for a double
	public double promptDouble(String message, Scanner scan, double min, double max) {
		double num = min - 1;
		while (num < min || num > max) {
			try {
				System.out.print(message);
				num = Double.parseDouble(scan.nextLine());
			} catch (Exception e) {
				System.out.println("Invalid selection.");
			}
		}
		return num;
	}

	public void parseRequest(int request, SQL db, User user, Scanner scan, String date, ArrayList<Stock> stocks,
			Portfolio portfolio) {
		switch (request) {
		case 1:
			System.out.println(portfolio);
			break;
		case 2:
			printStocks(stocks);
			break;
		case 3:
			System.out.println(portfolio.getTransactionsToString());
			break;
		case 4:
			Portfolio newPortfolio = createNewPortfolio(scan, db.getUniquePortfolioID(), user.getUserID());
			db.addPortfolio(newPortfolio);
			break;
		case 5:
			boolean confirm = promptConfirm(
					"Are you sure you want to delete portfolio " + portfolio.getPortfolioID() + "?", scan);
			if (confirm)
				db.deletePortfolio(portfolio);
			System.out.println("Log back in to create a new portfolio. Thank you for using VirtualStocks!");
				System.exit(0);
			break;
		case 6:
			int transactionID = db.getUniqueTransactionID();
			Transaction newTransaction = createNewTransaction(scan, stocks, portfolio, date, transactionID);
			promptConfirm("Perform: " + newTransaction + "?", scan);
			portfolio.addTransaction(newTransaction);
			db.replacePortfolio(portfolio);
			break;
		case 7:
			user = createNewUser(scan, user.getUserID());
			db.replaceUser(user);
			break;
		case 8:
			break;
		}
	}

	// Method to get a simple yes/no confirmation from the user
	public boolean promptConfirm(String message, Scanner scan) {
		// Loop forever until an answer is given
		String line = "";
		while (!line.toUpperCase().equals("N")) {
			System.out.print(message + " (Y/N) ");
			line = scan.nextLine();
			if (line.toUpperCase().equals("Y"))
				return true;
			if (line.toUpperCase().equals("N"))
				return false;
			System.out.println("Unknown answer.");
		}
		return false;
	}
}
