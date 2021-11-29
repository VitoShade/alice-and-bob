package uni.project.a.b.api;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uni.project.a.b.domain.AppKeys;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.security.SecurityConfig;
import uni.project.a.b.service.UserService;
import uni.project.a.b.utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.ObjectUtils.isEmpty;


/**
 * Main controller of the application.
 * The registration and the token refreshing are managed here.
 * Login is done within the configuration of Spring Security
 */

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class MainController {

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

        AppKeys keys = new AppKeys();
        AppUser user = new AppUser(username, password, keys);
        userService.saveUser(user);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            try {

                DecodedJWT decodedJWT = JwtUtils.decodeToken(authorizationHeader);
                String username = decodedJWT.getSubject();
                AppUser user = userService.getUser(username);

                String[] token = JwtUtils.encodeToken(user, request, authorizationHeader);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", token[0]);
                tokens.put("refresh_token", token[1]);
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);

            }catch (Exception exception){
                log.error("Logging error: {}", exception.getMessage());
                response.sendError(403, exception.getMessage());
            }

        }else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

}
