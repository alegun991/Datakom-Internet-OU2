import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;

public class NET_JOIN_RESPONSE_PDU extends PDU {

    private String nextAddress;
    private int nextPort;
    private int rangeStart;
    private int rangeEnd;

    public NET_JOIN_RESPONSE_PDU(String nextAddress, int nextPort, int rangeStart, int rangeEnd){
        super(PDU.NET_JOIN_RESPONSE);
        this.nextAddress = nextAddress;
        this.nextPort = nextPort;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public NET_JOIN_RESPONSE_PDU(ByteBuffer buf, ByteChannel src) throws IOException {
        super(PDU.NET_JOIN_RESPONSE);
        var buffer = ByteBuffer.allocate(21);
        readAllBytes(src, 21, buffer);
        buffer.flip();
        byte[] a = new byte[16];
        buffer.get(a);
        nextAddress = new String(a);
        buffer.get();
        nextPort = Short.toUnsignedInt(buffer.getShort());
        rangeStart = Byte.toUnsignedInt(buffer.get());
        rangeEnd = Byte.toUnsignedInt(buffer.get());
    }

    @Override
    public void send(ByteChannel channel) throws IOException {

        var buffer = ByteBuffer.allocate(50);

        buffer.put((byte) type);
        buffer.put(nextAddress.getBytes());
        buffer.put((byte) 0);
        buffer.putShort((short) nextPort).order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte) rangeStart);
        buffer.put((byte) rangeEnd);

        buffer.flip();
        channel.write(buffer);

    }

    public String getAddress() {
        return nextAddress;
    }
    public int getPort() {
        return nextPort;
    }
    public int getRangeStart(){
        return  rangeStart;
    }
    public int getRangeEnd(){
        return  rangeEnd;
    }

}
