package com.Arthur.cep.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.Arthur.cep.Model.Entities.ViaCepResponse;
import com.opencsv.CSVReader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class CepService {

    public String buscarCepPorEndereco(String uf, String cidade, String rua) {

        try {

            String ufLimpo = uf.trim();
            String cidadeLimpa = cidade.trim();
            String ruaLimpa = rua.trim();

            String url = "https://viacep.com.br/ws/{uf}/{cidade}/{rua}/json/";

            RestTemplate restTemplate = new RestTemplate();

            ViaCepResponse[] resposta = restTemplate.getForObject(url, ViaCepResponse[].class, ufLimpo, cidadeLimpa,
                    ruaLimpa);

            if (resposta != null && resposta.length > 0) {
                return resposta[0].getCep();
            }

        } catch (Exception e) {
            System.err.println("Erro ao buscar CEP para: " + rua + ", " + cidade + " - " + uf);
        }

        return "Não encontrado";
    }

    public List<String[]> processarCsv(MultipartFile arquivo) {
        List<String[]> resultados = new ArrayList<>();
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            String[] linha;
            // Pular cabeçalho se houver: reader.readNext();

            while ((linha = reader.readNext()) != null) {
                // Supondo CSV: rua, cidade, uf
                if (linha.length >= 3) {
                    String rua = linha[0];
                    String cidade = linha[1];
                    String uf = linha[2];
                    String cep = buscarCepPorEndereco(uf, cidade, rua);

                    resultados.add(new String[] { rua, cidade, uf, cep });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultados;
    }

    public ByteArrayInputStream gerarExcel(List<String[]> dados) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("CEPs Encontrados");

            // Estilo para o cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] colunas = { "Rua", "Cidade", "UF", "CEP" };
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Preencher dados
            int rowIdx = 1;
            for (String[] linha : dados) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(linha[0]);
                row.createCell(1).setCellValue(linha[1]);
                row.createCell(2).setCellValue(linha[2]);
                row.createCell(3).setCellValue(linha[3]);
            }

            // Auto-ajuste das colunas
            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Excel: " + e.getMessage());
        }
    }
}