package sat.imme_login_v2;

import android.net.Uri;

import com.google.android.gms.tasks.Task;

/**
 * Created by tianlerk on 6/3/18.
 */

public interface onFirebaseUserCallback {
    void onSuccess(User user);
    void onFailure(Task task);
}
