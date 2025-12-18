package com.tickatch.notificationservice.template.application;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TemplateRenderer {

  private final MustacheFactory mustacheFactory;

  public TemplateRenderer() {
    this.mustacheFactory = new DefaultMustacheFactory();
  }

  /**
   * Mustache 템플릿을 렌더링합니다.
   *
   * @param template 템플릿 문자열
   * @param variables 템플릿 변수
   * @return 렌더링된 결과
   */
  public String render(String template, Map<String, Object> variables) {
    try {
      log.debug("템플릿 렌더링 시작: variables={}", variables.keySet());

      Mustache mustache = mustacheFactory.compile(new StringReader(template), "template");

      StringWriter writer = new StringWriter();
      mustache.execute(writer, variables);
      writer.flush();

      String result = writer.toString();
      log.debug("템플릿 렌더링 완료: length={}", result.length());

      return result;

    } catch (Exception e) {
      log.error("템플릿 렌더링 실패: template={}, variables={}", template, variables, e);
      throw new RuntimeException("템플릿 렌더링 중 오류가 발생했습니다.", e);
    }
  }
}
