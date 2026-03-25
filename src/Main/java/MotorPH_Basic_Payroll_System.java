import java.io.*;
import java.util.*;

public class MotorPH_Basic_Payroll_System {

    static String[] empHeaders;
    static String[] attHeaders;

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        // ===== LOGIN =====
        System.out.print("Username: ");
        String user = sc.nextLine();

        System.out.print("Password: ");
        String pass = sc.nextLine();

        if (!(user.equals("employee") || user.equals("payroll_staff")) || !pass.equals("12345")) {
            System.out.println("Incorrect username and/or password.");
            return;
        }

        Map<String, String[]> employees = loadEmployees("Employee Details.csv");
        List<String[]> attendance = loadAttendance("Attendance Record.csv");

        // ================= EMPLOYEE =================
        if (user.equals("employee")) {

            System.out.println("\n1. Enter your employee number");
            System.out.println("2. Exit");
            int choice = sc.nextInt(); sc.nextLine();

            if (choice == 2) return;

            System.out.print("Enter Employee Number: ");
            String empNum = sc.nextLine();

            if (!employees.containsKey(empNum)) {
                System.out.println("Employee number does not exist.");
                return;
            }

            String[] emp = employees.get(empNum);

            String name = emp[getIndex(empHeaders, "First Name")] + " " +
                          emp[getIndex(empHeaders, "Last Name")];

            String birthday = emp[getIndex(empHeaders, "Birthday")];

            System.out.println("\nEmployee Number: " + empNum);
            System.out.println("Employee Name: " + name);
            System.out.println("Birthday: " + birthday);

            return;
        }

        // ================= PAYROLL STAFF =================
        if (user.equals("payroll_staff")) {

            System.out.println("\n1. Process Payroll");
            System.out.println("2. Exit");
            int choice = sc.nextInt(); sc.nextLine();

            if (choice == 2) return;

            System.out.println("\n1. One employee");
            System.out.println("2. All employees");
            System.out.println("3. Exit");
            int sub = sc.nextInt(); sc.nextLine();

            if (sub == 3) return;

            if (sub == 1) {

                System.out.print("Enter Employee Number: ");
                String empNum = sc.nextLine();

                if (!employees.containsKey(empNum)) {
                    System.out.println("Employee number does not exist.");
                    return;
                }

                processPayroll(empNum, employees, attendance);

            } else if (sub == 2) {

                for (String empNum : employees.keySet()) {
                    processPayroll(empNum, employees, attendance);
                }
            }
        }
    }

    // ================= PROCESS PAYROLL =================
    static void processPayroll(String empNum, Map<String, String[]> employees, List<String[]> attendance) {

        String[] emp = employees.get(empNum);

        String name = emp[getIndex(empHeaders, "First Name")] + " " +
                      emp[getIndex(empHeaders, "Last Name")];

        String birthday = emp[getIndex(empHeaders, "Birthday")];

        double rate = Double.parseDouble(
                emp[getIndex(empHeaders, "Hourly Rate")]
                .replace(",", "").replace("\"", "").trim()
        );

        double hours1 = 0;
        double hours2 = 0;

        int empIndex = getIndexFlexible(attHeaders, "Employee #");
        int dateIndex = getIndexFlexible(attHeaders, "Date");
        int inIndex = getIndexFlexible(attHeaders, "Log In", "Time In");
        int outIndex = getIndexFlexible(attHeaders, "Log Out", "Time Out");

        // ===== ONLY JUNE =====
        for (String[] row : attendance) {

            if (!row[empIndex].equals(empNum)) continue;

            String[] d = row[dateIndex].split("/");
            int month = Integer.parseInt(d[0]);
            int day = Integer.parseInt(d[1]);

            if (month != 6) continue;

            double in = parseTime(row[inIndex]);
            double out = parseTime(row[outIndex]);

            double start = Math.max(in, 8);
            double end = Math.min(out, 17);

            double worked = end - start;

            if (in <= 8.0833 && out >= 17) worked = 8;

            if (worked < 0) worked = 0;

            if (day <= 15) hours1 += worked;
            else hours2 += worked;
        }

        double gross1 = hours1 * rate;
        double gross2 = hours2 * rate;

        double totalGross = gross1 + gross2;

        double sss = totalGross * 0.05;
        double philhealth = totalGross * 0.03;
        double pagibig = 50;
        double tax = totalGross * 0.10;

        double totalDeduction = sss + philhealth + pagibig + tax;

        double net1 = gross1;
        double net2 = gross2 - totalDeduction;

        // ===== OUTPUT =====
        System.out.println("\n==============================");
        System.out.println("Employee #: " + empNum);
        System.out.println("Employee Name: " + name);
        System.out.println("Birthday: " + birthday);

        System.out.println("\nCutoff Date: June 1 to June 15");
        System.out.println("Total Hours Worked: " + String.format("%.2f", hours1));
        System.out.println("Gross Salary: " + String.format("%.2f", gross1));
        System.out.println("Net Salary: " + String.format("%.2f", net1));

        System.out.println("\nCutoff Date: June 16 to June 30");
        System.out.println("Total Hours Worked: " + String.format("%.2f", hours2));
        System.out.println("Gross Salary: " + String.format("%.2f", gross2));

        System.out.println("SSS: " + String.format("%.2f", sss));
        System.out.println("PhilHealth: " + String.format("%.2f", philhealth));
        System.out.println("Pag-IBIG: " + String.format("%.2f", pagibig));
        System.out.println("Tax: " + String.format("%.2f", tax));
        System.out.println("Total Deductions: " + String.format("%.2f", totalDeduction));
        System.out.println("Net Salary: " + String.format("%.2f", net2));
    }

    // ================= FILE LOADERS =================
    static Map<String, String[]> loadEmployees(String file) throws Exception {

        Map<String, String[]> map = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(file));

        empHeaders = splitCSV(br.readLine());

        String line;
        while ((line = br.readLine()) != null) {
            String[] row = splitCSV(line);
            map.put(row[getIndex(empHeaders, "Employee #")], row);
        }

        br.close();
        return map;
    }

    static List<String[]> loadAttendance(String file) throws Exception {

        List<String[]> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));

        attHeaders = splitCSV(br.readLine());

        String line;
        while ((line = br.readLine()) != null) {
            list.add(splitCSV(line));
        }

        br.close();
        return list;
    }

    // ================= HELPERS =================
    static double parseTime(String t) {
        String[] p = t.split(":");
        return Integer.parseInt(p[0]) + Integer.parseInt(p[1]) / 60.0;
    }

    static String[] splitCSV(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    static int getIndex(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(name)) return i;
        }
        throw new RuntimeException("Missing column: " + name);
    }

    static int getIndexFlexible(String[] headers, String... names) {
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].toLowerCase().replace(" ", "");
            for (String n : names) {
                if (h.equals(n.toLowerCase().replace(" ", ""))) return i;
            }
        }
        throw new RuntimeException("Column not found");
    }
}