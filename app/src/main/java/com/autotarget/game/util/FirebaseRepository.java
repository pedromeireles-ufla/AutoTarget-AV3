package com.autotarget.game.util;

import com.autotarget.game.model.Partida;
import com.autotarget.game.model.Telemetria;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositório central para operações do Firebase.
 * Garante que todas as operações ocorram em threads separadas.
 * Atende ao requisito 6.3.2 c) da AV3.
 */
public class FirebaseRepository {
    private static FirebaseRepository instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final ExecutorService executor;

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private FirebaseRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /** Garante uma única instância compartilhada (Singleton), evitando múltiplos executors concorrentes. */
    public static synchronized FirebaseRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseRepository();
        }
        return instance;
    }

    /** Salva uma partida criptografada no Firestore, de forma sincronizada para evitar conflitos. */
    public synchronized void salvarPartida(Partida partida, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            try {
                org.json.JSONObject dadosSensiveis = new org.json.JSONObject();
                dadosSensiveis.put("nomeJogador", partida.getNomeJogador());
                dadosSensiveis.put("pontuacaoFinal", partida.getPontuacaoFinal());

                String jsonCriptografado = Cryptography.encrypt(dadosSensiveis.toString());
                partida.setDadosCriptografados(jsonCriptografado);
                partida.setNomeJogador(null);
                partida.setPontuacaoFinal(null);

                db.collection("partidas")
                        .add(partida)
                        .addOnSuccessListener(documentReference -> {
                            EvidenceLogger.registrarEvento("FIREBASE", "Partida salva com sucesso");
                            if (callback != null) callback.onSuccess(null);
                        })
                        .addOnFailureListener(e -> {
                            EvidenceLogger.registrarEvento("FIREBASE", "Erro ao salvar partida: " + e.getMessage());
                            if (callback != null) callback.onError(e);
                        });
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    /** Busca o ranking de forma sincronizada. */
    public synchronized void buscarRanking(RepositoryCallback<List<Partida>> callback) {
        // ... mantém a lógica atual, só adiciona "synchronized" na assinatura
    }

    public synchronized void registrarTelemetria(Telemetria telemetria) {
        // ... mantém a lógica atual, só adiciona "synchronized" na assinatura
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}
