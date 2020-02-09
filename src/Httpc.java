import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            } else if (!input.contains("http://")) {
                System.out.println("[INVALID INPUT] URL not specified");
                HELP("");
            } else if (requestType.equalsIgnoreCase("get")) {
                if (input.contains("-d") || input.contains("-f")) {
                    System.out.println("[INVALID INPUT] GET request cannot contain -d or -f arguments");
                    HELP("get");
                } else {
                    GET(input);
                }
            } else if (requestType.equalsIgnoreCase("post")) {
                if (input.contains("-d") && input.contains("-f")) {
                    System.out.println("[INVALID INPUT] POST request can contain either -d or -f, but not both");
                    HELP("post");
                } else {
                    POST(input);
                }
            } else {
                System.out.println("Invalid command");
                HELP("");
            }
        }
        System.out.println("Httpc terminated successfully");
    }

    private String[] parseInput(String input) {
        String[] parameters = new String[]{"web", "header", "data"};
        String web = "";
        String headers = "";
        String data = "";
        StringBuilder headerBuilder = new StringBuilder();

        if (input.contains("-d")){
            data = create_body(input);
        } else if (input.contains("-f")){
            String fileToOpen = "";
            try {
                String[] inputWords = input.split(" ");
                for (int i = 0; i < inputWords.length; i++){
                    if(inputWords[i].trim().equals("-f")){
                        if(i+1 != inputWords.length) //handles case where -f is not succeeded by any string
                        {
                            fileToOpen = inputWords[i+1];
                            System.out.println("Opening " + fileToOpen);
                        }
                    }
                }
                String wholeText = new String(Files.readAllBytes(Paths.get(fileToOpen)));
                String objectData = wholeText.substring(wholeText.indexOf("{"),wholeText.lastIndexOf("}")+ 1);
                data = create_body(objectData);
            } catch (FileNotFoundException e) {
                if (fileToOpen.equals("")){
                    System.out.println("File name was not specified in command line. Default will be printed.");
                }else System.out.println("File does not exist. Default will be printed.");
            } catch (IOException e) {
                System.out.println("File name was not specified in command line. Default will be printed.");
            }
        }

        if (input.contains("-h")) {
            while(input.contains("-h")) {
                // indexHeaderFlag is the index of -h in the input string, and +2 because we want to splice -h
                int indexHeaderFlag = input.indexOf("-h")+2;

                // Remove -h argument from input string
                input = input.substring(indexHeaderFlag).trim();

                // indexHeaderEnd marks the end of the header key/value which is denoted by a space
                int indexHeaderEnd = input.indexOf(" ");
                String headerKeyValue = input.substring(0, indexHeaderEnd).trim();
                headerBuilder.append(headerKeyValue + "\r\n");

                // Remove processed header key-value pair
                input = input.substring(indexHeaderEnd).trim();
            }
            if (data.length() != 0) {
                headerBuilder.append("Content-Length: " + data.length() + "\r\n");
            }
            headerBuilder.append("\r\n");
            headers = headerBuilder.toString();
        }
        // All arguments (-v, -h, -d, -f) are all processed, so all that's left is the URL
        input = input.replace("'", "").trim();
        web = input.substring(input.indexOf("http://"));

        parameters[0] = web;
        parameters[1] = headers;
        parameters[2] = data;

        return parameters;
    }

    private String create_body(String input){
        StringBuilder body = new StringBuilder();
        String[] keys = new String[5];
        String[] values = new String[5];
        //regex with pattern and matcher created to find all key values between double quotes
        String dataToParse = input.substring(input.indexOf("{") + 1, input.indexOf("}"));
        Pattern key = Pattern.compile("\"([^\"]*)\"");
        Matcher match = key.matcher(dataToParse);
        int keyCount = 0;
        while(match.find()) {
            keys[keyCount] = match.group(1);
            keyCount++;
        }
        //regex matches the value between : and , except for the final value which is between , and the end of the string $ and puts them in the values array;
        Pattern val = Pattern.compile(":[^,]*(,|$)");
        match = val.matcher(dataToParse);
        int valCount = 0;
        while(match.find()){
            if (valCount != keyCount - 1)
                values[valCount] = (match.group(0).substring(1, match.group(0).length() - 1)).trim();
            else values[valCount] = (match.group(0).substring(1)).trim();
            valCount++;
        }
        String[] bodyEntries = new String[keyCount];

        for(int i = 0; i < bodyEntries.length; i++){
            bodyEntries[i] = "\"" + keys[i] + "\": " + values[i];
        }

        //body is a combination of all key:value combinations
        body = new StringBuilder("{");
        for(int bodyEntry = 0; bodyEntry< bodyEntries.length; bodyEntry++){
            if (bodyEntry != bodyEntries.length -1)
                body.append(bodyEntries[bodyEntry]).append(",");
            else body.append(bodyEntries[bodyEntry]).append("}");
        }
        return body.toString();
    }

    // get [-v] [-h key:value] URL
    private void GET(String input) {
        boolean verbose = false;
        if (input.contains("-v")) {
            input = input.replace("-v", "").trim();
            verbose = true;
        }

        /* parameters is an array with:
            params[0] = web
            params[1] = headers
            params[2] = data
         */
        String[] parameters = parseInput(input);

        try {
            send_request("GET", parameters[0], parameters[1], parameters[2], verbose);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // post [-v] [-h key:value] [-d inline-data] [-f file] URL
    private void POST(String input) {
        boolean verbose = false;
        if (input.contains("-v")) {
            input = input.replace("-v", "");
            verbose = true;
        }

        /* parameters is an array with:
            params[0] = web
            params[1] = headers
            params[2] = data
         */
        String[] parameters = parseInput(input);

        try {
            send_request("POST", parameters[0], parameters[1], parameters[2], verbose);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void send_request(String requestType, String web, String headers, String data, boolean verbose) throws Exception {
        URL url = new URL(web);

        String host = url.getHost();
        String path = url.getPath();
        String query = url.getQuery();
        if (query != null) {
            query = "?" + query;
        } else
            query = "";

        // Referenced: Cristian's Httpc from tutorial [
        // Create socket using standard port 80 for web
        Socket socket = new Socket(host, 80);
        String request = requestType + " " + path + query + " HTTP/1.0\r\n"
                + headers + data + "\r\n";

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        outputStream.write(request.getBytes());
        outputStream.flush();

        StringBuilder response = new StringBuilder();

        int response_data = inputStream.read();

        while(response_data != -1) {
            response.append((char) response_data);
            response_data = inputStream.read();
        }

        if (response.length() > 0) {
            if (verbose) {
                System.out.println(response);
            } else {
                System.out.println(response.substring(response.indexOf("\r\n\r\n")));
            }
        } else {
            System.out.println("The response is empty");
        }

        socket.close();
        // ] End of reference
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
