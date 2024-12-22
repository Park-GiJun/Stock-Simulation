package com.gijun.backend.controller;

import com.gijun.backend.domain.dto.account.AccountResponseDTO;
import com.gijun.backend.domain.dto.common.commonResponse;
import com.gijun.backend.domain.entity.Account;
import com.gijun.backend.domain.entity.User;
import com.gijun.backend.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Account", description = "계좌 관련 API")
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;


    @Operation(summary = "계좌 생성", description = "계좌 미존재 시 새로운 계좌를 생성합니다.")
    @PostMapping("/new")
    public commonResponse<AccountResponseDTO> createAccount(
            @AuthenticationPrincipal(expression = "user") User user
    ) {
        if (user == null) {
            return commonResponse.error("User is not authenticated");
        }

        Account account = accountService.createAccount(user);

        // Account 엔티티를 DTO로 변환
        AccountResponseDTO responseDTO = AccountResponseDTO.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .build();

        return commonResponse.success(responseDTO);
    }

}
