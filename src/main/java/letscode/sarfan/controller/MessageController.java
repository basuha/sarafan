package letscode.sarfan.controller;

import com.fasterxml.jackson.annotation.JsonView;
import letscode.sarfan.domain.Message;
import letscode.sarfan.domain.Views;
import letscode.sarfan.dto.EventType;
import letscode.sarfan.dto.ObjectType;
import letscode.sarfan.repo.MessageRepo;
import letscode.sarfan.util.WsSender;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

@RestController
@RequestMapping("message")
public class MessageController {
    private final MessageRepo messageRepo;
    private final BiConsumer<EventType, Message> wsSender;

    @Autowired
    public MessageController(MessageRepo messageRepo, WsSender wsSender) {
        this.messageRepo = messageRepo;
        this.wsSender = wsSender.getSender(ObjectType.MESSAGE, Views.IdName.class);
    }

    @GetMapping
    @JsonView(Views.IdName.class)
    public List<Message> list() {
        return messageRepo.findAll();
    }

    @GetMapping("{id}")
    @JsonView(Views.FullMessage.class)
    public Message getOne(@PathVariable("id") Long id) {
        return messageRepo.findById(id).get();
    }

    @PostMapping
    public Message create(@RequestBody Message message) {
        message.setCreationDate(LocalDateTime.now());
        Message updatedMessage = messageRepo.save(message);

        wsSender.accept(EventType.CREATE, updatedMessage);

        return updatedMessage;
    }

    @PutMapping("{id}")
    public Message update(
            @PathVariable("id") Long messageFromDbId,
            @RequestBody Message changedMessage
    ) {
        Message messageFromDb = messageRepo.findById(messageFromDbId).get();
        BeanUtils.copyProperties(changedMessage, messageFromDb, "id");

        Message updatedMessage = messageRepo.save(messageFromDb);
        wsSender.accept(EventType.UPDATE, updatedMessage);

        return updatedMessage;
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Long messageid) {
        Message messageForDelete = messageRepo.getOne(messageid);
        messageRepo.delete(messageForDelete);
        wsSender.accept(EventType.REMOVE, messageForDelete);
    }
}
