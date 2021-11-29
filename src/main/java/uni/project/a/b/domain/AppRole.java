package uni.project.a.b.domain;



import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.*;
import java.util.stream.Collectors;

public enum AppRole {

    USER(Set.of(AppPermission.GLOBAL));

    private Set<AppPermission> permissions;

    AppRole(Set<AppPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<AppPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<AppPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<SimpleGrantedAuthority> getAuth(){
        return getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
    }


}
