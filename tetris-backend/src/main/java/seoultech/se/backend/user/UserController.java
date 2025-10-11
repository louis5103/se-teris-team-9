package seoultech.se.backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;


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
    @GetMapping("/login")
    public String login(@RequestBody LoginRequestDto user) {
        return new String();
    }

    /**
     * 로그아웃
     */
    @PostMapping("logout")
    public String postMethodName(Long id) {
        //TODO: process POST request
        
        return new String();
    }
    


    /**
     * 회원 삭제
     */
    @DeleteMapping("/delete")
    public String deleteAccount(Long id) {
        
        return new String();
    }
    
    
    
}