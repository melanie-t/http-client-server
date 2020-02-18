import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Httpfs {

    final int DEFAULT_PORT = 8080;
    final String DEFAULT_DIRECTORY = "/data/";

    public Httpfs() {
        init();
    }

    private void init() {
        // Default port is 8080 if not specified
        int port_number = DEFAULT_PORT;
        String directory = DEFAULT_DIRECTORY;
        boolean verbose = false;

        System.out.println("Welcome to httpfs. Usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
        Scanner kb = new Scanner(System.in);

        while (true) {
            System.out.print("httpfs > ");
            String input = kb.nextLine().toLowerCase().trim();

//            if (input.contains("/q"))
//                break;
//
//            // Parse verbose, port and directory
//            if (input.contains("-p")) {
//                // port_number = Integer.valueOf(input);
//            }
//
//            if (input.contains("-d")) {
//
//            }
//
//            if (input.contains("-v")) {
//                verbose = true;
//            }
//
//            if (input.contains("help")) {
//                HELP();
//            }

            // Start server
            server_socket(port_number);
        }
    }

    private void server_socket(int server_port) {

        // Source: https://github.com/SebastienBah/COMP445TA/blob/master/Lab02/httpfs/httpfs.java
        try (ServerSocket server = new ServerSocket(server_port)) {
            System.out.println("Server has been instantiated at port " + server_port);
            // Server initialized and waits for client requests
            while(true) {
                // Is this a blocking or non-blocking call?
                // What would you need to do to service multiple clients at the same time?
                try (Socket client_connection = server.accept()) {
                    PrintWriter outbount_client = new PrintWriter(client_connection.getOutputStream(), true);
                    outbount_client.println("Well hello to you too.");
                    client_connection.close();
                    System.out.println("Httpfs terminated successfully");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // End source
    }

    private void HELP() {
        System.out.println("httpfs is a simple file server." +
                "\n" + "Usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]" +
                "\n\t" + "-v\t Prints debugging messages. " +
                "\n\t" + "-p\t Specifies the port number that the server will listen and serve at. Default is 8080." +
                "\n\t" + "-d\t Specifies the directory that the server will use to read/write requested files. " +
                "Default is the current directory when launching the application.\n"
        );
    }
}
