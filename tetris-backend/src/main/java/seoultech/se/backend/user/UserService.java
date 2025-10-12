package seoultech.se.backend.user;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;


/** TO STUDY
 * 1. PasswordEncoder
 * 2. throw & catch
 * 3. IllegalArgumentExcption
 * 4. Builder
 */

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository; 
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResultDto signUp(SignUpRequestDto dto) {

        // Validate email
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        // Validate Password
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // Dto to Entity
        UserEntity newUser = UserEntity.builder().name(dto.getName()).email(dto.getEmail()).password(encodedPassword).build();

        // save
        UserEntity savedUser = userRepository.save(newUser);
        
        // Entitiy to ResultDto
        SignUpResultDto result = SignUpResultDto.toDto(savedUser);

        return result;
    }

    @Transactional
    public LoginResultDto login(LoginRequestDto requestDto) {

        // 요청된 email이 있는지 확인
        UserEntity user = userRepository.findByEmail(
            requestDto.getEmail()).orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));

        // 해당 email과 비밀번호 일치하는지 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) { // 일치한다면, Id를 담은 ResultDto 생성 + status == LOGIN
            throw new IllegalArgumentException("비밀번호가 틀립니다.");
        } 

        user.login();

        return LoginResultDto.toDto(user);
    }

    @Transactional
    public String logout(String email) {
        // 요청된 email이 있는지 확인
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));

        user.logout();

        return new String("로그아웃 성공");
    }
    
}
