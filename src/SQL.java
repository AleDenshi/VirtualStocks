import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;

// SQL Class
// Deals with all the interfacing with the database
// All of this is in useful methods, like findUser, getStocks etc...

public class SQL {
	Connection sql;
	ArrayList<Stock> stocks;

	public SQL(String url, String user, String pswd) {
		String databaseName = "VirtualStocks";
		stocks = new ArrayList<Stock>();

		// Try connecting to the database
		try {
			this.sql = DriverManager.getConnection(url, user, pswd);
		} catch (SQLException e) {
			System.out.println("Failed to getConnection!");
			System.out.println(e.getMessage());
			System.exit(0);
		}

		// Check if the database exists
		createDB(databaseName);
		// Go through and run the schema code
		createSchemas();
	}

	// Method to create the database
	private void createDB(String databaseName) {
		try {
			Statement stmt;
			stmt = sql.createStatement();
			stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
			stmt.executeUpdate("USE " + databaseName);
		} catch (SQLException e) {
			System.out.println("Failed to connect or create " + databaseName + ", quitting!");
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	// Method to create all the initial schemas we need
	private void createSchemas() {
		try {
			String schema;
			Statement stmt;
			stmt = sql.createStatement();

			// Stocks Table
			schema = """
					CREATE TABLE IF NOT EXISTS Stocks (
					ticker VARCHAR(5),
					stockName VARCHAR(25),
					currentPrice FLOAT,
					capitalization FLOAT,
					PRIMARY KEY (ticker)
					)""";
			stmt.executeUpdate(schema);

			// dayPrice table
			schema = """
					CREATE TABLE IF NOT EXISTS DayPrices (
					pricingDate DATE,
					ticker VARCHAR(5),
					dayPrice FLOAT,
					PRIMARY KEY (ticker, pricingDate),
					FOREIGN KEY (ticker) REFERENCES Stocks(ticker) ON DELETE CASCADE
					)""";
			stmt.executeUpdate(schema);

			// User info table
			schema = """
					CREATE TABLE IF NOT EXISTS Users (
					userID VARCHAR(25),
					firstName VARCHAR(25),
					lastName VARCHAR(25),
					passwdHash VARCHAR(65),
					PRIMARY KEY (userID)
					)""";
			stmt.executeUpdate(schema);

			// Portfolio table
			schema = """
					CREATE TABLE IF NOT EXISTS Portfolios (
					portfolioID INT,
					value FLOAT,
					userID VARCHAR(25),
					PRIMARY KEY (portfolioID),
					FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE
					)""";
			stmt.executeUpdate(schema);

			// Portfolio Holdings table
			schema = """
					CREATE TABLE IF NOT EXISTS Holds (
					ticker VARCHAR(5),
					portfolioID INT,
					shares FLOAT,
					buyInAmount FLOAT,
					PRIMARY KEY (ticker, portfolioID),
					FOREIGN KEY (ticker) REFERENCES Stocks(ticker),
					FOREIGN KEY (portfolioID) REFERENCES Portfolios(portfolioID) ON DELETE CASCADE
					)""";
			stmt.executeUpdate(schema);

			// Transaction table
			schema = """
					CREATE TABLE IF NOT EXISTS Transactions (
					transactionID INT,
					transactionDate DATE,
					amount FLOAT,
					sharesBought FLOAT,
					ticker VARCHAR(5),
					portfolioID INT,
					PRIMARY KEY (transactionID),
					FOREIGN KEY (portfolioID) REFERENCES Portfolios(portfolioID) ON DELETE CASCADE
					)""";
			stmt.executeUpdate(schema);

		} catch (SQLException e) {
			System.out.println("Failed to create tables, quitting!");
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

	// Method to parse CSV stockInfo file into the Stocks table
	public void parseStockInfo(String filename) {
		try {
			File f = new File(filename);
			Scanner fscanner = new Scanner(f);
			
			// Skip first line, as it's only a label
			fscanner.nextLine();
			
			while (fscanner.hasNext()) {
				String line = fscanner.nextLine();
				String[] values = line.split(",");
				// Move onto next line if something is wrong.
				if (values.length != 4)
					continue;
				// Parse the actual data from the csv file
				String ticker = values[0];
				String stockName = values[1];
				double capitalization = Double.parseDouble(values[2]);
				double currentPrice = Double.parseDouble(values[3]);
							
				try {	
					PreparedStatement insertStatement;
					insertStatement = sql.prepareStatement("INSERT INTO Stocks VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE ticker = VALUES(ticker)");
					insertStatement.setString(1, ticker);
					insertStatement.setString(2, stockName);
					insertStatement.setFloat(3, (float) currentPrice);
					insertStatement.setFloat(4, (float) capitalization);
					insertStatement.execute();
				} catch (Exception e) {
					System.out.println("FAILED to add " + line + " to SQL.");
				}
			}
			fscanner.close();
			
		} catch (Exception e) {
			System.out.println("Error reading file:" + e.getMessage());
		}
	}

	// Method to parse CSV dayPrices file into the DayPrices table
	public void parseDayPrices(String filename) {
		try {
			File f = new File(filename);
			Scanner fscanner = new Scanner(f);

			// Skip first line, as it's only a label
			fscanner.nextLine();

			while (fscanner.hasNext()) {
				String line = fscanner.nextLine();
				String[] values = line.split(",");
				// Move onto next line if something is wrong.
				if (values.length != 3)
					continue;
				// Parse the actual data from the csv file
				String ticker = values[0];
				Date pricingDate = Date.valueOf(values[1]);
				double dayPrice = Double.parseDouble(values[2]);
				// Insert it with an SQL statement
				try {
					PreparedStatement insertStatement;
					insertStatement = sql.prepareStatement("INSERT INTO DayPrices VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ticker = VALUES(ticker)");
					insertStatement.setDate(1, pricingDate);
					insertStatement.setString(2, ticker);
					insertStatement.setFloat(3, (float) dayPrice);
					insertStatement.execute();
				} catch (Exception e) {
					System.out.println("FAILED to add " + line + " to SQL.");
				}
			}
			// Close the Scanner.
			fscanner.close();

		} catch (Exception e) {
			System.out.println("Error reading file:" + e.getMessage());
		}
	}

	// Method to add a user object to the database
	public void addUser(User user) {
		User findResult = findUser(user.getUserID());
		if (findResult == null) {
			try {
				PreparedStatement insertStatement;
				insertStatement = sql.prepareStatement("INSERT INTO Users VALUES (?, ?, ?, ?)");
				insertStatement.setString(1, user.getUserID());
				insertStatement.setString(2, user.getFirstName());
				insertStatement.setString(3, user.getLastName());
				insertStatement.setString(4, user.getPasswdHash());
				insertStatement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Username " + user.getUserID() + " found, cannot add/replace!");
		}
	}

	public void deleteUser(User user) {
		try {
			PreparedStatement insertStatement;
			// Delete old entry
			insertStatement = sql.prepareStatement("DELETE FROM Users WHERE userID = ?");
			insertStatement.setString(1, user.getUserID());
			insertStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Method to find a user in the database
	public User findUser(String userID) {
		User user;
		try {
			PreparedStatement queryStatement;
			queryStatement = sql.prepareStatement("SELECT * FROM Users WHERE userID = ?");
			queryStatement.setString(1, userID);
			ResultSet results = queryStatement.executeQuery();

			if (!results.next()) {
				return null;
			}

			String firstName = results.getString(2);
			String lastName = results.getString(3);
			String passwdHash = results.getString(4);
			user = new User(userID, firstName, lastName, passwdHash);
			return user;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Replaces a user's old information with the new information passed in
	public void replaceUser(User user) {
		User findResult = findUser(user.getUserID());
		if (findResult == null) {
			addUser(user);
		} else {
			try {
				PreparedStatement updateStatement = sql.prepareStatement("UPDATE Users SET firstName = ?, lastName = ?, passwdHash = ? WHERE userID = ?");
				updateStatement.setString(1, user.getFirstName());
				updateStatement.setString(2, user.getLastName());
				updateStatement.setString(3, user.getPasswdHash());
				updateStatement.setString(4, user.getUserID());
				updateStatement.execute();
			} catch (Exception e) {
				System.out.println("Failed to update user entry!");
			}
		}
	}

	// Get list of stock tickers
	public ArrayList<Stock> getStocks(Date date) {
		try {
			PreparedStatement queryStatement = sql
					.prepareStatement("SELECT ticker, stockName, capitalization FROM Stocks");
			ResultSet results = queryStatement.executeQuery();

			while (results.next()) {
				Stock stock = new Stock(results.getString(1), results.getString(2));
				stock.setCapitalization((double) results.getFloat(3));
				stock.setPrices(getPrices(stock.getTicker()), date);
				stocks.add(stock);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stocks;
	}

	// Takes in a ticker and returns all the prices in a hashmap (date -> price)
	public Map<Date, Double> getPrices(String ticker) {
		Map<Date, Double> prices = new Hashtable<>();
		try {
			PreparedStatement queryStatement = sql.prepareStatement("SELECT pricingDate, dayPrice FROM DayPrices WHERE ticker = ?");
			queryStatement.setString(1, ticker);
			ResultSet results = queryStatement.executeQuery();

			while (results.next()) {
				prices.put(results.getDate(1), (double) results.getFloat(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prices;
	}

	public boolean addPortfolio(Portfolio portfolio) {
		Portfolio findResult = findPortfolio(portfolio.getPortfolioID());
		if (findResult == null) {
			try {
				PreparedStatement insertStatement;
				insertStatement = sql.prepareStatement("INSERT INTO Portfolios VALUES (?, ?, ?)");
				insertStatement.setInt(1, portfolio.getPortfolioID());
				insertStatement.setFloat(2, (float) portfolio.getValue());
				insertStatement.setString(3, portfolio.getUserID());
				insertStatement.execute();
				setPortfolioTransactions(portfolio);
				setPortfolioHolds(portfolio);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Portfolio " + portfolio.getPortfolioID() + " found, cannot add/replace!");
			return false;
		}
		return false;
	}

	public void deletePortfolio(Portfolio portfolio) {
		try {
			PreparedStatement deleteStatement;
			deleteStatement = sql.prepareStatement("DELETE FROM Portfolios WHERE portfolioID = ?");
			deleteStatement.setInt(1, portfolio.getPortfolioID());
			deleteStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Delete all existing transactions and replace them with the current ones
	public void setPortfolioTransactions(Portfolio portfolio) {
		try {
			PreparedStatement deleteStatement;
			deleteStatement = sql.prepareStatement("DELETE FROM Transactions WHERE portfolioID = ?");
			deleteStatement.setInt(1, portfolio.getPortfolioID());
			deleteStatement.execute();
			// Now go through and all all entries
			for (int i = 0; i < portfolio.getNumTransactions(); i++) {
				Transaction transaction = portfolio.getTransactionByIndex(i);
				PreparedStatement insertStatement;
				insertStatement = sql.prepareStatement("INSERT INTO Transactions VALUES (?, ?, ?, ?, ?, ?)");
				insertStatement.setInt(1, transaction.getTransactionID());
				insertStatement.setDate(2, Date.valueOf(transaction.getTransactionDate()));
				insertStatement.setFloat(3, (float) transaction.getAmount());
				insertStatement.setFloat(4, (float) transaction.getSharesBought());
				insertStatement.setString(5, transaction.getTicker());
				insertStatement.setInt(6, portfolio.getPortfolioID());
				insertStatement.execute();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Delete all existing held stocks and replace them with the current ones
	public void setPortfolioHolds(Portfolio portfolio) {
		try {
			PreparedStatement deleteStatement;
			deleteStatement = sql.prepareStatement("DELETE FROM Holds WHERE portfolioID = ?");
			deleteStatement.setInt(1, portfolio.getPortfolioID());
			deleteStatement.execute();
			// Now go through and all all entries
			for (int i = 0; i < portfolio.getNumStocks(); i++) {
				int portfolioID = portfolio.getPortfolioID();
				String ticker = portfolio.getTickerByIndex(i);
				double sharesOwned = portfolio.getStocksOwnedByTicker(ticker);
				double buyInAmount = portfolio.getInvestmentByTicker(ticker);
				PreparedStatement insertStatement;
				insertStatement = sql.prepareStatement("INSERT INTO Holds VALUES (?, ?, ?, ?)");
				insertStatement.setString(1, ticker);
				insertStatement.setInt(2, portfolioID);
				insertStatement.setFloat(3, (float) sharesOwned);
				insertStatement.setFloat(4, (float) buyInAmount);
				insertStatement.execute();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Portfolio findPortfolio(int portfolioID) {
		Portfolio portfolio;
		try {
			PreparedStatement queryStatement = sql.prepareStatement("SELECT * FROM Portfolios WHERE portfolioID = ?");
			queryStatement.setInt(1, portfolioID);
			ResultSet results = queryStatement.executeQuery();

			if (!results.next()) {
				return null;
			}

			portfolio = new Portfolio(results.getInt(1), results.getString(3));
			return portfolio;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Replaces a portfolio's old information with the new information passed in
	public void replacePortfolio(Portfolio portfolio) {
		Portfolio findResult = findPortfolio(portfolio.getPortfolioID());
		if (findResult == null) {
			addPortfolio(portfolio);
		} else {
			try {
				PreparedStatement updateStatement = sql.prepareStatement("UPDATE Portfolios SET value = ? WHERE portfolioID = ?");
				updateStatement.setFloat(1, (float) portfolio.getValue());
				updateStatement.setFloat(2, portfolio.getPortfolioID());
				updateStatement.execute();
			} catch (Exception e) {
				System.out.println("Failed to update user entry!");
			}
		}
		setPortfolioHolds(portfolio);
		setPortfolioTransactions(portfolio);
	}

	// Get portfolios owned by a user
	public ArrayList<Portfolio> getPortfolios(String userID) {
		if (findUser(userID) == null)
			return null;
		ArrayList<Portfolio> portfolios = new ArrayList<Portfolio>();
		try {
			PreparedStatement queryStatement = sql.prepareStatement("SELECT * FROM Portfolios WHERE userID = ?");
			queryStatement.setString(1, userID);
			ResultSet results = queryStatement.executeQuery();

			while (results.next()) {
				int portfolioID = results.getInt(1);
				Portfolio portfolio = new Portfolio(portfolioID, userID);
				// Get all transactions and add them
				ArrayList<Transaction> transactions = getTransactions(portfolioID);
				for (int i = 0; i < transactions.size(); i++) {
					portfolio.addTransaction(transactions.get(i));
				}
				// Add the portfolio to the array
				portfolios.add(portfolio);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return portfolios;
	}

	// Get a unique portfolioID
	public int getUniquePortfolioID() {
		int portfolioID = 1000;
		try {
			PreparedStatement queryStatement = sql.prepareStatement("SELECT MAX(portfolioID) FROM Portfolios");
			ResultSet results = queryStatement.executeQuery();

			if (!results.next())
				return portfolioID;
			portfolioID = results.getInt(1) + 1;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return portfolioID;
	}

	// Get a unique transactionID
	public int getUniqueTransactionID() {
		int transactionID = 2000;
		try {
			PreparedStatement queryStatement = sql.prepareStatement("SELECT MAX(transactionID) FROM Transactions");
			ResultSet results = queryStatement.executeQuery();

			while (results.next()) {
				transactionID = results.getInt(1) + 1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return transactionID;
	}

	public Stock getStockFromTicker(String ticker) {
		for (int i = 0; i < stocks.size(); i++) {
			if (stocks.get(i).getTicker().equals(ticker))
				return stocks.get(i);
		}
		return null;
	}

	// Get all the transactions for a particular portfolio
	public ArrayList<Transaction> getTransactions(int portfolioID) {
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		try {
			PreparedStatement queryStatement = sql.prepareStatement("SELECT * FROM Transactions WHERE portfolioID = ?");
			queryStatement.setInt(1, portfolioID);
			ResultSet results = queryStatement.executeQuery();

			while (results.next()) {
				String ticker = results.getString(5);
				Stock stock = getStockFromTicker(ticker);
				Transaction transaction = new Transaction(results.getInt(1), results.getString(2),
						(double) results.getFloat(3), (double) results.getFloat(4), stock);
				transactions.add(transaction);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return transactions;
	}
}
