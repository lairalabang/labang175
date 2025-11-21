package main;

import java.util.*;
import java.time.LocalDate;
import config.config;

public class AdminMenu {

    public static void show() {
        config db = new config();
        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View All Students");
            System.out.println("2. View & Approve Users");
            System.out.println("3. Add Student");
            System.out.println("4. View Scholarship Applications");
            System.out.println("5. Review Applications");
            System.out.println("6. Update Application Status");
            System.out.println("7. Manage Scholarships");
            System.out.println("8. Generate Reports");
            System.out.println("9. Logout");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1: 
                    viewStudents(db);
                    break;
                case 2: 
                    viewUsers(db, sc); 
                    break;
                case 3:
                    addStudent(db, sc);
                    break;
                case 4: 
                    viewApplications(db);
                    break;
                case 5: 
                    reviewApplications(db, sc);
                    break;
                case 6: 
                    updateApplicationStatus(db, sc); 
                    break;
                case 7:
                    manageScholarships(db, sc);
                    break;
                case 8: 
                    generateReports(db); 
                    break;
                case 9:
                    System.out.println("Logging out...");
                    break;
                default: 
                    System.out.println("Invalid choice.");
            }
        } while (choice != 9);
    }

    // 1️⃣ VIEW STUDENTS
    private static void viewStudents(config db) {
        String sql = "SELECT * FROM student";
        String[] headers = {"ID", "Name", "Email", "Course"};
        String[] columns = {"student_id", "name", "email", "course"};
        db.viewRecords(sql, headers, columns);
    }

    // 2️⃣ VIEW & APPROVE USERS
    private static void viewUsers(config db, Scanner sc) {
        System.out.println("\n=== USERS ===");
        String sqlUsers = "SELECT * FROM tbl_user";
        String[] headers = {"User ID", "Name", "Email", "Type", "Status"};
        String[] columns = {"user_id", "name", "email", "type", "status"};
        db.viewRecords(sqlUsers, headers, columns);

        System.out.print("Enter User ID to approve: ");
        int userId = sc.nextInt();
        sc.nextLine();

        if (userId == 0) {
            System.out.println("Skipping approval.");
            return;
        }

        String getUser = "SELECT * FROM tbl_user WHERE user_id = ?";
        List<Map<String, Object>> userData = db.fetchRecords(getUser, userId);

        if (userData != null && !userData.isEmpty()) {
            Map<String, Object> user = userData.get(0);
            String status = user.get("status").toString();

            if (status.equalsIgnoreCase("Pending")) {
                System.out.print("Do you want to approve this user? (yes/no): ");
                String confirm = sc.nextLine();

                if (confirm.equalsIgnoreCase("yes")) {
                    String updateSql = "UPDATE tbl_user SET status = 'Approved' WHERE user_id = ?";
                    db.updateRecord(updateSql, userId);

                    String insertStudent = "INSERT INTO student(student_id, name, email, password, course) VALUES(?,?,?,?,?)";
                    db.addRecord(insertStudent,
                            Integer.parseInt(user.get("user_id").toString()),
                            user.get("name").toString(),
                            user.get("email").toString(),
                            user.get("password").toString(),
                            "");

                    System.out.println("✅ User approved and added as student!");
                } else {
                    System.out.println("❌ Approval cancelled by admin.");
                }
            } else {
                System.out.println("⚠️ User is already approved or not pending.");
            }
        } else {
            System.out.println("❌ User not found.");
        }
    }

    // 3️⃣ ADD NEW STUDENT
    private static void addStudent(config db, Scanner sc) {
        System.out.print("Enter Name: "); String name = sc.nextLine();
        System.out.print("Enter Email: "); String email = sc.nextLine();
        System.out.print("Enter Password: "); String pass = sc.nextLine();
        System.out.print("Enter Course: "); String course = sc.nextLine();
        String hashed = config.hashPassword(pass);

        db.addRecord("INSERT INTO tbl_user(name,email,type,status,password) VALUES(?,?,?,?,?)",
                name, email, "Student", "Approved", hashed);

        List<Map<String,Object>> userData = db.fetchRecords("SELECT user_id FROM tbl_user WHERE email=?", email);
        int id = Integer.parseInt(userData.get(0).get("user_id").toString());

        db.addRecord("INSERT INTO student(student_id,name,email,password,course) VALUES(?,?,?,?,?)",
                id, name, email, hashed, course);

        System.out.println("✅ New student added successfully!");
    }

    // 4️⃣ VIEW SCHOLARSHIP APPLICATIONS
    private static void viewApplications(config db) {
        String sql = "SELECT * FROM application";
        String[] headers = {"Application ID", "Student ID", "Scholarship ID", "Status", "documents", "deadline"};
        String[] columns = {"application_id", "student_id", "scholarship_id", "status", "documents", "deadline"};
        db.viewRecords(sql, headers, columns);
    }

    // 5️⃣ REVIEW APPLICATIONS
      private static void reviewApplications(config db, Scanner sc) {
        viewApplications(db);
        System.out.print("Enter Application ID to review: ");
        int appId = sc.nextInt(); sc.nextLine();
        List<Map<String,Object>> data = db.fetchRecords("SELECT * FROM application WHERE application_id=?", appId);
        if(data == null || data.isEmpty()) {
            System.out.println("❌ Application not found!"); return;
        }
        Map<String,Object> app = data.get(0);
        System.out.println("Student ID: " + app.get("student_id"));
        System.out.println("Scholarship ID: " + app.get("scholarship_id"));
        System.out.println("Status: " + app.get("status"));
        System.out.println("Date Applied: " + app.get("date_applied"));
        System.out.println("Documents: " + app.get("documents"));
    }

    // 6️⃣ UPDATE APPLICATION STATUS
   // UPDATE APPLICATION STATUS
