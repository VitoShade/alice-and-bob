package uni.project.a.b.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;



@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    private String password;

    private AppRole role;

    // Looking for all the signal related stuff
    private String token;

    private String refreshToken;

    private String pubKey;

    private String privKey;





    public AppUser(String username, String password) {
        this.username = username;
        this.password = password;
        //TODO: Token and keys???
    }


}
