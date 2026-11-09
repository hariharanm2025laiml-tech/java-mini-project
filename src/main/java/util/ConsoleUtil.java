package util;

import java.util.Scanner;

public class ConsoleUtil {

    private static final Scanner scanner = new Scanner(System.in);

    public static void printHeader(String title) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(centerText(title.toUpperCase(), 50));
        System.out.println("==================================================");
    }

    public static void printSection(String sectionTitle) {
        System.out.println();
        System.out.println("--- " + sectionTitle + " ---");
    }

    public static void printSuccess(String message) {
        System.out.println("\n[SUCCESS] " + message);
    }

    public static void printError(String message) {
        System.out.println("\n[ERROR] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("\n[WARNING] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("\n[INFO] " + message);
    }

    public static String readString(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    public static String readRequiredString(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("-> This field cannot be empty. Please enter a value.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("-> Please enter a number between %d and %d.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("-> Invalid input! Please enter a valid number.");
            }
        }
    }

    public static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("-> Value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("-> Invalid number format! Please enter a valid decimal number.");
            }
        }
    }

    public static boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("-> Please type 'y' for yes or 'n' for no.");
        }
    }

    public static void pressEnterToContinue() {
        System.out.print("\nPress [ENTER] to continue...");
        scanner.nextLine();
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) sb.append(" ");
        sb.append(text);
        while (sb.length() < width) sb.append(" ");
        return sb.toString();
    }
}
