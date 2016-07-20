package com.ss.fun2sh.ui.activities.profile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;
import com.ss.fun2sh.CRUD.Const;
import com.ss.fun2sh.CRUD.M;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.ss.fun2sh.R;
import com.ss.fun2sh.ui.activities.base.BaseLoggableActivity;
import com.ss.fun2sh.ui.activities.main.MainActivity;
import com.ss.fun2sh.utils.ToastUtils;
import com.ss.fun2sh.utils.ValidationUtils;
import com.ss.fun2sh.utils.helpers.ImagePickHelper;
import com.ss.fun2sh.utils.image.ImageLoaderUtils;
import com.ss.fun2sh.utils.image.ImageUtils;
import com.ss.fun2sh.utils.listeners.OnImagePickedListener;

import java.io.File;

import butterknife.Bind;
import butterknife.OnTextChanged;

// activity_first_time_user_profile

public class FirstTimeUserProfileActivity extends BaseLoggableActivity implements OnImagePickedListener {


    private static String TAG = FirstTimeUserProfileActivity.class.getSimpleName();

    @Bind(R.id.avatar_imageview)
    CircularImageView photoImageView;

    @Bind(R.id.input_displaytx)
    TextView fullNameTextInputLayout;

    @Bind(R.id.full_name_edittext)
    EditText fullNameEditText;

    @Bind(R.id.btn_next)
    Button btn_next;

    @Bind(R.id.input_statustx)
    TextView inputstatustx;


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
        Intent intent = new Intent(context, FirstTimeUserProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_first_time_user_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();
        initData();
        addActions();
        updateOldData();

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNetworkAvailableWithError()) {
                    updateUser();
                }
            }
        });


        inputstatustx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheet();
            }
        });


        photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullNameEditText.setError(null);
                imagePickHelper.pickAnImage(FirstTimeUserProfileActivity.this, ImageUtils.IMAGE_REQUEST_CODE);
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public void onImagePicked(int requestCode, File file) {
        canPerformLogout.set(false);
        startCropActivity(Uri.fromFile(file));
    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {
        canPerformLogout.set(false);
        M.E(e.getMessage());
    }

    @Override
    public void onImagePickClosed(int requestCode) {
        canPerformLogout.set(false);
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
        fullNameTextInputLayout.setText(qbUser.getLogin());
        if (statusCurrent != null) {
            inputstatustx.setText(statusCurrent);
        } else {
            inputstatustx.setText(getString(R.string.dummy_status));
        }
    }

    private void initCurrentData() {
        currentFullName = fullNameEditText.getText().toString();
        statusCurrent = inputstatustx.getText().toString();
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
        statusOld = inputstatustx.getText().toString();
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
    }

    private void startCropActivity(Uri originalUri) {
        canPerformLogout.set(false);
        imageUri = Uri.fromFile(new File(getCacheDir(), Crop.class.getName()));
        M.E(imageUri.toString());
        M.E("Orignal URL" + originalUri.toString());
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
            //  startMainActivity();
        }
    }

    protected void startMainActivity() {
        MainActivity.start(FirstTimeUserProfileActivity.this);
        finish();
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

    public void openBottomSheet() {

        View view = getLayoutInflater().inflate(R.layout.fileattach_dialog_top, null);

        final EditText txtStatusdialog = (EditText) view.findViewById(R.id.statusET);
        Button setStatusBT = (Button) view.findViewById(R.id.setStatusBT);

        final Dialog mBottomSheetDialog = new Dialog(FirstTimeUserProfileActivity.this, R.style.MaterialDialogSttusSheet);// R.style.MaterialDialogSheet
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.TOP);
        mBottomSheetDialog.show();
        setStatusBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = txtStatusdialog.getText().toString();
                inputstatustx.setText(status);
                mBottomSheetDialog.dismiss();
            }
        });

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
            PrefsHelper.getPrefsHelper().savePref(Const.App_Ver.firstTimeProfile, true);
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            AppSession.getSession().updateUser(user);
            updateOldData();
            startMainActivity();

        }
    }


}
