package StartUpSupport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Scanner;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



class DatabaseManager {
    private static final String url = "jdbc:mysql://localhost:3306/miniproject";
    private static final String username = "root";
    private static final String password = "Ankita@2006";

    static Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            // ✅ Load MySQL JDBC Driver (Important!)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // ✅ Establish Connection
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Database connected successfully!");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Database connection error!");
            e.printStackTrace();
        }
        return conn;
    }

    static void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class EmailValidator {
    private static final String EMAIL_PATTERN = 
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
        "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

class StartupManager {
	Scanner scanner=new Scanner(System.in);
	StartupManager(Connection conn,int startupId) throws SQLException,ClassNotFoundException{
		display(conn,startupId);
	}
	void display(Connection conn,int startupID) throws SQLException,ClassNotFoundException{
		while (true) {
            System.out.println("Menu:\n1) View all Investors\n2) Raise Fund Request\n3) Withdraw Request\n4) Success Story\n5) Logout");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    viewAllInvestors(conn,startupID);
                    break;
                case 2:
                    raiseFundRequest(conn,startupID);
                    break;
                case 3:
                    withdrawRequest(conn,startupID);
                    break;
                case 4:
                	successStory(conn);
                	break;
                case 5:
                    System.out.println("Logged out successfully.");
                    return; 
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
	}
	

	 void viewAllInvestors(Connection conn,int startupId) throws SQLException {
		 String queryView = "SELECT * FROM investorView";
		    try (PreparedStatement pstmt = conn.prepareStatement(queryView);
		         ResultSet rs = pstmt.executeQuery()) {

		        System.out.println("+---------------+--------------+-------------------+");
		        System.out.println("| InvestorName  | IndustryName | InvestorEmail     |");
		        System.out.println("+---------------+--------------+-------------------+");

		        while (rs.next()) {
		            String investorName = rs.getString("InvestorName");
		            String industryName = rs.getString("IndustryName");
		            String investorEmail = rs.getString("InvestorEmail");

		            System.out.printf("| %-13s | %-12s | %-17s |\n", investorName, industryName, investorEmail);
		        }

		        System.out.println("+---------------+--------------+-------------------+");
		    }
	}

	void raiseFundRequest(Connection conn, int startupId)throws ClassNotFoundException {
		try { 
			String checkQuery = "SELECT COUNT(*) AS numRequests FROM request WHERE StartupID = ? AND Status = 'R'";
			try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
				checkStmt.setInt(1, startupId);
				try (ResultSet rs = checkStmt.executeQuery()) {
					if (rs.next()) {
						int numRequests = rs.getInt("numRequests");
						if (numRequests > 0) {
							System.out.println("Error: The startup already has a running fund request.");
							return; 
						}
					}
				}
			}

			String sql = "INSERT INTO request (StartupID, RequestType, Amount, Description, Status) VALUES (?, ?, ?, ?, ?)";
			System.out.println("Enter the type of request: ");
			String requestType = scanner.next();
			System.out.println("Enter the amount to be requested: ");
			double amountRequested = scanner.nextDouble();
			System.out.println("Enter the description: ");
			String description = scanner.next();
			description += scanner.next();
			String status = "R";

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, startupId);
				pstmt.setString(2, requestType);
				pstmt.setDouble(3, amountRequested);
				pstmt.setString(4, description);
				pstmt.setString(5, status);
				int rowsInserted = pstmt.executeUpdate();
				if (rowsInserted > 0) {
					System.out.println("Fund request raised successfully.");
					MatchingAlgorithm matches=new MatchingAlgorithm();
					matches.match(conn,startupId);
		                
				} else {
					System.out.println("Failed to raise fund request.");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error raising fund request: " + e.getMessage());
		}
	}

	void withdrawRequest(Connection conn,int startupId) {
		try {
			String sql = "UPDATE request SET Status = 'W' WHERE StartupID = ? AND Status = 'R'";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setInt(1, startupId);
				int rowsUpdated = pstmt.executeUpdate();
				if (rowsUpdated > 0) {
					System.out.println("Fund request(s) withdrawn successfully.");
				} else {
					System.out.println("No pending fund requests found to withdraw.");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error withdrawing fund request: " + e.getMessage());
		}
	}
	void successStory(Connection conn) {
	    try {
	        
	        SuccessStoryManager successStoryManager = new SuccessStoryManager(conn);
	        successStoryManager.displayAllSuccessStories();
	    } catch (SQLException e) {
	        System.out.println("Error displaying success stories: " + e.getMessage());
	    }
	}}
class InvestorManager {
	Scanner scanner = new Scanner(System.in);
    ArrayList<Integer> matching = new ArrayList<>();

    InvestorManager(Connection conn, int investor_ID) throws SQLException, ClassNotFoundException {
        display(conn, investor_ID);
    }

    void display(Connection conn, int investor_ID) throws SQLException, ClassNotFoundException {
        while (true) {
            System.out.println("Menu:\n1) View all Startups\n2) Show matched Startups\n3) Fund a startup\n4) Logout");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    viewAllStartups(conn, investor_ID);
                    break;
                case 2:
                    showAllRequests(conn, investor_ID);
                    break;
                case 3:
                    FundAStartUp(conn, investor_ID);
                    break;
                case 4:
                    System.out.println("Logged out successfully.");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    void viewAllStartups(Connection conn, int investor_ID) throws SQLException {
        String queryView = "SELECT * FROM startupView";
        try (PreparedStatement pstmt = conn.prepareStatement(queryView);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("+--------------+------------------+------------------------------------------------------------------------------------+---------------------+");
            System.out.println("| StartupName  | StartupIndustry | StartupDescription                                                                     | StartupEmail        |");
            System.out.println("+--------------+------------------+------------------------------------------------------------------------------------+---------------------+");

            while (rs.next()) {
                String startupName = rs.getString("Startup_name");
                String startupIndustry = rs.getString("Startup_industry");
                String startupDescription = rs.getString("startup_description");
                String startupEmail = rs.getString("startup_email");
                System.out.printf("| %-12s | %-15s | %-82s | %-19s |\n", startupName, startupIndustry, startupDescription, startupEmail);
            }
            System.out.println("+--------------+------------------+------------------------------------------------------------------------------------+---------------------+");
        }
    }

    public void showAllRequests(Connection conn, int investor_ID) throws SQLException {
        String query = "SELECT s.Startup_no, s.Startup_name, s.Startup_description, r.RequestType, r.Amount, r.Description " +
                "FROM request r " +
                "JOIN startup s ON r.StartupID = s.Startup_no " +
                "JOIN domain d ON s.Startup_industry = d.Preference " +
                "WHERE d.Investor_ID = ? AND r.Status = 'R'";
 try (PreparedStatement pstmt = conn.prepareStatement(query)) {
     pstmt.setInt(1, investor_ID);
     try (ResultSet rs = pstmt.executeQuery()) {
         if (!rs.isBeforeFirst()) {
             System.out.println("No matching fund requests found.");
         } else {
             System.out.println("Fund Requests Matched to Investor:");
             System.out.println("+-----------------+--------------+------------------------+---------------+------------+-------------------+");
             System.out.println("| Startup Number  | Startup Name | Startup Description    | Request Type  | Amount     | Description       |");
             System.out.println("+-----------------+--------------+------------------------+---------------+------------+-------------------+");
             while (rs.next()) {
                 int startupNumber = rs.getInt("Startup_no");
                 String startupName = rs.getString("Startup_name");
                 String startupDescription = rs.getString("Startup_description");
                 String requestType = rs.getString("RequestType");
                 double amount = rs.getDouble("Amount");
                 String description = rs.getString("Description");

                 System.out.printf("| %-15s | %-12s | %-22s | %-13s | %-10.2f | %-17s |\n", startupNumber, startupName, startupDescription, requestType, amount, description);
             }
             System.out.println("+-----------------+--------------+------------------------+---------------+------------+-------------------+");
         }
     }
 }
}

    void FundAStartUp(Connection conn, int investor_ID) throws SQLException {
        boolean flag = false;
        int id = 0;
        while (!flag) {
            System.out.println("Please enter the id of the start-up you are interested in funding: ");
            id = scanner.nextInt();

            for (int i = 0; i < matching.size(); i++) {
                if (id == matching.get(i)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                System.out.println("Please enter a valid id.");
            }
        }
        if (investor_ID != -1) {
            String query = "INSERT INTO ReceivedFunding (Startup_no, InvestorNumber) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, id);
                pstmt.setInt(2, investor_ID);
                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Funding details inserted successfully.");
                    String query1 = "UPDATE request SET Status='S' WHERE StartupID=?";
                    try (PreparedStatement pstmt1 = conn.prepareStatement(query1)) {
                        pstmt1.setInt(1, id);
                        int rowsUpdated = pstmt1.executeUpdate();
                        if (rowsUpdated > 0) {
                            System.out.println("Status updated successfully.");
                        } else {
                            System.out.println("Failed to update status.");
                        }
                    }
                } else {
                    System.out.println("Failed to insert funding details.");
                }
            }
        } else {
            System.out.println("Error: Failed to retrieve investor ID.");
        }
    }
}

class Info {
	static Scanner sc = new Scanner(System.in);

	static void InfoStartUp(Connection conn,String username)throws SQLException, ParseException {
	
		String query1 = "INSERT INTO StartUp (Startup_name, Startup_description, Startup_industry, "
				+ "Startup_founding_year, Startup_email,User_ID) VALUES (?, ?, ?, ?, ?,?)";
		String query2 = "INSERT INTO contactDetails (EmailAddress, Phno, WebsiteLink) VALUES (?, ?, ?)";
		String query3 = "INSERT INTO FoundingMembers (s_symbol, s_no, Members) VALUES (?, ?, ?)";
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		ResultSet rs = null;
		try {
			conn.setAutoCommit(false); 
			pstmt2 = conn.prepareStatement(query2);
			String mail;
			while(true) {
				System.out.println("Enter the Email of StartUp: ");
				 mail = sc.next();
				 if (!EmailValidator.isValidEmail(mail)) {
			            System.out.println("Invalid email format. Please enter a valid email address.");
			            
			        }
				 else {
					 break;
				 }
			}
			
			long phno;
		  while(true) {
			  System.out.println("Enter the phone number: ");
				 phno = sc.nextLong();
			String phno1=Long.toString(phno);
			if(phno1.length()!=10) {
				System.out.println("Enter correct number");
			}
			else {
				break;
			}
		  }
			
			System.out.println("Enter the website link: ");
			String website = sc.next();
			pstmt2.setString(1, mail);
			pstmt2.setLong(2, phno);
			pstmt2.setString(3, website);
			int rowsInserted = pstmt2.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("inserted successfully.");

			}
			pstmt1 = conn.prepareStatement(query1, Statement.RETURN_GENERATED_KEYS);
			System.out.println("Enter the name of your StartUp: ");
			String name = sc.nextLine();
			name+=sc.nextLine();
			System.out.println("Enter the description of your StartUp: ");
			String description = sc.nextLine();
			System.out.println("Enter the domain of your StartUp: ");
			String domain = sc.next();
			System.out.println("Enter the founding year of your StartUp (yyyy-MM-dd): ");
			String dateString = sc.next();
			java.util.Date utilDate = parseDate(dateString);


			pstmt1.setString(1, name);
			pstmt1.setString(2, description);
			pstmt1.setString(3, domain);
			java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
			pstmt1.setDate(4, sqlDate);
			pstmt1.setString(5, mail);
			pstmt1.setString(6, username);
			pstmt1.executeUpdate();
			rs = pstmt1.getGeneratedKeys();
			int startupId = 0;
			if (rs.next()) {
				startupId = rs.getInt(1);
			} else {
				throw new SQLException("Failed to retrieve generated startup ID");
			}

			pstmt3 = conn.prepareStatement(query3);
			pstmt3.setString(1, "S");
			pstmt3.setInt(2, startupId);
			System.out.println("Enter the names of founding members (comma-separated): ");
			String membersInput = sc.next();
			String[] members = membersInput.split(",");
			for (String member : members) {
				pstmt3.setString(3, member);
				pstmt3.executeUpdate();
			}
			conn.commit(); 
			System.out.println("Startup information inserted successfully.");
		} catch (SQLException e) {
			System.out.println("Error inserting startup information: " + e.getMessage());
			if (conn != null) {
				conn.rollback(); 
			}
		} finally {
			if (pstmt1 != null) {
				pstmt1.close();
			}
			if (pstmt2 != null) {
				pstmt2.close();
			}
			if (pstmt3 != null) {
				pstmt3.close();
			}
			if (rs != null) {
				rs.close();
			}
			conn.setAutoCommit(true); 
			conn.close();
		}
	}

	static void InfoInvestor(Connection conn,String username) throws SQLException, ParseException{
		String query1 = "INSERT INTO Investor (InvestorName, IndustryName, InvestorEmail,IndustryDomain,User_ID) VALUES (?, ?, ?,?,?)";
		String query2 = "INSERT INTO contactDetails (EmailAddress, Phno, WebsiteLink) VALUES (?, ?, ?)";
		String query3 = "INSERT INTO Domain (Investor_ID, Preference) VALUES (?, ?)";
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		ResultSet rs = null;
		try {
			conn.setAutoCommit(false); 
			pstmt2 = conn.prepareStatement(query2);
			System.out.println("Enter Email: ");
			String mail = sc.next();
			System.out.println("Enter the phone number: ");
			long phno = sc.nextLong();
			System.out.println("Enter the website link: ");
			String website = sc.next();
			pstmt2.setString(1, mail);
			pstmt2.setLong(2, phno);
			pstmt2.setString(3, website);
			pstmt2.executeUpdate();
			pstmt1 = conn.prepareStatement(query1);
			sc.nextLine(); 
			System.out.println("Enter your name: ");
			String name = sc.nextLine();
			System.out.println("Enter the industry name: ");
			String indName = sc.nextLine();
			System.out.println("Enter the domain of your work");
			String invdomain=sc.nextLine();
			pstmt1.setString(1, name);
			pstmt1.setString(2, indName);
			pstmt1.setString(3, mail);
			pstmt1.setString(4, invdomain);
			pstmt1.setString(5, username);
			pstmt1.executeUpdate();
			pstmt1 = conn.prepareStatement("SELECT LAST_INSERT_ID()");
			rs = pstmt1.executeQuery();
			int investorId = 0;
			if (rs.next()) {
				investorId = rs.getInt(1);
			} else {
				throw new SQLException("Failed to retrieve investor ID");
			}
			System.out.println("Enter the industry domains you're interested in (comma-separated): ");
			String domainsInput = sc.nextLine();
			String[] domains = domainsInput.split(",");
			pstmt3 = conn.prepareStatement(query3);
			for (String domain : domains) {
				pstmt3.setInt(1, investorId);
				pstmt3.setString(2, domain.trim());
				pstmt3.executeUpdate();
			}

			conn.commit(); 
			System.out.println("Investor information inserted successfully.");
		} catch (SQLException e) {
			System.out.println("Error inserting investor information: " + e.getMessage());
			if (conn != null) {
				conn.rollback(); 
			}
		} finally {
			if (pstmt1 != null) {
				pstmt1.close();
			}
			if (pstmt2 != null) {
				pstmt2.close();
			}
			if (pstmt3 != null) {
				pstmt3.close();
			}
			if (rs != null) {
				rs.close();
			}
			conn.setAutoCommit(true); 
			conn.close();
		}
	}
	static java.util.Date parseDate(String dateString) {
	    java.text.SimpleDateFormat[] formats = {
	            new java.text.SimpleDateFormat("yyyy-MM-dd"),
	            new java.text.SimpleDateFormat("dd/MM/yyyy"),
	    };
	    for (java.text.SimpleDateFormat format : formats) {
	        try {
	            return format.parse(dateString);
	        } catch (java.text.ParseException e) {
	           
	        }
	    }
	  
	    System.out.println("Invalid date format. Please enter the founding year in yyyy-MM-dd format: ");
	    dateString = sc.next();
	    
	    return parseDate(dateString);
	}
}


public class connect {
	 private static final String SALT = "random_salt";
	public static String hashPassword(String password) {
	    try {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        String saltedPassword = password + SALT; 
	        md.update(saltedPassword.getBytes());
	        byte[] bytes = md.digest();
	        StringBuilder sb = new StringBuilder();
	        for (byte aByte : bytes) {
	            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
	        }
	        return sb.toString();
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	
	    public static boolean checkPassword(String enteredPassword, String hashedPassword) {
	        String hashedEnteredPassword = hashPassword(enteredPassword);
	        return hashedEnteredPassword.equals(hashedPassword);
	    }
}
