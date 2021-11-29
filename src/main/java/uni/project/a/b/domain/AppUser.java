package uni.project.a.b.domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;



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

    /**
     * Post-Protocol
     *
     */

    @OneToOne(cascade = CascadeType.ALL)
    private AppKeys keys;




    public AppUser(String username, String password, AppKeys keys) {
        this.username = username;
        this.password = password;
        this.keys = keys;
    }

    public Long getId() {
        return id;
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

    public AppKeys getKeys() {
        return keys;
    }

}
