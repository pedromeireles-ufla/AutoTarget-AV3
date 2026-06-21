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
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final ExecutorService executor;

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public FirebaseRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /** Salva uma partida criptografada no Firestore. */
    public void salvarPartida(Partida partida, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            // Criptografar campos sensíveis antes de salvar (Requisito 6.3.2 b)
            partida.setNomeJogador(Cryptography.encrypt(partida.getNomeJogador()));
            partida.setPontuacaoFinal(Cryptography.encrypt(partida.getPontuacaoFinal()));

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
        });
    }

    /** Salva dados de telemetria no Firestore. */
    public void registrarTelemetria(Telemetria telemetria) {
        executor.execute(() -> {
            db.collection("telemetria")
                .add(telemetria)
                .addOnFailureListener(e -> 
                    EvidenceLogger.registrarEvento("FIREBASE", "Erro ao registrar telemetria: " + e.getMessage())
                );
        });
    }

    /** Busca o ranking das melhores pontuações. */
    public void buscarRanking(RepositoryCallback<List<Partida>> callback) {
        executor.execute(() -> {
            db.collection("partidas")
                    .orderBy("alvosAbatidos", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Partida> todas = queryDocumentSnapshots.toObjects(Partida.class);

                        // Descriptografa todos os registros
                        for (Partida p : todas) {
                            p.setNomeJogador(Cryptography.decrypt(p.getNomeJogador()));
                            p.setPontuacaoFinal(Cryptography.decrypt(p.getPontuacaoFinal()));
                        }

                        // Mantém apenas a melhor partida por jogador (sem repetir nome no ranking)
                        java.util.Map<String, Partida> melhoresPorJogador = new java.util.LinkedHashMap<>();
                        for (Partida p : todas) {
                            if (!melhoresPorJogador.containsKey(p.getUserId())) {
                                melhoresPorJogador.put(p.getUserId(), p);
                            }
                        }

                        // Limita a 5 jogadores únicos
                        List<Partida> top5 = new java.util.ArrayList<>(melhoresPorJogador.values());
                        if (top5.size() > 5) top5 = top5.subList(0, 5);

                        if (callback != null) callback.onSuccess(top5);
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError(e);
                    });
        });
    }

    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}
