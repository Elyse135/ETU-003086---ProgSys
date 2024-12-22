import java.io.*;
import java.net.Socket;
import java.util.Properties;

@SuppressWarnings("CallToPrintStackTrace")
public class Client {
    private static  String SERVER_HOST;
    private static  int SERVER_PORT;


    static {
        try {
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                properties.load(fis);
            }

            SERVER_HOST = properties.getProperty("server.master.host", "localhost");
            SERVER_PORT = Integer.parseInt(properties.getProperty("server.master.port", "12345"));

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier de configuration : " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Terminer si la configuration échoue
        }
    }



    public static void envoyerFichier(File file) {
        if (!file.exists() || !file.isFile()) {
            System.err.println("Le fichier spécifié est introuvable ou invalide.");
            return;
        }

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
             FileInputStream fileIn = new FileInputStream(file)) {

            System.out.println("Connexion au serveur maître...");

            // Envoyer le nom du fichier
            dataOut.writeUTF(file.getName());

            // Envoyer la taille du fichier
            long fileSize = file.length();
            dataOut.writeLong(fileSize);
            System.out.println("Taille du fichier : " + fileSize + " octets");

            // Envoyer les données du fichier avec progression
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesSent = 0;

            while ((bytesRead = fileIn.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;

                // Calculer et afficher la progression
                int progress = (int) ((totalBytesSent * 100) / fileSize);
                System.out.print("\rProgression de l'envoi : " + progress + "%");
            }

            dataOut.flush();
            System.out.println("\nFichier envoyé avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi du fichier : " + e.getMessage());
        }
    }

    public static void telechargerFichier(){
        
    }
}
