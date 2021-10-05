package uni.project.a.b.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uni.project.a.b.domain.AppRole;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.repo.RoleRepo;
import uni.project.a.b.repo.UserRepo;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Override
    public AppUser findByUsername(String username) {
        AppUser user = userRepo.findByUsername(username);
        return user;
    }

    @Override
    public AppUser createUser(AppUser user) {
        return null;
    }

    @Override
    public void saveUser(AppUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
    }


    @Override
    public AppRole saveRole(AppRole role) {
        return roleRepo.save(role);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepo.findByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException("User not found in the database");
        } else {
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            user.getRole().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
            return new User(user.getUsername(), user.getPassword(), authorities);
        }

    }

    @Override
    public void addRoleToUser(String username, String roleName) {
      AppUser user = userRepo.findByUsername(username);
      AppRole role = roleRepo.findByName(roleName);

      user.getRole().add(role);

    }
}
