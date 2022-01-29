package hu.schbme.pay.station.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import hu.schbme.pay.station.serialize.CsvSerializable;
import hu.schbme.pay.station.serialize.CsvSerializer;

@JsonSerialize(using = CsvSerializer.class)
public enum ValidationStatus implements CsvSerializable {
    OK,
    INVALID;

    @Override
    public String csvSerialize() {
        return name();
    }

}
