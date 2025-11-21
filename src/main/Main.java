package main;

import java.util.*;
import config.config;

public class Main {

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
                    String hashedLogin = config.hashPassword(password);

                    String qry = "SELECT * FROM tbl_user WHERE email = ? AND password = ? AND status = ?";
                    List<Map<String, Object>> resultList = db.fetchRecords(qry, email, hashedLogin, "Approved");

                    if (resultList == null || resultList.isEmpty()) {
                        System.out.println("❌ Invalid credentials or not approved yet.");
                    } else {
                        Map<String, Object> user = resultList.get(0);
                        int userId = Integer.parseInt(user.get("user_id").toString());
                        String userName = user.get("name").toString();
                        String userType = user.get("type").toString();

                        System.out.println("\n✅ Login Successful! Welcome " + userName + " (" + userType + ")");

                        if (userType.equalsIgnoreCase("Admin"))
                            AdminMenu.show();
                        else if (userType.equalsIgnoreCase("Student"))
                            StudentMenu.show(userId);
                    }
                    break;

                case 2:
                    System.out.println("\n=== USER REGISTRATION ===");
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Email: ");
                    String regEmail = sc.nextLine();

                    String checkEmail = "SELECT * FROM tbl_user WHERE email = ?";
                    List<Map<String, Object>> existing = db.fetchRecords(checkEmail, regEmail);
                    while (existing != null && !existing.isEmpty()) {
                        System.out.println("⚠️ Email already exists. Try again: ");
                        regEmail = sc.nextLine();
                        existing = db.fetchRecords(checkEmail, regEmail);
                    }

                    System.out.print("Enter Password: ");
                    String pass = sc.nextLine();
                    String hashedPassReg = config.hashPassword(pass);

                    // ===== Select Role =====
                    System.out.println("Select Role:");
                    System.out.println("1. Student");
                    System.out.println("2. Admin");
                    System.out.print("Enter choice: ");
                    int roleChoice = sc.nextInt();
                    sc.nextLine();

                    String role = "Student"; // default
                    String status = "Pending"; // default

                    if (roleChoice == 1) {
                        role = "Student";
                        status = "Pending"; // Students need admin approval
                    } else if (roleChoice == 2) {
                        role = "Admin";
                        status = "Approved"; // Admins can be directly approved (or change to Pending if you want)
                    } else {
                        System.out.println("Invalid role selected. Defaulting to Student.");
                    }
                    // =======================

                    System.out.print("Enter Course (if applicable, otherwise leave blank): ");
                    String course = sc.nextLine();

                    // Insert into tbl_user
                    String sqlInsert = "INSERT INTO tbl_user(name, email, type, status, password) VALUES(?,?,?,?,?)";
                    db.addRecord(sqlInsert, name, regEmail, role, status, hashedPassReg);

                    // Get new user ID
                    String getNewId = "SELECT user_id FROM tbl_user WHERE email = ?";
                    List<Map<String, Object>> data = db.fetchRecords(getNewId, regEmail);
                    int newId = Integer.parseInt(data.get(0).get("user_id").toString());

                    // Insert into student table if role is Student
                    if (role.equalsIgnoreCase("Student")) {
                        String insertStudent = "INSERT INTO student(student_id, name, email, password, course) VALUES(?,?,?,?,?)";
                        db.addRecord(insertStudent, newId, name, regEmail, hashedPassReg, course);
                    }

                    System.out.println("✅ Registration complete! Awaiting admin approval if Student.");
                    break;

                case 3:
                    System.out.println("👋 Thank you for using the system!");
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        } while (option != 3);
        sc.close();
    }
}