/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package br.com.treinanorma;

import br.com.treinanorma.view.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.EventQueue;
import javax.swing.UIManager;

/**
 *
 * @author Maxwell
 */
public class TreinaNormaApp {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 90);
        UIManager.put("TextComponent.arc", 90);               

        javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);
        javax.swing.JDialog.setDefaultLookAndFeelDecorated(true);

        EventQueue.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
