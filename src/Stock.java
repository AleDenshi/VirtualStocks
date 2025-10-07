import java.sql.Date;
import java.util.Hashtable;
import java.util.Map;

public class Stock {

	private String ticker;
	private String stockName;
	private double currentPrice;
	private double capitalization;
	private Map<Date, Double> prices;
	
	// Once created, a stock's ticker and name cannot be changed
	public Stock(String ticker, String stockName) {
		prices = new Hashtable<>();
		this.ticker = ticker;
		this.stockName = stockName;
		this.currentPrice = 0.0;
		this.capitalization = 0.0;
	}

	public double getDatedPrice(Date date) {
		return prices.get(date);
	}
	
	// Sets the historical price map and the current price
	public void setPrices(Map<Date, Double> prices, Date today) {
		this.prices = prices;
		this.currentPrice = prices.get(today);
	} 
	
	public String getTicker() {
		return ticker;
	}

	public String getStockName() {
		return stockName;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public double getCapitalization() {
		return capitalization;
	}

	public void setCapitalization(double capitalization) {
		this.capitalization = capitalization;
	}
	
	public String toString() {
		return stockName + "(" + ticker + ")\t"+ currentPrice + ",\t Cap: " + capitalization;
	}

}
