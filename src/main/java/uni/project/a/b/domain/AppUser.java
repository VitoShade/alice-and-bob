package uni.project.a.b.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;



@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    /**
     * Pre-Protocol
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    private String password;

    private AppRole role;

    private String token;

    private String refreshToken;

    /**
     * Post-Protocol
     *
     */

    @OneToOne
    private AppKeys keys;




    public AppUser(String username, String password) {
        this.username = username;
        this.password = password;
        //TODO: Token and keys???
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AppRole getRole() {
        return role;
    }

    public void setRole(AppRole role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public AppKeys getKeys() {
        return keys;
    }

    public void setKeys(AppKeys keys) {
        this.keys = keys;
    }
}
