package hu.schbme.pay.station.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingRequest {

    private String card;
    private String gatewayCode;

}
