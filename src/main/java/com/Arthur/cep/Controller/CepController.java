package com.Arthur.cep.Controller;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.Arthur.cep.Service.CepService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CepController {

    @Autowired
    private CepService cepService;

    @GetMapping("/")
    public String paginaInicial() {
        return "cep";
    }

    @PostMapping("/buscar-cep")
    public String buscarCepPorEndereco(
            @RequestParam String rua,
            @RequestParam String cidade,
            @RequestParam String estado,
            Model model) {

        String cep = cepService.buscarCepPorEndereco(estado, cidade, rua);

        if (cep.equals("Não encontrado")) {
            model.addAttribute("erro", "Endereço não encontrado!");
        } else {
            model.addAttribute("rua", rua);
            model.addAttribute("cidade", cidade);
            model.addAttribute("estado", estado);
            model.addAttribute("cep", cep);
        }

        return "cep";
    }

    @PostMapping("/upload-csv")
    public String uploadCsv(@RequestParam("arquivo") MultipartFile arquivo,
            Model model,
            HttpSession session) {
        if (arquivo.isEmpty()) {
            model.addAttribute("erro", "Por favor, selecione um arquivo CSV.");
            return "cep";
        }

        List<String[]> listaResultados = cepService.processarCsv(arquivo);

        session.setAttribute("dadosCep", listaResultados);

        model.addAttribute("listaResultados", listaResultados);

        return "resultado";
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<InputStreamResource> exportarExcel(HttpSession session) {
        Object objetoSessao = session.getAttribute("dadosCep");

        if (!(objetoSessao instanceof List<?>)) {
            return ResponseEntity.badRequest().build();
        }

        @SuppressWarnings("unchecked")
        List<String[]> dados = (List<String[]>) objetoSessao;

        if (dados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        ByteArrayInputStream in = cepService.gerarExcel(dados);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=relatorio_ceps.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}