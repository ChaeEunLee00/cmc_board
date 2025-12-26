package com.cmc.board.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String nickname;
}
