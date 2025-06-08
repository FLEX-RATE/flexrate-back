package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.application.SignupService;
import com.flexrate.flexrate_back.member.application.WebAuthnService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * 회원가입 로그인 API 컨트롤러
 * @since 2025.04.28
 * @author 윤영찬
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignUpController {

    private final SignupService signupService;
    private final WebAuthnService webAuthService;
    private final MemberService memberService;

    /*
     * 회원가입
     * @param signupDTO 데이터
     * @return 생성된 회원 ID, 이메일 (201 Created)
     * @throws FlexrateException 유효성 검사 또는 중복 등 오류 발생 시
     * @since 2025.04.28
     */
    @Operation(
            summary = "회원가입",
            description = "사용자로부터 이메일, 비밀번호, 이름 등의 정보를 입력받아 회원을 등록합니다. " +
                    "이메일 중복 여부와 입력값의 유효성을 검사합니다."
    )
    @PostMapping("/signup/password")
    public SignupResponseDTO signupByPassword(@RequestBody @Valid SignupPasswordRequestDTO dto) {
        return signupService.registerByPassword(dto);
    }

    @Operation(
            summary = "소비타입 특정",
            description = "사용자 정보를 바탕으로 소비타입을 특정합니다. "
    )
    @GetMapping("/consumption-type")
    public AnalyzeConsumptionTypeResponse analyzeConsumptionType() {
        return  signupService.analyzeConsumptionType();
    }

    @Operation(
            summary = "FIDO2 등록용 챌린지 발급",
            description = "패스키(FIDO2)를 등록하기 위해 필요한 Challenge 값을 발급합니다. " +
                    "클라이언트는 이 값을 WebAuthn API의 challenge 파라미터로 사용해야 합니다. " +
                    "사용자는 로그인된 상태여야 하며, 해당 challenge는 서버에 5분간 저장됩니다."
    )
    @GetMapping("/fido2/register/options")
    public ResponseEntity<Fido2RegisterOptionsResponse> getFido2RegistrationChallenge() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long memberId;
        try {
            memberId = Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Member member = memberService.findById(memberId);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // WebAuthService에 있는 메서드 호출
        Fido2RegisterOptionsResponse response = webAuthService.generateRegistrationOptions(member);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "패스키 등록여부 확인",
            description = "이메일을 통해 사용자가 패스키를 등록했는지 확인합니다."
    )
    @GetMapping("/fido2/exists")
    public ResponseEntity<Boolean> isPasskeyRegistered() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.warn("Authentication is null! 인증 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String memberIdStr = auth.getName();
        log.info("Authenticated principal from token (getName): {}", memberIdStr);

        Long memberId;
        try {
            memberId = Long.parseLong(memberIdStr);
            log.info("Parsed memberId: {}", memberId);
        } catch (NumberFormatException e) {
            log.error("Invalid memberId format: '{}'", memberIdStr, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        boolean exists = signupService.isFidoCredentialRegisteredByMemberId(memberId);
        log.info("Fido credential registered for memberId {}: {}", memberId, exists);

        return ResponseEntity.ok(exists);
    }

    /*
     * FIDO2 패스키 등록 검증 및 저장
     * @param credentialDTO 패스키 등록 정보
     * @param memberId 회원 ID
     * @return ResponseEntity
     */
    @Operation(
            summary = "FIDO2 패스키 등록 검증 및 저장",
            description = "클라이언트에서 받은 패스키 등록 정보를 검증하고, 성공 시 해당 정보를 DB에 저장합니다. " +
                    "이 API는 FIDO2 패스키 등록 과정의 마지막 단계로, 클라이언트는 이 API를 호출하여 패스키를 등록해야 합니다."
    )
    @PostMapping("/fido2/register/verify")
    public ResponseEntity<?> verifyAndRegisterFidoCredential(@RequestBody PasskeyRegistrationRequest request, HttpServletRequest httpRequest) throws IOException {
        System.out.println("Request credentialId: " + request.credentialId());
        System.out.println("Request rawId: " + request.rawId());
        System.out.println("Request clientDataJSON: " + request.clientDataJSON());
        System.out.println("Request attestationObject: " + request.attestationObject());
        System.out.println("Request authenticatorData: " + request.authenticatorData());
        System.out.println("Request signature: " + request.signature());
        System.out.println("Request signCount: " + request.signCount());
        System.out.println("Request deviceInfo: " + request.deviceInfo());
        System.out.println("Request publicKey: " + request.publicKey());

        var auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Received credentialId: " + request.credentialId());

        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long memberId;
        try {
            memberId = Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            // attestationObject 파싱 → publicKey, signCount 등 추출
            PasskeyRequestDTO credentialDTO = webAuthService.parseAndBuildDTO(request);
            signupService.addFidoCredential(memberId, credentialDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
