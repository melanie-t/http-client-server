import java.util.Scanner;
public class Httpc {

    public void init() {}
    public Httpc() {
        init();
    }

    private void init() {
        System.out.println("Welcome to httpc. To quit the application, enter /q");

        Scanner kb = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.print("httpc> ");
            String requestType = kb.next();
            String input = kb.nextLine().trim();

            if (requestType.equalsIgnoreCase("/q")) {
                running = false;
            } else if (requestType.equalsIgnoreCase("help")) {
                HELP(input);
            } else if (requestType.equalsIgnoreCase("get")) {
                GET(input);
            } else if (requestType.equalsIgnoreCase("post")) {
                POST(input);
            } else {
                System.out.println("Invalid command. Input help for help");
            }
        }
        System.out.println("Httpc terminated successfully");
    }

    // httpc get [-v] [-h key:value] URL
    public void GET(String[] input) {
        System.out.println("GET METHOD");
    }

    // httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL
    public void POST(String[] input) {
        System.out.println("POST METHOD");
        // Process input
    }

    public void HELP(String input) {
        if (input.contains("get")) {
            System.out.println("Usage: httpc get [-v] [-h key:value] URL" +
                    "\n\n" + "Get executes a HTTP GET request for a given URL. " +
                    "\n\t" + "-v Prints the detail of the response such as protocol, status, and headers. " +
                    "\n\t" + "-h key:value Associates headers to HTTP Request with the format 'key:value'.\n"
            );
        } else if (input.contains("post")) {
            System.out.println("Usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL " +
                    "\n\n" + "Post executes a HTTP POST request for a given URL with inline data or from file. " +
                    "\n\t" + "-v Prints the detail of the response such as protocol, status, and headers. " +
                    "\n\t" + "-h key:value Associates headers to HTTP Request with the format 'key:value'. " +
                    "\n\t" + "-d string Associates an inline data to the body HTTP POST request. " +
                    "\n\t" + "-f file Associates the content of a file to the body HTTP POST request. " +
                    "\n\n" + "Either [-d] or [-f] can be used but not both.\n"
            );
        } else {
            System.out.println("httpc is a curl-like application but supports HTTP protocol only. " +
                    "\n" + "Usage: " +
                    "\n\t" + "httpc command [arguments] " +
                    "\n" + "The commands are: " +
                    "\n\t" + "get \t executes a HTTP GET request and prints the response. " +
                    "\n\t" + "post \t executes a HTTP POST request and prints the response. " +
                    "\n\t" + "help \t prints this screen. " +
                    "\n\n" + "Use \"httpc help [command]\" for more information about a command.\n"
            );
        }
    }
}
