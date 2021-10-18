package uni.project.a.b.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.repo.UserRepo;

import javax.transaction.Transactional;
import java.util.Collection;

@Service
@Transactional
@Slf4j
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Load user by username ");
        AppUser user = userRepo.findByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException("User not found in the database");
        } else {
            Collection<SimpleGrantedAuthority> authorities = user.getRole().getAuth();
            // user.getRole().getPermissions().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
            return new User(user.getUsername(), user.getPassword(), authorities);
        }
    }

    public Authentication getAuth(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean hasAuth(String auth){
        return getAuth().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority()
                        .equals(auth));
    }

    public UserDetails getUserDetails() {
        UserDetails user = (UserDetails) getAuth().getPrincipal();
        return user;
    }

    public String getUsername(){
        return getUserDetails().getUsername();
    }

    public boolean isAuth() {
        return getAuth().isAuthenticated();
    }


}
