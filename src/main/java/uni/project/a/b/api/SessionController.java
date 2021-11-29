package uni.project.a.b.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uni.project.a.b.domain.AppSession;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.service.SessionService;
import uni.project.a.b.service.UserService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The session controller is used for the init of the session and for getting the session opened
 */

@RestController
@RequestMapping("/api/conversation")
@AllArgsConstructor
@Slf4j
public class SessionController {


    private SessionService sessionService;

    private UserService userService;





    @PostMapping("/init")
    public void initializeSession(HttpServletRequest request, HttpServletResponse response) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

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
            try{
                sessionService.establishSession(username1, username2);
                response.setStatus(200);
            } catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e){
                e.printStackTrace();
                response.sendError(400, "The initialization of the session has not been completed due to a protocol error ");
            }

        }
    }


    @GetMapping("/get")
    public ResponseEntity<Map<String,String>> getSessions(HttpServletRequest request){

        String username2 = request.getParameter("username");
        String username1 = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Map<String, String> out = new HashMap<>();

        if (username2 != null){
            Optional<AppSession> sess = sessionService.getByUsers(username1,username2);
            sess.ifPresent(appSession -> out.put(username2, appSession.getId().toString()));

        } else {
            Stream<AppSession> sessions = sessionService.getByUsers(username1);


            sessions.forEach(sess -> {
                if (!Objects.equals(sess.getAliceUser(), username1)) {
                    out.put(sess.getAliceUser(), sess.getId().toString());
                } else {
                    out.put(sess.getBobUser(), sess.getId().toString());
                }
            });
        }
        if (out.isEmpty()){
            out.put("none", "none");
        }
        return ResponseEntity.ok(out);


    }







}
