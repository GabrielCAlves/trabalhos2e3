package br.ufc.quixada.dadm.trabalho2.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.content.Context;
import java.io.IOException;

public class ImageUploadService {

    private static final String IMGBB_API_KEY = "16bbf5ef6ffa4d362262391113b5718d";
    private OkHttpClient client;
    private Context context;

    public ImageUploadService(Context context) {
        this.client = new OkHttpClient();
        this.context = context;
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    // Upload a partir de Bitmap (Câmera)
    public void uploadImage(Bitmap bitmap, UploadCallback callback) {
        new Thread(() -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageData = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageData, Base64.DEFAULT);

                uploadBase64Image(base64Image, callback);

            } catch (Exception e) {
                Log.e("ImageUpload", "Error: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Upload a partir de Uri (Galeria)
    public void uploadImage(Uri imageUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                // Converte Uri para Bitmap
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (bitmap != null) {
                    // Comprime e converte para Base64
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] imageData = baos.toByteArray();
                    String base64Image = Base64.encodeToString(imageData, Base64.DEFAULT);

                    uploadBase64Image(base64Image, callback);
                } else {
                    callback.onError("Não foi possível carregar a imagem da galeria");
                }

            } catch (Exception e) {
                Log.e("ImageUpload", "Error: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        }).start();
    }

    // Método comum para upload
    private void uploadBase64Image(String base64Image, UploadCallback callback) {
        try {
            RequestBody requestBody = new FormBody.Builder()
                    .add("image", base64Image)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            Log.d("ImageUpload", "Response: " + responseBody);

            JSONObject json = new JSONObject(responseBody);
            JSONObject data = json.optJSONObject("data");

            if (data != null && data.has("url")) {
                String imageUrl = data.getString("url");
                callback.onSuccess(imageUrl);
            } else {
                String error = json.optJSONObject("error").optString("message", "Upload failed");
                callback.onError(error);
            }

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}