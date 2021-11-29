package uni.project.a.b.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Triplet;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uni.project.a.b.domain.AppMessage;
import uni.project.a.b.domain.AppSession;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.service.MessageService;
import uni.project.a.b.service.SessionService;
import uni.project.a.b.service.UserService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


/**
 * The message controller manage the sending and the receiving of messages
 */

@RestController
@RequestMapping("/api/conversation/message")
@AllArgsConstructor
@Slf4j
public class MessageController {


    private final UserService userService;

    private final SessionService sessionService;

    private final MessageService messageService;



    @PostMapping("/send")
    public void sendMessage(HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {


        String username2 = request.getParameter("username");
        String username1 = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String mess = request.getParameter("message");

        //check if the receiver exist
        AppUser receiver = userService.getUser(username2);
        if (receiver == null){
            log.error("User not found");
            response.sendError(404, "User not found");
        } else if (Objects.equals(username1, username2)){
            log.error("Same username");
            response.sendError(400, "You cannot send a message to yourself (if you aren't in a multiverse interpretation of the world)");
        } else {
           Optional<AppSession> sess = sessionService.getByUsers(username1, username2);
           if (sess.isEmpty()){
               log.error("No session opened with {}, establish it before sending a message", username2);
               response.sendError(400, "No session opened with the user, establish it before sending a message");
           } else if (mess == null) {
               log.error("Message empty");
               response.sendError(400, "You cannot send an empty message ");
           } else{
               try {
                   log.info("Trying to sending the message");
                   AppMessage message = new AppMessage(sess.get().getId(), mess.getBytes(StandardCharsets.UTF_8), LocalDateTime.now(), username1);
                   messageService.sendMessage(message, sess.get().getId());
                   log.info("Message sended");
               } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e){
                   e.printStackTrace();
                   response.sendError(400, "The message cannot be sent due to a protocol error");
               }
            }

        }

    }



    @GetMapping("/get")
    public ResponseEntity<List<Triplet<String, String, LocalDateTime>>> getMessages(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username2 = request.getParameter("username");
        String username1 = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        AppUser receiver = userService.getUser(username2);
        if (receiver == null) {
            log.error("User not found");
            response.sendError(404, "User not found");
        } else if (Objects.equals(username1, username2)) {
            log.error("Same username");
            response.sendError(400, "You cannot have a conversation to yourself (if you aren't in a multiverse interpretation of the world)");
        } else {
            Optional<AppSession> sess = sessionService.getByUsers(username1, username2);
            if (sess.isEmpty()) {
                log.error("No session opened with the user");
                response.sendError(400, "No session opened with the user, establish it before sending a message");
            } else {
                log.info("Getting messages");
                List<AppMessage> messages;
                List<Triplet<String, String, LocalDateTime>> out = new ArrayList<>();
                if (Objects.equals(request.getParameter("options"), "all")) {
                    messages = messageService.findBySession(sess.get().getId(), username1);

                }else {
                    LocalTime time = LocalTime.of(Integer.parseInt(request.getParameter("hour")), Integer.parseInt(request.getParameter("minute")));
                    LocalDate date = LocalDate.now();
                    LocalDateTime localDateTime = LocalDateTime.of(date, time);
                    messages = messageService.findBySession(sess.get().getId(), localDateTime, username1);
                }
                messages.forEach(message -> out.add(new Triplet<>(message.getSenderUser(), new String(message.getBody()), message.getTime())));
                return ResponseEntity.ok(out);
            }
        }

        return ResponseEntity.internalServerError().build();
    }

}






