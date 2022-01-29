package hu.schbme.pay.station.service;

import hu.schbme.pay.station.dto.AccountCreateDto;
import hu.schbme.pay.station.dto.ItemCreateDto;
import hu.schbme.pay.station.dto.ItemQueryResult;
import hu.schbme.pay.station.dto.PaymentStatus;
import hu.schbme.pay.station.model.AccountEntity;
import hu.schbme.pay.station.model.ItemEntity;
import hu.schbme.pay.station.model.TransactionEntity;
import hu.schbme.pay.station.repo.AccountRepository;
import hu.schbme.pay.station.repo.ItemRepository;
import hu.schbme.pay.station.repo.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.schbme.pay.station.service.GatewayService.WEB_TERMINAL_NAME;

@SuppressWarnings({"DefaultAnnotationParam", "SpellCheckingInspection"})
@Slf4j
@Service
public class TransactionService {

    private final TransactionRepository transactions;
    private final AccountRepository accounts;
    private final ItemRepository items;
    private final LoggingService logger;

    @Autowired
    public TransactionService(TransactionRepository transactions, AccountRepository accounts, ItemRepository items, LoggingService logger) {
        this.transactions = transactions;
        this.accounts = accounts;
        this.items = items;
        this.logger = logger;
    }

    @Transactional(readOnly = true)
    public Optional<AccountEntity> getAccountByCard(String card) {
        return accounts.findByCard(card);
    }

    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public PaymentStatus proceedPayment(String card, int amount, String message, String gateway) {
        Optional<AccountEntity> possibleAccount = this.accounts.findByCard(card);
        if (possibleAccount.isEmpty()) {
            logger.failure("Transaction failed: <color>card not found</color>");
            return PaymentStatus.VALIDATION_ERROR;
        }

        var accountEntity = possibleAccount.get();
        if (!accountEntity.isAllowed()) {
            logger.failure(MessageFormat.format("Transaction failed: <badge>{0}</badge>  <color>is locked</color>", accountEntity.getName()));
            return PaymentStatus.CARD_REJECTED;
        }

        if (accountEntity.getBalance() - amount < accountEntity.getMinimumBalance()) {
            logger.failure(MessageFormat.format("Transaction failed: <color>{0}, not enough money</color>", accountEntity.getName()));
            return PaymentStatus.NOT_ENOUGH_CASH;
        }

        var transaction = new TransactionEntity(null, System.currentTimeMillis(), card, accountEntity.getId(),
                accountEntity.getName(), accountEntity.getName() + " payed " + amount + " with message: " + message,
                amount, message, gateway, "SYSTEM", true);
        accountEntity.setBalance(accountEntity.getBalance() - amount);
        accounts.save(accountEntity);
        transactions.save(transaction);
        log.info("Payment proceed: " + transaction.getId() + " with amount: " + transaction.getAmount() + " at gateway: " + transaction.getGateway());
        logger.success(MessageFormat.format("<badge>{0}</badge> transacion accepted: <color>{1} JMF</color>", accountEntity.getName(), amount));
        return PaymentStatus.ACCEPTED;
    }

    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public boolean addMoneyToAccount(Integer accountId, int amount) {
        Optional<AccountEntity> possibleAccount = this.accounts.findById(accountId);
        if (possibleAccount.isEmpty()) {
            logger.failure("Failed to deposit money: <color>user not found</color>");
            return false;
        }

        var accountEntity = possibleAccount.get();
        var transaction = new TransactionEntity(null, System.currentTimeMillis(), "NO-CARD-USED", -1,
                "SYSTEM", "SYSTEM payed " + amount + " with message: WEBTERM",
                amount, "WEBTERM", WEB_TERMINAL_NAME, accountEntity.getName(), false);

        accountEntity.setBalance(accountEntity.getBalance() + amount);
        accounts.save(accountEntity);
        transactions.save(transaction);
        log.info(transaction.getAmount() + " money added to: " + accountEntity.getName());
        logger.success(MessageFormat.format("<badge>{0}</badge> just deposited: <color>{1} JMF</color>", accountEntity.getName(), amount));
        return true;
    }

    @Transactional(readOnly = false)
    public void createAccount(String name, String email, String phone, String card, int amount, int minAmount, boolean allowed) {
        card = card.toUpperCase();
        log.info("New user was created with card: " + card);
        logger.note(MessageFormat.format("<badge>{0}</badge> registered", name));
        accounts.save(new AccountEntity(null, name, card, phone, email, amount, minAmount, allowed, false, ""));
    }

    @Transactional(readOnly = false)
    public void createItem(String name, String quantity, String code, String abbreviation, int price, boolean active) {
        log.info("New item was created: " + name + " (" + quantity + ") " + price + " JMF");
        logger.note(MessageFormat.format("New named item was created: <badge>{0}</badge>", name));
        items.save(new ItemEntity(null, name, quantity, code, abbreviation, price, active));
    }

