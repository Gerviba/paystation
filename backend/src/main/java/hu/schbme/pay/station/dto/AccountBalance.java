package hu.schbme.pay.station.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import hu.schbme.pay.station.serialize.CsvSerializable;
import hu.schbme.pay.station.serialize.CsvSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize(using = CsvSerializer.class)
public class AccountBalance implements CsvSerializable {

    private int balance;
    private boolean loadAllowed;
    private boolean allowed;

    @Override
    public String csvSerialize() {
        return balance + ";" + (loadAllowed ? "1" : "0") + ";" + (allowed ? "1" : "0");
    }

}
