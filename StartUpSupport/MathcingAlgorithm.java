package StartUpSupport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class MatchingAlgorithm{
	 public void match(Connection conn,int startup_ID) throws SQLException {
		 String startupDomain = null;
		    String startupQuery = "SELECT Startup_industry FROM startup WHERE startup_no = ?";
		    try (PreparedStatement startupStmt = conn.prepareStatement(startupQuery)) {
		        startupStmt.setInt(1, startup_ID);
		        ResultSet startupResult = startupStmt.executeQuery();
		        if (startupResult.next()) {
		            startupDomain = startupResult.getString("Startup_industry");
		        } else {
		            System.out.println("Startup not found.");
		            return;
		        }
		    }
		    String investorQuery = "SELECT InvestorNumber, InvestorName, InvestorEmail, IndustryName FROM investor WHERE InvestorNumber IN (SELECT Investor_ID FROM domain WHERE Preference = ?)";
		    try (PreparedStatement investorStmt = conn.prepareStatement(investorQuery)) {
		        investorStmt.setString(1, startupDomain);
		        ResultSet investorResult = investorStmt.executeQuery();
		        boolean matchFound = false;
		        while (investorResult.next()) {
		            matchFound = true;
		            System.out.println("Investor Name: " + investorResult.getString("InvestorName"));
		            System.out.println("Investor Email: " + investorResult.getString("InvestorEmail"));
		            System.out.println("Investor Industry: "+investorResult.getString("IndustryName"));
		            System.out.println(); 
		        }

		       
		        if (!matchFound) {
		            System.out.println("No investor found matching the startup's domain.");
		        }
		    }
			}

	public static void main(String args[]) {
		
	}
}

