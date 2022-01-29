package hu.schbme.pay.station.controller;

import hu.schbme.pay.station.dto.ItemCreateDto;
import hu.schbme.pay.station.model.ItemEntity;
import hu.schbme.pay.station.service.LoggingService;
import hu.schbme.pay.station.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin")
public class ItemsController {

    private final TransactionService system;
    private final LoggingService logger;

    @Autowired
    public ItemsController(TransactionService system, LoggingService logger) {
        this.system = system;
        this.logger = logger;
    }

    @GetMapping("/items")
    public String items(Model model) {
        final var items = system.getALlItems();
        model.addAttribute("items", items);
        model.addAttribute("invalid", items.stream()
                .filter(ItemEntity::isActive)
                .collect(Collectors.groupingBy(ItemEntity::getCode))
                .entrySet().stream()
                .filter(it -> it.getValue().size() > 1)
                .map(it -> "#" + it.getKey())
                .collect(Collectors.joining(", ")));

        return "admin/items";
    }

    @GetMapping("/create-item")
    public String createItem(Model model) {
        model.addAttribute("item", null);
        model.addAttribute("createMode", true);
        return "admin/item-manipulate";
    }

    @PostMapping("/create-item")
    public String createItem(ItemCreateDto itemDto) {
        itemDto.setAbbreviation(itemDto.getAbbreviation().trim());
        system.createItem(itemDto);
        return "redirect:/admin/items";
    }

    @GetMapping("/modify-item/{itemId}")
    public String modifyItem(@PathVariable Integer itemId, Model model) {
        Optional<ItemEntity> item = system.getItem(itemId);
        model.addAttribute("createMode", false);
        item.ifPresentOrElse(
                acc -> model.addAttribute("item", acc),
                () -> model.addAttribute("item", null));
        return "admin/item-manipulate";
    }

    @PostMapping("/modify-item")
    public String modifyItem(ItemCreateDto itemDto) {
        if (itemDto.getId() == null)
            return "redirect:/admin/items";

        itemDto.setAbbreviation(itemDto.getAbbreviation().trim());
        Optional<ItemEntity> item = system.getItem(itemDto.getId());
        if (item.isPresent()) {
            system.modifyItem(itemDto);
        }
        return "redirect:/admin/items";
    }

    @PostMapping("/items/activate")
    public String activateItem(@RequestParam Integer id) {
        Optional<ItemEntity> item = system.getItem(id);
        item.ifPresent(it -> {
            system.setItemActive(id, true);
            logger.action("Item purchase activated for <color>" + it.getName() + "</color>");
            log.info(MessageFormat.format("Item purchase activated for {0} ({1})", it.getName(), it.getQuantity()));
        });
        return "redirect:/admin/items";
    }

    @PostMapping("/items/deactivate")
    public String deactivateItem(@RequestParam Integer id) {
        Optional<ItemEntity> item = system.getItem(id);
        item.ifPresent(it -> {
            system.setItemActive(id, false);
            logger.failure("Item purchase deactivated for <color>" + it.getName() + "</color>");
            log.info(MessageFormat.format("Item purchase deactivated for {0} ({1})", it.getName(), it.getQuantity()));
        });
        return "redirect:/admin/items";
    }

}
