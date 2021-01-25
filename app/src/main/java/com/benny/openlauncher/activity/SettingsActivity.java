package com.benny.openlauncher.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.ui.login.SecurityPwdCheckActivity;
import com.benny.openlauncher.fragment.SettingsBaseFragment;
import com.benny.openlauncher.fragment.SettingsMasterFragment;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.util.BackupHelper;
import com.benny.openlauncher.util.Definitions;
import com.nononsenseapps.filepicker.Utils;

import net.gsantner.opoc.util.ContextUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends ColorActivity implements SettingsBaseFragment.OnPreferenceStartFragmentCallback {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    public void onCreate(Bundle b) {
        // must be applied before setContentView
        super.onCreate(b);

        ContextUtils contextUtils = new ContextUtils(this);
        contextUtils.setAppLanguage(_appSettings.getLanguage());

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.pref_title__settings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setBackgroundColor(_appSettings.getPrimaryColor());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, new SettingsMasterFragment()).commit();

        // if system exit is called the app will open settings activity again
        // this pushes the user back out to the home activity
        if (_appSettings.getAppRestartRequired()) {
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    /**
     * Every time user tries to open the preferences, we ask for password confirmation
     * So that, normal users can't change the settings
     * in this method, we check if user confirmed password, we reset the boolean,
     * otherwise open the prompt password activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!S_DID_CHK_SECURITY_PWD) {
            performPwdSecurityCheck();
        } else {
            S_DID_CHK_SECURITY_PWD = false;
        }
    }

    private static boolean S_DID_CHK_SECURITY_PWD = false;
    public static final int ACTIVITY_CODE_PWD_SECURITY_CHECK = 5632;

    private void performPwdSecurityCheck() {
        Intent intent = new Intent(this, SecurityPwdCheckActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivityForResult(intent, ACTIVITY_CODE_PWD_SECURITY_CHECK);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference preference) {
        Fragment fragment = Fragment.instantiate(this, preference.getFragment(), preference.getExtras());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, fragment).addToBackStack(fragment.getTag()).commit();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Setup.dataManager().close();
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            switch (requestCode) {
                case Definitions.INTENT_BACKUP:
                    BackupHelper.backupConfig(this, new File(Utils.getFileForUri(files.get(0)).getAbsolutePath() + "/openlauncher.zip").toString());
                    Setup.dataManager().open();
                    break;
                case Definitions.INTENT_RESTORE:
                    BackupHelper.restoreConfig(this, Utils.getFileForUri(files.get(0)).toString());
                    System.exit(0);
                    break;
                case ACTIVITY_CODE_PWD_SECURITY_CHECK:
                    S_DID_CHK_SECURITY_PWD = true;
                    break;
            }
        }
    }
}
