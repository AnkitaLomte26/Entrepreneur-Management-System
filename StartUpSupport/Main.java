package StartUpSupport;

import StartUpSupport.connect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws ParseException, ClassNotFoundException {
		Scanner scanner = new Scanner(System.in);
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			System.out.println("1) Login as a startup\n2) Login as an investor\n3) Signup as a startup\n4) Signup as an investor\n5)Show History");
			int choice = scanner.nextInt();
			scanner.nextLine(); 
			switch (choice) {
			case 1:
				startupLoginFlow(scanner, conn);
				String query1="Insert  into log  (Action) values (StartupLogin)";
				PreparedStatement pstmt1=conn.prepareStatement(query1);
				pstmt1.executeUpdate();
				break;
			case 2:
				investorLoginFlow(scanner, conn);
				String query2="Insert  into log  (Action) values ('InvestorLogin')";
				PreparedStatement pstmt2=conn.prepareStatement(query2);
				pstmt2.executeUpdate();
				break;
			case 3:
				startupSignupFlow(scanner, conn);
				String query3="Insert  into log  (Action) values ('StartupSignup')";
				PreparedStatement pstmt3=conn.prepareStatement(query3);
				pstmt3.executeUpdate();
				break;
			case 4:
				investorSignupFlow(scanner, conn);
				String query4="Insert  into log  (Action) values ('InvestorSignup')";
				PreparedStatement pstmt4=conn.prepareStatement(query4);
				pstmt4.executeUpdate();
				break;
				
			case 5:
				String query5="Insert  into log  (Action) values('HistorySearch')";
				PreparedStatement pstmt5=conn.prepareStatement(query5);
				pstmt5.executeUpdate();
				String query="Select * from receivedfunding";
				PreparedStatement pstmt=conn.prepareStatement(query);
				ResultSet rs=pstmt.executeQuery();
				while(rs.next()) {
					System.out.println("Startup number "+rs.getInt(1));
					System.out.println("Investor number "+rs.getInt(2));
				}
				break;
				
		
			default:
				System.out.println("Invalid choice.");
			}
		} catch (SQLException e) {
			System.out.println("Database connection error: " + e.getMessage());
		} finally {
			DatabaseManager.closeResources(conn, null, null);
			scanner.close();
		}
	}

	static void startupLoginFlow(Scanner scanner, Connection conn) throws ClassNotFoundException {
		System.out.println("Startup Login");
		System.out.println("Enter username: ");
		String username = scanner.nextLine();
		System.out.println("Enter password: ");
		String password = scanner.nextLine();

		try {
			boolean chk=authenticateStartup(conn,username,password);
			if(chk) {
				System.out.println("Startup logged in successfully.");
				String id_query = "SELECT Startup_no FROM startup WHERE User_ID=?";
				try (PreparedStatement pstmt = conn.prepareStatement(id_query)) {
					pstmt.setString(1, username); 
					try (ResultSet rs = pstmt.executeQuery()) {
						if (rs.next()) {
							int startup_no = rs.getInt("Startup_no");
							StartupManager s_menu = new StartupManager(conn, startup_no);
						} else {
							System.out.println("No startup found for the given username.");
						}
					}
				} catch (SQLException e) {
					System.out.println("Error executing query: " + e.getMessage());
				}

			}
			else {
				System.out.println("Invalid username or password.");
			}
					} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	static void investorLoginFlow(Scanner scanner, Connection conn) throws ClassNotFoundException {
		System.out.println("Investor Login");
		System.out.println("Enter username: ");
		String username = scanner.nextLine();
		System.out.println("Enter password: ");
		String password = scanner.nextLine();

		try {
			boolean chk = authenticateInvestor(conn, username, password);
			if(chk) {
				System.out.println("Investor logged in successfully");
				String id_query="SELECT InvestorNumber from Investor WHERE User_ID=?";
				try (PreparedStatement pstmt = conn.prepareStatement(id_query)){
					pstmt.setString(1, username); 
					try (ResultSet rs = pstmt.executeQuery()) {
						if (rs.next()) {
							int investor_no = rs.getInt("InvestorNumber");
							InvestorManager i_menu = new InvestorManager(conn, investor_no);
						} else {
							System.out.println("No investor found for the given username.");
						}
					}
				}
				
			}

			
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	static void startupSignupFlow(Scanner scanner, Connection conn) throws ParseException, ClassNotFoundException {
		System.out.println("Startup Signup");
		System.out.println("Enter username: ");
		String username = scanner.nextLine();
		System.out.println("Enter password: ");
		String password = scanner.nextLine();

		try {
			String hashedPassword = connect.hashPassword(password);
			
			String sql = "INSERT INTO User (UserName, Password, UserType) VALUES (?, ?, 'startup')";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, username);
				pstmt.setString(2, hashedPassword);
				int rowsInserted = pstmt.executeUpdate();
				if (rowsInserted > 0) {
					System.out.println("Startup signed up successfully.");
					Info.InfoStartUp(conn,username);
					//startupLoginFlow(scanner,conn);
				} else {
					System.out.println("Failed to sign up.");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	static void investorSignupFlow(Scanner scanner, Connection conn) throws ParseException, ClassNotFoundException {
		System.out.println("Investor Signup");
		System.out.println("Enter username: ");
		String username = scanner.nextLine();
		System.out.println("Enter password: ");
		String password = scanner.nextLine();

		try {
			String hashedPassword = connect.hashPassword(password);
			String sql = "INSERT INTO user (UserName, Password, UserType) VALUES (?, ?, 'investor')";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, username);
				pstmt.setString(2, hashedPassword);
				int rowsInserted = pstmt.executeUpdate();
				if (rowsInserted > 0) {
					System.out.println("Investor signed up successfully.");
					Info.InfoInvestor(conn,username);
					//investorLoginFlow(scanner,conn);
				} else {
					System.out.println("Failed to sign up.");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	static boolean authenticateStartup(Connection conn, String username, String password) throws SQLException {
		 String sql = "SELECT Password FROM user WHERE Username = ?";
		    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
		        pstmt.setString(1, username);
		        try (ResultSet rs = pstmt.executeQuery()) {
		            if (rs.next()) {
		                String storedPassword = rs.getString("Password");
		                return connect.checkPassword(password, storedPassword);
		             
		            }
		        }
		    } catch (SQLException e) {
		        System.out.println("Error authenticating startup: " + e.getMessage());
		        return false; 
		    }
		    return false;
	}

	static boolean authenticateInvestor(Connection conn, String username, String password) throws SQLException {
		String sql="SELECT Password FROM user WHERE Username=?";
		try(PreparedStatement pstmt=conn.prepareStatement(sql)){
			pstmt.setString(1,username);
			try(ResultSet rs=pstmt.executeQuery()){
				if(rs.next()) {
					String storedPassword=rs.getString("Password");
					 return connect.checkPassword(password, storedPassword);
					
				}
			}
		}
		catch (SQLException e) {
	        System.out.println("Error authenticating startup: " + e.getMessage());
	        return false; 
	    }
	    return false;
		
	}
}
// javac -cp ".;mysql-connector-j-9.2.0.jar" StartUpSupport/*.java
// java -cp ".;mysql-connector-j-9.2.0.jar" StartUpSupport.Main