package com.whinc.apksignaturedigest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

import java.util.List;

import static com.whinc.util.SystemServiceUtils.copyToClipboard;

/**
 * Created by wuhui on 8/31/15.
 */
public class ApkSignatureActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    public static final String TAG = ApkSignatureActivity.class.getSimpleName();

    MaterialAutoCompleteTextView mPkgNameEdt;
    Button mPkgNameButton;
    TextView mSignDigestTxt;
    GridLayout mVersionInfoLayout;

    private boolean mUpperCase = true;
    private List<PackageInfo> mPkgInfoList;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ApkSignatureActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_signature);
        initAppBar();
        initView();
    }

    private void initAppBar() {
        mPkgNameEdt = (MaterialAutoCompleteTextView) findViewById(R.id.pkg_name_editText);
        mPkgNameButton = (Button) findViewById(R.id.retrieve_signature_btn);
        mSignDigestTxt = (TextView) findViewById(R.id.signature_textView);
        mVersionInfoLayout = (GridLayout) findViewById(R.id.version_info_layout);
        mPkgNameButton.setOnClickListener(this);
        mSignDigestTxt.setOnClickListener(this);
        mSignDigestTxt.setOnLongClickListener(this);
    }

    private void initView() {
        mVersionInfoLayout.setVisibility(View.INVISIBLE);

        mPkgInfoList = PackageUtils.getInstance().getInstalledPackages(this);
        mPkgNameEdt.setAdapter(new ArrayAdapter<PackageInfo>(this, android.R.layout.simple_list_item_1, mPkgInfoList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(getItem(position).packageName);
                return view;
            }
        });
        mPkgNameEdt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PackageInfo packageInfo = (PackageInfo) parent.getItemAtPosition(position);
                updatePkgInfo(packageInfo);
            }
        });
        mPkgNameEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    mSignDigestTxt.setText("");
                    mVersionInfoLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // update package info list
        mPkgInfoList.clear();
        mPkgInfoList.addAll(PackageUtils.getInstance().getInstalledPackages(this));
    }

    private void updatePkgInfo(PackageInfo pkgInfo) {
        String digest = PackageUtils.getInstance().getSignatureDigest(pkgInfo);
        digest = mUpperCase ? digest.toUpperCase() : digest.toLowerCase();
        mSignDigestTxt.setText(digest);
        mPkgNameEdt.setText(pkgInfo.packageName);
        mPkgNameEdt.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mPkgNameEdt.getWindowToken(), 0);

        TextView versionCodeTxt = (TextView) mVersionInfoLayout.getChildAt(1);
        TextView versionNameTxt = (TextView) mVersionInfoLayout.getChildAt(3);
        versionCodeTxt.setText(String.valueOf(pkgInfo.versionCode));
        versionNameTxt.setText(pkgInfo.versionName);
        mVersionInfoLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.retrieve_signature_btn:
                String pkgName = mPkgNameEdt.getText().toString();
                if (TextUtils.isEmpty(pkgName)) {
                    Toast.makeText(this, "Package name cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                PackageInfo pkgInfo = null;
                for (PackageInfo v : mPkgInfoList) {
                    if (v.packageName.equals(pkgName)) {
                        pkgInfo = v;
                        break;
                    }
                }
                if (pkgInfo != null) {
                    updatePkgInfo(pkgInfo);
                } else {
                    Toast.makeText(this, "Invalid package name!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.signature_textView:
                mUpperCase = !mUpperCase;
                String digest = mSignDigestTxt.getText().toString();
                digest = mUpperCase ? digest.toUpperCase() : digest.toLowerCase();
                mSignDigestTxt.setText(digest);
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()){
            case R.id.signature_textView:
                copyToClipboard(this, mSignDigestTxt.getText());

                String tip = mSignDigestTxt.getText() + " has been copied to clipboard!";
                Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}
