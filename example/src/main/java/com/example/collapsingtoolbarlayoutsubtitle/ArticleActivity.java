package com.example.collapsingtoolbarlayoutsubtitle;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hendraanggrian.bundler.annotations.BindExtra;

import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public class ArticleActivity extends BaseActivity implements View.OnClickListener {

    private static final String OPTION_SET_TITLE = "Set title";
    private static final String OPTION_SET_SUBTITLE = "Set subtitle";
    private static final String OPTION_SET_EXPANDED_GRAVITY = "Set expanded gravity";
    private static final String OPTION_SET_COLLAPSED_GRAVITY = "Set collapsed gravity";
    private static final String OPTION_DISABLE_BACK_BUTTON = "Disable back button";
    private static final Map<CharSequence, Integer> GRAVITY = new LinkedHashMap<>();

    static {
        GRAVITY.put("START", Gravity.START);
        GRAVITY.put("TOP", Gravity.TOP);
        GRAVITY.put("END", Gravity.END);
        GRAVITY.put("BOTTOM", Gravity.BOTTOM);
        GRAVITY.put("CENTER_HORIZONTAL", Gravity.CENTER_HORIZONTAL);
        GRAVITY.put("CENTER_VERTICAL", Gravity.CENTER_VERTICAL);
        GRAVITY.put("CENTER", Gravity.CENTER);
    }

    @BindExtra int layoutRes;
    @BindView(R.id.collapsingtoolbarlayout_article) View collapsingToolbarLayout;
    @BindView(R.id.toolbar_article) Toolbar toolbar;
    @BindView(R.id.floatingactionbutton_article) FloatingActionButton floatingActionButton;

    @Override
    int getContentView() {
        return layoutRes;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSupportActionBar(toolbar);
        floatingActionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final BaseToolbarLayout toolbarLayout = getToolbarLayout();
        new MaterialDialog.Builder(this)
                .items(OPTION_SET_TITLE, OPTION_SET_SUBTITLE, OPTION_SET_EXPANDED_GRAVITY, OPTION_SET_COLLAPSED_GRAVITY, OPTION_DISABLE_BACK_BUTTON)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, final CharSequence text) {
                        switch (text.toString()) {
                            case OPTION_SET_TITLE:
                            case OPTION_SET_SUBTITLE:
                                new MaterialDialog.Builder(ArticleActivity.this)
                                        .input("Text", "", new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                switch (text.toString()) {
                                                    case OPTION_SET_TITLE:
                                                        toolbarLayout.setTitle(input);
                                                        break;
                                                    case OPTION_SET_SUBTITLE:
                                                        if (collapsingToolbarLayout instanceof SubtitleCollapsingToolbarLayout) {
                                                            ((SubtitleCollapsingToolbarLayout) collapsingToolbarLayout).setSubtitle(input);
                                                            return;
                                                        }
                                                        Toast.makeText(ArticleActivity.this, "Unsupported", Toast.LENGTH_SHORT).show();
                                                        break;
                                                }
                                            }
                                        })
                                        .show();
                                break;
                            case OPTION_SET_EXPANDED_GRAVITY:
                            case OPTION_SET_COLLAPSED_GRAVITY:
                                new MaterialDialog.Builder(ArticleActivity.this)
                                        .items(GRAVITY.keySet())
                                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] texts) {
                                                Integer flags = null;
                                                for (CharSequence text : texts) {
                                                    int flag = GRAVITY.get(text);
                                                    if (flags == null)
                                                        flags = flag;
                                                    else
                                                        flags |= flag;
                                                }
                                                if (flags != null) {
                                                    switch (text.toString()) {
                                                        case OPTION_SET_EXPANDED_GRAVITY:
                                                            toolbarLayout.setExpandedTitleGravity(flags);
                                                            break;
                                                        case OPTION_SET_COLLAPSED_GRAVITY:
                                                            toolbarLayout.setCollapsedTitleGravity(flags);
                                                            break;
                                                    }
                                                }
                                                return false;
                                            }
                                        })
                                        .positiveText(android.R.string.ok)
                                        .show();
                                break;
                            case OPTION_DISABLE_BACK_BUTTON:
                                toolbar.setNavigationIcon(null);
                                break;
                        }
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    BaseToolbarLayout getToolbarLayout() {
        return new BaseToolbarLayout() {
            @Override
            public void setTitle(CharSequence text) {
                if (collapsingToolbarLayout instanceof CollapsingToolbarLayout)
                    ((CollapsingToolbarLayout) collapsingToolbarLayout).setTitle(text);
                else if (collapsingToolbarLayout instanceof SubtitleCollapsingToolbarLayout)
                    ((SubtitleCollapsingToolbarLayout) collapsingToolbarLayout).setTitle(text);
            }

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