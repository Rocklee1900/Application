package com.example.carbooking.cloudinary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.*;

public class UploadImageToCloudinary {

    private static final OkHttpClient client = new OkHttpClient();
    private static final String CLOUDINARY_URL = "https://api.cloudinary.com/v1_1/drcicatxm/image/upload";
    private static final String UPLOAD_PRESET = "rrgaa4jy";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    public static void upload(Context context, Uri uri, UploadCallback callback) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "car.jpg", RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("folder", "CarRental")
                    .build();

            Request request = new Request.Builder()
                    .url(CLOUDINARY_URL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ((Activity) context).runOnUiThread(() ->
                            callback.onFailure("Image upload failed: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String imageUrl = json.getString("secure_url");

                            ((Activity) context).runOnUiThread(() ->
                                    callback.onSuccess(imageUrl));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Activity) context).runOnUiThread(() ->
                                    callback.onFailure("Failed to parse upload response"));
                        }
                    } else {
                        ((Activity) context).runOnUiThread(() ->
                                callback.onFailure("Upload error: " + response.message()));
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailure("Error preparing image: " + e.getMessage());
        }
    }
}
