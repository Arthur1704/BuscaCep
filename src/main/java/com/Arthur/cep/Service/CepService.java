package com.Arthur.cep.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.Arthur.cep.Model.Entities.ViaCepResponse;
import com.opencsv.CSVReader;

@Service
public class CepService {

    private final RestTemplate restTemplate;

    public CepService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);

        this.restTemplate = new RestTemplate(factory);
    }

    // =========================
    // BUSCAR CEP POR ENDEREÇO
    // =========================
    public ViaCepResponse buscarCepPorEndereco(String uf, String cidade, String rua) {

        try {
            if (uf == null || cidade == null || rua == null ||
                    uf.isBlank() || cidade.isBlank() || rua.isBlank()) {
                return null;
            }

            String url = "https://viacep.com.br/ws/{uf}/{cidade}/{rua}/json/";

            ViaCepResponse[] resposta = restTemplate.getForObject(
                    url,
                    ViaCepResponse[].class,
                    uf.trim(),
                    cidade.trim(),
                    rua.trim());

            if (resposta != null && resposta.length > 0) {
                return resposta[0];
            }

        } catch (Exception e) {
            System.err.println("Erro ao buscar CEP: " + e.getMessage());
        }

        return null;
    }

    // =========================
    // PROCESSAR CSV
    // =========================
    public List<String[]> processarCsv(MultipartFile arquivo) {

        List<String[]> resultados = new ArrayList<>();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {

            String[] linha;

            // Se tiver cabeçalho:
            // reader.readNext();

            while ((linha = reader.readNext()) != null) {

                if (linha.length >= 3) {

                    String rua = linha[0];
                    String cidade = linha[1];
                    String uf = linha[2];

                    ViaCepResponse resposta = buscarCepPorEndereco(uf, cidade, rua);

                    String bairro = (resposta != null) ? resposta.getBairro() : "Não encontrado";
                    String cep = (resposta != null) ? resposta.getCep() : "Não encontrado";

                    resultados.add(new String[] {
                            rua,
                            cidade,
                            uf,
                            bairro,
                            cep
                    });
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar CSV: " + e.getMessage());
        }

        return resultados;
    }

    // =========================
    // GERAR EXCEL
    // =========================
    public ByteArrayInputStream gerarExcel(List<String[]> dados) {

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("CEPs Encontrados");

            // Estilo do cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Cabeçalho atualizado
            String[] colunas = { "Rua", "Cidade", "UF", "Bairro", "CEP" };

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Preencher dados
            int rowIdx = 1;

            for (String[] linha : dados) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(linha[0]); // rua
                row.createCell(1).setCellValue(linha[1]); // cidade
                row.createCell(2).setCellValue(linha[2]); // uf
                row.createCell(3).setCellValue(linha[3]); // bairro
                row.createCell(4).setCellValue(linha[4]); // cep
            }

            // Ajustar largura automática
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