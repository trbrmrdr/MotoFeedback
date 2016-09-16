package me.motofeedback.visual;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;

import me.motofeedback.R;
import me.motofeedback.mApplication;
import me.motofeedback.mServices;

/**
 * Created by trbrm on 09.09.2016.
 */
public class SettingsActivity extends AppCompatActivity {

    public static void Start(final Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main);
        mServices.StartServices(this);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        ScrollView settingsScrollView = (ScrollView) findViewById(R.id.scrollView_settings);
        settingsScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                clearFocus();
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApplication.getSettings().updateUI(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApplication.getSettings().updateUI(null);
    }

    private void clearFocus() {
        ((EditText) findViewById(R.id.editTextXY)).clearFocus();
        ((EditText) findViewById(R.id.editTextXZ)).clearFocus();
        ((EditText) findViewById(R.id.editTextZY)).clearFocus();
        ((EditText) findViewById(R.id.editTextDR)).clearFocus();
        ((EditText) findViewById(R.id.editTextSDR)).clearFocus();
        ((EditText) findViewById(R.id.editTextRDR)).clearFocus();

        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_settings);
        scrollView.getChildAt(0).requestFocus();
        hideKeyboard(this);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}
