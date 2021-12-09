package com.docotel.ki.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ZxingUtil {
	protected static final Logger logger = LogManager.getLogger(ZxingUtil.class);

	public static byte[] textToQrCode(String qrText, int width, int height) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, width, height);
			MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
			return outputStream.toByteArray();
		} catch (WriterException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public static byte[] textToQrCode(String qrText, File logo, int width, int height, int logoWidth, int logoHeight) {
		return textToQrCode(qrText, logo, width, height, logoWidth, logoHeight, false);
	}

	public static byte[] textToQrCode(String qrText, File logo, int width, int height, int logoWidth, int logoHeight, boolean transparency) {
		try {
			Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, width, height, hints);
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
			
			if (transparency) {
				qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig(Color.BLACK.getRGB(), Color.WHITE.getTransparency()));
			}

			BufferedImage overlay = ImageIO.read(logo);
			BufferedImage resizedOverlay = new BufferedImage(logoWidth, logoHeight, overlay.getType());

			Graphics2D g2d = resizedOverlay.createGraphics();
			g2d.drawImage(overlay, 0, 0, logoWidth, logoHeight, null);
			g2d.dispose();

			int deltaWidth = width - logoWidth;
			int deltaHeight = height - logoHeight;

			BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) combined.getGraphics();

			g.drawImage(qrImage, 0, 0, null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			g.drawImage(resizedOverlay, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);

			ImageIO.write(combined, "PNG", outputStream);

			return outputStream.toByteArray();
		} catch (WriterException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
