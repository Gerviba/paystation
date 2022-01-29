package hu.schbme.pay.station.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

    @Value("${paymentsystem.bcrypt.strength:10}")
    private int bcryptStrength;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

}
