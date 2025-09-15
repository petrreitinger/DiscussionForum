import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // This System.out.println is functional for the hash generator utility
        System.out.println(encoder.encode("admin123"));
    }
}
