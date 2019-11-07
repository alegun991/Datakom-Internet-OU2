import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;

public class VAL_LOOKUP_PDU extends PDU {

    private String ssn;
    private String sendAddress;
    private int sendPort;

    public VAL_LOOKUP_PDU(ByteBuffer buf, ByteChannel src) throws IOException {
        super(PDU.VAL_LOOKUP);

        var buffer = ByteBuffer.allocate(31);
        src.read(buffer);
        buffer.flip();
        byte[] a = new byte[13];
        buffer.get(a);
        ssn = new String(a);
        byte[] b = new byte[16];
        buffer.get(b);
        sendAddress = new String(b);
        sendPort = Short.toUnsignedInt(buffer.getShort());

    }

    public VAL_LOOKUP_PDU(ByteBuffer buf){
        super(PDU.VAL_LOOKUP);
        byte[] a = new byte[13];
        buf.get(a);
        ssn = new String(a);
        byte[] b = new byte[16];
        buf.get(b);
        sendAddress = new String(b);
        sendPort = Short.toUnsignedInt(buf.getShort());

    }

    @Override
    public void send(ByteChannel channel) throws IOException {

        var buffer = ByteBuffer.allocate(40);

        buffer.put((byte) type);
        buffer.put(ssn.getBytes());
        buffer.put(sendAddress.getBytes());
        buffer.putShort((short) sendPort).order(ByteOrder.BIG_ENDIAN);

        buffer.flip();
        channel.write(buffer);

    }


    public String getSsn() {
        return ssn;
    }

    public String getSendAddress() {
        return sendAddress;
    }

    public int getSendPort() {
        return sendPort;
    }
}
