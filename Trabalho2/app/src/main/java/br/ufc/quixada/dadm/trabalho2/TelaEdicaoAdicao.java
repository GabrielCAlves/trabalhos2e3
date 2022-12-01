package br.ufc.quixada.dadm.trabalho2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TelaEdicaoAdicao extends AppCompatActivity {

    private EditText campoNomeItem, campoMarca, campoPreco, campoQuantidade;
    private TextView campoId, campoTotal;

    private String key, nomeItem, marca;
    private int id, quantidade;
    private double preco, total;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_edicao_adicao);

        campoId = findViewById(R.id.textViewId);
        campoNomeItem = findViewById(R.id.editTextNomeItem);
        campoMarca = findViewById(R.id.editTextMarca);
        campoPreco = findViewById(R.id.editTextPreco);
        campoQuantidade = findViewById(R.id.editTextQuantidade);
        campoTotal = findViewById(R.id.textViewTotalValor);

        Bundle extras = getIntent().getExtras();

        if(extras != null){
            key = getIntent().getStringExtra("key");
            id = getIntent().getIntExtra("id", 0);
            nomeItem = getIntent().getStringExtra("nome");
            marca = getIntent().getStringExtra("marca");
            preco = getIntent().getDoubleExtra("preco", 0);
            quantidade = getIntent().getIntExtra("quantidade", 0);
            total = preco*quantidade;

            campoId.setText(String.valueOf(id));
            campoNomeItem.setText(nomeItem);
            campoMarca.setText(marca);
            campoPreco.setText(String.valueOf(preco));
            campoQuantidade.setText(String.valueOf(quantidade));
            campoTotal.setText(String.format("%.2f",total));
        }

    }

    @SuppressLint("DefaultLocale")
    public void calcular(View v){
        preco = Double.parseDouble(campoPreco.getText().toString());
        quantidade = Integer.parseInt(campoQuantidade.getText().toString());

        if(limites()){
            campoTotal.setText(String.format("%.2f", preco*quantidade));
        }
    }

    public void atualizar(View v){
        if(getIntent().getStringExtra("add_edit_ap").equals("edit")){

            nomeItem = campoNomeItem.getText().toString();
            marca = campoMarca.getText().toString();
            preco = Double.parseDouble(campoPreco.getText().toString());
            quantidade = Integer.parseInt(campoQuantidade.getText().toString());

            if(limites()){
                Intent data = getIntent();

                data.putExtra("key", key);
                data.putExtra("nome", nomeItem);
                data.putExtra("marca", marca);
                data.putExtra("preco", preco);
                data.putExtra("quantidade", quantidade);
                data.putExtra("add_edit_ap", "edit");

                setResult(1, data);
                finish();
            }
        }else{
            Toast.makeText(this, "Não é possível atualizar ou excluir itens que ainda não pertencem a lista", Toast.LENGTH_SHORT).show();
        }
    }

    public void adicionar(View v){
        nomeItem = campoNomeItem.getText().toString();
        marca = campoMarca.getText().toString();
        preco = Double.parseDouble(campoPreco.getText().toString());
        quantidade = Integer.parseInt(campoQuantidade.getText().toString());

        if(limites()){
            Intent data = getIntent();
            data.putExtra("nome", nomeItem);
            data.putExtra("marca", marca);
            data.putExtra("preco", preco);
            data.putExtra("quantidade", quantidade);
            data.putExtra("add_edit_ap", "add");

            setResult(RESULT_OK, data);
            finish();
        }
    }

    public void apagar(View v){
        String acao = getIntent().getStringExtra("add_edit_ap");
        if(acao.equals("add")){
            Toast.makeText(this, "Não é possível excluir itens que ainda não pertencem a lista", Toast.LENGTH_SHORT).show();
        }else{
            Intent data = getIntent();
            data.putExtra("key", key);
            data.putExtra("add_edit_ap", "excluir");
            setResult(RESULT_OK, data);
            finish();
        }

    }

    public void cancelar(View v){
        Intent data = getIntent();
        data.putExtra("add_edit_ap", "cancelar");
        setResult(RESULT_CANCELED, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent data = getIntent();
        data.putExtra("add_edit_ap", "cancelar");
        setResult(RESULT_CANCELED, data);
        finish();
    }

    public boolean limites(){
        String precoS = String.valueOf(preco);
        int contagem = 0;
        int posicaoPonto = 99;

        if(precoS.contains(".")){
            posicaoPonto = precoS.indexOf(".");
        }

        for(int i = 0; i < precoS.length(); ++i){
            if(i > posicaoPonto){
                ++contagem;
            }
        }

        if(preco > 9999.99 || contagem > 2){
            Toast.makeText(this, "Por favor, limite-se a um preço máximo de 4 dígitos e 2 decimais", Toast.LENGTH_SHORT).show();
            return false;
        }else if(quantidade > 999999){
            Toast.makeText(this, "Por favor, limite-se a um valor máximo de 6 dígitos", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}