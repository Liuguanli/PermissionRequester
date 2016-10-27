package leo.com.permissionrequester.callback;

import android.support.annotation.NonNull;

/**
 * Created by liuguanli on 2016/10/25.
 */

public interface PermissionRequestCallback {

    void onPermissionRequestSucc(@NonNull String... permissionName);

    void onPermissionRequestFail(@NonNull String... permissionName);

    void onPermissionRequestDenied(@NonNull String... permissionName);

    void onPermissionRequestGranted(@NonNull String... permissionName);

    void onPermissionInexistence(@NonNull String... permissionName);

    /**
     * request failed and you can only go to Setting to grant the permission
     *
     * @param permissionName
     */
    void onPermissionRequestFailAndDenied(@NonNull String... permissionName);

    /**
     * you can show a dialog or toast to explain the reason.
     *
     * @param permissionName
     */
    void onShowHintBeforeRequest(@NonNull String permissionName);
}
