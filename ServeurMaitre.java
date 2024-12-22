import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// import java.util.Properties;

public class ServeurMaitre {
    private static int PORT;
    private static String[] SLAVE_HOSTS;
    private static int[] SLAVE_PORTS;
    private static final String SLAVE_CLASS_NAME="ServeurEsclave";
    private static int nb_Thread;

    public static void main(String[] args) {
        try {
            // Charger les configurations
            Config.chargerConfiguration("config.properties");
            initialiserConfigurations();

            //Démarrer les serveurs esclaves
            demarrerServeursEsclaves();
            
            // Démarrer le serveur maître
            demarrerServeur();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des configurations : " + e.getMessage());
        }
    }

    private static void initialiserConfigurations() {
        PORT = Config.getInt("server.master.port");
        SLAVE_HOSTS = Config.getArray("server.slave.hosts");

        nb_Thread = Config.getInt("nbr.thread");

         String[] ports = Config.getArray("server.slave.ports");
        SLAVE_PORTS = new int[ports.length];
        for (int i = 0; i < ports.length; i++) {
            SLAVE_PORTS[i] = Integer.parseInt(ports[i]);
        }
    }    

    private static void demarrerServeursEsclaves() {
        for (int port : SLAVE_PORTS) {
            try {
                ProcessBuilder pb = new ProcessBuilder("java", SLAVE_CLASS_NAME, String.valueOf(port));
                pb.inheritIO(); // Pour voir les sorties des processus esclaves dans la console
                pb.start();
                System.out.println("Serveur esclave démarré sur le port " + port);
            } catch (IOException e) {
                System.err.println("Erreur lors du démarrage du serveur esclave sur le port " + port + " : " + e.getMessage());
            }
        }
    }

    private static void demarrerServeur() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur maître en attente de connexion...");
            gererConnexionsClients(serverSocket);
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du serveur maître : " + e.getMessage());
        }
    }

    private static void gererConnexionsClients(ServerSocket serverSocket) {
        ExecutorService threadPool = Executors.newFixedThreadPool(nb_Thread); // Thread pool avec 10 threads
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> {
                    try {
                        System.out.println("Client connecté.");
                        DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());

                        // Lire le nom du fichier
                        String originalFileName = dataIn.readUTF();
                        System.out.println("Nom du fichier reçu : " + originalFileName);

                        // Lire les données du fichier
                        byte[] fileData = recevoirFichier(dataIn);
                        byte[][] fileParts = diviserFichier(fileData);

                        // Envoyer les parties aux serveurs esclaves
                        envoyerFichierAuxEsclaves(fileParts, originalFileName);
                        System.out.println("Fichier traité et distribué.");
                       
                    } catch (IOException e) {
                        System.err.println("Erreur lors du traitement du client : " + e.getMessage());
                    }
                });
            } catch (IOException e) {
                System.err.println("Erreur lors de l'acceptation de connexion : " + e.getMessage());
            }
        }
    }

    private static byte[] recevoirFichier(DataInputStream dataIn) throws IOException {
        // Lire la taille totale du fichier
        long fileSize = dataIn.readLong();
        byte[] fileData = new byte[(int) fileSize];
        System.out.println("Taille du fichier reçu : " + fileSize + " octets");
    
        // Lire les données avec progression
        long totalBytesRead = 0;
        int bytesRead;
        byte[] buffer = new byte[4096];
    
        while (totalBytesRead < fileSize) {
            bytesRead = dataIn.read(buffer);
            if (bytesRead == -1) break;
    
            System.arraycopy(buffer, 0, fileData, (int) totalBytesRead, bytesRead);
            totalBytesRead += bytesRead;
    
            // Calculer et afficher la progression
            int progress = (int) ((totalBytesRead * 100) / fileSize);
            System.out.print("\rProgression de la réception : " + progress + "%");
        }
    
        System.out.println("\nFichier reçu avec succès.");
        return fileData;
    }
    

    private static byte[][] diviserFichier(byte[] fileData) {
        System.out.println("Taille totale du fichier : " + fileData.length);
        int partSize = fileData.length / 3;

        System.out.println("Taille de la partSize : " + partSize);

        byte[] part1 = new byte[partSize];
        byte[] part2 = new byte[partSize];
        byte[] part3 = new byte[fileData.length - 2 * partSize];

        System.out.println("Taille de la part1 : " + part1.length);
        System.out.println("Taille de la part2 : " + part2.length);
        System.out.println("Taille de la part3 : " + part3.length);

        System.arraycopy(fileData, 0, part1, 0, partSize);
        System.arraycopy(fileData, partSize, part2, 0, partSize);
        System.arraycopy(fileData, 2 * partSize, part3, 0, part3.length);

        return new byte[][]{part1, part2, part3};
    }

    private static void envoyerFichierAuxEsclaves(byte[][] fileParts, String originalFileName) {
        for (int i = 0; i < SLAVE_HOSTS.length; i++) {
            envoyerFichier(SLAVE_HOSTS[i], SLAVE_PORTS[i], fileParts[i], originalFileName);
        }
    }

    private static void envoyerFichier(String host, int port, byte[] data, String originalFileName) {
        try (Socket socket = new Socket(host, port);
             DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream())) {

            // Envoyer le nom du fichier
            dataOut.writeUTF(originalFileName);

            // Envoyer la taille des données
            dataOut.writeInt(data.length);
            

            // Envoyer les données
            dataOut.write(data);
            dataOut.flush();

            System.out.println("Partie envoyée à " + host + ":" + port + " avec le nom " + originalFileName + "  de taille " + data.length);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'envoi à " + host + ":" + port + " : " + e.getMessage());
        }
    }
}
