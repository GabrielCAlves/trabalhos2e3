package br.ufc.quixada.dadm.trabalho2;

import static br.ufc.quixada.dadm.trabalho2.UserProfileActivity.REQUEST_ID_MULTIPLE_PERMISSIONS;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import br.ufc.quixada.dadm.trabalho2.databinding.ActivityTelaEdicaoAdicaoBinding;
import br.ufc.quixada.dadm.trabalho2.databinding.ActivityUserProfileBinding;

public class TelaEdicaoAdicao extends AppCompatActivity {

    private EditText campoNomeItem, campoMarca, campoPreco, campoQuantidade, campoDesconto;
    private TextView campoId, campoTotal;

    private String key, userId, nomeItem, marca, picture;
    private int id, quantidade;
    private double preco, total, desconto;

    private StorageReference storageReference;
    ImageView campoPhoto;
    ActivityTelaEdicaoAdicaoBinding binding;
    Uri imageUri;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTelaEdicaoAdicaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());//setContentView(R.layout.activity_tela_edicao_adicao);

        campoId = findViewById(R.id.textViewId);
        campoNomeItem = findViewById(R.id.editTextNomeItem);
        campoMarca = findViewById(R.id.editTextMarca);
        campoPreco = findViewById(R.id.editTextPreco);
        campoQuantidade = findViewById(R.id.editTextQuantidade);
        campoDesconto = findViewById(R.id.editTextDesconto);
        campoTotal = findViewById(R.id.textViewTotalValor);

        campoPhoto = binding.imageItem;

        Bundle extras = getIntent().getExtras();

        if(extras != null){
            key = getIntent().getStringExtra("key");
            System.out.println("key = "+key);
            id = getIntent().getIntExtra("id", 0);
            userId = getIntent().getStringExtra("userId");
            nomeItem = getIntent().getStringExtra("nome");
            marca = getIntent().getStringExtra("marca");
            preco = getIntent().getDoubleExtra("preco", 0);
            quantidade = getIntent().getIntExtra("quantidade", 0);
            desconto = getIntent().getDoubleExtra("desconto", 0);
            picture = getIntent().getStringExtra("imagem");
            if(picture != null){
                imageUri = Uri.parse(picture);
            }

            if(desconto == 0){
                total = preco*quantidade;
            }else{
                total = preco*quantidade-(preco*quantidade*(desconto/100));
            }


            campoId.setText(String.valueOf(id));
            campoPhoto.setImageURI(imageUri);
            campoNomeItem.setText(nomeItem);
            campoMarca.setText(marca);
            campoPreco.setText(String.valueOf(preco));
            campoQuantidade.setText(String.valueOf(quantidade));
            campoDesconto.setText(String.valueOf(desconto));
            campoTotal.setText(String.format("%.2f",total));

            if(picture == null){
                try {
                    getItemImage();
                } catch (IOException e) {
                    Toast.makeText(TelaEdicaoAdicao.this, "Item sem imagem ou falha em carregar. No segundo caso, erro: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void getItemImage() throws IOException {
        storageReference = FirebaseStorage.getInstance().getReference("Item/"+userId+"/"+key+".jpg");

        File localFile = File.createTempFile("tempImage", "jpg");

        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                campoPhoto.setImageBitmap(bitmap);
                campoPhoto.setRotation(getCameraPhotoOrientation(localFile.getAbsolutePath()));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TelaEdicaoAdicao.this, "Falha em carregar imagem", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void mudarFoto(View v){

        if(checkAndRequestPermissions(TelaEdicaoAdicao.this)){
            chooseImage(TelaEdicaoAdicao.this);
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
                if (ContextCompat.checkSelfPermission(TelaEdicaoAdicao.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                                    "É necessário permição para usar a câmera.", Toast.LENGTH_SHORT)
                            .show();
                } else if (ContextCompat.checkSelfPermission(TelaEdicaoAdicao.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "É necessário permissão para acessar arquivos.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    chooseImage(TelaEdicaoAdicao.this);
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

        storageReference = FirebaseStorage.getInstance().getReference("Item/"+userId+"/"+key+".jpg");

        Log.d(String.valueOf(TelaEdicaoAdicao.this), "Data: "+data.getData());

        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");

                        Matrix mat = new Matrix();
                        mat.postRotate(0);

                        Bitmap selectedImageRotate = Bitmap.createBitmap(selectedImage, 0, 0,selectedImage.getWidth(),selectedImage.getHeight(), mat, true);

                        campoPhoto.setImageBitmap(selectedImageRotate);

                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        selectedImage.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                        byte bb[] = bytes.toByteArray();

                        storageReference.putBytes(bb).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(TelaEdicaoAdicao.this, "Imagem salva com sucesso", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(TelaEdicaoAdicao.this, "Erro: "+e.getMessage(), Toast.LENGTH_LONG).show();
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
                            campoPhoto.setImageBitmap(bitmap);
                            campoPhoto.setRotation(getCameraPhotoOrientation(String.valueOf(selectedImage)));
                            imageUri = data.getData();

                            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(TelaEdicaoAdicao.this, "Image salva no banco com sucesso", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(TelaEdicaoAdicao.this, "Falha em salvar imagem no banco", Toast.LENGTH_SHORT).show();
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

    @SuppressLint("DefaultLocale")
    public void calcular(View v){
        preco = Double.parseDouble(campoPreco.getText().toString());
        quantidade = Integer.parseInt(campoQuantidade.getText().toString());
        desconto = Double.parseDouble(campoDesconto.getText().toString());

        if(desconto == 0){
            total = preco*quantidade;
        }else{
            total = preco*quantidade-(preco*quantidade*(desconto/100));
        }

        if(limites()){
            campoTotal.setText(String.format("%.2f", total));
            //campoTotal.setText(String.format("%.2f", preco*quantidade));
        }
    }

    public void atualizar(View v){
        if(getIntent().getStringExtra("add_edit_ap").equals("edit")){

            //fazer o mesmo com imageUri aqui

            nomeItem = campoNomeItem.getText().toString();
            marca = campoMarca.getText().toString();
            preco = Double.parseDouble(campoPreco.getText().toString());
            quantidade = Integer.parseInt(campoQuantidade.getText().toString());
            desconto = Double.parseDouble(campoDesconto.getText().toString());

            String currentImageUri = (imageUri != null) ? imageUri.toString() : picture;

            if(limites()){
                Intent data = getIntent();

                data.putExtra("key", key);
                data.putExtra("userId", userId);
                data.putExtra("imagem", currentImageUri);
                data.putExtra("nome", nomeItem);
                data.putExtra("marca", marca);
                data.putExtra("preco", preco);
                data.putExtra("quantidade", quantidade);
                data.putExtra("desconto", desconto);
                data.putExtra("add_edit_ap", "edit");

                setResult(1, data);
                finish();
            }
        }else{
            Toast.makeText(this, "Não é possível atualizar ou excluir itens que ainda não pertencem a lista", Toast.LENGTH_SHORT).show();
        }
    }

    public void adicionar(View v){
        //Aqui tbm
        nomeItem = campoNomeItem.getText().toString();
        marca = campoMarca.getText().toString();
        preco = Double.parseDouble(campoPreco.getText().toString());
        quantidade = Integer.parseInt(campoQuantidade.getText().toString());
        desconto = Double.parseDouble(campoDesconto.getText().toString());
        userId = getIntent().getStringExtra("userId");

        if(limites()){
            Intent data = getIntent();
            data.putExtra("key", key);
            data.putExtra("userId", userId);
            data.putExtra("imagem", (imageUri != null) ? imageUri.toString() : null);
            data.putExtra("nome", nomeItem);
            data.putExtra("marca", marca);
            data.putExtra("preco", preco);
            data.putExtra("quantidade", quantidade);
            data.putExtra("desconto", desconto);
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
        Intent data = new Intent(); //getIntent();
        data.putExtra("add_edit_ap", "cancelar");
        setResult(RESULT_CANCELED, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        }else if(preco*quantidade > 999999){
            Toast.makeText(this, "ERRO: O valor total supera o limite estabelecido de 6 dígitos", Toast.LENGTH_SHORT).show();
            return false;
        }else if(desconto > 100){
            Toast.makeText(this, "Não é possível descontar mais de 100%", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}