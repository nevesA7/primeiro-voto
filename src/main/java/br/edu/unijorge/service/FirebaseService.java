package br.edu.unijorge.service;

import br.edu.unijorge.config.FirebaseConfig;
import br.edu.unijorge.model.CargoEnum;
import br.edu.unijorge.model.TipoVotoEnum;
import br.edu.unijorge.model.Voto;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Grava os votos no Cloud Firestore.
 *
 * - Sem chave do Firebase: cai para o modo local (memória) e a urna continua funcionando.
 * - Com falha de rede: o voto vai para uma fila persistida em disco (votos-pendentes.tsv)
 *   e é reenviado automaticamente no próximo voto ou na próxima abertura do programa.
 */
public class FirebaseService implements VotoRepository {

    private static final String COLECAO = "votos_simulacao";
    private static final long TIMEOUT_SEGUNDOS = 10;
    private static final String SEPARADOR = "\t";

    private final boolean online;
    private final Path arquivoPendentes;
    private final MemoriaVotoRepository local = new MemoriaVotoRepository();
    private final List<Voto> pendentes = new ArrayList<>();
    private volatile int qtdPendentes; // lida pela interface sem esperar a rede

    public FirebaseService() {
        this(Paths.get("votos-pendentes.tsv"));
    }

    public FirebaseService(Path arquivoPendentes) {
        this.online = FirebaseConfig.inicializar();
        this.arquivoPendentes = arquivoPendentes;
        if (online) {
            carregarPendentesDoDisco();
            reenviarPendentes();
        }
    }

    @Override
    public synchronized void salvar(Voto voto) {
        if (!online) {
            local.salvar(voto);
            System.out.println("[Simulação Local] Voto registrado: Cargo=" + voto.getCargo()
                    + " | Tipo=" + voto.getTipoVoto());
            return;
        }

        reenviarPendentes();
        if (!enviar(voto)) {
            pendentes.add(voto);
            qtdPendentes = pendentes.size();
            gravarPendentesNoDisco();
            System.err.println("Sem conexão com o Firestore: voto guardado na fila local ("
                    + pendentes.size() + " pendente(s)).");
        }
    }

    @Override
    public synchronized List<Voto> listarPorSessao(String sessaoId) {
        if (!online) {
            return local.listarPorSessao(sessaoId);
        }

        List<Voto> resultado = new ArrayList<>();
        try {
            Firestore db = FirestoreClient.getFirestore();
            QuerySnapshot snapshot = db.collection(COLECAO)
                    .whereEqualTo("sessaoId", sessaoId)
                    .get()
                    .get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            resultado.addAll(snapshot.toObjects(Voto.class));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            System.err.println("Erro ao ler votos do Firestore: " + e.getMessage());
        }

        // inclui votos que ainda não conseguiram subir
        for (Voto p : pendentes) {
            if (sessaoId.equals(p.getSessaoId())) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    @Override
    public String getStatus() {
        if (!online) {
            return "Modo local (sem Firebase)";
        }
        int n = qtdPendentes;
        return n == 0 ? "Firebase online" : "Firebase: " + n + " voto(s) na fila local";
    }

    // ---------------------------------------------------------------- internos

    private boolean enviar(Voto voto) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            String id = db.collection(COLECAO).add(voto).get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS).getId();
            System.out.println("Voto registrado no Firestore (ID: " + id + ")");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException | TimeoutException | RuntimeException e) {
            System.err.println("Erro ao salvar voto no Firebase: " + e.getMessage());
            return false;
        }
    }

    private void reenviarPendentes() {
        if (pendentes.isEmpty()) {
            return;
        }
        List<Voto> copia = new ArrayList<>(pendentes);
        for (Voto p : copia) {
            if (!enviar(p)) {
                break; // ainda sem conexão; mantém a ordem e tenta depois
            }
            pendentes.remove(p);
        }
        qtdPendentes = pendentes.size();
        gravarPendentesNoDisco();
    }

    private void gravarPendentesNoDisco() {
        try {
            if (pendentes.isEmpty()) {
                Files.deleteIfExists(arquivoPendentes);
                return;
            }
            List<String> linhas = new ArrayList<>();
            for (Voto v : pendentes) {
                linhas.add(String.join(SEPARADOR,
                        v.getCargo(),
                        v.getNumeroDigitado() == null ? "" : v.getNumeroDigitado(),
                        v.getTipoVoto(),
                        v.getSessaoId() == null ? "" : v.getSessaoId(),
                        String.valueOf(v.getDataHora().getTime())));
            }
            Files.write(arquivoPendentes, linhas, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Não foi possível gravar a fila de votos pendentes: " + e.getMessage());
        }
    }

    private void carregarPendentesDoDisco() {
        if (!Files.isRegularFile(arquivoPendentes)) {
            return;
        }
        try {
            for (String linha : Files.readAllLines(arquivoPendentes, StandardCharsets.UTF_8)) {
                String[] c = linha.split(SEPARADOR, -1);
                if (c.length < 5) {
                    continue;
                }
                pendentes.add(new Voto(
                        CargoEnum.valueOf(c[0]),
                        c[1].isEmpty() ? null : c[1],
                        TipoVotoEnum.valueOf(c[2]),
                        c[3],
                        new Date(Long.parseLong(c[4]))));
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Fila de votos pendentes ilegível: " + e.getMessage());
        }
        qtdPendentes = pendentes.size();
    }
}
