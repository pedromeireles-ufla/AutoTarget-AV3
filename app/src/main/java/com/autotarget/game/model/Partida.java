package com.autotarget.game.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Modelo de dados para armazenar o histórico de jogos no Firebase Firestore.
 * Atende ao requisito 6.3.2 a) da AV3.
 */
public class Partida {
    private String userId;
    private String nomeJogador; // Será criptografado
    private String pontuacaoFinal; // Será criptografado
    private int alvosAbatidos;
    private int canhoesUtilizados;
    private String dadosCriptografados; // JSON criptografado com nome e pontuação
    
    @ServerTimestamp
    private Date timestamp;

    public String getDadosCriptografados() { return dadosCriptografados; }
    public void setDadosCriptografados(String dadosCriptografados) { this.dadosCriptografados = dadosCriptografados; }
    public Partida() {
        // Construtor vazio necessário para o Firestore
    }

    public Partida(String userId, String nomeJogador, String pontuacaoFinal, int alvosAbatidos, int canhoesUtilizados) {
        this.userId = userId;
        this.nomeJogador = nomeJogador;
        this.pontuacaoFinal = pontuacaoFinal;
        this.alvosAbatidos = alvosAbatidos;
        this.canhoesUtilizados = canhoesUtilizados;
    }

    // Getters e Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNomeJogador() { return nomeJogador; }
    public void setNomeJogador(String nomeJogador) { this.nomeJogador = nomeJogador; }

    public String getPontuacaoFinal() { return pontuacaoFinal; }
    public void setPontuacaoFinal(String pontuacaoFinal) { this.pontuacaoFinal = pontuacaoFinal; }

    public int getAlvosAbatidos() { return alvosAbatidos; }
    public void setAlvosAbatidos(int alvosAbatidos) { this.alvosAbatidos = alvosAbatidos; }

    public int getCanhoesUtilizados() { return canhoesUtilizados; }
    public void setCanhoesUtilizados(int canhoesUtilizados) { this.canhoesUtilizados = canhoesUtilizados; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
