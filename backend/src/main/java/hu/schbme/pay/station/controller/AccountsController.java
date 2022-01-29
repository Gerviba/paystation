package hu.schbme.pay.station.controller;

import hu.schbme.pay.station.dto.AccountCreateDto;
import hu.schbme.pay.station.dto.PaymentStatus;
import hu.schbme.pay.station.service.LoggingService;
import hu.schbme.pay.station.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("SpellCheckingInspection")
@Slf4j
@Controller
@RequestMapping("/admin")
public class AccountsController {

    public static final String DUPLICATE_CARD_ERROR = "DUPLICATE_CARD";

    private final TransactionService system;
    private final LoggingService logger;

    @Autowired
    public AccountsController(TransactionService system, LoggingService logger) {
        this.system = system;
        this.logger = logger;
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", system.getAllAccounts());
        return "admin/accounts";
    }

    @GetMapping("/manual-transaction/{accountId}")
    public String manualTransaction(@PathVariable Integer accountId, Model model) {
        system.getAccount(accountId)
                .ifPresentOrElse(acc -> model.addAttribute("balance", acc.getBalance())
                        .addAttribute("loan", Math.abs(acc.getMinimumBalance()))
                        .addAttribute("name", acc.getName())
                        .addAttribute("max", acc.getBalance() >= 0 ? (acc.getBalance() + -acc.getMinimumBalance()) : (-acc.getMinimumBalance() + acc.getBalance()))
                        .addAttribute("id", acc.getId()),
                () -> model.addAttribute("balance", 0)
                        .addAttribute("loan", 0)
                        .addAttribute("name", "Nem található")
                        .addAttribute("max", 0)
                        .addAttribute("id", -1));
        return "admin/manual";
    }

    @PostMapping("/manual-transaction")
    public String manualTransaction(@RequestParam Integer id, @RequestParam Integer money) {
        if (money == null || money < 0)
            return "redirect:/admin/manual-transaction/" + id;

        final var result = system.createTransactionToSystem(id, money);
        if (result == PaymentStatus.ACCEPTED)
            return "redirect:/admin/manual-transaction-done?money=" + money;
        return "redirect:/admin/manual-transaction/" + id + "?failed=" + result.name();
    }

    @GetMapping("/manual-transaction-done")
    public String manualTransactionDone(@RequestParam Long money, Model model) {
        model.addAttribute("money", money);
        return "admin/manual-done";
    }

    @GetMapping("/upload-money/{accountId}")
    public String uploadMoney(@PathVariable Integer accountId, Model model) {
        final var account = system.getAccount(accountId);
        account.ifPresentOrElse(
                acc -> model.addAttribute("name", acc.getName()).addAttribute("id", acc.getId()),
                () -> model.addAttribute("name", "Nem található").addAttribute("id", -1));
        return "admin/upload-money";
    }

    @PostMapping("/upload-money")
    public String uploadMoney(@RequestParam Integer id, @RequestParam Integer money) {
        if (money == null || money < 0)
            return "redirect:/admin/upload-money/" + id;

        if (system.addMoneyToAccount(id, money))
            return "redirect:/admin/upload-money-done?money=" + money;
        return "redirect:/admin/upload-money/" + id + "?failed=";
    }

    @GetMapping("/upload-money-done")
    public String uploadMoneyDone(@RequestParam Long money, Model model) {
        model.addAttribute("money", money);
        return "admin/upload-money-done";
    }

    @GetMapping("/create-account")
    public String createUser(Model model, @RequestParam(defaultValue = "") String card) {
        final var account = new AccountCreateDto();
        account.setCard(card);
        model.addAttribute("acc", account);
        model.addAttribute("createMode", true);
        return "admin/account-manipulate";
    }

    @PostMapping("/create-account")
    public String createAccount(Model model, AccountCreateDto acc) {
        acc.setCard(acc.getCard().trim().toUpperCase());
        if (system.createAccount(acc)) {
            return "redirect:/admin/accounts";
        } else {
            model.addAttribute("acc", acc);
            model.addAttribute("error", DUPLICATE_CARD_ERROR);
            model.addAttribute("createMode", true);
            return "admin/account-manipulate";
        }
    }

    @GetMapping("/modify-account/{accountId}")
    public String modifyAccount(@PathVariable Integer accountId, Model model, @RequestParam(defaultValue = "") String card) {
        final var account = system.getAccount(accountId);
        model.addAttribute("createMode", false);
        account.map(it -> {
            if (!card.isBlank())
                it.setCard(card);
            return it;
        }).ifPresentOrElse(
                acc -> model.addAttribute("acc", acc),
                () -> model.addAttribute("acc", null));
        return "admin/account-manipulate";
    }

    @PostMapping("/modify-account")
    public String modifyAccount(Model model, AccountCreateDto acc) {
        if (acc.getId() == null)
            return "redirect:/admin/accounts";

        acc.setCard(acc.getCard().trim().toUpperCase());
        final var account = system.getAccount(acc.getId());
        if (account.isPresent() && !system.modifyAccount(acc)) {
            model.addAttribute("createMode", false);
            model.addAttribute("acc", account.get());
            model.addAttribute("error", DUPLICATE_CARD_ERROR);
            return "account-manipulate";
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/allow")
    public String allowAccount(@RequestParam Integer id) {
        final var account = system.getAccount(id);
        account.ifPresent(acc -> {
            system.setAccountAllowed(id, true);
            logger.action("User purchases allowed for <color>" + acc.getName() + "</color>");
            log.info("User purchases allowed for " + acc.getName());
        });
        return "redirect:/admin/accounts";
    }

    @PostMapping("/disallow")
    public String disallowAccount(@RequestParam Integer id) {
        final var account = system.getAccount(id);
        account.ifPresent(acc -> {
            system.setAccountAllowed(id, false);
            logger.failure("User purchases disallowed for <color>" + acc.getName() + "</color>");
            log.info("User purchases disallowed for " + acc.getName());
        });
        return "redirect:/admin/accounts";
    }

    @PostMapping("/set-processed")
    public String setProcessedAccount(@RequestParam Integer id) {
        final var account = system.getAccount(id);
        account.ifPresent(acc -> {
            system.setAccountProcessed(id, true);
            logger.success("User status set processed for <color>" + acc.getName() + "</color>");
            log.info("User status set processed for " + acc.getName());
        });
        return "redirect:/admin/accounts";
    }

    @PostMapping("/unset-processed")
    public String unsetProcessedAccount(@RequestParam Integer id) {
        final var account = system.getAccount(id);
        account.ifPresent(acc -> {
            system.setAccountProcessed(id, false);
            logger.failure("User processed status unset for <color>" + acc.getName() + "</color>");
            log.info("User processed status unset for " + acc.getName());
        });
        return "redirect:/admin/accounts";
    }

    @GetMapping("/assign-to-account/{card}")
    public String uploadMoneyDone(Model model, @PathVariable String card) {
        model.addAttribute("card", card);
        model.addAttribute("accounts", system.getAllAccounts());
        return "admin/assign-to-account";
    }

}
