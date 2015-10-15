package com.nlbhub.instead.standalone;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.nlbhub.instead.R;
import com.nlbhub.instead.SDLActivity;

import java.io.*;
import java.util.*;

/**
 * Created by Antokolos on 14.10.15.
 */
public abstract class MainMenuAbstract extends ListActivity implements SimpleAdapter.ViewBinder {

    protected boolean onpause = false;
    protected boolean dwn = false;
    protected ProgressDialog dialog;
    protected static final String BR = "<br>";
    protected static final String LIST_TEXT = "list_text";

    protected class ListItem {
        public String text;
        public int icon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // The following line is to workaround AndroidRuntimeException: requestFeature() must be called before adding content
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(this);
        dialog.setTitle(getString(R.string.wait));
        dialog.setMessage(getString(R.string.init));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        setContentView(R.layout.mnhead);

        ListView listView = getListView();
        registerForContextMenu(listView);
        showMenu();
        if (!dwn) {
            checkRC();
        }
    }

    @Override
    public boolean setViewValue(View view, Object data, String stringRepresetation) {
        ListItem listItem = (ListItem) data;

        TextView menuItemView = (TextView) view;
        menuItemView.setText(Html.fromHtml(listItem.text));
        menuItemView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(listItem.icon), null, null, null);
        return true;
    }

    protected String getHtmlTagForName(String s) {
        return "<b>" + s + "</b>";
    }

    protected String getHtmlTagForSmall(String s) {
        return "<small><i>" + s + "</i></small>";
    }

    protected void showMenu() {
        showMenu(new ArrayList<Map<String, ListItem>>());
    }

    protected abstract void showMenu(List<Map<String, ListItem>> additionalListData);

    protected Map<String, ListItem> addListItem(String s, int i) {
        Map<String, ListItem> iD = new HashMap<String, ListItem>();
        ListItem l = new ListItem();
        l.text = s;
        l.icon = i;
        iD.put(LIST_TEXT, l);
        return iD;
    }

    protected void startAppAlt() {
        if (checkInstall()) {
            Intent myIntent = new Intent(this, SDLActivity.class);
            startActivity(myIntent);
        } else {
            checkRC();
        }
    }

    public boolean checkInstall() {
        String path = StorageResolver.getOutFilePath(StorageResolver.DataFlag);

        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(path)), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return false;
        } catch (FileNotFoundException e) {
            return false;
        } catch (SecurityException e) {
            return false;
        }

        String line;

        try {

            line = input.readLine();
            try {
                if (line.toLowerCase().matches(
                        ".*" + InsteadApplication.AppVer(this).toLowerCase() + ".*")) {
                    input.close();
                    return true;
                }
            } catch (NullPointerException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
            ;

        } catch (IOException e) {
            return false;
        }
        try {
            input.close();
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    protected abstract void deleteAdditionalAssets();

    protected abstract void copyAdditionalAssets();

    protected void checkRC() {
        if (checkInstall()) {
            if (!(new File(StorageResolver.getOutFilePath(StorageResolver.Options))).exists()) {
                CreateRC();
            }
        } else {
            deleteAdditionalAssets();
            showMenu();
            loadData();
        }
        copyAdditionalAssets();
    }

    private void CreateRC() {
        String path = StorageResolver.getOutFilePath(StorageResolver.Options);
        if (!(new File(path)).exists()) {
            OutputStream out = null;
            byte buf[] = getConf().getBytes();
            try {
                out = new FileOutputStream(path);
                out.write(buf);
                out.close();
            } catch (FileNotFoundException e) {
            } catch (SecurityException e) {
            } catch (java.io.IOException e) {
                Log.e("Instead-NG ERROR", "Error writing file " + path);
                return;
            };
        }
    }

    private String getConf() {
        final String BR = "\n";
        final float xVGA = (float)320 / (float)240;
        final float HVGA = (float)480 / (float)320;

        String suff = "";


        suff = "-"+ThemeHelper.PORTRAIT_KEY.toUpperCase();


        String locale = null;
        if (Locale.getDefault().toString().equals("ru_RU")
                || Locale.getDefault().toString().equals("ru")) {
            locale = "lang = ru\n";
        } else if (Locale.getDefault().toString().equals("uk_UA")
                || Locale.getDefault().toString().equals("uk")) {
            locale = "lang = ua\n";
        } else if (Locale.getDefault().toString().equals("it_IT")
                || Locale.getDefault().toString().equals("it")
                || Locale.getDefault().toString().equals("it_CH")) {
            locale = "lang = it\n";
        } else if (Locale.getDefault().toString().equals("es_ES")
                || Locale.getDefault().toString().equals("es")) {
            locale = "lang = es\n";
        } else if (Locale.getDefault().toString().equals("be_BE")
                || Locale.getDefault().toString().equals("be")) {
            locale = "lang = ru\n";
        } else {
            locale = "lang = en\n";
        }

        float res = getResRatio();
        String theme = null;
        if (res == xVGA) {
            theme = "theme = android-xVGA"+suff+BR;
        } else

        if (res == HVGA) {
            theme = "theme = android-HVGA"+suff+BR;
        } else
        {
            if (Build.VERSION.SDK_INT > 10 && getResX()>900 ) {
                theme = "theme = android-Tablet"+BR;
            } else {
                theme = "theme = android-WxVGA"+suff+BR;
            }
        }

        String s = "game = "+StorageResolver.BundledGame +"\nkbd = 2\nautosave = 1\nowntheme = 1\nhl = 0\nclick = 1\nmusic = 1\nfscale = 12\njustify = 0\n"
                + locale + theme + "\n";
        return s;
    }

    private float getResRatio() {
        Display display = getWindowManager().getDefaultDisplay();
        float x = display.getWidth();
        float y = display.getHeight();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return y/x;
        } else {
            return x/y;
        }
    }

    private float getResX() {
        Display display = getWindowManager().getDefaultDisplay();
        return display.getWidth();

    }

    protected void loadData() {
        dwn = true;
        ShowDialog(getString(R.string.init));
        new DataDownloader(this, dialog);
    }

    public void ShowDialog(String m) {
        dialog.setMessage(m);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onPause() {
        onpause = true;
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onPause();
    }

    public void setDownGood() {
        dwn = false;
    }

    public void onError(String s) {
        dialog.setCancelable(true);
        dwn = false;
        Log.e("Instead-NG ERROR: ", s);
    }

    public void showRun() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        dwn = false;
        checkRC();
    }

    public boolean isOnpause() {
        return onpause;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!dwn) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            checkRC();
        } else {
            if (onpause && !dialog.isShowing()) {
                dialog.show();
            }
        }
        onpause = false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("onpause", onpause);
        savedInstanceState.putBoolean("dwn", dwn);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        dwn = savedInstanceState.getBoolean("dwn");
        onpause = savedInstanceState.getBoolean("onpause");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return true;
    }
}