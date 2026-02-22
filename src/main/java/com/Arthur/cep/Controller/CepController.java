package com.Arthur.cep.Controller;

import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Arthur.cep.Model.Entities.Endereco;
import com.Arthur.cep.Service.CepService;

@Controller
public class CepController {

    @Autowired
    private CepService cepService;

    @GetMapping("/")
    public String paginaInicial() {
        return "cep";
    }

    @PostMapping("/buscar")
    public String buscarCep(@RequestParam String cep, Model model) {

        cep = cep.replace("-", "");

        if (cep.length() != 8) {
            model.addAttribute("erro", "CEP inválido!");
            return "cep";
        }

        Endereco endereco = cepService.buscarCep(cep);

        if (endereco == null) {
            model.addAttribute("erro", "CEP não encontrado!");
        } else {
            model.addAttribute("endereco", endereco);
        }

        return "cep";
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarExcel(@ModelAttribute("endereco") Endereco endereco) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Endereco");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Rua");
        header.createCell(1).setCellValue("Bairro");
        header.createCell(2).setCellValue("Cidade");
        header.createCell(3).setCellValue("Estado");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue(endereco.getRua());
        row.createCell(1).setCellValue(endereco.getBairro());
        row.createCell(2).setCellValue(endereco.getCidade());
        row.createCell(3).setCellValue(endereco.getEstado());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("endereco.xlsx").build());

        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }
}