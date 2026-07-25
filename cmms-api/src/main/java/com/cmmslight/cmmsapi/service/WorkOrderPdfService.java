package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.domain.WorkOrderChecklistResult;
import com.cmmslight.cmmsapi.domain.WorkOrderEvent;
import com.cmmslight.cmmsapi.repository.WorkOrderChecklistResultRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderEventRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderRepository;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Gera um PDF de impressao/exportacao da OS, 100% local via Apache PDFBox (sem servico externo). */
@Service
public class WorkOrderPdfService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneOffset.UTC);
    private static final float MARGIN = 50f;
    private static final float LEADING = 16f;

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderEventRepository eventRepository;
    private final WorkOrderChecklistResultRepository checklistResultRepository;

    public WorkOrderPdfService(WorkOrderRepository workOrderRepository,
                                WorkOrderEventRepository eventRepository,
                                WorkOrderChecklistResultRepository checklistResultRepository) {
        this.workOrderRepository = workOrderRepository;
        this.eventRepository = eventRepository;
        this.checklistResultRepository = checklistResultRepository;
    }

    public byte[] generate(Long workOrderId) {
        WorkOrder wo = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new NotFoundException("Ordem de servico nao encontrada: " + workOrderId));
        List<WorkOrderEvent> events = eventRepository.findByWorkOrderIdOrderByCreatedAtAsc(workOrderId);
        List<WorkOrderChecklistResult> checklistResults = checklistResultRepository.findByWorkOrderId(workOrderId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PdfCursor cursor = new PdfCursor(document, page);

            cursor.title("Ordem de Servico - " + wo.getCode());
            cursor.line("Titulo: " + wo.getTitle());
            cursor.line("Ativo: " + wo.getAsset().getName() + " (" + wo.getAsset().getCode() + ")");
            cursor.line("Tipo: " + wo.getType() + "   Prioridade: " + wo.getPriority() + "   Status: " + wo.getStatus());
            cursor.line("Aberta em: " + format(wo.getOpenedAt()));
            if (wo.getScheduledAt() != null) cursor.line("Agendada para: " + format(wo.getScheduledAt()));
            if (wo.getStartedAt() != null) cursor.line("Iniciada em: " + format(wo.getStartedAt()));
            if (wo.getCompletedAt() != null) cursor.line("Concluida em: " + format(wo.getCompletedAt()));
            if (wo.getAssignedTo() != null) cursor.line("Tecnico responsavel: " + wo.getAssignedTo().getName());
            if (wo.getRequestedBy() != null) cursor.line("Solicitado por: " + wo.getRequestedBy().getName());
            cursor.blank();

            if (wo.getDescription() != null && !wo.getDescription().isBlank()) {
                cursor.subtitle("Descricao");
                cursor.paragraph(wo.getDescription());
                cursor.blank();
            }

            if (!checklistResults.isEmpty()) {
                cursor.subtitle("Checklist");
                for (WorkOrderChecklistResult result : checklistResults) {
                    String status = result.isCompleted() ? "[OK]" : "[PENDENTE]";
                    String value = result.getValue() != null ? " -> " + result.getValue() : "";
                    cursor.line(status + " " + result.getChecklistItem().getDescription() + value);
                }
                cursor.blank();
            }

            cursor.subtitle("Historico / Timeline");
            for (WorkOrderEvent event : events) {
                String author = event.getCreatedBy() != null ? " (" + event.getCreatedBy().getName() + ")" : "";
                cursor.line(format(event.getCreatedAt()) + " [" + event.getEventType() + "]" + author + ": " + event.getMessage());
            }
            cursor.blank();

            if (wo.getSignedByName() != null) {
                cursor.subtitle("Assinatura");
                cursor.line("Assinado por: " + wo.getSignedByName() + " em " + format(wo.getSignedAt()));
            }

            cursor.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar PDF da OS", e);
        }
    }

    private String format(java.time.Instant instant) {
        return instant == null ? "-" : FMT.format(instant);
    }

    /** Cursor simples de escrita sequencial de texto no PDF, com quebra automatica de pagina. */
    private static final class PdfCursor {
        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        PdfCursor(PDDocument document, PDPage page) throws IOException {
            this.document = document;
            this.page = page;
            this.stream = new PDPageContentStream(document, page);
            this.y = page.getMediaBox().getHeight() - MARGIN;
        }

        void title(String text) throws IOException {
            ensureSpace();
            write(text, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            y -= 6;
        }

        void subtitle(String text) throws IOException {
            ensureSpace();
            write(text, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        }

        void line(String text) throws IOException {
            ensureSpace();
            write(text, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        }

        void paragraph(String text) throws IOException {
            for (String segment : text.split("\n")) {
                line(segment);
            }
        }

        void blank() {
            y -= LEADING / 2;
        }

        private void write(String text, PDType1Font font, int size) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(sanitize(text));
            stream.endText();
            y -= LEADING;
        }

        private String sanitize(String text) {
            return text.replaceAll("[\\r\\n]", " ");
        }

        private void ensureSpace() throws IOException {
            if (y < MARGIN + LEADING) {
                stream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                stream = new PDPageContentStream(document, page);
                y = page.getMediaBox().getHeight() - MARGIN;
            }
        }

        void close() throws IOException {
            stream.close();
        }
    }
}
