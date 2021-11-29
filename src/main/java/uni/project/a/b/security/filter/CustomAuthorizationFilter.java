package uni.project.a.b.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import uni.project.a.b.utils.JwtUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Pass through if is the login path
        if (request.getServletPath().equals("/api/login")
                || request.getServletPath().equals("/api/token/refresh")
                || request.getServletPath().equals("/api/register")) {
            log.info("No need for authorization, pass the filter");
            filterChain.doFilter(request, response);
        } else {
            log.info("Need authorization! Check token");
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
                try {

                    DecodedJWT decodedJWT = JwtUtils.decodeToken(authorizationHeader);

                    String username = decodedJWT.getSubject();
                    String role = decodedJWT.getClaim("roles").asString();

                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(role));
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username,null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request, response);


                }catch (Exception exception){
                    log.error("Logging error: {}", exception.getMessage());
                    response.sendError(403);
                }

            }else {
                filterChain.doFilter(request, response);
            }
        }

    }
}
