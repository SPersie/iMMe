package sat.imme_login_v2;

import android.graphics.Bitmap;

/**
 * Created by tianlerk on 8/3/18.
 */

public interface onFirebaseGetImageCallback {
    void onSuccess(Bitmap image);
    void onFailure(Exception e);
}
