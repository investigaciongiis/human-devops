package com.suken27.humanfactorsjava.rest.api;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;

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

import com.suken27.humanfactorsjava.model.dto.ActionDto;
import com.suken27.humanfactorsjava.model.controller.ModelController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class ActionsExportController {

  private final ModelController modelController;

  @GetMapping("/actions/export")
  public ResponseEntity<?> exportActions(
          @RequestParam(name = "format", defaultValue = "csv") String format) {

      String teamManagerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
      List<ActionDto> list = modelController.getRecommendedActions(teamManagerEmail)
        .stream()
        .map(a -> {
            if (a.getScore() != null) {
                a.setScore(a.getScore() * 100);
            }
            return a;
        })
        .collect(Collectors.toList());


      List<ActionDto> sorted = new ArrayList<>(list);
      sorted.sort(
          Comparator
              .comparing(ActionDto::getScore, Comparator.nullsLast(Comparator.reverseOrder()))
              .thenComparing(ActionDto::getId)
      );

      String f = format.trim().toLowerCase();
      switch (f) {
          case "csv": {
              String csv = toCsvActions(sorted);
              HttpHeaders csvHeaders = new HttpHeaders();
              csvHeaders.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8"));
              csvHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recommendations.csv\"");
              return new ResponseEntity<>(csv.getBytes(StandardCharsets.UTF_8), csvHeaders, HttpStatus.OK);
          }
          case "json": {
              HttpHeaders jsonHeaders = new HttpHeaders();
              jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
              jsonHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recommendations.json\"");
              return new ResponseEntity<>(sorted, jsonHeaders, HttpStatus.OK);
          }
          case "xlsx": {
              try {
                  byte[] bytes = toXlsxActions(sorted);
                  return ResponseEntity.ok()
                      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"recommendations.xlsx\"")
                      .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                      .contentLength(bytes.length)
                      .body(bytes);
              } catch (Exception e) {
                  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                      .body("Export error: " + e.getMessage());
              }
          }
          default:
              return ResponseEntity.badRequest().body("Unsupported format. Use 'csv', 'json' or 'xlsx'.");
      }
    }

    private String toCsvActions(List<ActionDto> list) {
        String header = String.join(",",
            "id",
            "title",
            "description",
            "score"
        );

        return header + "\n" + list.stream()
            .map(a -> String.join(",",
                String.valueOf(a.getId()),
                csvEscape(a.getTitle()),
                csvEscape(a.getDescription()),
                a.getScore() == null ? "" : a.getScore().toString()
            ))
            .collect(Collectors.joining("\n"));
      }

    private String csvEscape(String s) {
        if (s == null) return "";
        boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        return mustQuote ? "\"" + v + "\"" : v;
    }

    private byte[] toXlsxActions(List<ActionDto> list) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Actions");

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle wrapStyle = wb.createCellStyle();
            wrapStyle.setWrapText(true);

            String[] headers = new String[] { "id", "title", "description", "score" };
            Row h = sh.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            int r = 1;
            for (ActionDto a : list) {
                Row row = sh.createRow(r++);
                int col = 0;
                row.createCell(col++).setCellValue(a.getId());
                row.createCell(col++).setCellValue(safe(a.getTitle()));
                Cell cDesc = row.createCell(col++); cDesc.setCellValue(safe(a.getDescription())); cDesc.setCellStyle(wrapStyle);
                if (a.getScore() != null) row.createCell(col++).setCellValue(a.getScore()); else row.createCell(col++).setBlank();
            }

            for (int i = 0; i < headers.length; i++) sh.autoSizeColumn(i);

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }


}

