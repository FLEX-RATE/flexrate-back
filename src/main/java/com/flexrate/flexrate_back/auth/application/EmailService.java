package com.flexrate.flexrate_back.auth.application;

import com.flexrate.flexrate_back.common.dto.EmailVerificationRequest;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.common.util.EmailMessage;
import com.flexrate.flexrate_back.common.util.EmailSender;
import com.flexrate.flexrate_back.common.util.RandomStringUtil;
import com.flexrate.flexrate_back.common.util.StringRedisUtil;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailService {

    private final StringRedisUtil redisUtil;
    private final EmailSender emailSender;
    private final MemberRepository memberRepository;

    // 인증 유효 시간
    private static final Duration AUTH_CODE_TTL = Duration.ofMinutes(5);

    public void sendAuthEmail(String email) {
        log.info("이메일 인증 요청 진입:\nemail={}", email);

        // 1. 이미 가입된 이메일인지 확인한다.
        checkDuplicatedEmail(email);

        // 2. 인증번호를 발급한다.
        String code = RandomStringUtil.numeric(6);
        log.info("인증번호 생성 완료:\nemail={}, code={}", email, code);

        // 3. redis에 이메일을 키로 하여 인증번호를 저장한다.
        if (redisUtil.exists(email)) {
            redisUtil.delete(email);
            log.info("기존 인증번호 삭제:\nemail={}", email);
        }
        redisUtil.set(email, code, AUTH_CODE_TTL);
        log.info("인증번호 Redis 저장 완료:\nemail={}", email);

        // 5. 이메일을 발송한다.
        String subject = "[FLEXRATE] 인증 메일입니다.";
        String filename = "email-authentication.html";

        emailSender.send(EmailMessage.create(email, subject, filename).addContext("code", code));
        log.info("인증 메일 발송 완료:\nemail={}", email);
    }

    /**
     * 주어진 email을 사용하는 회원이 존재하는지 확인하고, 다른 사람이 이미 사용한다면 예외를 발생시키는 메서드이다.
     * @param email 회원 이메일
     */
    private void checkDuplicatedEmail(String email) {
        Optional<Member> optional = memberRepository.findByEmail(email);

        // 중복된 이메일을 사용하는 회원이 존재하지 않으면 성공
        if (optional.isPresent()) {
            log.warn("이미 가입된 이메일로 인증 요청:\nemail={}", email);
            throw new FlexrateException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    /**
     * 인증번호를 검증하고, 성공한 경우 회원 정보의 이메일을 수정하는 메서드이다.
     * @param request 인증번호 검증 요청 DTO
     */
    @Transactional
    public void verifyAuthCode(EmailVerificationRequest request) {
        String email = request.email();
        log.info("이메일 인증번호 검증 요청 진입:\nemail={}", email);

        // 2. 이미 가입된 이메일인지 확인한다.
        checkDuplicatedEmail(email);

        // 3. 인증번호가 올바른지 검증한다.
        checkAuthCode(request.code(), email);
        log.info("이메일 인증번호 검증 성공:\nemail={}", email);
    }

    private void checkAuthCode(String code, String email) throws FlexrateException {
        String foundCode = redisUtil.get(email);

        // 1. 요청된 회사 이메일을 키로 갖는 인증번호가 없거나 만료된 경우
        if (foundCode == null) {
            log.warn("Redis에 인증번호 없음 또는 만료:\nemail={}", email);
            throw new FlexrateException(ErrorCode.REDIS_NOT_FOUND);
        }

        // 2. 주어진 인증번호가 틀린 경우
        if (!foundCode.equals(code)) {
            log.warn("인증번호 불일치:\nemail={}", email);
            throw new FlexrateException(ErrorCode.WRONG_AUTH_CODE);
        }
    }
}