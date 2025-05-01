package com.flexrate.flexrate_back;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexrate.flexrate_back.member.api.SignUpController;
import com.flexrate.flexrate_back.member.application.SignupMemberService;
import com.flexrate.flexrate_back.member.dto.SignupRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SignUpController.class)
class SignUpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private SignupMemberService signupMemberService;  // @MockBean -> @Mock

    @InjectMocks
    private SignUpController signUpController;  // 실제 컨트롤러 주입

    @Test
    @DisplayName("POST /api/auth/signup 성공 테스트")
    void signup_success() throws Exception {
        // given
        SignupRequestDTO request = SignupRequestDTO.builder()
                .email("user123@naver.com")
                .password("yourPassword123!")
                .sex("MALE")
                .name("yoon")
                .birthDate(LocalDate.of(1990, 1, 1))
                .consumptionType("절약형")           // String 필드
                .consumptionGoal("일주일에 한 번 '무지출 데이' 실천하기")
                .build();

        // 서비스가 반환할 모의 응답
        SignupResponseDTO responseDto = SignupResponseDTO.builder()
                .userId(42L)
                .email("user123@naver.com")
                .build();
        when(signupMemberService.registerMember(any())).thenReturn(responseDto);

        // when/then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.email").value("user123@naver.com"));

        verify(signupMemberService).registerMember(any());
    }

    @Test
    @DisplayName("POST /api/auth/signup - 필수값 누락 시 400")
    void signup_validationError() throws Exception {
        // 이메일 누락
        String badJson = """
            {
              "password":"abc123!",
              "sex":"MALE",
              "name":"yoon",
              "birthDate":"1990-01-01",
              "consumptionType":"절약형",
              "consumptionGoal":"목표"
            }
            """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }
}
