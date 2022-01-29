package hu.schbme.pay.station.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import hu.schbme.pay.station.serialize.CsvSerializable;
import hu.schbme.pay.station.serialize.CsvSerializer;

@JsonSerialize(using = CsvSerializer.class)
public enum PaymentStatus implements CsvSerializable {
    ACCEPTED,
    INTERNAL_ERROR,
    NOT_ENOUGH_CASH,
    VALIDATION_ERROR, // Card or user not found
    CARD_REJECTED,
    UNAUTHORIZED_TERMINAL;

    @Override
    public String csvSerialize() {
        return name();
    }

}
