package br.ufc.quixada.dadm.trabalho2.Model;

import com.google.firebase.database.Exclude;

public class ItensDeFeiraModel {

    @Exclude
    private String key;
    private String userId;
    int id;
    private String imagemUri;
    String nome;
    String marca;
    int quantidade;
    double preco, desconto;

    public ItensDeFeiraModel() {}

    public ItensDeFeiraModel(int id, String key, String userId, String imagemUri, String nome, String marca, int quantidade, double preco, double desconto) {
        this.id = id;
        this.key = key;
        this.userId = userId;
        this.imagemUri = imagemUri;
        this.nome = nome;
        this.marca = marca;
        this.quantidade = quantidade;
        this.preco = preco;
        this.desconto = desconto;
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
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getImagemUri() {
        return imagemUri;
    }
    public void setImagemUri(String imagemUri) {
        this.imagemUri = imagemUri;
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

    public double getDesconto() {
        return desconto;
    }

    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }
}
