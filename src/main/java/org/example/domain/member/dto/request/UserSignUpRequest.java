package org.example.domain.member.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserSignUpRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(regexp = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$")
    @Size(max = 30, message = "이메일 형식이 깁니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp="(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{8,15}",
            message = "비밀번호는 대소문자, 숫자, 특수문자를 포함한 8~15자리로 생성해주세요.")
    private String password;

    @NotEmpty(message = "이름을 입력해주세요")
    @Size(max = 10)
    private String name;

}
