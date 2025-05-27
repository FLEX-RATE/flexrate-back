package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.application.SignupService;
import com.flexrate.flexrate_back.member.dto.AnalyzeConsumptionTypeResponse;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupPasswordRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

/*
 * 회원가입 로그인 API 컨트롤러
 * @since 2025.04.28
 * @author 윤영찬
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SignUpController {

    private final SignupService signupService;

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
    public ResponseEntity<String> getFido2RegistrationChallenge(@RequestParam Long memberId) {
        String challenge = signupService.generateFidoChallenge(memberId);
        return ResponseEntity.ok(Base64.getEncoder().encodeToString(challenge.getBytes()));
    }


    @PostMapping("/fido2/register/verify")
    public ResponseEntity<?> verifyAndRegisterFidoCredential(
            @RequestBody PasskeyRequestDTO credentialDTO,
            @RequestParam Long memberId) {
        signupService.addFidoCredential(memberId, credentialDTO);
        return ResponseEntity.ok().build();
    }

}
