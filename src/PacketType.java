//public enum PacketType {
//    SYN(0), SYN_ACK(1), ACK(2);
//    private int value;
//
//    private PacketType(int value) {
//        this.value = value;
//    }
//    public int value() {
//        return this.value;
//    }
//
//}

import java.util.HashMap;
import java.util.Map;

public enum PacketType {
    SYN(0),
    SYN_ACK(1),
    ACK(2);

    private int value;
    private static Map map = new HashMap<>();

    private PacketType(int value) {
        this.value = value;
    }

    static {
        for (PacketType pageType : PacketType.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static PacketType valueOf(int pageType) {
        return (PacketType) map.get(pageType);
    }

    public int getValue() {
        return value;
    }
}