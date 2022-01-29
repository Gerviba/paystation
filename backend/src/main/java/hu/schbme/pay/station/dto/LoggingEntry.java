package hu.schbme.pay.station.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static hu.schbme.pay.station.config.AppUtil.DATE_TIME_FORMATTER;

@Getter
@AllArgsConstructor
public class LoggingEntry {

    private final long timestamp;
    private final LogSeverity severity;
    private final String message;

    public String getFormattedDate() {
        return DATE_TIME_FORMATTER.get().format(timestamp).replace(" ", "&nbsp;");
    }

    public String getFormattedMessage() {
        return message
                .replace("<badge>", "<span class=\"badge badge-" + severity.getColor() + "\">")
                .replace("</badge>", "</span>")
                .replace("<color>", "<span class=\"text-" + severity.getColor() + "\">")
                .replace("</color>", "</span>");

    }

    public String getColor() {
        return severity.getColor();
    }

    public String getMarkdownMessage() {
        return message.replaceAll("</?badge>", "`").replaceAll("</?color>", "*");
    }

}
