import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

public class Httpc {

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

    // get [-v] [-h key:value] URL
    private void GET(String input) {
        System.out.println("GET METHOD");
    }

    // post [-v] [-h key:value] [-d inline-data] [-f file] URL
    private void POST(String input) {
        // TO-DO Process input for headers, body and URL
        String body = "{"
                + "\"key1\":value1,"
                + "\"key2\":value2"
                + "}";

        String web = "http://httpbin.org/post";

        try {
            URL url = new URL(web);
            String host = url.getHost();
            String path = url.getPath();
            String query = url.getQuery();
            if (query != null) {
                query = "?" + query;
            } else
                query = "";

            // Create socket using standard port 80 for web
            Socket socket = new Socket(host, 80);

            String post_request = "POST " + path + query + " HTTP/1.0\r\n"
                    + "Content-Type:application/application/json\r\n"
                    + "Content-Length: " + body.length() + "\r\n"
                    + "\r\n"
                    + body;

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(post_request.getBytes());
            outputStream.flush();

            StringBuilder response = new StringBuilder();

            int data = inputStream.read();

            while(data != -1) {
                response.append((char) data);
                data = inputStream.read();
            }

            if (input.contains("-v")) {
                System.out.println(response);
            } else {
                System.out.println(response.substring(response.indexOf("\r\n\r\n")).trim());
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void HELP(String input) {
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
