// Melanie Taing (40009850)
// Ziad Hawa (40050712)
// COMP 445
// Nagi Basha
// Assignment 3

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDPHttpc {

    final static int CLIENT_PORT = 41830;
    final static String CLIENT_IP = "192.168.2.125";

    private UDPHttpc() {}
    public static void main(String[] args) {
        init();
    }

    private static void init() {
        System.out.println("Welcome to httpc. To quit the application, enter /q");
        DatagramSocket socket = null;
        Scanner kb = new Scanner(System.in);
        try {
            InetAddress clientAddr = InetAddress.getByName(CLIENT_IP);
            socket = new DatagramSocket(CLIENT_PORT);
            socket.setSoTimeout(5000);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

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
                    GET(input, socket);
                }
            } else if (requestType.equalsIgnoreCase("post")) {
                if (input.contains("-d") && input.contains("-f")) {
                    System.out.println("[INVALID INPUT] POST request can contain either -d or -f, but not both");
                    HELP("post");
                } else {
                    POST(input, socket);
                }
            } else {
                System.out.println("Invalid command");
                HELP("");
            }
        }
        System.out.println("Httpc terminated successfully");
    }

    private static String[] parseInput(String input) {
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
                } else System.out.println("File does not exist. Default will be printed.");
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
                if (!headerKeyValue.contains(":")) {
                    System.out.println("[INVALID ARGUMENT] Header should contain key:value");
                    break;
                } else {
                    headerBuilder.append(headerKeyValue + "\r\n");
                    // Remove processed header key-value pair
                    input = input.substring(indexHeaderEnd).trim();
                }
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

    private static String create_body(String input){
        StringBuilder body = new StringBuilder();
        String[] keys = new String[5];
        String[] values = new String[5];
        //regex with pattern and matcher created to find all key values between double quotes
        if (input.contains("{") && input.contains("}")){
            int first_brace = input.indexOf("{");
            int second_brace = input.indexOf("}");

            if (first_brace != -1 && second_brace != -1) {
            String dataToParse = input.substring(first_brace + 1, second_brace);
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
            for(int bodyEntry = 0; bodyEntry< bodyEntries.length; bodyEntry++) {
                if (bodyEntry != bodyEntries.length - 1)
                    body.append(bodyEntries[bodyEntry]).append(",");
                else body.append(bodyEntries[bodyEntry]).append("}");
            }
            }
        }else if (input != null){
            //String[] parameters = StringUtils.split(" ");
            String contentToWrite = "\r\nno content";
            Pattern p = Pattern.compile("'([^\"]*)'");
            Matcher m = p.matcher(input);
            m.find();
            contentToWrite = m.group(1).substring(0,m.group(1).indexOf("'"));
            body = new StringBuilder(contentToWrite);
        } else {
            System.out.println("[INVALID ARGUMENT] Data not specified or in the wrong format");
        }
        return body.toString();
    }

    // get [-v] [-h key:value] URL
    private static void GET(String input, DatagramSocket socket) {
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
            send_request(socket,"GET", parameters[0], parameters[1], parameters[2], verbose);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // post [-v] [-h key:value] [-d inline-data] [-f file] URL
    private static void POST(String input, DatagramSocket socket) {
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
            send_request(socket, "POST", parameters[0], parameters[1], parameters[2], verbose);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void send_request(DatagramSocket socket, String requestType, String web, String headers, String data, boolean verbose) throws Exception {
        URL url = new URL(web);

        String host = url.getHost();
        String path = url.getPath();
        String query = url.getQuery();
        if (query != null) {
            query = "?" + query;
        } else
            query = "";

        // Referenced: Cristian's Httpc from tutorial [
        String request = requestType + " " + path + query + " HTTP/1.0\r\n"
                + headers + data + "\r\n";

        // Router address
        String routerHost = "localhost";
        int routerPort = 3000;

        // Server address
        String serverHost = "localhost";
        int serverPort = 8007;

        InetAddress routerAddr = InetAddress.getByName(routerHost);
        SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
        InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);

        try {
            // Reference: UDPClient.java provided to us
            long sequenceNumber = 1L;

            try {
                // TCP Handshake (SYN, SYN_ACK, ACK)
                // Send SYN to server
                Packet synPkt = new Packet.Builder()
                        .setType(PacketType.SYN.getValue())
                        .setSequenceNumber(0)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(new byte[0])
                        .create();
                DatagramPacket synPacket = new DatagramPacket(synPkt.toBytes(), synPkt.toBytes().length, routerAddr, routerPort);

                Packet payloadP = new Packet.Builder()
                        .setType(PacketType.ACK.getValue())
                        .setSequenceNumber(1)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(request.getBytes())
                        .create();
                DatagramPacket payloadPacket = new DatagramPacket(payloadP.toBytes(), payloadP.toBytes().length,
                        routerAddr, routerPort);

                socket.send(synPacket); // Send SYN
                System.out.printf("---------------------------------------------------\n");
                System.out.printf("SEND: Packet: %s\n", synPkt);
                System.out.printf("Type: %s (%s)\n", PacketType.valueOf(synPkt.getType()), synPkt.getType());
                if (verbose) {
                    System.out.printf("Sequence number: %s\n", synPkt.getSequenceNumber());
                    System.out.printf("Router: %s:%s\n", routerAddr, routerPort);
                    System.out.printf("Server: %s:%s\n", synPkt.getPeerAddress(), synPkt.getPeerPort());
                }
                System.out.printf("---------------------------------------------------\n");
                byte[] synAckBuff = new byte[Packet.MAX_LEN];
                while(true) {
                    DatagramPacket getSynAck = new DatagramPacket(synAckBuff, synAckBuff.length);
                    try {
                        socket.receive(getSynAck);
                    } catch (SocketTimeoutException e) {
                        // resend
                        socket.send(synPacket);
                        continue;
                    }
                    // Check data received if it's SYN_ACK for handshake confirmation
                    Packet synAckPkt = Packet.fromBytes(synAckBuff);
                    if (PacketType.SYN_ACK.getValue() == synAckPkt.getType()) {
                        System.out.printf("---------------------------------------------------\n");
                        System.out.printf("RCVD: Packet: %s\n", synAckPkt);
                        System.out.printf("Type: %s (%s)\n", PacketType.valueOf(synAckPkt.getType()), synAckPkt.getType());
                        if (verbose) {
                            System.out.printf("Sequence number: %s\n", synAckPkt.getSequenceNumber());
                            System.out.printf("Router: %s:%s\n", routerAddr, routerPort);
                            System.out.printf("Server: %s:%s\n", synAckPkt.getPeerAddress(), synAckPkt.getPeerPort());
                        }
                        System.out.printf("---------------------------------------------------\n");

                        // We received SYN_ACK from Server, send ACK and payload to server
                        System.out.printf("---------------------------------------------------\n");
                        System.out.printf("SEND: ACK Packet: %s\n", payloadP);
                        System.out.printf("Type: %s (%s)\n", PacketType.valueOf(payloadP.getType()), payloadP.getType());
                        if (verbose) {
                            System.out.printf("Sequence number: %s\n", sequenceNumber);
                            System.out.printf("Router: %s\n", routerAddress);
                            System.out.printf("Server: %s:%s\n", serverAddress.getAddress(), serverAddress.getPort());
                            String payload = new String(payloadP.getPayload(), StandardCharsets.UTF_8);
                            System.out.printf("%s\n", payload);
                        }
                        System.out.printf("---------------------------------------------------\n");
                        System.out.printf("---------------------------------------------------\n");
                        System.out.printf("INFO: Waiting for the response\n");
                        System.out.printf("---------------------------------------------------\n");
                        socket.send(payloadPacket);
                        break;
                    } else {
                        // Send again if no SYN_ACK was received
                        socket.send(synPacket);
                    }
                }
                // We process the response from server
                byte[] ackRespBuf = new byte[Packet.MAX_LEN];
                // Source: https://stackoverflow.com/questions/12363078/adding-timeout-to-datagramsocket-receive
                while(true) {
                    DatagramPacket respPkt = new DatagramPacket(ackRespBuf, ackRespBuf.length);
                    try {
                        socket.receive(respPkt);
                    } catch (SocketTimeoutException e) {
                        // resend payload packet
                        socket.send(payloadPacket);
                        continue;
                    }
                    // check received data...
                    Packet ackRespPkt = Packet.fromBytes(ackRespBuf);
                    System.out.printf("---------------------------------------------------\n");
                    System.out.printf("RCVD: Packet: %s\n", ackRespPkt);
                    System.out.printf("Type: %s (%s)\n", PacketType.valueOf(ackRespPkt.getType()), ackRespPkt.getType());
                    if (verbose) {
                        System.out.printf("Sequence number: %s\n",  ackRespPkt.getSequenceNumber());
                        System.out.printf("Router: %s:%s\n", routerAddr, routerPort);
                        System.out.printf("Server: %s:%s\n", ackRespPkt.getPeerAddress(), ackRespPkt.getPeerPort());
                    }
                    String respPayload = new String(ackRespPkt.getPayload(), StandardCharsets.UTF_8);
                    System.out.printf("%s\n", respPayload);
                    System.out.printf("---------------------------------------------------\n");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void HELP(String input) {
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
