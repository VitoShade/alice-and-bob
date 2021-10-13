package uni.project.a.b.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uni.project.a.b.domain.AppUser;
import uni.project.a.b.service.UserService;
import uni.project.a.b.validation.UserVal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MainController {

    private UserService userService;

    /*
    @PostMapping("/register")
    public ResponseEntity<AppUser> register(final Model model) {

        model.addAttribute("userVal", new UserVal());
        return "account/register";


    }

     */
}
