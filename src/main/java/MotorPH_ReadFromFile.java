import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MotorPH_ReadFromFile {

    public static double computeSSS(double grossSalary) {
        return grossSalary * 0.045;
    }

    public static double computePhilHealth(double grossSalary) {
        return grossSalary * 0.035;
    }

    public static double computePagIbig(double grossSalary) {
        return 100.00;
    }

    public static double computeIncomeTax(double grossSalary) {
        if (grossSalary <= 25000) {
            return 0;
        } else if (grossSalary <= 50000) {
            return grossSalary * 0.10;
        } else {
            return grossSalary * 0.20;
        }
    }

    public static double computeNetPay(double grossSalary) {
        double sss = computeSSS(grossSalary);
        double philHealth = computePhilHealth(grossSalary);
        double pagIbig = computePagIbig(grossSalary);
        double incomeTax = computeIncomeTax(grossSalary);
        return grossSalary - (sss + philHealth + pagIbig + incomeTax);
    }

    public static void main(String[] args) {
        String fileName = "MotorPH_Employee Data - Employee Details.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine(); 
            if (line == null) {
                System.out.println("File is empty.");
                return;
            }

            String[] headers = line.split("\t");
            int lastNameIndex = -1;
            int firstNameIndex = -1;
            int salaryIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                if (header.contains("last name")) lastNameIndex = i;
                if (header.contains("first name")) firstNameIndex = i;
                if (header.contains("basic salary")) salaryIndex = i;
            }

            if (lastNameIndex == -1 || firstNameIndex == -1 || salaryIndex == -1) {
                System.out.println("Required columns not found in file.");
                return;
            }

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length <= salaryIndex) continue;

                String name = parts[firstNameIndex].trim() + " " + parts[lastNameIndex].trim();
                double grossSalary;

                try {
                    grossSalary = Double.parseDouble(parts[salaryIndex].replace(",", "").replace("\"", "").trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid salary for " + name);
                    continue;
                }

                double sss = computeSSS(grossSalary);
                double philHealth = computePhilHealth(grossSalary);
                double pagIbig = computePagIbig(grossSalary);
                double incomeTax = computeIncomeTax(grossSalary);
                double netPay = computeNetPay(grossSalary);

                System.out.println("====================================");
                System.out.println("Employee Name: " + name);
                System.out.println("Gross Salary : " + grossSalary);
                System.out.println("SSS          : " + sss);
                System.out.println("PhilHealth   : " + philHealth);
                System.out.println("Pag-IBIG     : " + pagIbig);
                System.out.println("Income Tax   : " + incomeTax);
                System.out.println("Net Pay      : " + netPay);
                System.out.println("====================================");
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}