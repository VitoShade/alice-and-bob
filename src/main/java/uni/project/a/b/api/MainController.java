package uni.project.a.b.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uni.project.a.b.domain.AppRole;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.service.UserService;
import uni.project.a.b.utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.ObjectUtils.isEmpty;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class MainController {

    private UserService userService;

    // Login is done within the security configuration of Spring

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
