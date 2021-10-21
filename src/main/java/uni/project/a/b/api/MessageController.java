package uni.project.a.b.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uni.project.a.b.domain.AppSession;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.service.SessionService;
import uni.project.a.b.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/conversation/message")
@RequiredArgsConstructor
@Slf4j
public class MessageController {


    private final UserService userService;

    private final SessionService sessionService;


    @PostMapping("/send")
    public void sendMessage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username2 = request.getParameter("username");
        String username1 = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

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
           } else {
               // TODO: not true! Implement WebSocket before!
               log.info("Message sended");
           }

        }

    }



}


