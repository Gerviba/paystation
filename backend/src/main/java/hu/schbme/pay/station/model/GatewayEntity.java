package hu.schbme.pay.station.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "gateways")
public class GatewayEntity implements Serializable {

    public static final String TYPE_PHYSICAL = "physical";
    public static final String TYPE_WEB = "web";
    public static final String TYPE_MOBILE = "mobile";

    @Id
    @Column
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String type;

    public GatewayEntity(String name, String token, String type) {
        this.name = name;
        this.token = token;
        this.type = type;
    }
}
