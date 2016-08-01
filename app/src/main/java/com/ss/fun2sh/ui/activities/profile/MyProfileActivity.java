package com.ss.fun2sh.ui.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;
import com.ss.fun2sh.utils.ToastUtils;
import com.ss.fun2sh.utils.ValidationUtils;
import com.ss.fun2sh.utils.helpers.ImagePickHelper;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;
import com.ss.fun2sh.utils.image.ImageUtils;
import com.ss.fun2sh.utils.listeners.OnImagePickedListener;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MyProfileActivity extends BaseLoggableActivity implements OnImagePickedListener {

    private static String TAG = MyProfileActivity.class.getSimpleName();

    @Bind(R.id.avatar_imageview)
    CircularImageView photoImageView;

    @Bind(R.id.input_displaytx)
    TextView fullNameTextInputLayout;

    @Bind(R.id.full_name_edittext)
    EditText fullNameEditText;

    @Bind(R.id.status_edittext)
    EditText statusEditText;

    private QBUser qbUser;
    private boolean isNeedUpdateImage;
    private UserCustomData userCustomData;
    private String currentFullName;
    private String oldFullName;
    private String statusCurrent;
    private String statusOld;
    private ImagePickHelper imagePickHelper;
    private Uri imageUri;

    public static void start(Context context) {
        Intent intent = new Intent(context, MyProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_my_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();

        initData();
        addActions();
        updateOldData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                if (checkNetworkAvailableWithError()) {
                    updateUser();
                }
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @OnTextChanged(R.id.full_name_edittext)
    void onTextChangedFullName(CharSequence text) {
        fullNameEditText.setError(null);
    }

    @OnClick(R.id.avatar_imageview)
    void changePhoto(View view) {
        fullNameEditText.setError(null);
        imagePickHelper.pickAnImage(this, ImageUtils.IMAGE_REQUEST_CODE);
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        canPerformLogout.set(true);
        startCropActivity(Uri.fromFile(file));
    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {
        canPerformLogout.set(true);
        ErrorUtils.showError(this, e);
    }

    @Override
    public void onImagePickClosed(int requestCode) {
        canPerformLogout.set(true);
    }

    private void initFields() {
        title = getString(R.string.profile_title);
        imagePickHelper = new ImagePickHelper();
        qbUser = AppSession.getSession().getUser();
    }

    private void initData() {
        currentFullName = qbUser.getFullName();
        initCustomData();
        loadAvatar();
        statusCurrent = userCustomData.getStatus();
        fullNameEditText.setText(currentFullName);
        fullNameTextInputLayout.setText(qbUser.getLogin());
        if (statusCurrent != null) {
            statusEditText.setText(statusCurrent);
        } else {
            statusEditText.setText(getString(R.string.dummy_status));
        }
    }

    private void initCurrentData() {
        currentFullName = fullNameEditText.getText().toString();
        statusCurrent = statusEditText.getText().toString();
        initCustomData();
    }

    private void initCustomData() {
        userCustomData = Utils.customDataToObject(qbUser.getCustomData());
        if (userCustomData == null) {
            userCustomData = new UserCustomData();
        }
    }

    private void loadAvatar() {
        if (userCustomData != null && !TextUtils.isEmpty(userCustomData.getAvatar_url())) {
            ImageLoader.getInstance().displayImage(userCustomData.getAvatar_url(),
                    photoImageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
        }
    }

    private void updateOldData() {
        oldFullName = fullNameEditText.getText().toString();
        statusOld = statusEditText.getText().toString();
        isNeedUpdateImage = false;
    }

    private void resetUserData() {
        qbUser.setFullName(oldFullName);
        isNeedUpdateImage = false;
        initCurrentData();
    }

    private void addActions() {
        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, new UpdateUserSuccessAction());
        addAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION, new UpdateUserFailAction());

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION);
        removeAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateImage = true;
            photoImageView.setImageBitmap(new ImageUtils(this).getBitmap(imageUri));
        } else if (resultCode == Crop.RESULT_ERROR) {
            ToastUtils.longToast(Crop.getError(result).getMessage());
        }
        canPerformLogout.set(true);
    }

    private void startCropActivity(Uri originalUri) {
        canPerformLogout.set(false);
        imageUri = Uri.fromFile(new File(getCacheDir(), Crop.class.getName()));
        Crop.of(originalUri, imageUri).asSquare().start(this);
    }

    private QBUser createUserForUpdating() {
        QBUser newUser = new QBUser();
        newUser.setId(qbUser.getId());
        newUser.setPassword(qbUser.getPassword());
        newUser.setOldPassword(qbUser.getOldPassword());
        qbUser.setFullName(currentFullName);
        newUser.setFullName(currentFullName);
        userCustomData.setStatus(statusCurrent);
        newUser.setCustomData(Utils.customDataToString(userCustomData));
        return newUser;
    }

    private boolean isDataChanged() {
        return isNeedUpdateImage || !oldFullName.equals(currentFullName) || oldFullName.equals(currentFullName) || !statusOld.equals(statusCurrent) || statusOld.equals(statusCurrent);
    }

    private boolean isFullNameNotEmpty() {
        return !TextUtils.isEmpty(currentFullName.trim());
    }

    private void updateUser() {
        initCurrentData();

        if (isDataChanged()) {
            saveChanges();
        } else {
            fullNameEditText.setError(getString(R.string.profile_full_name_and_photo_not_changed));
        }
    }

    private void saveChanges() {
        if (isFullNameNotEmpty()) {
            showProgress();

            if (isNeedUpdateImage && imageUri != null) {
                QBUser newUser = createUserForUpdating();
                QBUpdateUserCommand.start(this, newUser, ImageUtils.getCreatedFileFromUri(imageUri));
            } else {
                QBUser newUser = createUserForUpdating();
                QBUpdateUserCommand.start(this, newUser, null);
            }
        } else {
            new ValidationUtils(this).isFullNameValid(fullNameEditText, oldFullName, currentFullName);
        }
    }

    public class UpdateUserFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();

            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            if (exception != null) {
                ToastUtils.longToast(exception.getMessage());
            }

            resetUserData();
        }
    }

    private class UpdateUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();

            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            AppSession.getSession().updateUser(user);
            updateOldData();
            MyProfileActivity.this.finish();
        }
    }
}