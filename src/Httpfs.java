import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Httpfs {

    final int DEFAULT_PORT = 8080;
    final String DEFAULT_DIRECTORY = "data/";

    public Httpfs() {
        init();
    }

    private void init() {
        // Default port is 8080 if not specified
        int port_number = DEFAULT_PORT;
        String directory = null;
        boolean verbose = true;

        System.out.println("Welcome to httpfs. Usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
        Scanner kb = new Scanner(System.in);

        System.out.println("\nInput httpfs options or press enter to start the server.");
        while (true) {
            System.out.print("httpfs > ");
            String input = kb.nextLine().toLowerCase().trim();

            if (input.contains("/q"))
                break;

            // Parse verbose, port and directory
            if (input.contains("-p")) {
                // port_number = Integer.valueOf(input);
            }

            if (input.contains("-d")) {

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

                    // Process HTTP Request
                    if (requestLine.contains("HTTP")) {
                        String requestType = requestLine.substring(0, requestLine.indexOf("HTTP"));
                        String httpVersion = requestLine.substring(requestLine.indexOf("HTTP"), requestLine.indexOf("\n"));

                        if (requestType.toString().contains("GET")) {
                            // TODO Ziad: Append the data (ex: Opening data/data_1.txt) to response
                            //  String data = GET(requestType);
                            //  response.append(data)
                            GET(requestType);
                            }

                        // TODO Melanie
                        else if (requestType.toString().contains("POST")) {
                            String post = POST(directory, httpVersion, userAgent, requestType, payload.toString(), verbose);
                            response.append(post);
                        }

                        else {
                            response.append("\n" + httpVersion + " 400 Bad Request \n" + userAgent);
                        }
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

    private void GET(String input){
        boolean verbose = false;
        if (input.contains("-v")) {
            input = input.replace("-v", "").trim();
            verbose = true;
        }
        String[] inputDivided = input.split(" ");

        if (input.contains("/")){
            if((input.charAt(input.length() - 1) == '/') || (input.charAt(input.indexOf("/") + 1) == ' ')) {
                listFiles("data");
            } else {
                String fileName = inputDivided[1];
                boolean readable = false;
                String fileToOpen = "data" + fileName;
                while (!readable) {
                    if (fileToOpen.contains(".")) {
                        readable = true;
                    } else {
                        listFiles(fileToOpen);
                        System.out.print("File specified was a directory, choose a file name within this directory to read: ");
                        Scanner kb = new Scanner(System.in);
                        String userInput = kb.nextLine().trim();
                        fileToOpen += "/" + userInput;
                    }
                }
                System.out.println("Opening " + fileToOpen);
            }

        }
    }
    private void listFiles(String parent){
        File dataDir = new File(parent);
        File[] filesInDirectory = dataDir.listFiles();
        System.out.println("File/Directory Name         Type");
        if(filesInDirectory.length != 0){
            for (int i = 0; i < filesInDirectory.length; i++){
                String fileNameComplete = filesInDirectory[i].getName();
                String fileName;
                String fileType;
                if(fileNameComplete.contains(".")) {
                    fileName = fileNameComplete.substring(0, fileNameComplete.lastIndexOf('.'));
                    fileType = fileNameComplete.substring(fileNameComplete.lastIndexOf('.'));
                } else {
                    fileName = fileNameComplete.trim();
                    fileType = "\tFolder";
                }
                System.out.println(fileName + "\t\t\t\t\t\t" + fileType); //FilenameUtils.getExtension(filesInDirectory[i])
            }
        }
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
