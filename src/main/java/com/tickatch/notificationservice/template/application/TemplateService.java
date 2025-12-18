package com.tickatch.notificationservice.template.application;

import com.tickatch.notificationservice.template.domain.TemplateType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

  private final TemplateRenderer templateRenderer;

  /** 템플릿 렌더링 */
  public String renderTemplate(
      String templateCode, TemplateType type, Map<String, Object> variables) {

    log.debug("템플릿 렌더링 요청: code={}, type={}, variables={}", templateCode, type, variables.keySet());

    String templateContent = loadTemplateFromFile(templateCode, type);

    return templateRenderer.render(templateContent, variables);
  }

  /** 이메일 제목 렌더링 */
  public String renderEmailSubject(String templateCode, Map<String, Object> variables) {
    String subjectTemplate = loadEmailSubjectTemplate(templateCode);

    return templateRenderer.render(subjectTemplate, variables);
  }

  /** 파일에서 템플릿 로드 */
  private String loadTemplateFromFile(String templateCode, TemplateType type) {
    String fileName = buildFileName(templateCode, type);
    String path = String.format("templates/%s/%s", type.name().toLowerCase(), fileName);

    try {
      log.debug("템플릿 파일 로드: {}", path);

      ClassPathResource resource = new ClassPathResource(path);
      byte[] bytes = resource.getInputStream().readAllBytes();
      String content = new String(bytes, StandardCharsets.UTF_8);

      log.debug("템플릿 파일 로드 완료: size={}", content.length());
      return content;

    } catch (IOException e) {
      log.error("템플릿 파일 로드 실패: path={}", path, e);
      throw new RuntimeException("템플릿을 찾을 수 없습니다: " + path, e);
    }
  }

  /** 이메일 제목 템플릿 로드 */
  private String loadEmailSubjectTemplate(String templateCode) {
    String path =
        String.format(
            "templates/email/%s-subject.txt", templateCode.toLowerCase().replace('_', '-'));

    try {
      ClassPathResource resource = new ClassPathResource(path);
      byte[] bytes = resource.getInputStream().readAllBytes();

      return new String(bytes, StandardCharsets.UTF_8).trim();
    } catch (IOException e) {
      log.warn("제목 템플릿 없음. 기본값 사용: {}", path);
      return "Notification";
    }
  }

  /** 템플릿 파일명 생성 */
  private String buildFileName(String templateCode, TemplateType type) {
    String fileName = templateCode.toLowerCase().replace('_', '-');

    return switch (type) {
      case EMAIL -> fileName + ".html";
      case SMS -> fileName + ".txt";
      case SLACK -> fileName + ".json";
    };
  }
}
