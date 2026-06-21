package com.autotarget.game.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.autotarget.game.R;
import com.autotarget.game.model.Partida;
import com.autotarget.game.util.FirebaseRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RankingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvCarregando;
    private FirebaseRepository firebaseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        // Botão de voltar
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerRanking);
        tvCarregando = findViewById(R.id.tvCarregando);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseRepository = FirebaseRepository.getInstance();

        firebaseRepository.buscarRanking(new FirebaseRepository.RepositoryCallback<List<Partida>>() {
            @Override
            public void onSuccess(List<Partida> partidas) {
                runOnUiThread(() -> {
                    tvCarregando.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(new RankingAdapter(partidas));
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    tvCarregando.setText("Erro ao carregar ranking.");
                    Toast.makeText(RankingActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    static class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {
        private final List<Partida> partidas;
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());

        RankingAdapter(List<Partida> partidas) {
            this.partidas = partidas != null ? partidas : new ArrayList<>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ranking, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Partida p = partidas.get(position);
            holder.tvPosicao.setText("#" + (position + 1));
            holder.tvNome.setText(p.getNomeJogador());
            holder.tvAlvos.setText(p.getAlvosAbatidos() + " abates");
            holder.tvData.setText(p.getTimestamp() != null ? sdf.format(p.getTimestamp()) : "");
        }

        @Override
        public int getItemCount() { return partidas.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPosicao, tvNome, tvAlvos, tvData;
            ViewHolder(View v) {
                super(v);
                tvPosicao = v.findViewById(R.id.tvPosicao);
                tvNome    = v.findViewById(R.id.tvNome);
                tvAlvos   = v.findViewById(R.id.tvAlvos);
                tvData    = v.findViewById(R.id.tvData);
            }
        }
    }
}
