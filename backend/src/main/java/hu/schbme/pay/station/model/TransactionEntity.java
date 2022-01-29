package hu.schbme.pay.station.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.io.Serializable;

import static hu.schbme.pay.station.config.AppUtil.DATE_TIME_FORMATTER;

@Data
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity implements Serializable {

    @Id
    @Column
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private long time;

    @Column(nullable = false)
    private String cardId;

    @Column(nullable = false)
    private Integer account;

    @Column(nullable = false)
    private String cardHolder;

    @Column(nullable = false)
    private String paymentDescription;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String gateway;

    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private boolean regular;

    @Transient
    public String formattedTime() {
        return DATE_TIME_FORMATTER.get().format(getTime());
    }

}
