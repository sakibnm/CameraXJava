package space.sakibnm.cameraxdemo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_CODE = 0x100;
    private FrameLayout containerRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        containerRoot = findViewById(R.id.containerRoot);



//        Asking for permissions in runtime......
        Boolean cameraAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        Boolean readAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Boolean writeAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if(cameraAllowed && readAllowed && writeAllowed){
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();

        }else{
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>2){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerRoot, FragmenrCameraController.newInstance(), "cameraFragment")
                    .commit();
        }else{
            Toast.makeText(this, "You must allow Camera and Storage permissions!", Toast.LENGTH_LONG).show();
        }
    }
}