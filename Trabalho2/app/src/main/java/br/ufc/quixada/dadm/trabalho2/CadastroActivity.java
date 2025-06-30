package br.ufc.quixada.dadm.trabalho2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import br.ufc.quixada.dadm.trabalho2.Util.ConfiguraBD;
import br.ufc.quixada.dadm.trabalho2.dao.DAOUsuario;
import br.ufc.quixada.dadm.trabalho2.Model.Usuario;
import br.ufc.quixada.dadm.trabalho2.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CadastroActivity extends AppCompatActivity {

    Usuario usuario;
    FirebaseAuth autenticacao;
    EditText campoNome, campoEmail, campoSenha, campoConfSenha;

    DAOUsuario dao;

    FirebaseFirestore db;

    String nome, email, senha, confSenha;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        dao = new DAOUsuario();

        db = FirebaseFirestore.getInstance();

        campoNome = findViewById(R.id.editTextNome);
        campoEmail = findViewById(R.id.editTextE_mail);
        campoSenha = findViewById(R.id.editTextSenha);
        campoConfSenha = findViewById(R.id.editTextConfirmarSenha);
    }

    public void validarCampos(View v){
        nome = campoNome.getText().toString();
        email = campoEmail.getText().toString();
        senha = campoSenha.getText().toString();
        confSenha = campoConfSenha.getText().toString();

        if(verNome()){
            if(!email.isEmpty()){
                if(senha.length() >= 8){
                    if(!confSenha.equals(senha)){
                        Toast.makeText(this, "Preencha o campo Confirme sua senha corretamente", Toast.LENGTH_SHORT).show();
                    }else{
                        usuario = new Usuario();
                        usuario.setNome(nome);
                        usuario.setEmail(email);
                        usuario.setSenha(senha);

                        cadastrarUsuario();
                    }
                }else{
                    Toast.makeText(this, "Preencha o campo Senha com no mínimo 8 dígitos e no máximo 16", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Preencha o campo E-mail", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Preencha o campo Nome", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean verNome() {
        for(int i = 0; i < nome.length(); ++i) {
            char ch = nome.charAt(i);

            if(i < nome.length()-1){
                char ch1 = nome.charAt(i + 1);

                if (ch == ' ' && ch1 == ' ') {
                    return false;
                }
            }
        }

        if (nome.length() == 0) {
            return false;
        } else {
            for (int i = 0; i < nome.length(); ++i) {
                char ch = nome.charAt(i);

                if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= 'á' && ch <= 'ú') || (ch >= 'Á' && ch <= 'Ú') || ch == 'ã' || ch == 'õ' || ch == ' ') || (i == 0 && ch == ' ')) {
                    Toast.makeText(this, "Preencha o campo Nome apropriadamente (Verifique espaços indesejados)", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    private void cadastrarUsuario() {
        autenticacao = ConfiguraBD.FirebaseAutenticacao();

        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CadastroActivity.this, "Sucesso ao cadastrar o usuário", Toast.LENGTH_SHORT).show();

                    FirebaseUser user = autenticacao.getCurrentUser();
                    updateUI(user);

                }else{
                    String excecao;

                    try{
                        throw Objects.requireNonNull(task.getException());
                    }catch(FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte";
                    }catch(FirebaseAuthInvalidCredentialsException e){
                        excecao = "Digite um email válido";
                    }catch(FirebaseAuthUserCollisionException e){
                        excecao = "Esta conta já existe";
                    }catch(Exception e){
                        excecao = "Erro ao cadastrar usuário " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {

            Map<String, Object> doc = new HashMap<>();
            doc.put("id", user.getUid());
            doc.put("nome", usuario.getNome());
            doc.put("email", usuario.getEmail());
            doc.put("senha", usuario.getSenha());

            db.collection("Usuario").document(user.getUid()).set(doc)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(CadastroActivity.this, "Dados armazenados...", Toast.LENGTH_SHORT).show();
                                    abrirHome();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CadastroActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                    });
        } else {
            Toast.makeText(this, "Usuário nulo", Toast.LENGTH_SHORT).show();
        }


    }

    private void abrirHome() {
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);

        finish();
    }

    public void voltarCadastro(View v){
        finish();
    }

}