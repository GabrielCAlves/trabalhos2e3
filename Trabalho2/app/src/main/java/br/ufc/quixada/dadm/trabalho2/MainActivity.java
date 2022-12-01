package br.ufc.quixada.dadm.trabalho2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.ufc.quixada.dadm.trabalho2.Adapter.ItensAdapter;
import br.ufc.quixada.dadm.trabalho2.Model.ItensDeFeiraModel;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private ItensAdapter adapter;

    private List<ItensDeFeiraModel> listItens;

    private EditText campoId;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private String nome, marca;
    private double preco;
    private int id, quantidade;

    DAOItensDeFeiraModel dao;

    String key = null;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        campoId = findViewById(R.id.editTextId);
        
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dao = new DAOItensDeFeiraModel();

        listItens = new ArrayList<>();

        loadData();

        adapter = new ItensAdapter(MainActivity.this);
        recyclerView.setAdapter(adapter);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result != null){
                            key = result.getData().getExtras().getString("key");
                            nome = result.getData().getExtras().getString("nome");
                            marca = result.getData().getExtras().getString("marca");
                            preco = result.getData().getExtras().getDouble("preco");
                            quantidade = result.getData().getExtras().getInt("quantidade");

                            if(result.getData().getExtras().getString("add_edit_ap").equals("edit")){

                                HashMap<String, Object> hashMap = new HashMap();
                                hashMap.put("nome", nome);
                                hashMap.put("marca", marca);
                                hashMap.put("preco", preco);
                                hashMap.put("quantidade", quantidade);

                                dao.update(key, hashMap).addOnSuccessListener(suc -> {
                                    Toast.makeText(MainActivity.this, "Sucesso em atualizar item da lista.", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(er -> {
                                    Toast.makeText(MainActivity.this, ""+er.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                            }else if(result.getData().getExtras().getString("add_edit_ap").equals("add")){
                                ItensDeFeiraModel itensDeFeiraModel = new ItensDeFeiraModel(listItens.size(), nome, marca, quantidade, preco);

                                dao.add(itensDeFeiraModel).addOnSuccessListener(suc -> {
                                    Toast.makeText(MainActivity.this, "Sucesso em adicionar item a lista.", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(er -> {
                                    Toast.makeText(MainActivity.this, ""+er.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                            }else if(result.getData().getExtras().getString("add_edit_ap").equals("excluir")){

                                dao.remove(key).addOnSuccessListener(suc -> {
                                    Toast.makeText(MainActivity.this, "Sucesso em eliminar item da lista.", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(er -> {
                                    Toast.makeText(MainActivity.this, ""+er.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }

                            if(!result.getData().getExtras().getString("add_edit_ap").equals("cancelar")){
                                loadData();
                            }
                        }
                    }
                });
    }

    private void loadData(){
        dao.get().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listItens = new ArrayList<>();

                id = 0;
                for(DataSnapshot data : snapshot.getChildren()){
                    ItensDeFeiraModel itens = data.getValue(ItensDeFeiraModel.class);
                    itens.setId(id);
                    itens.setKey(data.getKey());
                    listItens.add(itens);

                    ++id;
                }
                adapter.clear();
                adapter.notifyDataSetChanged();
                adapter.setItems(listItens);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void editar(View v){
        if(campoId.getText().toString().isEmpty()){
            Toast.makeText(this, "Preencha o campo ID para editar", Toast.LENGTH_SHORT).show();
        }else if(Integer.parseInt(campoId.getText().toString()) < listItens.size()){
            Intent intent = new Intent(this, TelaEdicaoAdicao.class);

            intent.putExtra("key", listItens.get(Integer.parseInt(campoId.getText().toString())).getKey());
            intent.putExtra("id", Integer.parseInt(campoId.getText().toString()));
            intent.putExtra("nome", listItens.get(Integer.parseInt(campoId.getText().toString())).getNome());
            intent.putExtra("marca", listItens.get(Integer.parseInt(campoId.getText().toString())).getMarca());
            intent.putExtra("preco", listItens.get(Integer.parseInt(campoId.getText().toString())).getPreco());
            intent.putExtra("quantidade", listItens.get(Integer.parseInt(campoId.getText().toString())).getQuantidade());
            intent.putExtra("add_edit_ap", "edit");

            activityResultLauncher.launch(intent);
        }else{
            Toast.makeText(this, "Id inválido", Toast.LENGTH_SHORT).show();
        }
    }

    public void adicionar(View v){
        Intent intent = new Intent(this, TelaEdicaoAdicao.class);
        intent.putExtra("id", listItens.size());
        intent.putExtra("add_edit_ap", "add");
        activityResultLauncher.launch(intent);
    }
}