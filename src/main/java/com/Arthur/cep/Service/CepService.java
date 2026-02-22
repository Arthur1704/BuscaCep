package com.Arthur.cep.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.Arthur.cep.Model.Entities.ViaCepResponse;

@Service
public class CepService {

    public String buscarCepPorEndereco(String uf, String cidade, String rua) {

        try {

            cidade = URLEncoder.encode(cidade, StandardCharsets.UTF_8);
            rua = URLEncoder.encode(rua, StandardCharsets.UTF_8);

            String url = "https://viacep.com.br/ws/" +
                    uf + "/" + cidade + "/" + rua + "/json/";

            RestTemplate restTemplate = new RestTemplate();

            ViaCepResponse[] resposta =
                    restTemplate.getForObject(url, ViaCepResponse[].class);

            if (resposta != null && resposta.length > 0) {
                return resposta[0].getCep();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Não encontrado";
    }
}