package leo.com.permissionrequester;

import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;
import static android.Manifest.permission.WRITE_SETTINGS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import leo.com.permissionrequester.callback.PermissionRequestCallback;

/**
 * Created by liuguanli on 2016/10/25.
 */

public class PermissionRequester {

    private static final String TAG = "PermissionRequester";

    private static final int PERMISSIONS_REQUEST_CODE = 0;
    private static final int SYSTEM_ALERT_WINDOW_REQUEST_CODE = 1;
    private static final int WRITE_SETTINGS_REQUEST_CODE = 2;

    private boolean isShowPreAckDialog = false;
    private boolean isForceAccept = false;

    @NonNull
    private PermissionRequestCallback callback;
    @NonNull
    private Activity context;

    public PermissionRequester(@NonNull Activity context) {
        this.context = context;
        if (context instanceof PermissionRequestCallback) {
            this.callback = (PermissionRequestCallback) context;
        } else {
            throw new IllegalArgumentException("No PermissionRequestCallback! You can implement the callback by "
                    + "context or use Anonymous inner class with");
        }
    }

    public PermissionRequester(@NonNull Activity context,
                               @NonNull PermissionRequestCallback permissionRequestCallback) {
        this.context = context;
        this.callback = permissionRequestCallback;
    }

    public PermissionRequester showPreAskDialog(boolean flag) {
        this.isShowPreAckDialog = flag;
        return this;
    }

    public PermissionRequester setPermissionForceAccepted(boolean flag) {
        this.isForceAccept = flag;
        return this;
    }

    /**
     * if request only one permission
     * <p>
     * if request more than one permission
     * first check if denied
     *
     * @param requestedPermissionNames
     */
    public void request(String... requestedPermissionNames) {
        if (requestedPermissionNames == null) {
            throw new NullPointerException("permissionName can not be null");
        }
        if (requestedPermissionNames.length == 0) {
            return;
        }
        if (requestedPermissionNames.length == 1) {
            String requestedPermissionName = requestedPermissionNames[0];
            if (checkPermission(requestedPermissionName)) {
                if (isSystemPermission(requestedPermissionName)) {
                    requestSystemPermission(requestedPermissionName);
                } else {
                    if (isPermissionDenied(requestedPermissionName)) {
                        if (shouldShowRequestPermissionRationale(requestedPermissionName)) {
                            callback.onShowHintBeforeRequest(requestedPermissionName);
                        } else {
                            realRequestPermissionMethod(requestedPermissionName);
                        }
                    } else {
                        callback.onPermissionRequestGranted();
                    }
                }
            } else {
                callback.onPermissionRequestDenied(requestedPermissionName);
            }
        } else {
            List<String> leaglPermissions = new ArrayList<>();
            for (String requestedPermissionName : requestedPermissionNames) {
                if (isPermissionDenied(requestedPermissionName) && checkPermission(requestedPermissionName)) {
                    leaglPermissions.add(requestedPermissionName);
                }
            }
            if (leaglPermissions.size() == 0) {
                callback.onPermissionRequestGranted();
            } else {
                removeSystemPermission(leaglPermissions);
                realRequestPermissionMethod(leaglPermissions.toArray(new String[leaglPermissions.size()]));
            }
        }

    }

    public void requestAfterShowHint(String permissionName) {
        if (permissionName == null) {
            throw new NullPointerException("permissionName can not be null");
        }
        if (isPermissionDenied(permissionName)) {
            realRequestPermissionMethod(permissionName);
        } else {
            callback.onPermissionRequestGranted(permissionName);
        }
    }

    public void realRequestPermissionMethod(String... permissionNames) {
        if (permissionNames == null) {
            throw new NullPointerException("permissionName can not be null");
        }
        ActivityCompat.requestPermissions(context, permissionNames, PERMISSIONS_REQUEST_CODE);
    }

