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
            String[] arguments = input.split(" ");
            String requestType = arguments[0];

            if (input.equalsIgnoreCase("/q")) {
                running = false;
            } else if (requestType.equalsIgnoreCase("help")) {
                client.HELP(input);
            } else if (requestType.equalsIgnoreCase("get")) {
                client.GET(arguments);
            } else if (requestType.equalsIgnoreCase("post")) {
                client.POST(arguments);
            } else {
                System.out.println("Invalid command. Input help for help");
            }
        }
        System.out.println("Httpc terminated successfully");
    }
}
