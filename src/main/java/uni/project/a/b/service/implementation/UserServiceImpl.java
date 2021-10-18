package uni.project.a.b.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uni.project.a.b.domain.AppRole;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.repo.UserRepo;
import uni.project.a.b.service.UserService;
import uni.project.a.b.validation.UserVal;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;


    private final UserDetailServiceImpl userDetailsService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AppUser createUser(UserVal userVal) {
        log.info("Creating user");
        return new AppUser(userVal.getUsername(), userVal.getPassword());
        //userRepo.save(user)
        //TODO: Token, Key etc...
    }

    @Override
    public AppUser getUser(String username) {
        log.info("Getting user");
        return userRepo.findByUsername(username);
    }

    @Override
    public Optional<AppUser> getUser(Long id) {
        log.info("Getting user");
        return userRepo.findById(id);
    }

    @Override
    public void saveUser(AppUser user) {
        log.info("Saving user");
        user.setRole(AppRole.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);

    }


    @Override
    public boolean isAuth() {
        return userDetailsService.isAuth();
    }


}
