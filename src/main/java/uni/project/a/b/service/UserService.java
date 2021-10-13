package uni.project.a.b.service;

import org.springframework.stereotype.Service;
import uni.project.a.b.domain.AppRole;
import uni.project.a.b.domain.AppUser;

@Service
public interface UserService {

    void saveUser (AppUser user);

    AppRole saveRole(AppRole role);

    AppUser getUser(String username);

    
    void addRoleToUser(String username, String roleName);




}
