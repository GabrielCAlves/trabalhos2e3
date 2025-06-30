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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.widget.SearchView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import br.ufc.quixada.dadm.trabalho2.Adapter.ItensAdapter;
import br.ufc.quixada.dadm.trabalho2.Model.ItensDeFeiraModel;
import br.ufc.quixada.dadm.trabalho2.Util.ConfiguraBD;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private ItensAdapter adapter;

    private List<ItensDeFeiraModel> listItens;

    //private EditText campoId;
    private SearchView searchView;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private String imageStr;
    Uri image;
    private String nome, marca;
    private double preco, desconto;
    private int id, quantidade;

    DAOItensDeFeiraModel dao;

    String key = null;

    public String userId;

    private FirebaseAuth auth;

    private Intent intentMA;

    private SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
        auth = ConfiguraBD.FirebaseAutenticacao();

        //Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
//
        //campoId = findViewById(R.id.editTextId);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        searchView = findViewById(R.id.searchView);
        configurarSearchView();

        // Configuração adicional para garantir que o hint apareça
        /*searchView.setQueryHint("Procurar por nome ou marca...");
        searchView.setIconified(false); // Garante que não fique no modo ícone
        searchView.clearFocus(); // Remove o foco inicial para mostrar o hint

        new Handler().postDelayed(() -> {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }, 200);*/

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //dao = new DAOItensDeFeiraModel();
        userId = auth.getCurrentUser().getUid();
        dao = new DAOItensDeFeiraModel(userId);

        listItens = new ArrayList<>();

        loadData();

        adapter = new ItensAdapter(MainActivity.this);

        // Configura o adapter para receber cliques
        adapter.setOnItemClickListener(item -> {
            abrirTelaEdicao(item);
        });

        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Configura a pesquisa
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarItens(newText);
                return true;
            }
        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            Bundle extras = data.getExtras();

                            if(extras != null) {
                                key = extras.getString("key");
                                userId = extras.getString("userId");
                                nome = extras.getString("nome");
                                marca = extras.getString("marca");
                                preco = extras.getDouble("preco");
                                quantidade = extras.getInt("quantidade");
                                desconto = extras.getDouble("desconto");
                                imageStr = extras.getString("imagem");

                                System.out.println("imageStr = "+imageStr);

                                if(imageStr != null){
                                    image = (imageStr != null) ? Uri.parse(imageStr) : null;
                                }

                                String action = extras.getString("add_edit_ap");

                                if(action != null) {
                                    if(action.equals("edit")){
                                        HashMap<String, Object> hashMap = new HashMap();
                                        hashMap.put("imagem", imageStr);
                                        hashMap.put("nome", nome);
                                        hashMap.put("marca", marca);
                                        hashMap.put("preco", preco);
                                        hashMap.put("quantidade", quantidade);
                                        hashMap.put("desconto", desconto);

                                        dao.update(key, hashMap).addOnSuccessListener(suc -> {
                                            Toast.makeText(MainActivity.this, "Sucesso em atualizar item da lista.", Toast.LENGTH_SHORT).show();

                                            if (imageStr != null) {
                                                FirebaseStorage.getInstance().getReferenceFromUrl(imageStr)
                                                        .getDownloadUrl()
                                                        .addOnSuccessListener(uri -> {
                                                            // Atualizar o item na lista local
                                                            for (ItensDeFeiraModel item : listItens) {
                                                                if (item.getKey().equals(key)) {
                                                                    item.setImagemUri(uri.toString());
                                                                    break;
                                                                }
                                                            }
                                                            // Forçar recarregamento dos dados
                                                            //loadData(); // Força a atualização da View
                                                            //finish();
                                                            //startActivity(intentMA);
                                                            //finish();
                                                            Toast.makeText(MainActivity.this, "Item atualizado", Toast.LENGTH_SHORT).show();
                                                        });
                                            }

                                        }).addOnFailureListener(er -> {
                                            Toast.makeText(MainActivity.this, ""+er.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                                    }else if(action.equals("add")){
                                        ItensDeFeiraModel itensDeFeiraModel = new ItensDeFeiraModel(listItens.size(), key, userId, imageStr, nome, marca, quantidade, preco, desconto);

                                        dao.add(itensDeFeiraModel).addOnSuccessListener(suc -> {
                                            Toast.makeText(MainActivity.this, "Sucesso em adicionar item a lista.", Toast.LENGTH_SHORT).show();
                                        }).addOnFailureListener(er -> {
                                            Toast.makeText(MainActivity.this, ""+er.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                                    }else if(action.equals("excluir")){
                                        dao.remove(key).addOnSuccessListener(suc -> {
                                            Toast.makeText(MainActivity.this, "Sucesso em eliminar item da lista.", Toast.LENGTH_SHORT).show();
                                        }).addOnFailureListener(er -> {
                                            Toast.makeText(MainActivity.this, ""+er.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    if(!action.equals("cancelar")){
                                        loadData();
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void loadData() {
        dao.get().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listItens = new ArrayList<>();

                // 1. Primeiro, carrega todos os itens sem ID
                for (DataSnapshot data : snapshot.getChildren()) {
                    ItensDeFeiraModel item = data.getValue(ItensDeFeiraModel.class);
                    item.setKey(data.getKey()); // Mantém a chave do Firebase
                    listItens.add(item);
                }

                // 2. Ordena alfabeticamente (A-Z)
                Collections.sort(listItens, new Comparator<ItensDeFeiraModel>() {
                    @Override
                    public int compare(ItensDeFeiraModel item1, ItensDeFeiraModel item2) {
                        return item1.getNome().compareToIgnoreCase(item2.getNome());
                    }
                });

                // 3. Agora atribui os IDs em ordem sequencial
                for (int i = 0; i < listItens.size(); i++) {
                    listItens.get(i).setId(i+1); // IDs começam em 0 e vão em ordem
                }

                // 4. Atualiza o adapter
                adapter.clear();
                adapter.notifyDataSetChanged();
                adapter.setItems(listItens);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Falha ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adicione este método no seu MainActivity
    private void configurarSearchView() {
        //SearchView searchView = findViewById(R.id.searchView);

        // 1. Alterar cor do hint (texto de busca)
        try {
            // Obtém o EditText interno do SearchView
            int searchTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
            EditText searchEditText = searchView.findViewById(searchTextId);

            // Define a cor do hint (cinza)
            searchEditText.setHintTextColor(getResources().getColor(R.color.gray));

            // Define a cor do texto digitado (preto)
            searchEditText.setTextColor(getResources().getColor(R.color.black));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Alterar cor dos ícones (opcional)
        try {
            // Ícone de busca
            int searchIconId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
            ImageView searchIcon = searchView.findViewById(searchIconId);
            searchIcon.setColorFilter(getResources().getColor(R.color.black));

            // Ícone de fechar (X)
            int closeIconId = getResources().getIdentifier("android:id/search_close_btn", null, null);
            ImageView closeIcon = searchView.findViewById(closeIconId);
            closeIcon.setColorFilter(getResources().getColor(R.color.black));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void filtrarItens(String texto) {
        List<ItensDeFeiraModel> listaFiltrada = new ArrayList<>();
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listItens); // Mostra todos quando a pesquisa está vazia
        } else {
            for (ItensDeFeiraModel item : listItens) {
                if (item.getNome().toLowerCase().contains(texto.toLowerCase()) ||
                        (item.getMarca() != null && item.getMarca().toLowerCase().contains(texto.toLowerCase()))) {
                    listaFiltrada.add(item);
                }
            }
        }
        adapter.clear();
        adapter.notifyDataSetChanged();
        adapter.setItems(listaFiltrada);
        adapter.notifyDataSetChanged();
    }

    public void abrirTelaEdicao(ItensDeFeiraModel item){
        Intent intent = new Intent(this, TelaEdicaoAdicao.class);
        intent.putExtra("key", item.getKey());
        intent.putExtra("id", item.getId());
        intent.putExtra("userId", item.getUserId());
        intent.putExtra("imagem", item.getImagemUri());
        intent.putExtra("nome", item.getNome());
        intent.putExtra("marca", item.getMarca());
        intent.putExtra("preco", item.getPreco());
        intent.putExtra("quantidade", item.getQuantidade());
        intent.putExtra("desconto", item.getDesconto());
        intent.putExtra("add_edit_ap", "edit");

        activityResultLauncher.launch(intent);
    }

    public void adicionar(View v){
        Intent intent = new Intent(this, TelaEdicaoAdicao.class);
        intent.putExtra("id", listItens.size());
        intent.putExtra("userId", auth.getCurrentUser().getUid());
        intent.putExtra("add_edit_ap", "add");
        activityResultLauncher.launch(intent);
    }

    public void deslogar(View v){
        try{
            auth.signOut();
            finish();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void abrirPerfil(View v) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivity(intent);
    }
}