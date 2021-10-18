package uni.project.a.b.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.project.a.b.domain.AppRole;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.service.UserService;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.util.ObjectUtils.isEmpty;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class MainController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<AppUser> register(HttpServletRequest request) {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        log.info("username = {}", username);

        if (!isEmpty(userService.getUser(username))) {
            log.error("utente trovato");
            return ResponseEntity.status(HttpStatus.FOUND).build();
        }

        log.info("creo utente e salvo nel db");


        AppUser user = new AppUser(username, password);
        userService.saveUser(user);

        return ResponseEntity.ok().build();

    }

    /*
    @PostMapping("/register")
    public ResponseEntity<AppUser> register(@RequestBody UserVal userVal) {

        AppUser user = userService.getUser(userVal.getUsername());
        if (!isEmpty(user)) {
            return ResponseEntity.status(HttpStatus.FOUND).build();
        }

        user = userService.createUser(userVal);
        userService.saveUser(user);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/login")
    public ResponseEntity<AppUser> login(@RequestBody UserVal userVal) {

        AppUser user = userService.getUser(userVal.getUsername());
        if (isEmpty(user)) {
            log.error("user not found");
            return ResponseEntity.notFound().build();
        }

        if (userVal.getPassword() == user.getPassword()){
            return ResponseEntity.ok().body(user);
        } else return ResponseEntity.badRequest().build();
    }

 */

}
