package uni.project.a.b.service;

import org.springframework.stereotype.Service;
import uni.project.a.b.domain.AppUser;

import java.util.Optional;

@Service
public interface UserService {

    AppUser getUser(String username);

    Optional<AppUser> getUser(Long id);

    void saveUser(AppUser user);



}
