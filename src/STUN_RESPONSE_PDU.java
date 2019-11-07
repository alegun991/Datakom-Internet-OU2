import java.nio.ByteBuffer;
import java.nio.channels.*;

class STUN_RESPONSE_PDU extends PDU {
    private String address;

    public STUN_RESPONSE_PDU(ByteBuffer buf) {
        super(PDU.STUN_RESPONSE);
        byte[] b = new byte[16];
        buf.get(b);
        address = new String(b);
    }

    @Override
    public void send(ByteChannel channel) {

    }

    public String getAddress() {
        return address;
    }
}
