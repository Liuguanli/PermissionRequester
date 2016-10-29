package leo.com.runtimeauth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import leo.com.permissionrequester.PermissionRequester;
import leo.com.permissionrequester.callback.PermissionRequestCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PermissionRequestCallback {

    private static final String TAG = "MainActivity";

    private Button callAuth;
    private Button cameraAuth;
    private Button alertWindowAuth;

    private TextView textView;

    PermissionRequester permissionRequester;

    private static final int MY_CALL_PHONE_REQUEST_CODE = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callAuth = (Button) findViewById(R.id.call_auth);
        cameraAuth = (Button) findViewById(R.id.camera_auth);
        alertWindowAuth = (Button) findViewById(R.id.alert_window_auth);
        callAuth.setOnClickListener(this);
        cameraAuth.setOnClickListener(this);
        alertWindowAuth.setOnClickListener(this);
        textView = (TextView) findViewById(R.id.show_call_auth_resut);

        permissionRequester = new PermissionRequester(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call_auth:
                permissionRequester.request(Manifest.permission.CALL_PHONE);
                break;
            case R.id.camera_auth:
//                permissionRequester.request(null);
                break;
            case R.id.alert_window_auth:
                permissionRequester.request(Manifest.permission.SYSTEM_ALERT_WINDOW);
                break;
            default:
                break;
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 打开相机
        this.startActivity(intent);
    }

    private void call() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "15002443348"));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.startActivity(intent);
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult requestCode-> " + requestCode);
        permissionRequester.onActivityForResult(requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult requestCode-> " + requestCode);
        permissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionRequestSucc(@NonNull String... permissionName) {
        Log.i(TAG, "onPermissionRequestSucc permissionName-> " + permissionName);
        Toast.makeText(MainActivity.this, "onPermissionRequestSucc", Toast.LENGTH_LONG).show();
        call();
    }

    @Override
    public void onPermissionRequestFail(@NonNull String... permissionName) {
        Log.i(TAG, "onPermissionRequestFail permissionName-> " + permissionName);
        Toast.makeText(MainActivity.this, "onPermissionRequestFail", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionRequestDenied(@NonNull String... permissionName) {
        Log.i(TAG, "onPermissionRequestDenied permissionName-> " + permissionName);
        Toast.makeText(MainActivity.this, "onPermissionRequestDenied", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionRequestGranted(@NonNull String... permissionName) {
        Log.i(TAG, "onPermissionRequestGranted permissionName-> " + permissionName);
        Toast.makeText(MainActivity.this, "onPermissionRequestGranted", Toast.LENGTH_LONG).show();
        call();
    }

    @Override
    public void onPermissionInexistence(@NonNull String... permissionName) {
        Log.i(TAG, "onPermissionInexistence permissionName-> " + permissionName);
        Toast.makeText(MainActivity.this, "onPermissionInexistence", Toast.LENGTH_LONG).show();
    }

    /**
     * request failed and you can only go to Setting to grant the permission
     *
     * @param permissionName
     */
    @Override
    public void onPermissionRequestFailAndDenied(@NonNull String... permissionName) {
        Log.i(TAG, "onPermissionRequestFailAndDenied permissionName-> " + permissionName);
        Toast.makeText(MainActivity.this, "onPermissionRequestFailAndDenied", Toast.LENGTH_LONG).show();
    }

    /**
     * you can show a dialog or toast to explain the reason.
     *
     * @param permissionName
     */
    @Override
    public void onShowHintBeforeRequest(@NonNull String permissionName) {
        Log.i(TAG, "onShowHintBeforeRequest permissionName-> " + permissionName);
        Toast.makeText(MainActivity.this, "onShowHintBeforeRequest", Toast.LENGTH_LONG).show();
//        permissionRequester.requestAfterShowHint(permissionName);
    }

}
