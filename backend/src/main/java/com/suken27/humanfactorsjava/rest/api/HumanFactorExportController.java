package com.suken27.humanfactorsjava.rest.api;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.LinkedHashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.suken27.humanfactorsjava.model.dto.HumanFactorDto;
import com.suken27.humanfactorsjava.model.controller.ModelController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class HumanFactorExportController {

    private final ModelController modelController;

    @GetMapping("/humanfactor/export")
    public ResponseEntity<?> exportHumanFactors(
            @RequestParam(name = "format", defaultValue = "csv") String format) {

        String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<HumanFactorDto> list = modelController.getAllHumanFactors(teamManagerEmail);

        List<HumanFactorDto> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingLong(HumanFactorDto::getId));

        String f = format.trim().toLowerCase();
        switch (f) {
            case "csv":
                String csv = toCsv(sorted);
                HttpHeaders csvHeaders = new HttpHeaders();
                csvHeaders.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8"));
                csvHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"human-factors.csv\"");
                return new ResponseEntity<>(csv.getBytes(StandardCharsets.UTF_8), csvHeaders, HttpStatus.OK);

            case "json":
                List<Map<String, Object>> jsonList = toJSON(sorted);
                HttpHeaders jsonHeaders = new HttpHeaders();
                jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
                jsonHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"human-factors.json\"");
                return new ResponseEntity<>(jsonList, jsonHeaders, HttpStatus.OK);
            case "xlsx": {
                try{
                byte[] bytes = toXlsx(sorted);
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"human-factors.xlsx\"")
                    .contentLength(bytes.length)
                    .body(bytes);
                } catch(Exception e){
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Export error: " + e.getMessage()));
                }
            }
            default:
              return ResponseEntity.badRequest().body("Unsupported format. Use 'csv', 'json' or 'xlsx'.");
        }
    }

    private List<Map<String, Object>> toJSON(List<HumanFactorDto> list){
         return list.stream()
            .map(h -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", h.getId());
                map.put("title", h.getTitle());
                map.put("description", h.getDescription());
                map.put("cluster", h.getCluster());
                map.put("score", h.getScore());
                map.put("affectedBy", h.getAffectedBy());
                map.put("affectsTo", h.getAffectsTo());
                int affectedByCount = (h.getAffectedBy() == null) ? 0 : h.getAffectedBy().size();
                map.put("affectedByCount", affectedByCount);
                map.put("origin", affectedByCount > 0 ? "Inferred" : "Measured");
                map.put("fullyMeasured", h.isFullyMeasured());

                return map;
            })
            .collect(Collectors.toList());
    }

    private String toCsv(List<HumanFactorDto> list) {
        String header = String.join(",",
                "id","title","description", "cluster","score",
                "affectedBy","affectsTo","affectedByCount","origin","fullyMeasured");

        String body = list.stream().map(h -> {
            String title   = csvEscape(h.getTitle());
            String description   = csvEscape(h.getDescription());
            String cluster = csvEscape(h.getCluster());
            String score   = h.getScore() == null ? "" : h.getScore().toString();

            String affectedBy = (h.getAffectedBy() == null || h.getAffectedBy().isEmpty())
                    ? ""
                    : csvEscape(h.getAffectedBy().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("-")));

            String affectsTo = (h.getAffectsTo() == null || h.getAffectsTo().isEmpty())
                    ? ""
                    : csvEscape(h.getAffectsTo().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("-")));

            int affectedByCount = (h.getAffectedBy() == null) ? 0 : h.getAffectedBy().size();
            String origin = affectedByCount > 0 ? "Inferred" : "Measured";

            String fullyMeasured = csvEscape(h.isFullyMeasured() ? "true" : "false");

            return String.join(",",
                    String.valueOf(h.getId()),
                    title,
                    description,
                    cluster,
                    score,
                    affectedBy,
                    affectsTo,
                    String.valueOf(affectedByCount),
                    origin,
                    fullyMeasured
            );
        }).collect(Collectors.joining("\r\n"));

        return header + "\r\n" + body;
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String out = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + out + "\"" : out;
    }

    private byte[] toXlsx(List<HumanFactorDto> list) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Human Factors");

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle wrapStyle = wb.createCellStyle();
            wrapStyle.setWrapText(true);

            String[] headers = new String[]{
                "id","title","Description","cluster","score",
                "affectedBy","affectsTo","affectedByCount","origin","fullyMeasured"
            };
            Row h = sh.createRow(0);
            for (int i=0; i<headers.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int r = 1;
            for (HumanFactorDto hf : list) {
                Row row = sh.createRow(r++);

                String affectedByStr = (hf.getAffectedBy()==null || hf.getAffectedBy().isEmpty())
                        ? ""
                        : String.join(";", hf.getAffectedBy().stream().map(String::valueOf).toList());

                String affectsToStr = (hf.getAffectsTo()==null || hf.getAffectsTo().isEmpty())
                        ? ""
                        : String.join(";", hf.getAffectsTo().stream().map(String::valueOf).toList());

                boolean measured = (hf.getAffectedBy()==null || hf.getAffectedBy().isEmpty());
                String origin = measured ? "Measured" : "Inferred";

                int col = 0;
                row.createCell(col++).setCellValue(hf.getId());
                row.createCell(col++).setCellValue(safe(hf.getTitle()));
                row.createCell(col++).setCellValue(safe(hf.getDescription()));
                row.createCell(col++).setCellValue(safe(hf.getCluster()));
                if (hf.getScore() != null) row.createCell(col++).setCellValue(hf.getScore()); else row.createCell(col++).setBlank();
                Cell c6 = row.createCell(col++); c6.setCellValue(affectedByStr); c6.setCellStyle(wrapStyle);
                Cell c7 = row.createCell(col++); c7.setCellValue(affectsToStr); c7.setCellStyle(wrapStyle);
                row.createCell(col++).setCellValue(hf.getAffectedBy()==null ? 0 : hf.getAffectedBy().size());
                row.createCell(col++).setCellValue(origin);
                row.createCell(col++).setCellValue(Boolean.TRUE.equals(hf.isFullyMeasured()));
            }

            for (int i=0; i<headers.length; i++) {
                sh.autoSizeColumn(i);
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}