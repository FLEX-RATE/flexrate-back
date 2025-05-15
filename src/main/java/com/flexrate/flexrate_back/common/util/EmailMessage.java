package com.flexrate.flexrate_back.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.thymeleaf.context.Context;

@Getter
@AllArgsConstructor
public class EmailMessage {

    private String to;

    private String subject;

    private String template;

    private final Context context = new Context();

    /**
     * 새로운 EmailMessage 인스턴스를 생성하는 정적 팩토리 메서드이다.
     * @param to 수신자 email 주소
     * @param subject 메일 제목
     * @param template Thymeleaf로 작성된 메일 HTML 이름
     * @return 생성된 EmailMessage 인스턴스
     */
    public static EmailMessage create(String to, String subject, String template) {
        return new EmailMessage(to, subject, template);
    }

    /**
     * Thymeleaf HTML에 포함된 변수를 추가하는 메서드이다.
     * @param name 변수명
     * @param value 변수값
     * @return 수정된 EmailMessage 인스턴스
     */
    public EmailMessage addContext(String name, Object value) {
        this.context.setVariable(name, value);
        return this;
    }
}
