import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.HashMap;

public class Httpfs {

    final int DEFAULT_PORT = 8081;
    final String DEFAULT_DIRECTORY = "data/";

    //initializing the hashmap with common extensions and content types
    //Source:https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types?fbclid=IwAR2STAFbQmgUA7oW6OQvGsR1oODTXBbR8tupP2DQ0RV5Ta0uUPIJPACaNXY

    public Httpfs() {
        init();
    }

    private void init() {
        // Default port is 8080 if not specified
        int port_number = DEFAULT_PORT;
        String directory = null;
        boolean verbose = false;

        System.out.println("Welcome to httpfs. Usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
        Scanner kb = new Scanner(System.in);

        System.out.println("\nInput httpfs options or press enter to start the server.");
        while (true) {
            System.out.print("httpfs > ");
            String input = kb.nextLine().toLowerCase().trim();
            String[] inputSplit = input.split(" ");
            if (input.contains("/q"))
                break;

            // Parse verbose, port and directory
            if (input.contains("-p")) {
                for(int i = 0; i < inputSplit.length; i++){
                    if(inputSplit[i].equalsIgnoreCase("-p")){
                        port_number = Integer.parseInt(inputSplit[i+1]);
                    }
                }
            }

            if (input.contains("-d")) {
                for(int i = 0; i < inputSplit.length; i++){
                    if(inputSplit[i].equalsIgnoreCase("-d")){
                        directory = inputSplit[i+1];
                        if(directory.charAt(directory.length()-1) != '/'){
                            directory += "/";
                        }
                    }
                }
            }

            if (input.contains("-v")) {
                verbose = true;
            }

            if (input.contains("help")) {
                HELP();
            }


            // Start server
            if (directory == null)
                directory = DEFAULT_DIRECTORY;
            server_socket(directory, port_number, verbose);
        }
    }

    private void server_socket(String directory, int server_port, boolean verbose) {

        // Source #1: https://github.com/SebastienBah/COMP445TA/blob/master/Lab02/httpfs/httpfs.java
        // Source #2: https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
        //initializing the hashmap with common extensions and content types
        //Source:https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types?fbclid=IwAR2STAFbQmgUA7oW6OQvGsR1oODTXBbR8tupP2DQ0RV5Ta0uUPIJPACaNXY
        HashMap<String, String> extensionMap = new HashMap<>();
        extensionMap.put(".xml", "application/xml");
        extensionMap.put(".abw", "application/x-abiword");
        extensionMap.put(".arc", "application/x-freearc");
        extensionMap.put(".avi", "video/x-msvideo");
        extensionMap.put(".css", "text/css");
        extensionMap.put(".csv", "text/csv");
        extensionMap.put(".doc", "application/msword");
        extensionMap.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        extensionMap.put(".html", "text/html");
        extensionMap.put(".js", "text/javascript");
        extensionMap.put(".json", "application/json");
        extensionMap.put(".pdf", "application/pdf");
        extensionMap.put(".php", "application/php");
        extensionMap.put(".txt", "text/plain");

        try (ServerSocket server = new ServerSocket(server_port)) {
            System.out.println("Server has been instantiated at port " + server_port);
            // Server initialized and waits for client requests
            while (true) {
                System.out.println("\nWaiting for client requests...");
                try (Socket socket = server.accept()) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    StringBuilder requestHeaders = new StringBuilder();
                    StringBuilder payload = new StringBuilder();
                    StringBuilder response = new StringBuilder();
                    String userAgent = null;
                    // Source: https://stackoverflow.com/questions/3033755/reading-post-data-from-html-form-sent-to-serversocket
                    String headerLine = null;

                    while((headerLine = in.readLine()).length() != 0){
                        if (headerLine.contains("User-Agent"))
                            userAgent = headerLine;
                        requestHeaders.append(headerLine + "\n");
                    }

                    //code to read the post payload data
                    while(in.ready()){
                        payload.append((char) in.read());
                    }

                    System.out.print(requestHeaders);
                    System.out.println(payload + "\n");

                    // Get the first line containing the HTTP response
                    String requestLine = requestHeaders.toString();


//                    if (verbose) {
//                        response.append(requestHeaders);
//                        if (payload.length() > 0)
//                            response.append(payload + "\n");
//                    }
                    System.out.println(requestLine);
                    String contentType = "";
                    if(requestLine.contains(".")){
                        contentType = extensionMap.get(requestLine.substring(requestLine.indexOf("."), requestLine.indexOf("HTTP")).trim());
                    } else contentType = "folder";

                    // Process HTTP Request
                        boolean directoryForbidden = false;
                        if (requestLine.contains("HTTP")) {
                            String requestType = requestLine.substring(0, requestLine.indexOf("HTTP"));
                            String httpVersion = requestLine.substring(requestLine.indexOf("HTTP"), requestLine.indexOf("\n"));
                            if (directory.length() >= 4) {
                                if (directory.substring(0, 4).equalsIgnoreCase("out/") || directory.substring(0, 4).equalsIgnoreCase("src/")) {
                                    directory = DEFAULT_DIRECTORY;
                                    response.append("\n" + httpVersion + " 403 Forbidden " + "\n" + userAgent + "\nDefaulting to data/");
                                    System.out.println("Directory is not accessible by client. Setting directory to data/");
                                    directoryForbidden = true;
                                }
                            }
                            if (!directoryForbidden) {
                                if (requestType.toString().contains("GET")) {
                                    // TODO Ziad: Append the data (ex: Opening data/data_1.txt) to response
                                    //  String data = GET(requestType);
                                    //  response.append(data)
                                    String get = GET(requestLine, directory, httpVersion, userAgent, contentType, verbose);
                                    response.append(get);
                                }

                                // TODO Melanie
                                else if (requestType.toString().contains("POST")) {
                                    String post = POST(directory, httpVersion, userAgent, requestType, payload.toString(), verbose);
                                    response.append(post);
                                } else {
                                    response.append("\n" + httpVersion + " 400 Bad Request \n" + userAgent);
                                }
                            };
                        }
                    // Send the response back
                    out.print(response.toString() + "\n");
                    out.close();
                    in.close();
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // End source
    }

    private String GET(String input, String directory, String httpVersion, String userAgent, String contentType, boolean verbose) {
        StringBuilder returned = new StringBuilder();
        String[] inputDivided = input.split(" ");

        if (input.contains("/")){
            if((input.charAt(input.length() - 1) == '/') || (input.charAt(input.indexOf("/") + 1) == ' ')) {
                if (verbose) {
                System.out.println("Processing GET request");
                System.out.println(input);
                System.out.println("Showing contents of directory: " + directory);
                }
                returned.append(listFiles(directory, httpVersion, userAgent, verbose));
            } else {
                String fileName = inputDivided[1].substring(1);
                String fileToOpen = directory + fileName;
                if (fileToOpen.contains(".")) {
                    String wholeText= "";
                    if (verbose) {
                        System.out.println("Processing GET request");
                        System.out.println(input);
                        System.out.println("Opening file: " + fileToOpen);
                    }
                    try {
                        wholeText = new String(Files.readAllBytes(Paths.get(fileToOpen)));
                        returned.append("\n" + httpVersion + " 200 OK " + "\n" + userAgent);
                        if (wholeText.length() > 0) {
                            returned.append("\nContent-Length: " + wholeText.length());
                            returned.append("\nContent-Type: " + contentType + "\n");
                        }
                        returned.append("\n" + wholeText);
                    } catch (IOException o){
                        System.out.println("File cannot be opened/has no content");
                        returned.append("\n" + httpVersion + " 404 Not found " + "\n" + userAgent);
                    }

                } else {
                    if (verbose) {
                        System.out.println("Processing GET request");
                        System.out.println(input);
                        System.out.println("File to open was a folder named: " + fileToOpen);
                        System.out.println("Showing folder contents to client...");
                    }
                    returned.append("Opening Directory: " + fileToOpen + "\n");
                    returned.append(listFiles(fileToOpen, httpVersion, userAgent,verbose));
                }
            }

        }
        return returned.toString();
    }
    private String listFiles(String parent, String httpVersion, String userAgent, boolean verbose){
        StringBuilder returned = new StringBuilder();
        File dataDir = new File(parent);
        File[] filesInDirectory = dataDir.listFiles();
        if(filesInDirectory != null){
            if(filesInDirectory.length != 0){
                if (verbose) {
                returned.append("\n" + httpVersion + " 200 OK " + "\n" + userAgent + "\n");
                }
                returned.append("File/Directory Name\t\t\t\tType\n");
                for (int i = 0; i < filesInDirectory.length; i++){
                    String fileNameComplete = filesInDirectory[i].getName();
                    String fileName;
                    String fileType;
                    if(fileNameComplete.contains(".")) {
                        fileName = fileNameComplete.substring(0, fileNameComplete.lastIndexOf('.'));
                        fileType = fileNameComplete.substring(fileNameComplete.lastIndexOf('.'));
                    } else {
                        fileName = fileNameComplete.trim();
                        fileType = "Folder";
                    }
                    returned.append(fileName).append("\t\t\t\t\t\t").append(fileType).append("\n"); //FilenameUtils.getExtension(filesInDirectory[i])
                }

            } else returned.append("Directory is empty");
        } else {
            if (verbose){
                returned.append("\n" + httpVersion + " 404 Not found " + "\n" + userAgent);
            }
            returned.append("\nDirectory not found\n");

        }
        return returned.toString();
    }

    private String POST(String directory, String httpVersion, String userAgent, String requestLine, String body, boolean verbose) {
        String filePath = requestLine.substring(requestLine.indexOf("POST ")+5, requestLine.length()-1);
        String fileDirectory = directory + filePath;

        if (verbose) {
            System.out.println("Processing POST request");
            System.out.println(requestLine + body);
            System.out.println("Attempting to write file " + fileDirectory);
        }

        try {
            // Check if the file exists
            File file = new File(fileDirectory);
            if (file.createNewFile()) {
                String response = "\n" + httpVersion + " 201 Created " + "\n" + userAgent;
                if (verbose) {
                    System.out.println(response);
                }
                return response;
            }

            Writer fileWriter = new FileWriter(fileDirectory, false);
            fileWriter.write(body);
            fileWriter.close();
            if (verbose) {
                System.out.println("Successfully written to the file.");
            }
            String okResponse = "\n" + httpVersion + " 200 OK " + "\n" + userAgent;
            if (verbose) {
                System.out.println(okResponse);
            }
            return okResponse;
        } catch (IOException e) {
            if (verbose) {
                System.out.println("File cannot be overwritten");
            }
            String forbidden = "\n" + httpVersion + " 403 Forbidden " + "\n" + userAgent;
            return forbidden;
        }
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