private static void updateApplicationStatus(config db, Scanner sc) {
    System.out.print("Enter Application ID to update: ");
    int appId = sc.nextInt();
    sc.nextLine();

    // Fetch application first
    List<Map<String, Object>> data = db.fetchRecords(
        "SELECT * FROM application WHERE application_id = ?", appId
    );

    if (data == null || data.isEmpty()) {
        System.out.println("❌ Application not found!");
        return;
    }

    Map<String, Object> app = data.get(0);
    System.out.println("Current Status: " + app.get("status"));
    System.out.print("Enter New Status (Pending / Approved / Rejected): ");
    String newStatus = sc.nextLine();

    db.updateRecord(
        "UPDATE application SET status=? WHERE application_id=?",
        newStatus, appId
    );

    System.out.println("✅ Application status updated!");
}
 // 7️⃣ MANAGE SCHOLARSHIPS
// 7️⃣ MANAGE SCHOLARSHIPS
private static void manageScholarships(config db, Scanner sc) {

    System.out.println("=== MANAGE SCHOLARSHIPS ===");
    System.out.println("1. Add Scholarship");
    System.out.println("2. Edit Scholarship");
    System.out.println("3. Delete Scholarship");
    System.out.println("4. View Scholarship Availability");
    System.out.print("Enter choice: ");

    int choice = sc.nextInt();
    sc.nextLine();

    switch (choice) {

        case 1:
            addScholarship(db, sc);
            break;

        case 2:
            editScholarship(db, sc);
            break;

        case 3:
            deleteScholarship(db, sc);
            break;

        case 4:
            viewScholarshipAvailability(db);
            break;

        default:
            System.out.println("Invalid choice!");
    }
}

// ADD SCHOLARSHIP
private static void addScholarship(config db, Scanner sc) {
    System.out.print("Scholarship name: ");
    String name = sc.nextLine();

    System.out.print("Description: ");
    String description = sc.nextLine();

    System.out.print("Requirements: ");
    String requirements = sc.nextLine();

    double amount = 0;
    while (true) {
        System.out.print("Amount: ");
        String input = sc.nextLine();
        try {
            amount = Double.parseDouble(input);
            break;
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Please enter a valid number for amount!");
        }
    }

    System.out.print("Deadline (YYYY-MM-DD): ");
    String deadline = sc.nextLine();

    int slots = 0;
    while (true) {
        System.out.print("Slots available: ");
        String input = sc.nextLine();
        try {
            slots = Integer.parseInt(input);
            break;
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Please enter a valid integer for slots!");
        }
    }

    db.addRecord(
        "INSERT INTO scholarship(scholarship_name, discription, requirements, amount, deadline, slots_available) VALUES(?,?,?,?,?,?)",
        name, description, requirements, amount, deadline, slots
    );

    System.out.println("✅ Scholarship added!");
}

