import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Httpfs {
    public Httpfs() {
        init();
    }

    private void init() {
        System.out.println("Welcome to httpfs. To quit the application, enter /q");

        Scanner kb = new Scanner(System.in);
        boolean running = true;
        while (running) {

            System.out.print("httpfs> ");
            String requestType = kb.next();
            String input = kb.nextLine().trim();

            if (requestType.equalsIgnoreCase("/q")) {
                running = false;
            } else if (requestType.equalsIgnoreCase("help")) {
                HELP();
            }
        }
        System.out.println("Httpfs terminated successfully");
    }

    private void server_socket(int portNumber) throws IOException {
        ServerSocket socket = new ServerSocket(portNumber);
        Socket client = socket.accept();
        PrintWriter out = new PrintWriter(client.getOutputStream());

    }

    private void HELP() {
        System.out.println("httpfs is a simple file server." +
                "\n\n" + "Usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]" +
                "\n\t" + "-v\t Prints debugging messages. " +
                "\n\t" + "-p\t Specifies the port number that the server will listen and serve at. Default is 8080." +
                "\n\t" + "-d\t Specifies the directory that the server will use to read/write requested files. " +
                "Default is the current directory when launching the application."
        );
    }
}
