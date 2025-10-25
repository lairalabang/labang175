package main;
import java.util.*;
import config.config;

public class Main {

    public static void viewStudents() {
        config db = new config();
        String sql = "SELECT * FROM student";
        String[] headers = {"ID", "Name", "Email", "Password", "Course"};
        String[] columns = {"student_id", "name", "email", "password", "course"};
        db.viewRecords(sql, headers, columns);
    }

    public static void viewApplication() {
        config db = new config();
        String sql = "SELECT * FROM application";
        String[] headers = {"Application id", "Student id", "Status", "date_applied"};
        String[] columns = {"application_id", "student_id", "status", "date_applied"};
        db.viewRecords(sql, headers, columns);
    }

    public static void viewUser() {
        config db = new config();
        String sql = "SELECT * FROM tbl_user";
        String[] headers = {"ID", "Name", "Email", "Type", "Status", "Password"};
        String[] columns = {"user_id", "name", "email", "type", "status", "password"};
        db.viewRecords(sql, headers, columns);
    }

    
    public static void studentMenu(String studentEmail) {
        config db = new config();
        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n=== STUDENT MENU ===");
            System.out.println("1. View Available Scholarships");
            System.out.println("2. Apply for Scholarship");
            System.out.println("3. View My Applications");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    String sql = "SELECT * FROM scholarship";
                    String[] headers = {"Scholarship ID", "Scholarship Name", "Description"};
                    String[] columns = {"scholarship_id", "scholarship_name", "description"};
                    db.viewRecords(sql, headers, columns);
                    break;

                case 2:
                    System.out.print("Enter Scholarship ID to apply: ");
                    int sid = sc.nextInt();
                    sc.nextLine();

                    String findStudent = "SELECT * FROM tbl_user WHERE email = ?";
                    List<Map<String, Object>> userData = db.fetchRecords(findStudent, studentEmail);

                    if (userData == null || userData.isEmpty()) {
                        System.out.println("Student not found.");
                        break;
                    }

                    int studentId = Integer.parseInt(userData.get(0).get("user_id").toString());

                    String checkApp = "SELECT * FROM application WHERE student_id = ? AND scholarship_id = ?";
                    List<Map<String, Object>> existingApp = db.fetchRecords(checkApp, studentId, sid);

                    if (existingApp != null && !existingApp.isEmpty()) {
                        System.out.println("⚠️ You have already applied for this scholarship.");
                        break;
                    }

                    String insertApp = "INSERT INTO application (student_id, scholarship_id, status) VALUES (?, ?, ?)";
                    db.addRecord(insertApp, studentId, sid, "Pending");
                    System.out.println("✅ Scholarship application submitted successfully!");
                    break;

                case 3:
                    viewApplication();
                            
                    break;

                case 4:
                    System.out.println("Logging out...");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 4);

        sc.close();
    }

  
    public static void adminMenu() {
        config db = new config();
        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View All Users");
            System.out.println("2. Approve Pending Users");
            System.out.println("3. add student");
            System.out.println("4. View Scholarship Applications");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    viewUser();
                    break;

                case 2:
                    viewUser();
                    System.out.print("Enter User ID to approve: ");
                    int userId = sc.nextInt();
                    sc.nextLine();

                    String updateSql = "UPDATE tbl_user SET status = 'Approved' WHERE user_id = ?";
                    db.updateRecord(updateSql, userId);
                    System.out.println("✅ User approved successfully!");
                    break;

                case 3:
                  
                    break;

                case 4:
                    viewApplication();
                    
                            
                    break;

                case 5:
                    System.out.println("Logging out...");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 5);

        sc.close();
    }

    // 🔹 MAIN METHOD
    public static void main(String[] args) {
        config db = new config();
        Scanner sc = new Scanner(System.in);
        int option;

        do {
            System.out.println("\n=== WELCOME TO SCHOLARSHIP SYSTEM ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
            option = sc.nextInt();
            sc.nextLine();

            switch (option) {
                case 1:
                    System.out.print("Enter Email: ");
                    String email = sc.nextLine();

                    System.out.print("Enter Password: ");
                    String password = sc.nextLine();

                    String hashedLoginPassword = config.hashPassword(password);

                    String qry = "SELECT * FROM tbl_user WHERE email = ? AND password = ? AND status = ?";
                    List<Map<String, Object>> resultList = db.fetchRecords(qry, email, hashedLoginPassword, "Approved");

                    if (resultList == null || resultList.isEmpty()) {
                        System.out.println("❌ Invalid credentials or account not approved yet.");
                    } else {
                        Map<String, Object> user = resultList.get(0);
                        String userName = user.get("name").toString();
                        String userType = user.get("type").toString();

                        System.out.println("\n✅ Login Successful! Welcome " + userName + " (" + userType + ")");

                        if (userType.equalsIgnoreCase("Admin")) {
                            adminMenu();
                        } else if (userType.equalsIgnoreCase("Student")) {
                            studentMenu(email);
                        } else {
                            System.out.println("Unknown user type.");
                        }
                    }
                    break;

                case 2:
                    System.out.println("\n=== REGISTRATION ===");
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Email: ");
                    String Email = sc.nextLine();

                    String qry2 = "SELECT * FROM tbl_user WHERE email = ?";
                    List<Map<String, Object>> existing = db.fetchRecords(qry2, Email);

                    while (existing != null && !existing.isEmpty()) {
                        System.out.println("Email already exists. Enter new Email:");
                        Email = sc.nextLine();
                        existing = db.fetchRecords(qry2, Email);
                    }

                    System.out.print("Enter User Type (1 - Admin / 2 - Student): ");
                    int type = sc.nextInt();
                    sc.nextLine();

                    while (type < 1 || type > 2) {
                        System.out.print("Invalid, choose between 1 & 2 only: ");
                        type = sc.nextInt();
                        sc.nextLine();
                    }

                    String tp = (type == 1) ? "Admin" : "Student";

                    System.out.print("Enter Password: ");
                    String pass = sc.nextLine();

                    String hashedPassword = config.hashPassword(pass);

                    String sqlInsert = "INSERT INTO tbl_user(name, email, type, status, password) VALUES(?,?,?,?,?)";
                    db.addRecord(sqlInsert, name, Email, tp, "Pending", hashedPassword);

                    System.out.println("\n✅ Registration Successful! Awaiting admin approval.");
                    break;

                case 3:
                    System.out.println("\n👋 Thank you for using the Scholarship System. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Try again.");
            }

        } while (option != 3);

        sc.close();
    }
}
