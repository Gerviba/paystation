package hu.schbme.pay.station.dto;

import hu.schbme.pay.station.config.AppUtil;
import hu.schbme.pay.station.model.InMemoryGatewayInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Transient;
import java.util.Deque;

@Data
@AllArgsConstructor
public class GatewayInfo {

    private static final String JUST_NOW = "just now";
    private static final String NO_DATA = "no data";
    private static final String MINUTE = "m";
    private static final String SECOND = "s";
    private static final String HOURS = "h";

    private int id;
    private String name;
    private long lastPacket;
    private Deque<InMemoryGatewayInfo.CardReading> lastReadings;
    private long txCount;
    private long allTraffic;
    private String type;

    @Transient
    public String getLastPacketFormatted() {
        if (lastPacket < 0)
            return NO_DATA;
        long formatted = System.currentTimeMillis() - lastPacket;
        formatted /= 1000;
        if (formatted == 0)
            return JUST_NOW;
        if (formatted < 60)
            return formatted + SECOND;
        formatted /= 60;
        if (formatted < 60)
            return formatted + MINUTE;
        formatted /= 60;
        return formatted + HOURS;
    }

    @Transient
    public String getAllTrafficFormatted() {
        return AppUtil.formatNumber(allTraffic);
    }


}
