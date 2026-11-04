package br.com.ifba.desktop.model;

public final class ApiModels {

    private ApiModels() {
    }

    public record Credenciais(String telefone, String senha) {
    }

    public record LoginRequest(String telefone, String senha) {
    }

    public record LoginResponse(String token, String tipo, Long usuarioId, String perfil, long expiraEmMinutos) {
    }

    public record Localidade(Long id, String nome, String tipo, Long paiId, Double latitude, Double longitude) {
    }

    public record ComparacaoCultura(String cultura, String criterio, Double valorAtualMm, Double limiarMm,
                                    String fonteLimiar, boolean limiarPersonalizado, boolean limiarAtingido) {
    }

    public record Cultura(Long id, String nome, String tipoCriterio, Double limiarPadraoMm,
                          Integer janelaDias, String fonteLimiar) {
    }

    public record Painel(Long localidadeId, String localidadeNome, int ano, int mes,
                         Double acumuladoMesMm, Double normalMesMm, Double percentualDaNormal,
                         Double acumuladoEstacaoMm, Double maiorChuvaRecenteMm,
                         boolean historicoSuficiente, String resumo,
                         String fonte, String atualizadoEm, ComparacaoCultura comparacaoCultura) {
    }

    public record Sugestao(Long id, Long localidadeId, String localidadeNome, String cultura,
                           String resultado, String mensagem, Double dadoUsadoMm, Double referenciaMm,
                           String fonte, String dataDado, Double grauConfianca, String geradaEm) {
    }
}