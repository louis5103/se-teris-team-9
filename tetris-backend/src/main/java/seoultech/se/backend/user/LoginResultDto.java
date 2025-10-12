package seoultech.se.backend.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResultDto {
    private Long id;

    public static LoginResultDto toDto(UserEntity entity) {
        return new LoginResultDto(entity.getId());
    }
}
