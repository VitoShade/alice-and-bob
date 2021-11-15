package uni.project.a.b.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import uni.project.a.b.domain.AppUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.stream.Collectors;

public class JwtUtils {

    // TODO: Dove la salviamo? , IMPLEMENTARE KEYSTORE
        /*SecureRandom random = new SecureRandom();
        byte[] secret = new byte[512];
        random.nextBytes(secret);
         */
    private static final String key = "secret";

    private static final Algorithm algorithm = Algorithm.HMAC512(key);

    public static DecodedJWT decodeToken(String authorizationHeader){

        String token = authorizationHeader.substring("Bearer ". length());

        Algorithm algorithm = Algorithm.HMAC512(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return(verifier.verify(token));
    }

    public static Algorithm getAlgorithm(){
        return Algorithm.HMAC512(key);
    }

   // TODO: Modificata la durata del token per debugging, metterla a posto
    public static String[] encodeToken(User user, HttpServletRequest request){

        String access_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining()))
                .sign(algorithm);
        String refresh_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);

        return new String[]{access_token, refresh_token};


    }

    public static String[] encodeToken(AppUser user, HttpServletRequest request, String refresh_token){
        Algorithm algorithm = Algorithm.HMAC512(key);

        String access_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .withIssuer(request.getRequestURL().toString())
                .sign(algorithm);

        return new String[]{access_token, refresh_token};


    }

}
