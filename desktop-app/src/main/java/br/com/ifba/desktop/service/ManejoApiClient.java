package br.com.ifba.desktop.service;

import br.com.ifba.desktop.model.ApiModels.Cultura;
import br.com.ifba.desktop.model.ApiModels.Localidade;
import br.com.ifba.desktop.model.ApiModels.LoginRequest;
import br.com.ifba.desktop.model.ApiModels.LoginResponse;
import br.com.ifba.desktop.model.ApiModels.Painel;
import br.com.ifba.desktop.model.ApiModels.Sugestao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public final class ManejoApiClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ManejoApiClient() {
        this(System.getenv().getOrDefault("MANEJO_API_URL", "http://localhost:8080"));
    }

    ManejoApiClient(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.objectMapper = new ObjectMapper();
    }

    public LoginResponse login(String telefone, String senha) throws IOException, InterruptedException {
        return send("/api/v1/auth/login", "POST", new LoginRequest(telefone, senha), null, LoginResponse.class);
    }

    public List<Localidade> listarLocalidades() throws IOException, InterruptedException {
        return sendList("/api/v1/localidades", null, new TypeReference<>() {
        });
    }

    public List<Cultura> listarCulturas(String token) throws IOException, InterruptedException {
        return sendList("/api/v1/culturas", token, new TypeReference<>() {
        });
    }

    public Painel consultarPainel(String token, Long localidadeId) throws IOException, InterruptedException {
        return send("/api/v1/localidades/" + localidadeId + "/painel-chuva", "GET", null, token, Painel.class);
    }

    public Sugestao consultarSugestao(String token, Long localidadeId, String cultura)
            throws IOException, InterruptedException {
        String culturaCodificada = URLEncoder.encode(cultura, StandardCharsets.UTF_8);
        String path = "/api/v1/localidades/" + localidadeId + "/sugestao-plantio?cultura=" + culturaCodificada;
        return send(path, "GET", null, token, Sugestao.class);
    }

    private <T> T send(String path, String method, Object body, String token, Class<T> responseType)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = requestBuilder(path, token);
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(body)));
        }
        return objectMapper.readValue(enviar(request.build()), responseType);
    }

    private <T> List<T> sendList(String path, String token, TypeReference<List<T>> responseType)
            throws IOException, InterruptedException {
        String responseBody = enviar(requestBuilder(path, token).GET().build());
        return objectMapper.readValue(responseBody, responseType);
    }

    private HttpRequest.Builder requestBuilder(String path, String token) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private String enviar(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(mensagemErro(response.statusCode(), response.body()));
        }
        return response.body();
    }

    private String mensagemErro(int status, String body) {
        try {
            JsonNode resposta = objectMapper.readTree(body);
            JsonNode mensagem = resposta == null ? null : resposta.get("mensagem");
            if (mensagem != null && !mensagem.asText().isBlank()) {
                return mensagem.asText();
            }
        } catch (IOException ignored) {
        }
        return "A API respondeu com o status HTTP " + status + ".";
    }
}