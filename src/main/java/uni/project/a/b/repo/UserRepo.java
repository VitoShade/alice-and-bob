package uni.project.a.b.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.project.a.b.domain.AppUser;



@Repository
public interface UserRepo extends JpaRepository<AppUser, Long> {

    AppUser findByUsername(String username);

}