    public boolean shouldShowRequestPermissionRationale(String permissionName) {
        if (permissionName == null) {
            throw new NullPointerException("permissionName can not be null");
        }
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permissionName);
    }

    public boolean isPermissionDenied(String permissionName) {
        if (permissionName == null) {
            throw new NullPointerException("permissionName can not be null");
        }
        return ActivityCompat.checkSelfPermission(context, permissionName) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * TODO packageInfo.requestedPermissions 这个可以优化
     *
     * @param permissionName
     *
     * @return
     */
    public boolean checkPermission(String permissionName) {
        if (permissionName == null) {
            throw new NullPointerException("permissionName can not be null");
        }
        try {
            PackageInfo packageInfo =
                    context.getPackageManager()
                            .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);

            if (packageInfo != null && packageInfo.requestedPermissions != null) {
                if (Arrays.asList(packageInfo.requestedPermissions).contains(permissionName)) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeSystemPermission(List<String> permissionNames) {
        permissionNames.remove(SYSTEM_ALERT_WINDOW);
        permissionNames.remove(WRITE_SETTINGS);
    }

    public boolean isSystemPermission(String permissionName) {
        switch (permissionName) {
            case SYSTEM_ALERT_WINDOW:
                return true;
            case WRITE_SETTINGS:
                return true;
            default:
                return false;
        }
    }

    public void requestSystemPermission(String permissionName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.onPermissionRequestGranted();
        } else {
            switch (permissionName) {
                case SYSTEM_ALERT_WINDOW:
                    if (Settings.canDrawOverlays(context)) {
                        callback.onPermissionRequestGranted(permissionName);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context.getPackageName()));
                        context.startActivityForResult(intent, SYSTEM_ALERT_WINDOW_REQUEST_CODE);
                    }
                    break;
                case WRITE_SETTINGS:
                    if (Settings.System.canWrite(context)) {
                        callback.onPermissionRequestGranted(permissionName);
                    } else {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + context.getPackageName()));
                        context.startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * handle the result from Setting Page for the Special Permissions
     * {@link Activity#onActivityResult(int, int, Intent) }
     * <a>https://developer.android.com/guide/topics/security/permissions.html</a>
     *
     * @param requestCode
     */
    public void onActivityForResult(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch (requestCode) {
                case SYSTEM_ALERT_WINDOW_REQUEST_CODE:
                    if (Settings.canDrawOverlays(context)) {
                        callback.onPermissionRequestSucc(SYSTEM_ALERT_WINDOW);
                    } else {
                        callback.onPermissionRequestFail(SYSTEM_ALERT_WINDOW);
                    }
                    break;
                case WRITE_SETTINGS_REQUEST_CODE:
                    if (Settings.System.canWrite(context)) {
                        callback.onPermissionRequestSucc(WRITE_SETTINGS);
                    } else {
                        callback.onPermissionRequestFail(WRITE_SETTINGS);
                    }
                    break;
                default:
                    break;
            }
        } else {
            switch (requestCode) {
                case SYSTEM_ALERT_WINDOW_REQUEST_CODE:
                    callback.onPermissionRequestGranted(SYSTEM_ALERT_WINDOW);
                    break;
                case WRITE_SETTINGS_REQUEST_CODE:
                    callback.onPermissionRequestGranted(WRITE_SETTINGS);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * handle the result that from the callback of system.
     * <p>
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            List<String> unSuccessList = getUnSuccessPermissions(permissions, grantResults);
            removeSystemPermission(unSuccessList);
            if (unSuccessList.size() == 0) {
                callback.onPermissionRequestSucc(permissions);
            } else {
                callback.onPermissionRequestFail(unSuccessList.toArray(new String[unSuccessList.size()]));
            }
        }
    }

    /**
     * get unsuccess granted permissions except those that can not be requested again.
     *
     * @param permissions
     * @param grantResults
     *
     * @return
     */
    private List<String> getUnSuccessPermissions(@NonNull String[] permissions, @NonNull int[] grantResults) {

        List<String> unSuccessList = new ArrayList<>();

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (i < permissions.length && !TextUtils.isEmpty(permissions[i])) {
                    unSuccessList.add(permissions[i]);
                    if (!shouldShowRequestPermissionRationale(permissions[i])) {
                        callback.onPermissionRequestFailAndDenied(permissions[i]);
                    }
                }
            }
        }

        return unSuccessList;
    }

}