// EDIT SCHOLARSHIP
private static void editScholarship(config db, Scanner sc) {
    System.out.print("Enter Scholarship ID to edit: ");
    int editId = sc.nextInt();
    sc.nextLine();

    List<Map<String, Object>> data = db.fetchRecords("SELECT * FROM scholarship WHERE scholarship_id = ?", editId);

    if (data == null || data.isEmpty()) {
        System.out.println("❌ Scholarship not found!");
        return;
    }

    Map<String, Object> scholarship = data.get(0);

    System.out.print("New Name (" + scholarship.get("scholarship_name") + "): ");
    String name = sc.nextLine();
    if (name.isEmpty()) name = scholarship.get("scholarship_name").toString();

    System.out.print("New Description (" + scholarship.get("discription") + "): ");
    String description = sc.nextLine();
    if (description.isEmpty()) description = scholarship.get("discription").toString();

    System.out.print("New Requirements (" + scholarship.get("requirements") + "): ");
    String requirements = sc.nextLine();
    if (requirements.isEmpty()) requirements = scholarship.get("requirements").toString();

    // SAFE AMOUNT
    double amount = 0;
    while (true) {
        System.out.print("New Amount (" + scholarship.get("amount") + "): ");
        String input = sc.nextLine();
        if (input.isEmpty()) {
            amount = Double.parseDouble(scholarship.get("amount").toString());
            break;
        }
        try {
            amount = Double.parseDouble(input);
            break;
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Please enter a valid number for amount!");
        }
    }

    System.out.print("New Deadline (" + scholarship.get("deadline") + "): ");
    String deadline = sc.nextLine();
    if (deadline.isEmpty()) deadline = scholarship.get("deadline").toString();

    // SAFE SLOTS
    int slots = 0;
    while (true) {
        System.out.print("New Slots Available (" + scholarship.get("slots_available") + "): ");
        String input = sc.nextLine();
        if (input.isEmpty()) {
            slots = Integer.parseInt(scholarship.get("slots_available").toString());
            break;
        }
        try {
            slots = Integer.parseInt(input);
            break;
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Please enter a valid integer for slots!");
        }
    }

    db.updateRecord(
        "UPDATE scholarship SET scholarship_name=?, discription=?, requirements=?, amount=?, deadline=?, slots_available=? WHERE scholarship_id=?",
        name, description, requirements, amount, deadline, slots, editId
    );

    System.out.println("✅ Scholarship updated!");
}

// DELETE SCHOLARSHIP
private static void deleteScholarship(config db, Scanner sc) {
    System.out.print("Enter Scholarship ID to delete: ");
    int delId = sc.nextInt();
    sc.nextLine();

    db.updateRecord("DELETE FROM scholarship WHERE scholarship_id=?", delId);
    System.out.println("🗑️ Scholarship deleted!");
}

// VIEW SCHOLARSHIP AVAILABILITY
private static void viewScholarshipAvailability(config db) {
    System.out.println("\n=== SCHOLARSHIP AVAILABILITY ===");

    String sql = "SELECT scholarship_id, scholarship_name, slots_available FROM scholarship";
    List<Map<String, Object>> data = db.fetchRecords(sql);

    if (data == null || data.isEmpty()) {
        System.out.println("⚠️ No scholarships found!");
        return;
    }

    // HEADER BOX
    System.out.println("\n==============================================================");
    System.out.println("|                SCHOLARSHIP AVAILABILITY TABLE              |");
    System.out.println("==============================================================");
    System.out.println(String.format(
            "| %-5s | %-35s | %-10s |",
            "ID", "Scholarship Name", "Slots"
    ));
    System.out.println("--------------------------------------------------------------");

    // DISPLAY ROWS
    for (Map<String, Object> row : data) {
        Object idObj = row.get("scholarship_id");
        Object nameObj = row.get("scholarship_name");
        Object slotsObj = row.get("slots_available");

        int id = (idObj == null ? 0 : Integer.parseInt(idObj.toString()));
        String name = (nameObj == null ? "N/A" : nameObj.toString());
        String slots = (slotsObj == null ? "N/A" : slotsObj.toString());

        System.out.println(String.format(
                "| %-5d | %-35s | %-10s |",
                id, name, slots
        ));
    }

    // FOOTER BOX
    System.out.println("==============================================================");
}


    // 8️⃣ GENERATE REPORTS
    private static void generateReports(config db) {
        System.out.println("\n=== SCHOLARSHIP REPORTS ===");

        String totalApplicants = "SELECT COUNT(*) AS count FROM application";
        String approved = "SELECT COUNT(*) AS count FROM application WHERE status='Approved'";
        String rejected = "SELECT COUNT(*) AS count FROM application WHERE status='Rejected'";
        String disbursed = "SELECT SUM(s.amount) AS total FROM scholarship s JOIN application a ON s.scholarship_id=a.scholarship_id WHERE a.status='Approved'";

        System.out.println("Total Applicants: " + db.fetchRecords(totalApplicants).get(0).get("count"));
        System.out.println("Approved Applications: " + db.fetchRecords(approved).get(0).get("count"));
        System.out.println("Rejected Applications: " + db.fetchRecords(rejected).get(0).get("count"));
        System.out.println("Total Funds Disbursed: " + db.fetchRecords(disbursed).get(0).get("total"));
    }
}
