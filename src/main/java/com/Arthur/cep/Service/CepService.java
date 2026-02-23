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
}