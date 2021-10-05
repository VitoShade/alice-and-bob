package uni.project.a.b.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uni.project.a.b.domain.AppRole;

@Repository
public interface RoleRepo extends JpaRepository<AppRole, Long> {
    AppRole findByName(String name);



}