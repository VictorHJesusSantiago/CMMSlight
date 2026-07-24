package com.cmmslight.cmmsapi.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/** Gera QR Codes localmente (sem chamar nenhum servico externo) para etiquetas de ativos. */
@Service
public class QrCodeService {

    private static final int DEFAULT_SIZE = 300;

    public byte[] generatePng(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new UncheckedIOException("Falha ao gerar QR Code", new IOException(e));
        }
    }

    public byte[] generatePng(String content) {
        return generatePng(content, DEFAULT_SIZE);
    }

    /** Conteudo padrao codificado no QR: identificador estavel do ativo pelo codigo interno. */
    public String buildAssetQrContent(String assetCode) {
        return "CMMSLIGHT-ASSET:" + assetCode;
    }
}
