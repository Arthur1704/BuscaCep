package com.Arthur.cep.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.Arthur.cep.Service.CepService;

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
}