import java.io.File;
import javax.swing.*;

public class FileChooserUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileChooserUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Sélecteur de fichier");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        JLabel selectedFileLabel = new JLabel("Aucun fichier sélectionné.");
        JButton openButton = new JButton("Sélectionner un fichier");
    

        // Action sur le clic du bouton
        openButton.addActionListener(e -> openFileChooser(selectedFileLabel));

        panel.add(openButton);
        panel.add(selectedFileLabel);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private static void openFileChooser(JLabel selectedFileLabel) {
        JFileChooser fileChooser = new JFileChooser();

        // Supprimer les filtres pour accepter tous les fichiers
        fileChooser.setAcceptAllFileFilterUsed(true);

        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedFileLabel.setText("Fichier sélectionné : " + selectedFile.getName());

            try {
                Client.envoyerFichier(selectedFile);
                JOptionPane.showMessageDialog(null, "Fichier envoyé avec succès !");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erreur lors de l'envoi du fichier : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Aucun fichier sélectionné.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
