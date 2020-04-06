public enum PacketType {
    SYN(0), SYN_ACK(1), ACK(2);
    private int value;

    private PacketType(int value) {
        this.value = value;
    }
    public int value() {
        return this.value;
    }
};