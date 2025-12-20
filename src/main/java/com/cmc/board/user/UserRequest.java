package com.cmc.board.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UserRequest {
    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String nickname;
}
