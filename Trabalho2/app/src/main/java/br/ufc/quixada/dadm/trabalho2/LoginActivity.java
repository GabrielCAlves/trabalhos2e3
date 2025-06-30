package br.ufc.quixada.dadm.trabalho2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import br.ufc.quixada.dadm.trabalho2.Util.ConfiguraBD;
import br.ufc.quixada.dadm.trabalho2.Model.Usuario;
import br.ufc.quixada.dadm.trabalho2.MainActivity;
import br.ufc.quixada.dadm.trabalho2.RecuperarSenhaActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText campoEmail, campoSenha;
    Button botaoAcessar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = ConfiguraBD.FirebaseAutenticacao();
        inicializarComponentes();
    }

    public void validarAutenticacao(View v){
        String email = campoEmail.getText().toString();
        String senha = campoSenha.getText().toString();
        
        if(!email.isEmpty()){
            if(!senha.isEmpty()){
                Usuario usuario = new Usuario();
                usuario.setEmail(email);
                usuario.setSenha(senha);

                logar(usuario);

            }else{
                Toast.makeText(this, "Preencha o campo Senha", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Preencha o campo Email", Toast.LENGTH_SHORT).show();
        }
    }

    private void logar(Usuario usuario) {
        auth.signInWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();
                    updateUI(user);
                }else{
                    String excecao;

                    try{
                        throw Objects.requireNonNull(task.getException());
                    }catch(FirebaseAuthInvalidUserException e){
                        excecao = "Usuário não cadastrado";
                    }catch(FirebaseAuthInvalidCredentialsException e){
                        excecao = "Email ou senha incorreto";
                    }catch(Exception e){
                        excecao = "Erro ao logar o usuário " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    public void updateUI(FirebaseUser account){

        if(account != null){
            Toast.makeText(this,"Login realizado com sucesso",Toast.LENGTH_LONG).show();
            abrirHome();
        }else {
            Toast.makeText(this,"Login não realizado",Toast.LENGTH_LONG).show();
        }

    }

    private void abrirHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void cadastrar(View v){
        Intent intent = new Intent(this, CadastroActivity.class);
        startActivity(intent);
    }

    public void recuperarSenha(View v){
        Intent intent = new Intent(this, RecuperarSenhaActivity.class);
        this.startActivity(intent);
    }

    protected void onStart(){
        super.onStart();
        FirebaseUser usuarioAuth = auth.getCurrentUser();
        if(usuarioAuth != null){
            abrirHome();
        }
    }

    private void inicializarComponentes(){
        campoEmail = findViewById(R.id.editTextE_mailLogin);
        campoSenha = findViewById(R.id.editTextSenhaLogin);
        botaoAcessar = findViewById(R.id.buttonAcessar);
    }

}