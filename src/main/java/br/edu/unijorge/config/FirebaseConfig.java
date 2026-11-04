package br.edu.unijorge.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseConfig {

    private static boolean inicializado = false;

    /**
     * Procura a chave de serviço, em ordem:
     * 1) variável de ambiente FIREBASE_CONFIG_PATH;
     * 2) arquivo firebase-key.json na pasta de execução (ao lado do .jar);
     * 3) /firebase-key.json no classpath (somente desenvolvimento).
     * Se nada for encontrado, o sistema roda em modo de simulação local.
     */
    public static synchronized boolean inicializar() {
        if (inicializado) {
            return true;
        }

        try (InputStream chave = abrirChave()) {
            if (chave == null) {
                System.out.println("Aviso: chave do Firebase não encontrada. Rodando em modo de simulação local.");
                return false;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(chave))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            inicializado = true;
            System.out.println("Firebase inicializado com sucesso!");
            return true;
        } catch (IOException | RuntimeException e) {
            System.err.println("Erro ao inicializar o Firebase: " + e.getMessage());
            return false;
        }
    }

    private static InputStream abrirChave() throws IOException {
        String caminho = System.getenv("FIREBASE_CONFIG_PATH");
        if (caminho != null && !caminho.isBlank()) {
            return new FileInputStream(caminho);
        }

        File local = new File("firebase-key.json");
        if (local.isFile()) {
            return new FileInputStream(local);
        }

        return FirebaseConfig.class.getResourceAsStream("/firebase-key.json");
    }
}
