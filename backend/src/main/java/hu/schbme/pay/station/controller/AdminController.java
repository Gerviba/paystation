package hu.schbme.pay.station.controller;

import hu.schbme.pay.station.service.LoggingService;
import hu.schbme.pay.station.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static hu.schbme.pay.station.config.AppUtil.formatNumber;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    public static final String DUPLICATE_CARD_ERROR = "DUPLICATE_CARD";

    private final TransactionService system;
    private final LoggingService logger;

    @Autowired
    public AdminController(TransactionService system, LoggingService logger) {
        this.system = system;
        this.logger = logger;
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("userCount", system.getUserCount());
        model.addAttribute("txCount", system.getTransactionCount());
        model.addAttribute("sumOfIncome", formatNumber(system.getSumOfIncome()));

        model.addAttribute("logs", logger.getEntries());

        model.addAttribute("sumOfLoans", formatNumber(system.getSumOfLoans()));
        model.addAttribute("sumOfBalances", formatNumber(system.getSumOfBalances()));
        model.addAttribute("sumOfPayIns", formatNumber(system.getSumOfPayIns()));
        return "admin/analytics";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("transactions", system.getAllTransactions());
        return "admin/transactions";
    }

}
