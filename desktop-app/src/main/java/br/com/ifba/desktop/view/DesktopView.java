package br.com.ifba.desktop.view;

import br.com.ifba.desktop.model.ApiModels.Credenciais;
import br.com.ifba.desktop.model.ApiModels.Cultura;
import br.com.ifba.desktop.model.ApiModels.Localidade;
import br.com.ifba.desktop.model.ApiModels.Painel;
import br.com.ifba.desktop.model.ApiModels.Sugestao;

import javax.swing.JPasswordField;
import javax.swing.JOptionPane;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class DesktopView {

    private static final String[] OPCOES_MENU = {
            "Painel de chuva", "Sugestão de plantio", "Trocar localidade", "Sair"
    };

    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");

    public Optional<Credenciais> solicitarCredenciais() {
        String telefone = JOptionPane.showInputDialog(null, "Telefone:", "Manejo Caatingueiro | Acesso",
                JOptionPane.QUESTION_MESSAGE);
        if (telefone == null || telefone.isBlank()) {
            return Optional.empty();
        }

        JPasswordField campoSenha = new JPasswordField(18);
        int resultado = JOptionPane.showConfirmDialog(null, campoSenha, "Senha",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }

        char[] senha = campoSenha.getPassword();
        try {
            return Optional.of(new Credenciais(telefone.trim(), new String(senha)));
        } finally {
            Arrays.fill(senha, '\0');
        }
    }

    public Optional<Localidade> selecionarLocalidade(List<Localidade> localidades) {
        String[] opcoes = localidades.stream()
                .map(localidade -> localidade.id() + " - " + localidade.nome())
                .toArray(String[]::new);
        Object selecionada = JOptionPane.showInputDialog(null, "Escolha a localidade:", "Localidades",
                JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes.length > 0 ? opcoes[0] : null);
        if (selecionada == null) {
            return Optional.empty();
        }
        int indice = Arrays.asList(opcoes).indexOf(selecionada.toString());
        return indice >= 0 ? Optional.of(localidades.get(indice)) : Optional.empty();
    }

    public Optional<Cultura> selecionarCultura(List<Cultura> culturas) {
        String[] opcoes = culturas.stream().map(Cultura::nome).toArray(String[]::new);
        Object selecionada = JOptionPane.showInputDialog(null, "Escolha a cultura:", "Culturas",
                JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes.length > 0 ? opcoes[0] : null);
        if (selecionada == null) {
            return Optional.empty();
        }
        return culturas.stream().filter(cultura -> cultura.nome().equals(selecionada.toString())).findFirst();
    }

    public AcaoMenu exibirMenu(Localidade localidade) {
        int opcao = JOptionPane.showOptionDialog(null, "Localidade: " + localidade.nome(),
                "Manejo Caatingueiro", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, OPCOES_MENU, OPCOES_MENU[0]);
        return switch (opcao) {
            case 0 -> AcaoMenu.PAINEL;
            case 1 -> AcaoMenu.SUGESTAO;
            case 2 -> AcaoMenu.TROCAR_LOCALIDADE;
            default -> AcaoMenu.SAIR;
        };
    }

    public void mostrarPainel(Painel painel) {
        String mensagem = "Localidade: " + painel.localidadeNome()
                + "\nChuva no mês: " + formatar(painel.acumuladoMesMm()) + " mm"
                + "\nNormal do mês: " + formatar(painel.normalMesMm()) + " mm"
                + "\nPercentual da normal: " + formatar(painel.percentualDaNormal()) + "%"
                + "\nChuva na estação: " + formatar(painel.acumuladoEstacaoMm()) + " mm"
                + "\nMaior chuva recente: " + formatar(painel.maiorChuvaRecenteMm()) + " mm"
                + "\nHistórico suficiente: " + (painel.historicoSuficiente() ? "sim" : "não")
                + "\nResumo: " + texto(painel.resumo())
                + "\nFonte: " + texto(painel.fonte())
                + "\nAtualizado em: " + texto(painel.atualizadoEm());
        JOptionPane.showMessageDialog(null, mensagem, "Painel de chuva", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarSugestao(Sugestao sugestao) {
        String mensagem = "Localidade: " + sugestao.localidadeNome()
                + "\nCultura: " + sugestao.cultura()
                + "\nResultado: " + sugestao.resultado()
                + "\n" + texto(sugestao.mensagem())
                + "\nChuva considerada: " + formatar(sugestao.dadoUsadoMm()) + " mm"
                + "\nReferência: " + formatar(sugestao.referenciaMm()) + " mm"
                + "\nConfiança: " + formatar(sugestao.grauConfianca())
                + "\nFonte: " + texto(sugestao.fonte())
                + "\nGerada em: " + texto(sugestao.geradaEm());
        JOptionPane.showMessageDialog(null, mensagem, "Sugestão de plantio", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "Não foi possível concluir a operação",
                JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensagem(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "Manejo Caatingueiro", JOptionPane.INFORMATION_MESSAGE);
    }

    private String formatar(Double valor) {
        if (valor == null) {
            return "sem dados";
        }
        return NumberFormat.getNumberInstance(LOCALE_BR).format(valor);
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "não informado" : valor;
    }

    public enum AcaoMenu {
        PAINEL,
        SUGESTAO,
        TROCAR_LOCALIDADE,
        SAIR
    }
}