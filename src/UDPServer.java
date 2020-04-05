import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UDPServer {

    private void listenAndServe(int port) throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
            System.out.printf("INFO: EchoServer is listening at %s\n", channel.getLocalAddress());
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            for (; ; ) {
                buf.clear();
                SocketAddress router = channel.receive(buf);

                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                buf.flip();

                String payload = new String(packet.getPayload(), UTF_8);
                System.out.printf("INFO: Packet: %s\n", packet);
                System.out.printf("INFO: Payload: %s\n", payload);
                System.out.printf("INFO: Router: %s\n", router);

                // Send the response to the router not the client.
                // The peer address of the packet is the address of the client already.
                // We can use toBuilder to copy properties of the current packet.
                // This demonstrate how to create a new packet from an existing packet.
                Packet resp = packet.toBuilder()
                        .setPayload(payload.getBytes())
                        .create();
                channel.send(resp.toBuffer(), router);

            }
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8007;
        UDPServer server = new UDPServer();
        server.listenAndServe(port);
    }
}