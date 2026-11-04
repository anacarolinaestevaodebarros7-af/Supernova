package br.com.ifba.desktop.controller;

import br.com.ifba.desktop.model.ApiModels.Credenciais;
import br.com.ifba.desktop.model.ApiModels.Cultura;
import br.com.ifba.desktop.model.ApiModels.Localidade;
import br.com.ifba.desktop.model.ApiModels.LoginResponse;
import br.com.ifba.desktop.service.ManejoApiClient;
import br.com.ifba.desktop.view.DesktopView;
import br.com.ifba.desktop.view.DesktopView.AcaoMenu;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public final class DesktopController {

    private final DesktopView view;
    private final ManejoApiClient apiClient;

    public DesktopController(DesktopView view, ManejoApiClient apiClient) {
        this.view = view;
        this.apiClient = apiClient;
    }

    public void iniciar() {
        Optional<Credenciais> credenciais = view.solicitarCredenciais();
        if (credenciais.isEmpty()) {
            return;
        }

        try {
            Credenciais dados = credenciais.get();
            LoginResponse sessao = apiClient.login(dados.telefone(), dados.senha());
            List<Localidade> localidades = apiClient.listarLocalidades();
            List<Cultura> culturas = apiClient.listarCulturas(sessao.token());
            if (localidades.isEmpty()) {
                view.mostrarMensagem("Nenhuma localidade está cadastrada.");
                return;
            }
            if (culturas.isEmpty()) {
                view.mostrarMensagem("Nenhuma cultura está cadastrada.");
                return;
            }

            Optional<Localidade> localidadeEscolhida = view.selecionarLocalidade(localidades);
            while (localidadeEscolhida.isPresent()) {
                Localidade localidade = localidadeEscolhida.get();
                AcaoMenu acao = view.exibirMenu(localidade);
                if (acao == AcaoMenu.SAIR) {
                    return;
                }
                try {
                    executarAcao(acao, localidade, culturas, sessao.token());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    view.mostrarErro("A comunicação com a API foi interrompida.");
                    return;
                } catch (IOException e) {
                    view.mostrarErro(e.getMessage());
                }
                if (acao == AcaoMenu.TROCAR_LOCALIDADE) {
                    localidadeEscolhida = view.selecionarLocalidade(localidades);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            view.mostrarErro("A comunicação com a API foi interrompida.");
        } catch (IOException e) {
            view.mostrarErro(e.getMessage());
        }
    }

    private void executarAcao(AcaoMenu acao, Localidade localidade, List<Cultura> culturas, String token)
            throws IOException, InterruptedException {
        switch (acao) {
            case PAINEL -> view.mostrarPainel(apiClient.consultarPainel(token, localidade.id()));
            case SUGESTAO -> {
                Optional<Cultura> cultura = view.selecionarCultura(culturas);
                if (cultura.isPresent()) {
                    view.mostrarSugestao(apiClient.consultarSugestao(token, localidade.id(), cultura.get().nome()));
                }
            }
            case TROCAR_LOCALIDADE, SAIR -> {
            }
        }
    }
}