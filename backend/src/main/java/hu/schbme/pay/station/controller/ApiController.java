package hu.schbme.pay.station.controller;

import hu.schbme.pay.station.config.AppUtil;
import hu.schbme.pay.station.dto.*;
import hu.schbme.pay.station.error.UnauthorizedGateway;
import hu.schbme.pay.station.model.AccountEntity;
import hu.schbme.pay.station.service.GatewayService;
import hu.schbme.pay.station.service.LoggingService;
import hu.schbme.pay.station.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Optional;

import static hu.schbme.pay.station.PayStationApplication.VERSION;

@SuppressWarnings("SpellCheckingInspection")
@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {

    private final TransactionService system;
    private final GatewayService gateways;
    private final LoggingService logger;

    @Autowired
    public ApiController(TransactionService system, GatewayService gateways, LoggingService logger) {
        this.system = system;
        this.gateways = gateways;
        this.logger = logger;
    }

    @PostMapping("/pay/{gatewayName}")
    public PaymentStatus pay(@PathVariable String gatewayName, @RequestBody PaymentRequest request) {
        if (!gateways.authorizeGateway(gatewayName, request.getGatewayCode()))
            return PaymentStatus.UNAUTHORIZED_TERMINAL;
        gateways.updateLastUsed(gatewayName);
        if (request.getAmount() < 0)
            return PaymentStatus.INTERNAL_ERROR;

        try {
            return system.proceedPayment(request.getCard().toUpperCase(), request.getAmount(),
                    request.getDetails() == null ? "" : request.getDetails(),
                    gatewayName);
        } catch (Exception e) {
            log.error("Error during proceeding payment", e);
            logger.failure("Error during proceeding payment: " + e.getMessage());
            return PaymentStatus.INTERNAL_ERROR;
        }
    }

    /**
     * NOTE: Do not use for transaction purposes. Might be effected by dirty read.
     */
    @PutMapping("/balance/{gatewayName}")
    public AccountBalance balance(@PathVariable String gatewayName, @RequestBody BalanceRequest request) {
        if (!gateways.authorizeGateway(gatewayName, request.getGatewayCode()))
            throw new UnauthorizedGateway();

        gateways.updateLastUsed(gatewayName);
        log.info("New balance from gateway '" + gatewayName + "' card hash: '" + request.getCard().toUpperCase() + "'");
        Optional<AccountEntity> account = system.getAccountByCard(request.getCard().toUpperCase());
        var accountBalance = account.map(accountEntity -> new AccountBalance(accountEntity.getBalance(), isLoanAllowed(accountEntity), accountEntity.isAllowed()))
                .orElseGet(() -> new AccountBalance(0, false, false));

        logger.action(MessageFormat.format("<badge>{0}</badge>''s balance checked, it was: <color>{1} JMF</color>",
                account.map(AccountEntity::getName).orElse("n/a"), accountBalance.getBalance()));
        return accountBalance;
    }

    @PutMapping("/validate/{gatewayName}")
    public ValidationStatus validate(@PathVariable String gatewayName, @RequestBody ValidateRequest request) {
        boolean valid = gateways.authorizeGateway(gatewayName, request.getGatewayCode());
        log.info(MessageFormat.format("Gateways auth request: {0} ({1})", gatewayName, valid ? "OK" : "INVALID"));
        if (valid) {
            gateways.updateLastUsed(gatewayName);
            logger.action("Authentication was successful for terminal: <color>" + gatewayName + "</color>");
        } else {
            logger.failure("Authentication failed for terminal <color>" + gatewayName + "</color>");
        }
        return valid ? ValidationStatus.OK : ValidationStatus.INVALID;
    }

    @PutMapping("/reading/{gatewayName}")
    public ValidationStatus reading(@PathVariable String gatewayName, @RequestBody ReadingRequest readingRequest) {
        if (!gateways.authorizeGateway(gatewayName, readingRequest.getGatewayCode()))
            return ValidationStatus.INVALID;

        log.info(MessageFormat.format("New reading from gateway ''{0}'' read card hash: ''{1}''", gatewayName, readingRequest.getCard().toUpperCase()));
        logger.action(MessageFormat.format("New reading from gateway: <badge>{0}</badge> (card hash: {1})", gatewayName, readingRequest.getCard().toUpperCase()));
        gateways.appendReading(gatewayName, readingRequest.getCard().toUpperCase());
        gateways.updateLastUsed(gatewayName);
        return ValidationStatus.OK;
    }

    @PostMapping("/query/{gatewayName}")
    public ItemQueryResult query(@PathVariable String gatewayName, @RequestBody ItemQueryRequest request) {
        if (!gateways.authorizeGateway(gatewayName, request.getGatewayCode()))
            return new ItemQueryResult(false, "unauthorized", 0);
        gateways.updateLastUsed(gatewayName);

        try {
            return system.resolveItemQuery(request.getQuery());
        } catch (Exception e) {
            logger.failure("Invalid named item query: " + request.getQuery());
            return new ItemQueryResult(false, "invalid query", 0);
        }
    }

    @GetMapping("/status")
    public String test(HttpServletRequest request) {
        log.info("Status endpoint triggered from IP: " + request.getRemoteAddr());
        logger.serverInfo(MessageFormat.format("Status check from <color>{0}</color>", request.getRemoteAddr()));
        return "Server: " + VERSION + ";"
                + "by Schami;"
                + "Time:;"
                + AppUtil.DATE_ONLY_FORMATTER.get().format(System.currentTimeMillis()) + ";"
                + AppUtil.TIME_ONLY_FORMATTER.get().format(System.currentTimeMillis());
    }

    private boolean isLoanAllowed(AccountEntity accountEntity) {
        return accountEntity.getMinimumBalance() < 0;
    }

}
