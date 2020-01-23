import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Main Loop

        Scanner kb = new Scanner(System.in);
        boolean running = true;

        Httpc client = new Httpc();

        System.out.println("Welcome to httpc. To quit the application, enter /q");

        while (running) {
            System.out.print("httpc> ");
            String input = kb.nextLine().trim();

            if (input.equalsIgnoreCase("/q")) {
                running = false;
            } else if (input.contains("help")) {
                client.help(input);
            } else {
                System.out.println("Invalid command. Input help for help");
            }
        }
        System.out.println("Httpc terminated successfully");
    }
}
