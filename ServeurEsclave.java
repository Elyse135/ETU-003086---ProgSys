import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurEsclave {

    private final int port;
    private ServerSocket serverSocket; // Déclaration du ServerSocket
    private static String dossierStockage;

    public static String getDossierStockage() {
        return dossierStockage;
    }

    public static void setDossierStockage(String dossierStockage) {
        ServeurEsclave.dossierStockage = dossierStockage;
    }

    public ServeurEsclave(int port) {
        this.port = port;
    }

    public void demarrer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Serveur esclave en attente de connexion sur le port " + port + "...");
            gererConnexionsClients();
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du serveur esclave : " + e.getMessage());
        }
    }

    private void gererConnexionsClients() {
        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream())) {
    
                // Lire le nom du fichier
                String originalFileName = dataIn.readUTF();
                System.out.println("\rNom du fichier reçu : " + originalFileName);
    
                // Lire la taille de la partie reçue
                long partSize = dataIn.readInt();
                byte[] filePart = new byte[(int) partSize];
                System.out.println("\rTaille de la partie à recevoir : " + partSize + " octets");
    
                // Lire les données avec progression
                long totalBytesRead = 0;
                int bytesRead;
                byte[] buffer = new byte[4096];
    
                while (totalBytesRead < partSize) {
                    bytesRead = dataIn.read(buffer);
                    if (bytesRead == -1) break;
    
                    System.arraycopy(buffer, 0, filePart, (int) totalBytesRead, bytesRead);
                    totalBytesRead += bytesRead;
    
                    // Calculer et afficher la progression
                    int progress = (int) ((totalBytesRead * 100) / partSize);
                    System.out.print("\rProgression de la réception : " + progress + "%");
                }
    
                System.out.println("\nPartie reçue avec succès.");
    
                // Stocker la partie du fichier
                stockerPartieFichier(filePart, originalFileName);
            } catch (IOException e) {
                System.err.println("Erreur lors de la réception de la partie du fichier : " + e.getMessage());
            }
        }
    }

    private void stockerPartieFichier(byte[] filePart, String originalFileName) throws IOException {
        dossierStockage = "stockage_" + port; // Dossier de stockage
        File dossier = new File(dossierStockage);
        if (!dossier.exists()) {
            dossier.mkdir(); // Créer le dossier s'il n'existe pas
        }

        File fichier = new File(dossierStockage, "part_" + port + "_" + originalFileName);
        try (FileOutputStream fos = new FileOutputStream(fichier)) {
            fos.write(filePart);
            System.out.println("Partie stockée dans : " + fichier.getAbsolutePath());
        }
    }

    public void arreter() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Fermer le ServerSocket pour libérer le port
                System.out.println("Serveur esclave arrêté sur le port " + port);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'arrêt du serveur esclave : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java ServeurEsclave <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        ServeurEsclave serveurEsclave = new ServeurEsclave(port);

        // Ajouter un hook de shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serveurEsclave.arreter();
        }));

        serveurEsclave.demarrer();
    }
}