package kurzznovu;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class Main {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        System.out.println(encoder.encode("admin123"));
        System.out.println(encoder.encode("mod123"));
        System.out.println(encoder.encode("user123"));

    }
}