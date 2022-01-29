package hu.schbme.pay.station;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PayStationApplication {

    public static final String VERSION = "1.3.0";

    public static void main(String[] args) {
        SpringApplication.run(PayStationApplication.class, args);
    }

}
