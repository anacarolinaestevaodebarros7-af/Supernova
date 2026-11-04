package br.com.ifba.desktop;

import br.com.ifba.desktop.controller.DesktopController;
import br.com.ifba.desktop.service.ManejoApiClient;
import br.com.ifba.desktop.view.DesktopView;

import javax.swing.SwingUtilities;

public final class DesktopApplication {

    private DesktopApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DesktopController(new DesktopView(), new ManejoApiClient()).iniciar());
    }
}