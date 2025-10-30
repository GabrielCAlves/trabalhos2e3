package br.ufc.quixada.dadm.trabalho2;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import br.ufc.quixada.dadm.trabalho2.databinding.ActivityUserProfileBinding;
import br.ufc.quixada.dadm.trabalho2.service.ImageUploadService;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "ViewDatabase";

    private String nome, email, senha;
    private String emailU, senhaU;

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private StorageReference storageReference;

    private ImageUploadService uploadService;

    private String profileImageUrl; // ADICIONE ESTA VARIÁVEL

    private String key;

    SharedPreferences sp;
    TextView campoNomeProfile;
    TextInputEditText campoNome, campoEmail, campoSenha;

    ImageView photo;
    ActivityUserProfileBinding binding;

    Uri imageUri;

    static boolean valido;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        photo = findViewById(R.id.image_profile);

        campoNomeProfile = findViewById(R.id.textView_NomeProfile);
        campoNome = findViewById(R.id.editTextNome);
        campoEmail = findViewById(R.id.editTextEmail);
        campoSenha = findViewById(R.id.editTextSenha);

        uploadService = new ImageUploadService(this);

        getUserData();

    }

    private void getUserData(){
        Toast.makeText(this, "Carregando seus dados...", Toast.LENGTH_SHORT).show();

        key = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Usuario").document(key).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();

                            if(documentSnapshot != null && documentSnapshot.exists()){
                                // NOVO: Obter URL da imagem se existir
                                String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                                nome = documentSnapshot.getString("nome");
                                email = documentSnapshot.getString("email");
                                senha = documentSnapshot.getString("senha");

                                campoNomeProfile.setText(nome);
                                campoNome.setText(nome);
                                campoEmail.setText(email);
                                campoSenha.setText(senha);

                                // CARREGA A IMAGEM DIRETAMENTE - REMOVA A CHAMADA DUPLICADA
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    loadImageFromUrl(profileImageUrl);
                                } else {
                                    setDefaultProfileImage();
                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfileActivity.this, "Erro: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        setDefaultProfileImage(); // Define imagem padrão em caso de erro
                    }
                });
    }

    /*private void loadUserProfileImage() {
        // Verifica se temos uma URL de imagem salva no Firestore
        // Primeiro, precisamos buscar o documento do usuário para obter a URL

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Usuario").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Tenta obter a URL da imagem do perfil
                                String profileImageUrl = document.getString("profileImageUrl");

                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    // Carrega a imagem da URL usando Picasso
                                    loadImageFromUrl(profileImageUrl);
                                } else {
                                    // Se não há URL, usa imagem padrão
                                    setDefaultProfileImage();
                                    Log.d("UserProfile", "Usuário não tem imagem de perfil salva");
                                }
                            } else {
                                setDefaultProfileImage();
                            }
                        } else {
                            setDefaultProfileImage();
                            Log.e("UserProfile", "Erro ao buscar dados do usuário: " + task.getException());
                        }
                    }
                });
    }*/

    private void loadImageFromUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_img_profile)
                    .error(R.drawable.ic_img_profile)
                    .into(photo, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Quando a imagem carrega com sucesso, torna-a circular
                            makeImageCircular();
                            Log.d("UserProfile", "Imagem de perfil carregada com sucesso");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("UserProfile", "Erro ao carregar imagem: " + e.getMessage());
                            setDefaultProfileImage();
                        }
                    });
        } else {
            setDefaultProfileImage();
        }
    }

    private void makeImageCircular() {
        // Espera a imagem ser carregada e então a torna circular
        photo.post(new Runnable() {
            @Override
            public void run() {
                try {
                    BitmapDrawable drawable = (BitmapDrawable) photo.getDrawable();
                    if (drawable != null) {
                        Bitmap bitmap = drawable.getBitmap();
                        if (bitmap != null) {
                            RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                            roundedDrawable.setCircular(true);
                            roundedDrawable.setAntiAlias(true);
                            photo.setImageDrawable(roundedDrawable);
                        }
                    }
                } catch (Exception e) {
                    Log.e("UserProfile", "Erro ao tornar imagem circular: " + e.getMessage());
                }
            }
        });
    }

    private void setDefaultProfileImage() {
        // Define uma imagem padrão circular
        try {
            Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_img_profile);
            RoundedBitmapDrawable circularDrawable = RoundedBitmapDrawableFactory.create(getResources(), defaultBitmap);
            circularDrawable.setCircular(true);
            circularDrawable.setAntiAlias(true);
            photo.setImageDrawable(circularDrawable);
        } catch (Exception e) {
            Log.e("UserProfile", "Erro ao definir imagem padrão: " + e.getMessage());
            photo.setImageResource(R.drawable.ic_img_profile);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void mudarFoto(View v){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(checkAndRequestPermissions(UserProfileActivity.this, UserProfileActivity.this)){
                chooseImage(UserProfileActivity.this);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static boolean checkAndRequestPermissions(Context context, Activity activity) {
        List<String> listPermissionsNeeded = new ArrayList<>();

        // Permissões para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Permissões para versões anteriores
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        // Permissão da câmera (sempre necessária)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                chooseImage(UserProfileActivity.this);
            } else {
                Toast.makeText(UserProfileActivity.this, "Permissões necessárias foram negadas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void chooseImage(Context context){
        final CharSequence[] optionsMenu = {"Tirar Foto", "Escolher na Galeria", "Sair" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setItems(optionsMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(optionsMenu[i].equals("Tirar Foto")){

                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                }
                else if(optionsMenu[i].equals("Escolher na Galeria")){

                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);
                }
                else if (optionsMenu[i].equals("Sair")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    // ATUALIZE O onActivityResult PARA SALVAR A URL:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0: // Câmera
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");

                        // Mostra a imagem imediatamente
                        photo.setImageBitmap(selectedImage);

                        // Gera um nome único para o arquivo
                        String fileName = "profile_" + key + "_" + System.currentTimeMillis() + ".jpg";

                        uploadService.uploadImage(selectedImage, new ImageUploadService.UploadCallback() {
                            @Override
                            public void onSuccess(String imageUrl) {
                                runOnUiThread(() -> {
                                    Toast.makeText(UserProfileActivity.this, "Foto de perfil salva!", Toast.LENGTH_SHORT).show();
                                    // SALVA A URL NO FIRESTORE E ATUALIZA A IMAGEM
                                    saveProfileImageUrl(imageUrl);
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(UserProfileActivity.this, "Erro: " + error, Toast.LENGTH_LONG).show();
                                    setDefaultProfileImage(); // Volta para imagem padrão em caso de erro
                                });
                            }
                        });
                    }
                    break;

                case 1: // Galeria
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();

                        // Mostra a imagem imediatamente
                        photo.setImageURI(selectedImage);

                        // Gera um nome único para o arquivo
                        String fileName = "profile_" + key + "_" + System.currentTimeMillis() + ".jpg";

                        uploadService.uploadImage(selectedImage, new ImageUploadService.UploadCallback() {
                            @Override
                            public void onSuccess(String imageUrl) {
                                runOnUiThread(() -> {
                                    Toast.makeText(UserProfileActivity.this, "Foto de perfil salva!", Toast.LENGTH_SHORT).show();
                                    // SALVA A URL NO FIRESTORE E ATUALIZA A IMAGEM
                                    saveProfileImageUrl(imageUrl);
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(UserProfileActivity.this, "Erro: " + error, Toast.LENGTH_LONG).show();
                                    setDefaultProfileImage(); // Volta para imagem padrão em caso de erro
                                });
                            }
                        });
                    }
                    break;
            }
        }
    }

    // ADICIONE ESTE MÉTODO PARA SALVAR A URL NO FIRESTORE:
    private void saveProfileImageUrl(String imageUrl) {
        db.collection("Usuario").document(key)
                .update("profileImageUrl", imageUrl)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("UserProfile", "URL da imagem salva no Firestore: " + imageUrl);
                            // Atualiza a variável local e recarrega a imagem
                            profileImageUrl = imageUrl;
                            loadImageFromUrl(imageUrl);
                        } else {
                            Log.e("UserProfile", "Erro ao salvar URL: " + task.getException());
                            Toast.makeText(UserProfileActivity.this, "Erro ao salvar URL da imagem", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif  = null;
            try {
                exif = new ExifInterface(imagePath);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 90;
                    break;
                default:
                    rotate = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public void update(View v) {
        nome = Objects.requireNonNull(campoNome.getText()).toString();
        emailU = Objects.requireNonNull(campoEmail.getText()).toString();

        if (verEmail()){
            if (verNome()) {

                updateEmail();

                db.collection("Usuario").document(key)
                        .update("nome", nome, "email", emailU)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(UserProfileActivity.this, "Dados atualizados", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserProfileActivity.this, "Erro: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        }
    }

    private void updateEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updateEmail(emailU).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(UserProfileActivity.this, "Email atualizado com sucesso", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(UserProfileActivity.this, "Ocorreu um erro em atualizar o email, faça login novamente e tente de novo.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(UserProfileActivity.this, "Task: "+task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateSenha(View view){
        senhaU = Objects.requireNonNull(campoSenha.getText()).toString();

        Toast.makeText(this, "Senha: "+senhaU, Toast.LENGTH_SHORT).show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updatePassword(senhaU).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if(task.isSuccessful()){

                    atualizarSenhaNoBanco();

                    Toast.makeText(UserProfileActivity.this, "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(UserProfileActivity.this, "Ocorreu um erro ao atualizar a senha, tente fazer login de novo e tente novamente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void atualizarSenhaNoBanco(){
        db.collection("Usuario").document(key)
                .update("senha", senhaU)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(UserProfileActivity.this, "Banco de dados atualizado", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfileActivity.this, "Erro: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void delete(View v) {

        db.collection("Usuario").document(key)
                        .delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(UserProfileActivity.this, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserProfileActivity.this, "Erro: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("Usuario")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .setValue(null)
                    .addOnSuccessListener(new OnSuccessListener<Void>(){
                        @Override
                        public void onSuccess(Void unused) {
                            FirebaseAuth.getInstance().getCurrentUser().delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.d(TAG, "Conta deletada com sucesso");

                                                Intent intent= new Intent(UserProfileActivity.this, LoginActivity.class);
                                                startActivity(intent);

                                                finish();
                                            }else{
                                                Log.d(TAG, "Erro em deletar");
                                            }
                                        }
                                    });
                        }
                    });

    }

    private boolean verEmail() {

        if (emailU.equals(email)) {
            valido = true;
        } else {
            if(conteudoEmail()){
                valido = true;
            }else{
                Toast.makeText(UserProfileActivity.this, "Email inválido", Toast.LENGTH_SHORT).show();
                valido = false;
            }
        }

        return valido;
    }

    private boolean conteudoEmail(){
        int numTiposContas = 0;

        if(emailU.contains("@gmail.com")){
            ++numTiposContas;
        }

        if(emailU.contains("@alu.ufc.br")){
            ++numTiposContas;
        }

        if(emailU.contains("@hotmail.com")){
            ++numTiposContas;
        }

        if(numTiposContas > 1 || emailU.contains(" ")){
            return false;
        }else{
            StringBuilder ultimasLetras = new StringBuilder();

            for(int i = emailU.length() - 12; i < emailU.length(); ++i){
                char ch = emailU.charAt(i);
                ultimasLetras.append(ch);
            }

            return ultimasLetras.toString().contains("@gmail.com") || ultimasLetras.toString().contains("@alu.ufc.br") || ultimasLetras.toString().contains("@hotmail.com");
        }
    }

    private boolean verNome() {
        for(int i = 0; i < nome.length(); ++i){
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

    public void voltarUserProfile(View v) {
        finish();
    }
}