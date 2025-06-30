package br.ufc.quixada.dadm.trabalho2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class RecuperarSenhaActivity extends AppCompatActivity {

    Button btnRecSen;
    EditText campoEmailRec;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    String strEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        btnRecSen = findViewById(R.id.buttonRecuperarSenha);
        campoEmailRec = findViewById(R.id.editTextE_mailRecuperarSenha);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        btnRecSen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decision(strEmail);
        }});
    }

    public void decision(String email){
        email = campoEmailRec.getText().toString().trim();
        if(!TextUtils.isEmpty(email)){
            resetarSenha(email);
        }else{
            campoEmailRec.setError("Este campo não deve ficar vazio");
        }
    }

    public void resetarSenha(String email){
        progressBar.setVisibility(View.VISIBLE);
        btnRecSen.setVisibility(View.INVISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        finalizar(true);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finalizar(false);
                    }
                });
    }

    public boolean finalizar(Boolean success){
        if(success){
            showMessage("Um link para alterar sua senha foi mandado para o e-mail registrado");
            finish();
            return true;
        } else{
            progressBar.setVisibility(View.INVISIBLE);
            btnRecSen.setVisibility(View.VISIBLE);
            return false;
        }
    }

    public void showMessage(String message){
        Toast.makeText(RecuperarSenhaActivity.this, message, Toast.LENGTH_LONG).show();
    }

    public void voltarRecuperarSenha(View v) {
        finish();
    }
}