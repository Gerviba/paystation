package hu.schbme.pay.station.service;

import hu.schbme.pay.station.dto.GatewayCreateDto;
import hu.schbme.pay.station.dto.GatewayInfo;
import hu.schbme.pay.station.model.GatewayEntity;
import hu.schbme.pay.station.model.InMemoryGatewayInfo;
import hu.schbme.pay.station.model.TransactionEntity;
import hu.schbme.pay.station.repo.GatewayRepository;
import hu.schbme.pay.station.repo.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static hu.schbme.pay.station.model.GatewayEntity.*;

@SuppressWarnings({"DefaultAnnotationParam", "SpellCheckingInspection"})
@Slf4j
@Service
public class GatewayService {

    public static final String WEB_TERMINAL_NAME = "WebTerminal";

    private final LoggingService logger;
    private final TransactionRepository transactions;
    private final GatewayRepository gateways;
    private final Map<String, InMemoryGatewayInfo> gatewayInfo = new HashMap<>();

    @Autowired
    public GatewayService(LoggingService logger, TransactionRepository transactions, GatewayRepository gateways) {
        this.logger = logger;
        this.transactions = transactions;
        this.gateways = gateways;
    }

    public List<GatewayInfo> getAllGatewayInfo() {
        return gateways.findAll().stream()
                .map(it -> {
                    final var gw = getInfo(it.getName());
                    return new GatewayInfo(it.getId(), it.getName(), gw.getLastPacket(), gw.getLastReadings(),
                            readTxCount(it), readAllTraffic(it), it.getType());
                })
                .collect(Collectors.toUnmodifiableList());
    }

    private long readAllTraffic(GatewayEntity gw) {
        return transactions.findAllByGateway(gw.getName()).stream()
                .mapToInt(TransactionEntity::getAmount)
                .sum();
    }

    private long readTxCount(GatewayEntity gw) {
        return transactions.countAllByGateway(gw.getName());
    }

    @PostConstruct
    public void init() {
        if (gateways.findByName(WEB_TERMINAL_NAME).isEmpty()) {
            gateways.save(new GatewayEntity(WEB_TERMINAL_NAME, "", TYPE_WEB));
        }
        gateways.findAll().forEach(gateway -> log.info("Gateway '{}' of type {} registered with token: '{}'",
                gateway.getName(), gateway.getType(), gateway.getToken()));
    }

    public boolean authorizeGateway(String name, String token) {
        final var gateway = gateways.findByName(name);
        if (gateway.isEmpty() || TYPE_WEB.equals(gateway.get().getType())) {
            log.warn("Unauthorized gateway '{}' with token '{}'", name, token);
            logger.failure("Unauthoritzed terminal: <color>" + name + "</color>");
            return false;
        }
        return gateway.get().getToken().equals(token);
    }

    public void appendReading(String gatewayName, String card) {
        final var cardReadings = getInfo(gatewayName).getLastReadings();
        if (cardReadings.size() >= 5)
            cardReadings.removeLast();
        cardReadings.addFirst(new InMemoryGatewayInfo.CardReading(card, System.currentTimeMillis()));
    }

    public void updateLastUsed(String name) {
        getInfo(name).setLastPacket(System.currentTimeMillis());
    }

    private InMemoryGatewayInfo getInfo(String name) {
        return gatewayInfo.computeIfAbsent(formatGatewayName(name), it -> new InMemoryGatewayInfo());
    }

    @Transactional(readOnly = true)
    public Optional<GatewayEntity> getGateway(int gatewayId) {
        return gateways.findById(gatewayId);
    }

    @Transactional(readOnly = false)
    public boolean createGateway(GatewayCreateDto gatewayDto) {
        if (gateways.findByName(formatGatewayName(gatewayDto.getName())).isPresent())
            return false;

        final var type = List.of(TYPE_PHYSICAL, TYPE_MOBILE, TYPE_WEB)
                .contains(gatewayDto.getType()) ? gatewayDto.getType() : TYPE_WEB;
        gateways.save(new GatewayEntity(formatGatewayName(gatewayDto.getName()), gatewayDto.getToken(), type));
        return true;
    }

    @Transactional(readOnly = false)
    public boolean modifyGateway(GatewayCreateDto gatewayDto) {
        final var gateway = gateways.findById(gatewayDto.getId());
        final var gatewayName = formatGatewayName(gatewayDto.getName());
        final var gatewayByName = gateways.findByName(gatewayName);
        if (gatewayByName.isPresent() && !gatewayDto.getId().equals(gatewayByName.get().getId()))
            return false;

        gateway.ifPresent(gw -> {
            gw.setName(gatewayName);
            gw.setToken(gatewayDto.getToken());

            final var inMemoryGateway = getInfo(gatewayName);
            gatewayInfo.remove(gatewayName);
            gatewayInfo.put(gatewayName, inMemoryGateway);

            final var type = List.of(TYPE_PHYSICAL, TYPE_MOBILE, TYPE_WEB)
                    .contains(gatewayDto.getType()) ? gatewayDto.getType() : TYPE_WEB;
            gw.setType(type);
        });
        return true;
    }

    private String formatGatewayName(String name) {
        return name.replace(' ', '-').replaceAll("[^A-Za-z0-9_\\-.]", "");
    }

}
