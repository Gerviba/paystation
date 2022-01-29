package hu.schbme.pay.station.controller;

import hu.schbme.pay.station.config.AppUtil;
import hu.schbme.pay.station.service.LoggingService;
import hu.schbme.pay.station.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;

@Slf4j
@Controller
@RequestMapping("/admin/export")
public class ExportController {

    public static final String MIME_TYPE_TEXT_CSV = "text/csv";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_DISPOSITION_VALUE = "attachment; filename=\"paystsn-{0}-{1}.csv\"";

    private final TransactionService system;
    private final LoggingService logger;

    @Autowired
    public ExportController(TransactionService system, LoggingService logger) {
        this.system = system;
        this.logger = logger;
    }

    @GetMapping("")
    public String export() {
        return "admin/export";
    }

    @GetMapping("/accounts")
    @ResponseBody
    public String exportAccounts(HttpServletResponse response) {
        response.setContentType(MIME_TYPE_TEXT_CSV);
        response.setHeader(CONTENT_DISPOSITION, MessageFormat.format(
                CONTENT_DISPOSITION_VALUE, "accounts",
                AppUtil.DATE_TIME_FILE_FORMATTER.get().format(System.currentTimeMillis())));

        String csvExport = system.exportAccounts();
        logger.action("User list exported");
        return csvExport;
    }

    @GetMapping("/transactions")
    @ResponseBody
    public String exportTransactions(HttpServletResponse response) {
        response.setContentType(MIME_TYPE_TEXT_CSV);
        response.setHeader(CONTENT_DISPOSITION, MessageFormat.format(
                CONTENT_DISPOSITION_VALUE, "transactions",
                AppUtil.DATE_TIME_FILE_FORMATTER.get().format(System.currentTimeMillis())));

        String csvExport = system.exportTransactions();
        logger.action("Transaction list exported");
        return csvExport;
    }

    @GetMapping("/logs")
    @ResponseBody
    public String exportLogs(HttpServletResponse response) {
        response.setContentType(MIME_TYPE_TEXT_CSV);
        response.setHeader(CONTENT_DISPOSITION, MessageFormat.format(
                CONTENT_DISPOSITION_VALUE, "logs",
                AppUtil.DATE_TIME_FILE_FORMATTER.get().format(System.currentTimeMillis())));

        String csvExport = logger.exportLogs();
        logger.action("Audit log exported");
        return csvExport;
    }

    @GetMapping("/items")
    @ResponseBody
    public String exportItems(HttpServletResponse response) {
        response.setContentType(MIME_TYPE_TEXT_CSV);
        response.setHeader(CONTENT_DISPOSITION, MessageFormat.format(
                CONTENT_DISPOSITION_VALUE, "items",
                AppUtil.DATE_TIME_FILE_FORMATTER.get().format(System.currentTimeMillis())));

        String csvExport = system.exportItems();
        logger.action("Named items list exported");
        return csvExport;
    }

}
