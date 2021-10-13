package uni.project.a.b.domain;


import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public class AppRole {

    private Set<String> permissions;

    AppRole(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<SimpleGrantedAuthority> getAuth(){
        return getPermissions().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }
}
