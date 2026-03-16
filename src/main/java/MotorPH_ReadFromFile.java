import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;

public class MotorPH_ReadFromFile {

    public static double computeSSS(double grossSalary) {
        return grossSalary * 0.045;
    }

    public static double computePhilHealth(double grossSalary) {
        return grossSalary * 0.035;
    }

    public static double computePagIBIG(double grossSalary) {
        return 100;
    }

    public static double computeIncomeTax(double grossSalary) {
        if (grossSalary <= 25000)
            return 0;
        else if (grossSalary <= 40000)
            return (grossSalary - 25000) * 0.20;
        else
            return (grossSalary - 40000) * 0.25 + 3000;
    }

    public static double computeTotalDeductions(double grossSalary, boolean isSecondCutoff) {
        if (!isSecondCutoff)
            return 0;

        return computeSSS(grossSalary)
                + computePhilHealth(grossSalary)
                + computePagIBIG(grossSalary)
                + computeIncomeTax(grossSalary);
    }

    public static void processEmployee(String name, double timeIn, double timeOut,
            double hourlyRate, boolean isSecondCutoff) {

        double totalHoursWorked = timeOut - timeIn;

        if (totalHoursWorked < 0) {
            System.out.println("Invalid hours for " + name + ": Time Out earlier than Time In.");
            return;
        }

        double grossSalary = totalHoursWorked * hourlyRate;
        double totalDeductions = computeTotalDeductions(grossSalary, isSecondCutoff);
        double netSalary = grossSalary - totalDeductions;

        System.out.println("======================================");
        System.out.println("Employee Name: " + name);
        System.out.println("Total Hours  : " + totalHoursWorked);
        System.out.println("Hourly Rate  : " + hourlyRate);
        System.out.println("Gross Salary : " + grossSalary);
        System.out.println("Cutoff       : " + (isSecondCutoff ? "2nd Cutoff" : "1st Cutoff"));
        System.out.println("Total Deductions: " + totalDeductions);
        System.out.println("Net Salary   : " + netSalary);
        System.out.println("======================================");
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        String fileName = "employee_data.txt";
        File file = new File(fileName);

        // Create file with sample data if it does not exist
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {

                writer.write("Juan Dela Cruz,8,17,500,1\n");
                writer.write("Maria Santos,9,18,600,2\n");
                writer.write("Pedro Reyes,7,16,450,2\n");

                System.out.println("File created: " + fileName);

            } catch (IOException e) {
                System.out.println("Error creating file: " + e.getMessage());
                return;
            }
        }

        System.out.println("MotorPH Payroll System");
        System.out.println("1. Read from File");
        System.out.println("2. Enter Manually");
        System.out.print("Enter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {

                String line;

                while ((line = br.readLine()) != null) {

                    String[] parts = line.split(",");

                    if (parts.length != 5) {
                        System.out.println("Invalid record: " + line);
                        continue;
                    }

                    String name = parts[0].trim();
                    double timeIn = Double.parseDouble(parts[1].trim());
                    double timeOut = Double.parseDouble(parts[2].trim());
                    double hourlyRate = Double.parseDouble(parts[3].trim());
                    boolean isSecondCutoff = parts[4].trim().equals("2");

                    processEmployee(name, timeIn, timeOut, hourlyRate, isSecondCutoff);
                }

            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
            }

        } else if (choice == 2) {

            System.out.print("Enter employee name: ");
            String name = scanner.nextLine();

            System.out.print("Enter Time In: ");
            double timeIn = scanner.nextDouble();

            System.out.print("Enter Time Out: ");
            double timeOut = scanner.nextDouble();

            System.out.print("Enter Hourly Rate: ");
            double hourlyRate = scanner.nextDouble();

            System.out.print("Enter Cutoff (1 or 2): ");
            int cutoff = scanner.nextInt();

            boolean isSecondCutoff = (cutoff == 2);

            processEmployee(name, timeIn, timeOut, hourlyRate, isSecondCutoff);

            try (FileWriter writer = new FileWriter(file, true)) {

                writer.write(name + "," + timeIn + "," + timeOut + "," + hourlyRate + "," + cutoff + "\n");

                System.out.println("Employee record saved.");

            } catch (IOException e) {
                System.out.println("Error writing file: " + e.getMessage());
            }

        } else {
            System.out.println("Invalid choice.");
        }

        scanner.close();
    }
}