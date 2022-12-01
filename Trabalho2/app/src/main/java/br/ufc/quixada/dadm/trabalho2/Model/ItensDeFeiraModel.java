package br.ufc.quixada.dadm.trabalho2.Model;

import com.google.firebase.database.Exclude;

public class ItensDeFeiraModel {

    @Exclude
    private String key;
    int id;
    String nome;
    String marca;
    int quantidade;
    double preco;

    public ItensDeFeiraModel() {}

    public ItensDeFeiraModel(int id, String nome, String marca, int quantidade, double preco) {
        this.id = id;
        this.nome = nome;
        this.marca = marca;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }
}
