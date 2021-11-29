package uni.project.a.b.service.implementation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@AllArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Load user by username ");
        AppUser user = userRepo.findByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException("User not found in the database");
        } else {
            Collection<SimpleGrantedAuthority> authorities = user.getRole().getAuth();
            return new User(user.getUsername(), user.getPassword(), authorities);
        }
    }

    public Authentication getAuth(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserDetails getUserDetails() {
        return (UserDetails) getAuth().getPrincipal();
    }

    public String getUsername(){
        return getUserDetails().getUsername();
    }



}
