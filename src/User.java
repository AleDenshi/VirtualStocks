import java.util.ArrayList;

public class User {
	private String userID;
	private String firstName;
	private String lastName;
	private String passwdHash;
	private ArrayList<Portfolio> portfolios;
	
	public User(String userID, String firstName, String lastName, String passwdHash) {
		this.userID = userID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.passwdHash = passwdHash;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getPasswdHash() {
		return passwdHash;
	}
	public void setPasswdHash(String passwdhash) {
		this.passwdHash = passwdhash;
	}
	
	public String getUserID() {
		return userID;
	}

	public String toString() {
		String userInfo;
		userInfo = "UserID:\t" + userID;
		userInfo += "\nName:\t" + firstName + " " + lastName;
		return userInfo;
	}
	
	public Portfolio getPortfolioByID(int portfolioID) {
		for (int i = 0; i < portfolios.size(); i++) {
			if (portfolios.get(i).getPortfolioID() == portfolioID) return portfolios.get(i);
		}
		return null;
	}
	
	public Portfolio getPortfolioByIndex(int index) {
		return portfolios.get(index);
	}
	
}
