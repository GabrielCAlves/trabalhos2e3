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

import br.ufc.quixada.dadm.trabalho2.databinding.ActivityUserProfileBinding;

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
                                nome = documentSnapshot.getString("nome");
                                email = documentSnapshot.getString("email");
                                senha = documentSnapshot.getString("senha");

                                campoNomeProfile.setText(documentSnapshot.getString("nome"));
                                campoNome.setText(documentSnapshot.getString("nome"));
                                campoEmail.setText(documentSnapshot.getString("email"));
                                campoSenha.setText(documentSnapshot.getString("senha"));

                                try {
                                    getUserProfile();
                                } catch (IOException e) {
                                    Toast.makeText(UserProfileActivity.this, "Usuário sem imagem ou com falha em carregar. No segundo caso, erro: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserProfileActivity.this, "Erro: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void getUserProfile() throws IOException {
        storageReference = FirebaseStorage.getInstance().getReference("Usuario/"+key+".jpg");

        File localFile = File.createTempFile("tempImage", "jpg");

        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                binding.imageProfile.setImageBitmap(bitmap);
                binding.imageProfile.setRotation(getCameraPhotoOrientation(localFile.getAbsolutePath()));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserProfileActivity.this, "Falha em carregar imagem", Toast.LENGTH_SHORT).show();
            }
        });
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

        if(checkAndRequestPermissions(UserProfileActivity.this)){
            chooseImage(UserProfileActivity.this);
        }

    }

    public static boolean checkAndRequestPermissions(final Activity context) {
        /*int WExtstorePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);*/
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        /*if (WExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int readImagesPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES);
            if (readImagesPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            int readStoragePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (ContextCompat.checkSelfPermission(UserProfileActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                                    "É necessário permição para usar a câmera.", Toast.LENGTH_SHORT)
                            .show();
                } else if (ContextCompat.checkSelfPermission(UserProfileActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "É necessário permissão para acessar arquivos.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    chooseImage(UserProfileActivity.this);
                }
                break;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        storageReference = FirebaseStorage.getInstance().getReference("Usuario/"+key+".jpg");

        Log.d(String.valueOf(UserProfileActivity.this), "Data: "+data.getData());

        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");

                        Matrix mat = new Matrix();
                        mat.postRotate(0);

                        Bitmap selectedImageRotate = Bitmap.createBitmap(selectedImage, 0, 0,selectedImage.getWidth(),selectedImage.getHeight(), mat, true);

                        photo.setImageBitmap(selectedImageRotate);

                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        selectedImage.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                        byte bb[] = bytes.toByteArray();

                        storageReference.putBytes(bb).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(UserProfileActivity.this, "Imagem de perfil salva com sucesso", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserProfileActivity.this, "Erro: "+e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        //String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            InputStream inputStream = null;
                            try {
                                inputStream = getContentResolver().openInputStream(selectedImage);
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            photo.setImageBitmap(bitmap);
                            photo.setRotation(getCameraPhotoOrientation(String.valueOf(selectedImage)));
                            imageUri = data.getData();

                            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(UserProfileActivity.this, "Image salva no banco com sucesso", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UserProfileActivity.this, "Falha em salvar imagem no banco", Toast.LENGTH_SHORT).show();
                                }
                            });
                            /*Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            if (cursor != null) {

                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);

                                photo.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                photo.setRotation(getCameraPhotoOrientation(picturePath));

                                imageUri = data.getData();

                                storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Toast.makeText(UserProfileActivity.this, "Image salva no banco com sucesso", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UserProfileActivity.this, "Falha em salvar imagem no banco", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                cursor.close();
                            }*/
                        }
                    }
                    break;
            }
        }
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