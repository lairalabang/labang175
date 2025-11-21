package main;

import java.util.*;
import config.config;

public class StudentMenu {

    public static void show(int userId) {
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
            sc.nextLine(); // consume newline

            switch (choice) {

                // 1️⃣ View Available Scholarships
                case 1:
                    viewScholarshipAvailability(db);
                    break;

                // 2️⃣ Apply for Scholarship
                case 2:
                    System.out.print("Enter Scholarship ID to apply: ");
                    int sid = sc.nextInt();
                    sc.nextLine();

                    // Check if student already applied
                    String checkApp = "SELECT * FROM application WHERE student_id = ? AND scholarship_id = ?";
                    List<Map<String, Object>> existingApp = db.fetchRecords(checkApp, userId, sid);

                    if (existingApp != null && !existingApp.isEmpty()) {
                        System.out.println("⚠️ You already applied for this scholarship.");
                        break;
                    }

                    // Ask for document
                    System.out.print("Enter document path or description: ");
                    String documentsPath = sc.nextLine();

                    // Insert application (auto-increment ID)
                    String insertApp = "INSERT INTO application (student_id, scholarship_id, documents, status, date_applied) " +
                                       "VALUES (?, ?, ?, ?, datetime('now'))";
                    db.addRecord(insertApp, userId, sid, documentsPath, "Pending");
                    System.out.println("✅ Scholarship application submitted with documents!");
                    break;

                // 3️⃣ View My Applications
                case 3:
    // VIEW MY APPLICATIONS
    String myAppQuery = "SELECT a.Application_id, s.scholarship_name, a.status, a.date_applied, a.documents " +
                        "FROM application a " +
                        "JOIN scholarship s ON a.scholarship_id = s.scholarship_id " +
                        "WHERE a.student_id = ?";

    List<Map<String, Object>> applications = db.fetchRecords(myAppQuery, userId);

    if (applications == null || applications.isEmpty()) {
        System.out.println("You have not applied for any scholarships yet.");
    } else {
        System.out.printf("%-15s %-30s %-10s %-20s %-40s%n",
                          "Application ID", "Scholarship Name", "Status",
                          "Date Applied", "Documents");
        System.out.println("------------------------------------------------------------------------------------------");

        for (Map<String, Object> app : applications) {
            System.out.printf("%-15s %-30s %-10s %-20s %-40s%n",
                              app.get("Application_id"),     // FIXED key
                              app.get("scholarship_name"),
                              app.get("status"),
                              app.get("date_applied"),
                              app.get("documents"));
        }
    }
    break;

                 

                // 4️⃣ Logout
                case 4:
                    System.out.println("Logging out...");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 4);
    }

    // VIEW SCHOLARSHIP AVAILABILITY
    private static void viewScholarshipAvailability(config db) {
        System.out.println("\n=== SCHOLARSHIP AVAILABILITY ===");

        String sql = "SELECT scholarship_id, scholarship_name, slots_available, deadline FROM scholarship";
        List<Map<String, Object>> data = db.fetchRecords(sql);

        if (data == null || data.isEmpty()) {
            System.out.println("⚠️ No scholarships found!");
            return;
        }

        // HEADER BOX
        System.out.println("\n==============================================================");
        System.out.println("|                SCHOLARSHIP AVAILABILITY TABLE              |");
        System.out.println("==============================================================");
        System.out.printf("| %-5s | %-35s | %-10s | %-12s |\n",
                          "ID", "Scholarship Name", "Slots", "Deadline");
        System.out.println("--------------------------------------------------------------");

        // DISPLAY ROWS
        for (Map<String, Object> row : data) {
            Object idObj = row.get("scholarship_id");
            Object nameObj = row.get("scholarship_name");
            Object slotsObj = row.get("slots_available");
            Object deadlineObj = row.get("deadline");

            int id = (idObj == null ? 0 : Integer.parseInt(idObj.toString()));
            String name = (nameObj == null ? "N/A" : nameObj.toString());
            String slots = (slotsObj == null ? "N/A" : slotsObj.toString());
            String deadline = (deadlineObj == null ? "N/A" : deadlineObj.toString());

            System.out.printf("| %-5d | %-35s | %-10s | %-12s |\n",
                              id, name, slots, deadline);
        }

        // FOOTER BOX
        System.out.println("==============================================================");
    }
}