    @Transactional(readOnly = true)
    public Iterable<TransactionEntity> getAllTransactions() {
        return transactions.findAll();
    }

    @Transactional(readOnly = true)
    public long getUserCount() {
        return accounts.count();
    }

    @Transactional(readOnly = true)
    public long getTransactionCount() {
        return transactions.count();
    }

    @Transactional(readOnly = true)
    public long getSumOfIncome() {
        return transactions.findAllByRegularIsTrue().stream()
                .mapToInt(TransactionEntity::getAmount)
                .sum();
    }

    @Transactional(readOnly = true)
    public long getSumOfLoans() {
        return Math.abs(accounts.findAllByBalanceLessThan(0).stream()
                .mapToInt(AccountEntity::getBalance)
                .sum());
    }

    @Transactional(readOnly = true)
    public long getSumOfBalances() {
        return accounts.findAllByBalanceGreaterThan(0).stream()
                .mapToInt(AccountEntity::getBalance)
                .sum();
    }

    @Transactional(readOnly = true)
    public long getSumOfPayIns() {
        return transactions.findAllByRegularIsFalse().stream()
                .mapToInt(TransactionEntity::getAmount)
                .sum();
    }

    @Transactional(readOnly = true)
    public Iterable<AccountEntity> getAllAccounts() {
        final var all = accounts.findAll();
        all.sort(Comparator.comparing(AccountEntity::getName));
        return all;
    }

    @Transactional(readOnly = true)
    public Optional<AccountEntity> getAccount(Integer accountId) {
        return accounts.findById(accountId);
    }

    @Transactional(readOnly = false)
    public void setAccountAllowed(Integer accountId, boolean allow) {
        accounts.findById(accountId).ifPresent(accountEntity -> {
            accountEntity.setAllowed(allow);
            accounts.save(accountEntity);
        });
    }

    @Transactional(readOnly = false)
    public void setAccountProcessed(Integer accountId, boolean processed) {
        accounts.findById(accountId).ifPresent(accountEntity -> {
            accountEntity.setProcessed(processed);
            accounts.save(accountEntity);
        });
    }

    @Transactional(readOnly = false)
    public void setItemActive(Integer itemId, boolean activate) {
        items.findById(itemId).ifPresent(itemEntity -> {
            itemEntity.setActive(activate);
            items.save(itemEntity);
        });
    }

    @Transactional(readOnly = false)
    public boolean modifyAccount(AccountCreateDto acc) {
        Optional<AccountEntity> cardCheck = acc.getCard().length() > 24 ? accounts.findByCard(acc.getCard()) : Optional.empty();
        Optional<AccountEntity> user = accounts.findById(acc.getId());
        if (user.isPresent()) {
            final var account = user.get();
            if (acc.getCard().length() > 24 && cardCheck.isPresent() && !cardCheck.get().getId().equals(account.getId()))
                return false;

            account.setName(acc.getName());
            account.setEmail(acc.getEmail());
            account.setPhone(acc.getPhone());
            account.setCard(acc.getCard());
            account.setComment(acc.getComment());
            account.setMinimumBalance((acc.getLoan() == null || acc.getLoan() < 0) ? 0 : -acc.getLoan());
            logger.action(MessageFormat.format("<color>{0}</color> user profile updated", account.getName()));
            accounts.save(account);
        }
        return true;
    }

    @Transactional(readOnly = false)
    public boolean createAccount(AccountCreateDto acc) {
        if (acc.getCard().length() > 24 && accounts.findByCard(acc.getCard()).isPresent())
            return false;

        var account = new AccountEntity();
        account.setName(acc.getName());
        account.setEmail(acc.getEmail());
        account.setPhone(acc.getPhone());
        account.setCard(acc.getCard());
        account.setComment(acc.getComment());
        account.setMinimumBalance((acc.getLoan() == null || acc.getLoan() < 0) ? 0 : -acc.getLoan());
        account.setAllowed(true);
        logger.note(MessageFormat.format("<badge>{0}</badge> registered", account.getName()));
        accounts.save(account);
        return true;
    }

    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public PaymentStatus createTransactionToSystem(Integer accountId, Integer amount) {
        Optional<AccountEntity> possibleAccount = this.accounts.findById(accountId);
        if (possibleAccount.isEmpty()) {
            logger.failure("Transaction failed: <color>user not found</color>");
            return PaymentStatus.VALIDATION_ERROR;
        }

        var accountEntity = possibleAccount.get();
        if (accountEntity.getBalance() - amount < accountEntity.getMinimumBalance()) {
            logger.failure(MessageFormat.format("Transaction failed: <color>{0}, not enough money</color>", accountEntity.getName()));
            return PaymentStatus.NOT_ENOUGH_CASH;
        }

        var transaction = new TransactionEntity(null, System.currentTimeMillis(), "NO-CARD-USED", accountEntity.getId(),
                accountEntity.getName(), accountEntity.getName() + " payed " + amount + " with message: WEBTERM",
                amount, "WEBTERM", WEB_TERMINAL_NAME, "SYSTEM", true);

        accountEntity.setBalance(accountEntity.getBalance() - amount);
        accounts.save(accountEntity);
        transactions.save(transaction);
        log.info(MessageFormat.format("Payment proceed: {0} with amount: {1} at gateway: {2}", transaction.getId(), transaction.getAmount(), transaction.getGateway()));
        logger.success(MessageFormat.format("<badge>{0}</badge> transaction accepted: <color>{1} JMF</color>", accountEntity.getName(), amount));
        return PaymentStatus.ACCEPTED;
    }

