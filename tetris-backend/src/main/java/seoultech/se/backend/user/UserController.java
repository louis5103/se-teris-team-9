package seoultech.se.backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/tetris/users")
@RequiredArgsConstructor
public class UserController { 
    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResultDto> signUp(@Valid @RequestBody SignUpRequestDto newUser) {
        SignUpResultDto dto = userService.signUp(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResultDto> login(@RequestBody LoginRequestDto dto) {
        LoginResultDto loginResult = userService.login(dto);
        return ResponseEntity.ok(loginResult);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public String postMethodName(@RequestBody String email) {
        String message = userService.logout(email);
        return message;
    }
}