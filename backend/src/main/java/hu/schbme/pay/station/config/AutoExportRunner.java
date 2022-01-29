package hu.schbme.pay.station.config;

import hu.schbme.pay.station.service.LoggingService;
import hu.schbme.pay.station.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
@EnableScheduling
public class AutoExportRunner implements CommandLineRunner {

    private final TransactionService system;
    private final LoggingService logger;

    @Value("${server.port}")
    private int port;

    @Autowired
    public AutoExportRunner(TransactionService system, LoggingService logger) {
        this.system = system;
        this.logger = logger;
    }

    @PostConstruct
    public void onStarted() {
        logger.serverInfo("Backend started. This is the beginning of the audit log.");
    }

    @Override
    public void run(String... args) {
        System.out.println("|");
        System.out.println("| Admin panel available on: https://127.0.0.1:" + port + "/admin/");
        System.out.println("|");
    }

    @Scheduled(fixedRate = 1000 * 60 * 10)
    public void autoSave10m() {
        save("10m");
    }

    @Scheduled(fixedRate = 1000 * 60 * 60)
    public void autoSave1h() {
        save("1h");
    }

    @Scheduled(fixedRate = 1000 * 60 * 30)
    public void autoSave30mAndPersist() {
        save("30m-" + AppUtil.DATE_TIME_FILE_FORMATTER.get().format(System.currentTimeMillis()));
    }

    @PreDestroy
    public void autoSaveOnStop() {
        save("stop-at-" + AppUtil.DATE_TIME_FILE_FORMATTER.get().format(System.currentTimeMillis()));
    }

    private void save(String tag) {
        var saves = new File("saves");
        saves.mkdir();
        try {
            var filePattern = "saves/autosave-%s-%s.csv";
            Files.writeString(Path.of(String.format(filePattern, tag, "accounts")), system.exportAccounts());
            Files.writeString(Path.of(String.format(filePattern, tag, "transactions")), system.exportTransactions());
            Files.writeString(Path.of(String.format(filePattern, tag, "logs")), logger.exportLogs());
            Files.writeString(Path.of(String.format(filePattern, tag, "items")), system.exportItems());
        } catch (IOException e) {
            log.error("Exception happened during " + tag + " auto-save", e);
        }

        log.info("Auto (" + tag + ") log saved to '" + saves.getAbsolutePath() + "' folder");
    }

}
