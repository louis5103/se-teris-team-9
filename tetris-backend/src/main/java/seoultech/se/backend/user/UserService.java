package seoultech.se.backend.user;

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
    
}
