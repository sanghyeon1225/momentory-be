package com.momentory.auth.kakao.application;

import com.momentory.auth.kakao.infrastructure.KakaoApiClient;
import org.springframework.stereotype.Service;

@Service
public class KakaoLoginService {

    private final KakaoApiClient kakaoApiClient;
    private final KakaoLoginTransactionService transactionService;

    public KakaoLoginService(
            KakaoApiClient kakaoApiClient,
            KakaoLoginTransactionService transactionService
    ) {
        this.kakaoApiClient = kakaoApiClient;
        this.transactionService = transactionService;
    }

    public KakaoLoginResult login(String kakaoAccessToken) {
        KakaoUserInfo kakaoUserInfo = kakaoApiClient.getUserInfo(kakaoAccessToken);
        return transactionService.login(kakaoUserInfo);
    }
}
