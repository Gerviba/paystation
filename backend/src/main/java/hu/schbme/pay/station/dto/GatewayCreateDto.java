package hu.schbme.pay.station.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayCreateDto {

    private Integer id = null;
    private String name;
    private String token;
    private String type;

}
