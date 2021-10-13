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

}
