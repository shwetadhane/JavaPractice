import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public final class EmployeeStreamOperations {
    public record Employee(int id, String name, String department, double salary) {
    }

    private EmployeeStreamOperations() {
    }

    public static List<Employee> filterByDepartment(List<Employee> employees, String department) {
        return employees.stream()
                .filter(employee -> employee.department().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    public static List<Employee> sortBySalaryDescending(List<Employee> employees) {
        return employees.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .collect(Collectors.toList());
    }

        public static List<Employee> topPaidEmployees(List<Employee> employees, long limit) {
                return employees.stream()
                                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                                .limit(limit)
                                .collect(Collectors.toList());
        }

        public static List<Employee> skipHighestPaidEmployees(List<Employee> employees, long count) {
                return employees.stream()
                                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                                .skip(count)
                                .collect(Collectors.toList());
        }

    public static Map<String, List<Employee>> groupByDepartment(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::department));
    }

    public static Map<String, Double> averageSalaryByDepartment(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.averagingDouble(Employee::salary)));
    }

    public static Optional<Employee> highestPaidEmployee(List<Employee> employees) {
        return employees.stream()
                .max(Comparator.comparingDouble(Employee::salary));
    }

        public static Optional<Employee> highestPaidWithMaxBy(List<Employee> employees) {
                return employees.stream()
                                .reduce(BinaryOperator.maxBy(Comparator.comparingDouble(Employee::salary)));
        }

        public static Employee highestPaidWithCollectionsMax(List<Employee> employees) {
                return Collections.max(employees, Comparator.comparingDouble(Employee::salary));
        }

        public static List<String> topDepartmentsByAverageSalary(
                        Map<String, Double> averageSalaries, long limit) {
                return averageSalaries.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue(Comparator.<Double>reverseOrder()))
                                .limit(limit)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());
        }

    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Ava", "Engineering", 95000),
                new Employee(2, "Noah", "Sales", 72000),
                new Employee(3, "Mia", "Engineering", 105000),
                new Employee(4, "Liam", "HR", 68000),
                new Employee(5, "Zoe", "Sales", 81000));

        System.out.println("Engineering employees: "
                + filterByDepartment(employees, "Engineering"));
        System.out.println("Employees by descending salary: "
                + sortBySalaryDescending(employees));
        System.out.println("Top 2 salaries: " + topPaidEmployees(employees, 2));
        System.out.println("Employees after skipping the top 2: "
                + skipHighestPaidEmployees(employees, 2));
        System.out.println("Employees grouped by department: "
                + groupByDepartment(employees));
        Map<String, Double> averageSalaries = averageSalaryByDepartment(employees);
        System.out.println("Average salary by department: " + averageSalaries);
        System.out.println("Top 2 departments by average salary: "
                + topDepartmentsByAverageSalary(averageSalaries, 2));
        System.out.println("Highest-paid employee: "
                + highestPaidEmployee(employees).orElse(null));
        System.out.println("Highest-paid using BinaryOperator.maxBy: "
                + highestPaidWithMaxBy(employees).orElse(null));
        System.out.println("Highest-paid using Collections.max: "
                + highestPaidWithCollectionsMax(employees));
    }
}