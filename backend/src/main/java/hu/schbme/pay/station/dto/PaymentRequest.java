package hu.schbme.pay.station.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private String card;
    private Integer amount;
    private String gatewayCode;
    private String details;

}
