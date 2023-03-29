package space.sakibnm.cameraxdemo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class MainActivity extends AppCompatActivity implements FragmentCameraController.DisplayTakenPhoto, FragmentDisplayImage.RetakePhoto {

    private static final int PERMISSIONS_CODE = 0x100;
    private FrameLayout containerRoot;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        containerRoot = findViewById(R.id.containerRoot);

//        getting the instance of FirebaseStorage....
        storage = FirebaseStorage.getInstance();

//        Asking for permissions in runtime......
        Boolean cameraAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        Boolean readAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Boolean writeAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if(cameraAllowed && readAllowed && writeAllowed){
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerRoot, FragmentCameraController.newInstance(), "cameraFragment")
                    .commit();

        }else{
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE);
        }
    }

//    After the user grants or denies the permission....
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>2){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerRoot, FragmentCameraController.newInstance(), "cameraFragment")
                    .commit();
        }else{
            Toast.makeText(this, "You must allow Camera and Storage permissions!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTakePhoto(Uri imageUri) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerRoot,FragmentDisplayImage.newInstance(imageUri),"displayFragment")
                .commit();
    }
//Retrieving an image from gallery....
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==RESULT_OK){
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.containerRoot,FragmentDisplayImage.newInstance(selectedImageUri),"displayFragment")
                                .commit();
                    }
                }
            }
    );

    @Override
    public void onOpenGalleryPressed() {
        openGallery();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRetakePressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerRoot, FragmentCameraController.newInstance(), "cameraFragment")
                .commit();
    }

    @Override
    public void onUploadButtonPressed(Uri imageUri, ProgressBar progressBar) {
//        ProgressBar.......
        progressBar.setVisibility(View.VISIBLE);
//        Upload an image from local file....
        StorageReference storageReference = storage.getReference().child("images/"+imageUri.getLastPathSegment());
        UploadTask uploadImage = storageReference.putFile(imageUri);
        uploadImage.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Upload Failed! Try again!", Toast.LENGTH_SHORT).show();
            }
        })
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Upload successful! Check Firestore", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        })
//                Setting progressbar.....
        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                Log.d("demo", "onProgress: "+progress);
                progressBar.setProgress((int) progress);
            }
        });
    }

}