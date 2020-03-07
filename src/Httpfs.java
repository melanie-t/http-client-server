import java.io.*;
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
        boolean verbose = true;

        System.out.println("Welcome to httpfs. Usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]");
        Scanner kb = new Scanner(System.in);

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
            server_socket(port_number, verbose);
        }
    }

    private void server_socket(int server_port, boolean verbose) {

        // Source #1: https://github.com/SebastienBah/COMP445TA/blob/master/Lab02/httpfs/httpfs.java
        // Source #2: https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
        try (ServerSocket server = new ServerSocket(server_port)) {
            System.out.println("Server has been instantiated at port " + server_port + "\n");
            // Server initialized and waits for client requests
            while (true) {
                try (Socket socket = server.accept()) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String requestType;
                    StringBuilder httpRequest = new StringBuilder();
                    StringBuilder response = new StringBuilder();

                    // Get the first line containing the HTTP response
                    String requestLine = in.readLine();
                    httpRequest.append(requestLine + "\n");

                    // Source: https://codereview.stackexchange.com/questions/44135/is-it-ok-to-use-while-line-r-readline-null-construct
                    // Process all other lines
                    for (String line = in.readLine(); line != null; line = in.readLine()) {
                        if (line.equals(""))
                            break;
                        httpRequest.append(line + "\n");
                    }

                    if (verbose) {
                        response.append(httpRequest);
                    }

                    System.out.println(httpRequest);

                    // Process HTTP Request
                    if (requestLine.contains("HTTP")) {
                        requestType = requestLine.substring(0, requestLine.indexOf("HTTP"));
                        if (requestType.toString().contains("GET")) {
                            // TODO Ziad: Append the data (ex: Opening data/data_1.txt) to response
                            //  String data = GET(requestType);
                            //  response.append(data)
                            GET(requestType);
                            }

                        // TODO Melanie
                        else if (requestType.toString().contains("POST")) {

                        }
                    }
                    else {
                        response.append("Invalid request. Httpfs only supports POST and GET HTTP requests");
                    }

                    // Send the response back
                    out.print(response.toString());
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

    private String POST(String input, boolean verbose) {
        StringBuilder postResponse = new StringBuilder();
        // Process headers

        return postResponse.toString();
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
