package uni.project.a.b.validation;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

public class UserVal implements Serializable {

    @NotEmpty(message = "username cannot be empty")
    private String username;

    @NotEmpty(message = "password cannot be empty")
    private String password;

    /*
    private String token;

    private String PubIdKeyA;

    private String PubIdKeyB;
     */

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
}
