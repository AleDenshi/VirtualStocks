-- Alessandro Gaggioli
-- VirtualStocks Schema

-- Creating all the tables
-- Stock info table
CREATE TABLE IF NOT EXISTS Stocks (
  ticker VARCHAR(5),
  stockName VARCHAR(25),
  currentPrice FLOAT,
  capitalization FLOAT,
  PRIMARY KEY (ticker)
);

-- Day price table
CREATE TABLE IF NOT EXISTS DayPrices (
  pricingDate DATE,
  ticker VARCHAR(5),
  dayPrice FLOAT,
  PRIMARY KEY (ticker, pricingDate),
  FOREIGN KEY (ticker) REFERENCES Stocks(ticker) ON DELETE CASCADE
);

-- User info table
CREATE TABLE IF NOT EXISTS Users (
  userID VARCHAR(25),
  firstName VARCHAR(25),
  lastName VARCHAR(25),
  passwdHash VARCHAR(65),
  PRIMARY KEY (userID)
);

-- Portfolio table
CREATE TABLE IF NOT EXISTS Portfolios (
  portfolioID INT,
  value FLOAT,
  userID VARCHAR(25),
  PRIMARY KEY (portfolioID),
  FOREIGN KEY (userID) REFERENCES Users(userID) ON DELETE CASCADE
);

-- Portfolio holdings table
CREATE TABLE IF NOT EXISTS Holds (
  ticker VARCHAR(5),
  portfolioID INT,
  shares FLOAT,
  buyInAmount FLOAT,
  PRIMARY KEY (ticker, portfolioID),
  FOREIGN KEY (ticker) REFERENCES Stocks(ticker),
  FOREIGN KEY (portfolioID) REFERENCES Portfolios(portfolioID) ON DELETE CASCADE
);

-- Transaction table
CREATE TABLE IF NOT EXISTS Transactions (
  transactionID INT,
  transactionDate DATE,
  amount FLOAT,
  sharesBought FLOAT,
  ticker VARCHAR(5),
  portfolioID INT,
  PRIMARY KEY (transactionID),
  FOREIGN KEY (portfolioID) REFERENCES Portfolios(portfolioID) ON DELETE CASCADE
);

-- Inserting data into the tables
-- Insert Stocks
INSERT INTO Stocks VALUES 
('AAPL', 'Apple Inc.', 175.50, 2500000000000),
('GOOGL', 'Alphabet Inc.', 2800.75, 1800000000000),
('TSLA', 'Tesla Inc.', 900.25, 950000000000),
('MSFT', 'Microsoft Corp.', 320.80, 2400000000000);

-- Insert dayPrices (data for the stocks)
INSERT INTO DayPrices VALUES 
('2024-03-06', 'AAPL', 175.50),
('2024-03-06', 'GOOGL', 2800.75),
('2024-03-06', 'TSLA', 900.25),
('2024-03-06', 'MSFT', 320.80);

-- Insert Users
INSERT INTO Users VALUES 
('jdoe', 'John', 'Doe', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd7eb629f99a647e4b4'),
('jsmith', 'Jane', 'Smith', '2c9341ca4cf3d87b9a9619c48b24df558f8a0616604f760207809f21b251cb35'),
('ajohnson', 'Alice', 'Johnson', 'f5d1278e8109edd94e1e4197e04873b28dd25144d6996da7dc20bd722a65d116');

-- Insert Portfolios
INSERT INTO Portfolios VALUES 
(1, 50000.00, 'jdoe'),
(2, 75000.50, 'jsmith'),
(3, 30000.75, 'ajohnson');

-- Insert Holds (portfolios holding stocks)
INSERT INTO Holds VALUES 
('AAPL', 1, 50, 160.00),
('GOOGL', 1, 10, 2700.00),
('TSLA', 2, 20, 850.00),
('MSFT', 3, 15, 300.00);

-- Insert Transactions
INSERT INTO Transactions VALUES 
(101, '2024-02-28', 8000.00, 50, 'AAPL', 1),
(102, '2024-02-25', 27000.00, 10, 'GOOGL', 1),
(103, '2024-02-26', 17000.00, 20, 'TSLA', 2),
(104, '2024-02-27', 4500.00, 15, 'MSFT', 3),
(105, '2024-02-27', 4500.00, 15, 'MSFT', 3);