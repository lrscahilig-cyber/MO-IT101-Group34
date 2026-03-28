import java.io.*;
import java.util.*;

public class MotorPH_Basic_Payroll_System {

    static String[] empHeaders;
    static String[] attHeaders;

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println("\n===== MOTORPH SYSTEM LOGIN =====");

            String username;

            // ===== USERNAME VALIDATION =====
            while (true) {
                System.out.print("Username: ");
                username = sc.nextLine();

                if (username.equals("employee") || username.equals("payroll_staff")) break;
                System.out.println("Invalid username.");
            }

            // ===== PASSWORD VALIDATION =====
            while (true) {
                System.out.print("Password: ");
                String password = sc.nextLine();

                if (password.equals("12345")) break;
                System.out.println("Incorrect password.");
            }

            Map<String, String[]> employees = loadEmployees("Employee Details.csv");
            List<String[]> attendance = loadAttendance("Attendance Record.csv");

            if (username.equals("employee")) {
                handleEmployee(sc, employees);
            } else {
                handlePayrollStaff(sc, employees, attendance);
            }
        }
    }

    /**
     * Employee view (basic info only)
     */
    static void handleEmployee(Scanner sc, Map<String, String[]> employees) {

        while (true) {

            System.out.println("\n1. Enter Employee Number");
            System.out.println("2. Exit the Program");

            int choice = getValidNumber(sc);

            if (choice == 2) System.exit(0);

            if (choice != 1) {
                System.out.println("Invalid option.");
                continue;
            }

            String empNum = getValidEmployee(sc, employees);

            String[] emp = employees.get(empNum);

            System.out.println("\nEmployee #: " + empNum);
            System.out.println("Name: " +
                    emp[getIndex(empHeaders, "First Name")] + " " +
                    emp[getIndex(empHeaders, "Last Name")]);
            System.out.println("Birthday: " +
                    emp[getIndex(empHeaders, "Birthday")]);
        }
    }

    /**
     * Payroll staff menu
     */
    static void handlePayrollStaff(Scanner sc,
                                   Map<String, String[]> employees,
                                   List<String[]> attendance) {

        while (true) {

            System.out.println("\n1. One Employee");
            System.out.println("2. All Employees");
            System.out.println("3. Exit the Program");

            int choice = getValidNumber(sc);

            if (choice == 3) System.exit(0);

            if (choice != 1 && choice != 2) {
                System.out.println("Invalid option.");
                continue;
            }

            int selectedMonth = getMonthChoice(sc);

            if (choice == 1) {
                String empNum = getValidEmployee(sc, employees);
                processPayroll(empNum, employees, attendance, selectedMonth);
            } else {
                for (String empNum : new TreeSet<>(employees.keySet())) {
                    processPayroll(empNum, employees, attendance, selectedMonth);
                }
            }
        }
    }

    /**
     * Processes payroll for each employee
     */
    static void processPayroll(String empNum,
                               Map<String, String[]> employees,
                               List<String[]> attendance,
                               int selectedMonth) {

        String[] emp = employees.get(empNum);

        String name = emp[getIndex(empHeaders, "First Name")] + " " +
                      emp[getIndex(empHeaders, "Last Name")];

        double hourlyRate = Double.parseDouble(
                emp[getIndex(empHeaders, "Hourly Rate")]
                        .replace(",", "").replace("\"", "").trim()
        );

        int empIndex = getIndexFlexible(attHeaders, "Employee #");
        int dateIndex = getIndexFlexible(attHeaders, "Date");
        int loginIndex = getIndexFlexible(attHeaders, "Log In", "Time In");
        int logoutIndex = getIndexFlexible(attHeaders, "Log Out", "Time Out");

        for (int month = 6; month <= 12; month++) {

            if (selectedMonth != 0 && month != selectedMonth) continue;

            double[] hours = calculateHoursWorked(attendance, empNum,
                    empIndex, dateIndex, loginIndex, logoutIndex, month);

            if (hours[0] == 0 && hours[1] == 0) continue;

            double gross1 = hours[0] * hourlyRate;
            double gross2 = hours[1] * hourlyRate;

            double totalGross = gross1 + gross2;

            double[] deductions = calculateDeductions(totalGross);

            double net1 = gross1;
            double net2 = gross2 - deductions[4];

            printPayroll(empNum, name, month,
                    hours[0], hours[1],
                    gross1, gross2,
                    deductions, net1, net2);
        }
    }

    /**
     * Calculates hours worked per cutoff
     */
    static double[] calculateHoursWorked(List<String[]> attendance,
                                         String empNum,
                                         int empIndex, int dateIndex,
                                         int loginIndex, int logoutIndex,
                                         int month) {

        double cutoff1 = 0;
        double cutoff2 = 0;

        for (String[] row : attendance) {

            if (!row[empIndex].equals(empNum)) continue;

            String[] dateParts = row[dateIndex].split("/");
            int recordMonth = Integer.parseInt(dateParts[0]);
            int day = Integer.parseInt(dateParts[1]);

            // Skip records that are not in selected month
            if (recordMonth != month) continue;

            double loginTime = parseTime(row[loginIndex]);
            double logoutTime = parseTime(row[logoutIndex]);

            double workedHours = Math.min(logoutTime, 17) - Math.max(loginTime, 8);

            // Full shift rule
            if (loginTime <= 8.0833 && logoutTime >= 17) workedHours = 9;

            // Deduct 1 hour lunch
            if (workedHours > 5) workedHours -= 1;

            if (workedHours < 0) workedHours = 0;

            if (day <= 15) cutoff1 += workedHours;
            else cutoff2 += workedHours;
        }

        return new double[]{cutoff1, cutoff2};
    }

    /**
     * Calculates all deductions
     */
    static double[] calculateDeductions(double totalGross) {

        double sss = totalGross * 0.05;
        double philhealth = totalGross * 0.03;
        double pagibig = 50;
        double tax = totalGross * 0.10;

        double total = sss + philhealth + pagibig + tax;

        return new double[]{sss, philhealth, pagibig, tax, total};
    }

    /**
     * Prints payroll (clean & consistent format)
     */
    static void printPayroll(String empNum, String name, int month,
                             double h1, double h2,
                             double g1, double g2,
                             double[] d, double net1, double net2) {

        System.out.println("\n==== " + getMonthName(month) + " ====");
        System.out.println("Employee #: " + empNum + " | " + name);

        System.out.println("\nCutoff 1 (1–15)");
        System.out.printf("Hours: %.2f\n", h1);
        System.out.printf("Gross Salary: %.2f\n", g1);
        System.out.printf("Net Salary: %.2f\n", net1);

        System.out.println("\nCutoff 2 (16–End)");
        System.out.printf("Hours: %.2f\n", h2);
        System.out.printf("Gross Salary: %.2f\n", g2);

        System.out.println("\nDeductions:");
        System.out.printf("SSS: %.2f\n", d[0]);
        System.out.printf("PhilHealth: %.2f\n", d[1]);
        System.out.printf("Pag-IBIG: %.2f\n", d[2]);
        System.out.printf("Tax: %.2f\n", d[3]);
        System.out.printf("Total Deduction: %.2f\n", d[4]);

        System.out.printf("Net Salary: %.2f\n", net2);
    }

    // ===== UTILITIES =====

    static int getValidNumber(Scanner sc) {
        try { return Integer.parseInt(sc.nextLine()); }
        catch (Exception e) { return -1; }
    }

    static String getValidEmployee(Scanner sc, Map<String, String[]> employees) {
        while (true) {
            System.out.print("Enter Employee Number: ");
            String empNum = sc.nextLine();

            if (employees.containsKey(empNum)) return empNum;
            System.out.println("Invalid employee number.");
        }
    }

    static int getMonthChoice(Scanner sc) {

        System.out.println("\n1. Specific Month");
        System.out.println("2. All Months");

        int choice = getValidNumber(sc);

        if (choice == 1) {
            while (true) {
                System.out.print("Enter month (June–December): ");
                int m = convertMonthToNumber(sc.nextLine());

                if (m != -1) return m;
                System.out.println("Invalid month.");
            }
        }
        return 0;
    }

    static int convertMonthToNumber(String name) {
        String[] m = {"","january","february","march","april","may",
                      "june","july","august","september","october","november","december"};

        for (int i = 6; i <= 12; i++)
            if (m[i].equals(name.toLowerCase().trim())) return i;

        return -1;
    }

    static String getMonthName(int m) {
        String[] months = {"","January","February","March","April","May",
                "June","July","August","September","October","November","December"};
        return months[m];
    }

    static double parseTime(String t) {
        String[] p = t.split(":");
        return Integer.parseInt(p[0]) + Integer.parseInt(p[1]) / 60.0;
    }

    static String[] splitCSV(String l) {
        return l.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    static int getIndex(String[] h, String n) {
        for (int i = 0; i < h.length; i++)
            if (h[i].trim().equalsIgnoreCase(n)) return i;
        throw new RuntimeException("Missing column");
    }

    static int getIndexFlexible(String[] h, String... n) {
        for (int i = 0; i < h.length; i++) {
            String x = h[i].toLowerCase().replace(" ", "");
            for (String s : n)
                if (x.equals(s.toLowerCase().replace(" ", ""))) return i;
        }
        throw new RuntimeException("Column not found");
    }

    static Map<String, String[]> loadEmployees(String f) throws Exception {
        Map<String, String[]> map = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(f));

        empHeaders = splitCSV(br.readLine());

        String line;
        while ((line = br.readLine()) != null) {
            String[] row = splitCSV(line);
            map.put(row[getIndex(empHeaders, "Employee #")], row);
        }
        br.close();
        return map;
    }

    static List<String[]> loadAttendance(String f) throws Exception {
        List<String[]> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(f));

        attHeaders = splitCSV(br.readLine());

        String line;
        while ((line = br.readLine()) != null) {
            list.add(splitCSV(line));
        }
        br.close();
        return list;
    }
}