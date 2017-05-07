package com.example.collapsingtoolbarlayoutsubtitle;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.view.Gravity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hendraanggrian.bundler.annotations.BindExtra;

import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public class ArticleActivity extends BaseActivity implements View.OnClickListener {

    private static final Map<CharSequence, Integer> OPTIONS_GRAVITY = new LinkedHashMap<>();

    static {
        OPTIONS_GRAVITY.put("START", Gravity.START);
        OPTIONS_GRAVITY.put("TOP", Gravity.TOP);
        OPTIONS_GRAVITY.put("END", Gravity.END);
        OPTIONS_GRAVITY.put("BOTTOM", Gravity.BOTTOM);
        OPTIONS_GRAVITY.put("CENTER_HORIZONTAL", Gravity.CENTER_HORIZONTAL);
        OPTIONS_GRAVITY.put("CENTER_VERTICAL", Gravity.CENTER_VERTICAL);
        OPTIONS_GRAVITY.put("CENTER", Gravity.CENTER);
    }

    @BindExtra int layoutRes;
    @BindView(R.id.collapsingtoolbarlayout_article) View collapsingToolbarLayout;
    @BindView(R.id.floatingactionbutton_article) FloatingActionButton floatingActionButton;

    @Override
    int getContentView() {
        return layoutRes;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        floatingActionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final BaseToolbarLayout toolbarLayout = getToolbarLayout();
        new MaterialDialog.Builder(this)
                .items("Set expanded gravity", "Set collapsed gravity")
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, final int position, CharSequence text) {
                        new MaterialDialog.Builder(ArticleActivity.this)
                                .items(OPTIONS_GRAVITY.keySet())
                                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] texts) {
                                        Integer flags = null;
                                        for (CharSequence text : texts) {
                                            int flag = OPTIONS_GRAVITY.get(text);
                                            if (flags == null)
                                                flags = flag;
                                            else
                                                flags |= flag;
                                        }
                                        if (flags != null) {
                                            switch (position) {
                                                case 0:
                                                    toolbarLayout.setExpandedTitleGravity(flags);
                                                    break;
                                                case 1:
                                                    toolbarLayout.setCollapsedTitleGravity(flags);
                                                    break;
                                            }
                                        }
                                        return false;
                                    }
                                })
                                .positiveText(android.R.string.ok)
                                .show();
                    }
                })
                .show();
    }

    @NonNull
    BaseToolbarLayout getToolbarLayout() {
        return new BaseToolbarLayout() {
            @Override
            public void setExpandedTitleGravity(int gravity) {
                if (collapsingToolbarLayout instanceof CollapsingToolbarLayout)
                    ((CollapsingToolbarLayout) collapsingToolbarLayout).setExpandedTitleGravity(gravity);
                else if (collapsingToolbarLayout instanceof SubtitleCollapsingToolbarLayout)
                    ((SubtitleCollapsingToolbarLayout) collapsingToolbarLayout).setExpandedTitleGravity(gravity);
            }

            @Override
            public void setCollapsedTitleGravity(int gravity) {
                if (collapsingToolbarLayout instanceof CollapsingToolbarLayout)
                    ((CollapsingToolbarLayout) collapsingToolbarLayout).setCollapsedTitleGravity(gravity);
                else if (collapsingToolbarLayout instanceof SubtitleCollapsingToolbarLayout)
                    ((SubtitleCollapsingToolbarLayout) collapsingToolbarLayout).setCollapsedTitleGravity(gravity);
            }
        };
    }
}