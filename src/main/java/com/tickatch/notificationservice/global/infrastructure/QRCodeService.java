package com.tickatch.notificationservice.global.infrastructure;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QRCodeService {

  private static final int QR_CODE_SIZE_EMAIL = 300;
  private static final int QR_CODE_SIZE_MMS = 350;
  private static final int MAX_MMS_SIZE_BYTES = 200 * 1024; // 200KB
  private static final float JPG_QUALITY = 0.9f; // JPG 압축 품질 (90%)

  /** 티켓 ID로 QR 코드 생성 (Base64 인코딩) */
  public String generateQRCode(String target) {
    try {
      log.debug("QR 코드 생성 시작: target={}", target);

      // QR 코드 생성
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix bitMatrix =
          qrCodeWriter.encode(
              target, BarcodeFormat.QR_CODE, QR_CODE_SIZE_EMAIL, QR_CODE_SIZE_EMAIL);

      // BufferedImage로 변환
      BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

      // Base64로 인코딩
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "PNG", outputStream);
      byte[] imageBytes = outputStream.toByteArray();
      String base64Image = Base64.getEncoder().encodeToString(imageBytes);

      log.debug("QR 코드 생성 완료: target={}, size={}", target, base64Image.length());

      return "data:image/png;base64," + base64Image;

    } catch (Exception e) {
      log.error("QR 코드 생성 실패: target={}", target, e);
      throw new RuntimeException("QR 코드 생성 중 오류가 발생했습니다.", e);
    }
  }

  /** MMS용 QR 코드 생성 (JPG, Base64) */
  public String generateQRCodeForMms(String target) {
    try {
      log.debug("MMS용 QR 코드 생성: target={}", target);

      byte[] jpgBytes = generateQRCodeJpgBytes(target);
      String base64 = Base64.getEncoder().encodeToString(jpgBytes);

      log.debug("MMS용 QR 코드 생성 완료: size={}bytes", jpgBytes.length);

      return "data:image/jpeg;base64," + base64;

    } catch (Exception e) {
      log.error("MMS용 QR 코드 생성 실패: target={}", target, e);
      throw new RuntimeException("QR 코드 생성 중 오류가 발생했습니다.", e);
    }
  }

  /** MMS용 QR 코드 바이트 배열 생성 (JPG) */
  public byte[] generateQRCodeBytesForMms(String target) {
    try {
      log.debug("MMS용 QR 코드 바이트 생성: target={}", target);

      byte[] jpgBytes = generateQRCodeJpgBytes(target);

      log.debug("MMS용 QR 코드 바이트 생성 완료: size={}bytes", jpgBytes.length);

      return jpgBytes;

    } catch (Exception e) {
      log.error("QR 코드 바이트 생성 실패: target={}", target, e);
      throw new RuntimeException("QR 코드 바이트 생성 중 오류가 발생했습니다.", e);
    }
  }

  /** JPG 형식의 QR 코드 바이트 배열 생성 */
  private byte[] generateQRCodeJpgBytes(String target) throws WriterException, IOException {
    // 1. QR 코드 BitMatrix 생성 (최적화 옵션 사용)
    BitMatrix bitMatrix = createBitMatrix(target, QR_CODE_SIZE_MMS, true);

    // 2. BufferedImage로 변환 (흑백 설정)
    MatrixToImageConfig config =
        new MatrixToImageConfig(
            0xFF000000, // 검정 (불투명)
            0xFFFFFFFF // 흰색 (불투명)
            );
    BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix, config);

    // 3. PNG를 JPG로 변환 (투명도 제거)
    BufferedImage jpgImage = convertToJpg(image);

    // 4. JPG로 압축
    byte[] jpgBytes = compressToJpg(jpgImage, JPG_QUALITY);

    // 5. 크기 검증
    if (jpgBytes.length > MAX_MMS_SIZE_BYTES) {
      log.warn("QR 코드 크기 초과, 재압축: {}bytes → 목표 200KB", jpgBytes.length);

      // 품질을 낮춰서 재시도
      jpgBytes = compressToJpg(jpgImage, 0.8f);

      if (jpgBytes.length > MAX_MMS_SIZE_BYTES) {
        jpgBytes = compressToJpg(jpgImage, 0.7f);
      }

      if (jpgBytes.length > MAX_MMS_SIZE_BYTES) {
        throw new RuntimeException(
            String.format("QR 코드 크기가 200KB를 초과했습니다: %dbytes", jpgBytes.length));
      }
    }

    log.debug("JPG 변환 완료: size={}bytes, quality={}", jpgBytes.length, JPG_QUALITY);

    return jpgBytes;
  }

  /** BitMatrix 생성 (에러 정정 레벨 및 여백 설정) */
  private BitMatrix createBitMatrix(String target, int size, boolean optimize)
      throws WriterException {

    Map<EncodeHintType, Object> hints = new HashMap<>();

    // 에러 정정 레벨
    // L: 7% 복원 (파일 크기 최소)
    // M: 15% 복원 (권장)
    // Q: 25% 복원
    // H: 30% 복원 (파일 크기 최대)
    hints.put(
        EncodeHintType.ERROR_CORRECTION,
        optimize
            ? com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L
            : com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);

    // 문자 인코딩
    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

    // 여백 크기 (0~4, 기본값 4)
    hints.put(EncodeHintType.MARGIN, optimize ? 1 : 2);

    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    return qrCodeWriter.encode(target, BarcodeFormat.QR_CODE, size, size, hints);
  }

  /**
   * BufferedImage를 JPG용으로 변환 (투명도 제거)
   *
   * <p>JPG는 투명도를 지원하지 않으므로 TYPE_INT_ARGB → TYPE_INT_RGB로 변환하고 흰색 배경 추가
   */
  private BufferedImage convertToJpg(BufferedImage image) {
    // RGB 모드의 새 이미지 생성
    BufferedImage jpgImage =
        new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB // ARGB → RGB
            );

    // 흰색 배경 채우기
    Graphics2D g = jpgImage.createGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, image.getWidth(), image.getHeight());

    // 원본 이미지 그리기
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return jpgImage;
  }

  /**
   * BufferedImage를 JPG로 압축
   *
   * @param image 원본 이미지
   * @param quality 압축 품질 (0.0 ~ 1.0, 1.0이 최고 품질)
   * @return JPG 바이트 배열
   */
  private byte[] compressToJpg(BufferedImage image, float quality) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    // ImageWriter 가져오기
    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

    // 압축 파라미터 설정
    ImageWriteParam param = writer.getDefaultWriteParam();
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(quality); // 압축 품질 설정

    // JPG로 쓰기
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
      writer.setOutput(ios);
      writer.write(null, new IIOImage(image, null, null), param);
    } finally {
      writer.dispose();
    }

    return outputStream.toByteArray();
  }
}