    @Transactional(readOnly = true)
    public String exportAccounts() {
        return "id;name;email;phone;card;balance;minimumBalance;allowedToPay;processed;comment"
                + System.lineSeparator()
                + accounts.findAllByOrderById().stream()
                .map(it -> Stream.of("" + it.getId(), it.getName(), it.getEmail(), it.getPhone(), it.getCard(),
                            "" + it.getBalance(), "" + it.getMinimumBalance(), "" + it.isAllowed(), "" + it.isProcessed(),
                            it.getComment())
                        .map(attr -> attr.replace(";", "\\;"))
                        .collect(Collectors.joining(";")))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Transactional(readOnly = true)
    public String exportTransactions() {
        return "id;timestamp;time;sender;receiver;amount;card;description;message;senderId;paymentOrUpload"
                + System.lineSeparator()
                + transactions.findAllByOrderById().stream()
                .map(it -> Stream.of("" + it.getId(), "" + it.getTime(), it.formattedTime(), it.getCardHolder(),
                            it.getReceiver(), "" + it.getAmount(), it.getCardId(), it.getPaymentDescription(),
                            it.getMessage(), "" + it.getAmount(), "" + it.isRegular())
                        .map(attr -> attr.replace(";", "\\;"))
                        .collect(Collectors.joining(";")))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Transactional(readOnly = true)
    public String exportItems() {
        return "id;name;quantity;code;abbreviation;price;active"
                + System.lineSeparator()
                + items.findAllByOrderById().stream()
                .map(it -> Stream.of("" + it.getId(), "" + it.getName(), it.getQuantity(), it.getCode(), it.getAbbreviation(),
                            "" + it.getPrice(), "" + it.isActive())
                        .map(attr -> attr.replace(";", "\\;"))
                        .collect(Collectors.joining(";")))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Transactional(readOnly = true)
    public List<ItemEntity> getALlItems() {
        return items.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ItemEntity> getItem(Integer id) {
        return items.findById(id);
    }

    @Transactional(readOnly = false)
    public void modifyItem(ItemCreateDto itemDto) {
        Optional<ItemEntity> itemEntity = items.findById(itemDto.getId());
        if (itemEntity.isPresent()) {
            final var item = itemEntity.get();

            item.setCode(itemDto.getCode());
            item.setName(itemDto.getName());
            item.setQuantity(itemDto.getQuantity());
            item.setAbbreviation(itemDto.getAbbreviation());
            item.setPrice(itemDto.getPrice());
            logger.action(MessageFormat.format("<color>{0}</color> named item details updated", item.getName()));
            items.save(item);
        }
    }

    @Transactional(readOnly = false)
    public void createItem(ItemCreateDto itemDto) {
        var item = new ItemEntity();
        item.setCode(itemDto.getCode());
        item.setName(itemDto.getName());
        item.setQuantity(itemDto.getQuantity());
        item.setAbbreviation(itemDto.getAbbreviation());
        item.setPrice(itemDto.getPrice());
        item.setActive(false);
        logger.note(MessageFormat.format("<badge>{0}</badge> named item registered", item.getName()));
        items.save(item);
    }

    @Transactional(readOnly = true)
    public ItemQueryResult resolveItemQuery(String query) {
        if (query.startsWith("#"))
            query = query.substring(1);

        final String[] parts = query.split("\\*", 2);
        String code = parts[0];
        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

        return items.findAllByCodeAndActiveTrueOrderByPriceDesc(code)
                .stream().findFirst()
                .map(it -> new ItemQueryResult(true,
                it.getAbbreviation() + (amount > 1 ? ("x" + amount) : ""),
                it.getPrice() * amount))
                .orElseGet(() -> new ItemQueryResult(false, "not found", 0));
    }

}
