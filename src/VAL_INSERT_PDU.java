import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class VAL_INSERT_PDU extends PDU {

    private String ssn;
    private String name;
    private String email;

    public VAL_INSERT_PDU(String ssn, String name, String email) {
        super(PDU.VAL_INSERT);
        this.ssn = ssn;
        this.name = name;
        this.email = email;
    }


    public VAL_INSERT_PDU(ByteBuffer buffer, ByteChannel src) throws IOException {
        super(PDU.VAL_INSERT);

        int bytesRead;
        var buffer1 = ByteBuffer.allocate(15);
        bytesRead = src.read(buffer1);
        if(bytesRead != 15){
            src.read(buffer1);
        }
        buffer1.flip();

        byte[] a = new byte[13];
        buffer1.get(a);
        ssn = new String(a);
        var nameLength = buffer1.get();
        buffer1.get();

        var buffer2 = ByteBuffer.allocate(nameLength + 8);
        bytesRead = src.read(buffer2);
        if(bytesRead != (nameLength + 8)){
            src.read(buffer2);
        }
        buffer2.flip();
        byte[] b = new byte[nameLength];
        buffer2.get(b);
        name = new String(b);
        if (name.contains("\0")) {
            name = name.substring(0, name.indexOf("\0"));
        }
        var emailLength = buffer2.get();
        byte[] c = new byte[7];
        buffer2.get(c);

        var buffer3 = ByteBuffer.allocate(emailLength);
        bytesRead = src.read(buffer3);
        if(bytesRead != emailLength){
            src.read(buffer3);
        }
        buffer3.flip();
        byte[] d = new byte[emailLength];
        buffer3.get(d);
        email = new String(d);
        if (email.contains("\0")) {
            email = email.substring(0, email.indexOf("\0"));
        }
    }


    public VAL_INSERT_PDU(ByteBuffer buf) {

        super(PDU.VAL_INSERT);
        byte[] a = new byte[13];
        buf.get(a);
        ssn = new String(a);

        var nameLength = buf.get();
        buf.get();
        byte[] b = new byte[nameLength];
        buf.get(b);
        name = new String(b);

        var emailLength = buf.get();
        byte[] c = new byte[7];
        buf.get(c);
        byte[] d = new byte[emailLength];
        buf.get(d);
        email = new String(d);

    }

    @Override
    public void send(ByteChannel channel) throws IOException {
        var buffer = ByteBuffer.allocate(600);

        buffer.put((byte) type);
        buffer.put(ssn.getBytes());
        buffer.put(((byte) name.getBytes().length));
        buffer.put((byte) 0);
        buffer.put(name.getBytes());
        buffer.put((byte) email.getBytes().length);

        for (int i = 0; i < 7; i++) {

            buffer.put((byte) 0);
        }

        buffer.put(email.getBytes());
        buffer.flip();

        channel.write(buffer);
    }

    public String getSsn() {
        return ssn;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
