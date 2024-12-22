import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.*;

public class Affichage {
    public static void main(String[] args) {
        Properties properties = new Properties();
        String configPath = "config.properties";

        try (FileInputStream input = new FileInputStream(configPath)) {
            // Charger le fichier de configuration
            properties.load(input);

            // Lire les chemins
            String chooserPath = properties.getProperty("file.chooser.path", "Non défini");
            String serverSlavePath = properties.getProperty("file.server.slave.path", "Non défini");
            String serverMasterPath = properties.getProperty("file.server.master.path", "Non défini");
            String clientPath = properties.getProperty("file.client.path", "Non défini");
            String displayPath = properties.getProperty("file.display.path", "Non défini");

            // Construire un message pour l'affichage
            StringBuilder message = new StringBuilder("Chemins des fichiers utilisés :\n");
            message.append("- FileChooser : ").append(chooserPath).append("\n");
            message.append("- Serveur Esclave : ").append(serverSlavePath).append("\n");
            message.append("- Serveur Maître : ").append(serverMasterPath).append("\n");
            message.append("- Client : ").append(clientPath).append("\n");
            message.append("- Affichage : ").append(displayPath).append("\n");

            // Afficher le message dans une fenêtre graphique
            JOptionPane.showMessageDialog(
                null, 
                message.toString(), 
                "Affichage des Chemins", 
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException ex) {
            // Afficher un message d'erreur en cas de problème
            JOptionPane.showMessageDialog(
                null, 
                "Erreur lors du chargement du fichier de configuration : " + ex.getMessage(), 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
