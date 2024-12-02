
import java.io.*;
import java.util.*;

/**
 * Fleet Management System for Coconut Grove Sailing Club.
 */
public class FleetManagement {

    private static final String SERIALIZED_FILE = "FleetData.db";
    private static final List<Boat> fleet = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length > 0) {
            loadFromCSV(args[0]);
        } else {
            loadFromSerializedFile();
        }

        boolean exit = false;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Fleet Management System");
        System.out.println("--------------------------------------");

        while (!exit) {
            System.out.print("(P)rint, (A)dd, (R)emove, (E)xpense, e(X)it : ");
            char choice = scanner.next().toUpperCase().charAt(0);
            switch (choice) {
                case 'P':
                    printFleet();
                    break;
                case 'A':
                    addBoat(scanner);
                    break;
                case 'R':
                    removeBoat(scanner);
                    break;
                case 'E':
                    addExpense(scanner);
                    break;
                case 'X':
                    saveToSerializedFile();
                    System.out.println("Exiting the Fleet Management System");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid menu option, try again");
            }
        }
        scanner.close();
    }

    private static void loadFromCSV(String csvFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    BoatType type = BoatType.valueOf(parts[0].toUpperCase());
                    String name = parts[1];
                    int year = Integer.parseInt(parts[2]);
                    String makeModel = parts[3];
                    int length = Integer.parseInt(parts[4]);
                    double purchasePrice = Double.parseDouble(parts[5]);
                    fleet.add(new Boat(type, name, year, makeModel, length, purchasePrice, 0.0));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private static void loadFromSerializedFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SERIALIZED_FILE))) {
            List<Boat> loadedFleet = (List<Boat>) ois.readObject();
            fleet.addAll(loadedFleet);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No serialized data found, starting with an empty fleet.");
        }
    }

    private static void saveToSerializedFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SERIALIZED_FILE))) {
            oos.writeObject(fleet);
        } catch (IOException e) {
            System.out.println("Error saving fleet data: " + e.getMessage());
        }
    }

    private static void printFleet() {
        System.out.println("\nFleet report:");
        double totalPaid = 0;
        double totalSpent = 0;

        for (Boat boat : fleet) {
            System.out.printf("    %-8s %-20s %4d %-12s %3d' : Paid $ %10.2f : Spent $ %10.2f\n",
                    boat.getType(), boat.getName(), boat.getYear(), boat.getMakeModel(),
                    boat.getLength(), boat.getPurchasePrice(), boat.getExpenses());
            totalPaid += boat.getPurchasePrice();
            totalSpent += boat.getExpenses();
        }

        System.out.printf("    Total                                             : Paid $ %10.2f : Spent $ %10.2f\n",
                totalPaid, totalSpent);
    }

    private static void addBoat(Scanner scanner) {
        System.out.print("Please enter the new boat CSV data          : ");
        scanner.nextLine();
        String csvData = scanner.nextLine();
        String[] parts = csvData.split(",");

        try {
            BoatType type = BoatType.valueOf(parts[0].toUpperCase());
            String name = parts[1];
            int year = Integer.parseInt(parts[2]);
            String makeModel = parts[3];
            int length = Integer.parseInt(parts[4]);
            double purchasePrice = Double.parseDouble(parts[5]);
            fleet.add(new Boat(type, name, year, makeModel, length, purchasePrice, 0.0));
        } catch (Exception e) {
            System.out.println("Error adding boat: " + e.getMessage());
        }
    }

    private static void removeBoat(Scanner scanner) {
        System.out.print("Which boat do you want to remove?           : ");
        scanner.nextLine();
        String name = scanner.nextLine().trim();
        boolean removed = fleet.removeIf(boat -> boat.getName().equalsIgnoreCase(name));

        if (removed) {
            System.out.println("Boat removed.");
        } else {
            System.out.println("Cannot find boat " + name);
        }
    }

    private static void addExpense(Scanner scanner) {
        System.out.print("Which boat do you want to spend on?         : ");
        scanner.nextLine();
        String name = scanner.nextLine().trim();

        for (Boat boat : fleet) {
            if (boat.getName().equalsIgnoreCase(name)) {
                System.out.print("How much do you want to spend?              : ");
                double amount = scanner.nextDouble();

                if (amount <= boat.getRemainingBudget()) {
                    boat.addExpense(amount);
                    System.out.println("Expense authorized, $" + String.format("%.2f", amount) + " spent.");
                } else {
                    System.out.printf("Expense not permitted, only $ %.2f left to spend.%n", boat.getRemainingBudget());
                }
                return;
            }
        }
        System.out.println("Cannot find boat " + name);
    }
}

class Boat implements Serializable {
    private final BoatType type;
    private final String name;
    private final int year;
    private final String makeModel;
    private final int length;
    private final double purchasePrice;
    private double expenses;

    public Boat(BoatType type, String name, int year, String makeModel, int length, double purchasePrice, double expenses) {
        this.type = type;
        this.name = name;
        this.year = year;
        this.makeModel = makeModel;
        this.length = length;
        this.purchasePrice = purchasePrice;
        this.expenses = expenses;
    }

    public BoatType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }

    public String getMakeModel() {
        return makeModel;
    }

    public int getLength() {
        return length;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public double getExpenses() {
        return expenses;
    }

    public double getRemainingBudget() {
        return purchasePrice - expenses;
    }

    public void addExpense(double amount) {
        expenses += amount;
    }
}

enum BoatType {
    SAILING, POWER
}
