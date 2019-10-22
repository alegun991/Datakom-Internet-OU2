import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Hasher {

    private SHA1Hasher() {}

    /**
     * Computes a shortened SHA1 digest of a String.
     * The result is bound between 0-255
     */
    public static short computeDigest(String s) {
        try {
            byte[] sha = MessageDigest.getInstance("SHA1").digest(s.getBytes());
            short result = 0;

            for(byte b : sha) {
                result = (short)((result + (((short)b) & 0xff)) % 256);
            }

            return result;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
