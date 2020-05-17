package de.aramar.zoe.ui.about;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import de.aramar.zoe.R;
import de.aramar.zoe.utilities.Tools;

public class BaseAboutFragment extends Fragment {
    private static final String GITHUB_URI = "https://github.com/robertaramar/MyZOE";

    private static final String CHANGELOG_URI = GITHUB_URI + "/blob/master/CHANGELOG.md";

    private static final String MIT_URI = GITHUB_URI + "/blob/master/LICENSE";

    private static final String FAQ_URI = GITHUB_URI + "/wiki/Frequently-Asked-Questions";

    private static final String AUTHOR1_GITHUB = "https://github.com/robertaramar";

    private static final String CONTRIBUTORS_URI = GITHUB_URI + "/graphs/contributors";

    private static final String SUPPORT_URI = GITHUB_URI + "/blob/master/README.md#contribute";

    static final int[] imageResources =
            {R.id.aboutImgVersion, R.id.aboutImgLicense, R.id.aboutImgChangelog, R.id.aboutImgSource, R.id.aboutImgFaq, R.id.aboutImgContributors, R.id.aboutImgSupport};

    static long lastTap = 0;

    static int taps = 0;

    static Toast currentToast = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        ColorFilter filter =
                Tools.getThemeColorFilter(this.getActivity(), android.R.attr.textColorSecondary);
        for (int i : imageResources) {
            ImageView imgView = v.findViewById(i);
            imgView
                    .getDrawable()
                    .setColorFilter(filter);
        }

        String versionName = "";
        try {
            PackageInfo packageInfo = this
                    .getActivity()
                    .getPackageManager()
                    .getPackageInfo(this
                            .getActivity()
                            .getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        LinearLayout versionLayout = v.findViewById(R.id.about_layout_version);

        versionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long thisTap = System.currentTimeMillis();

                if (thisTap - lastTap < 500) {
                    taps = taps + 1;

                    if (currentToast != null && taps <= 7)
                        currentToast.cancel();

                    if (taps >= 3 && taps <= 7)
                        currentToast = Toast.makeText(BaseAboutFragment.this.getActivity(),
                                String.valueOf(taps), Toast.LENGTH_SHORT);

                    if (taps == 7) {
                        currentToast = Toast.makeText(BaseAboutFragment.this.getActivity(),
                                R.string.about_toast_special_features_enabled, Toast.LENGTH_LONG);
                        BaseAboutFragment.this.enableSpecialFeatures();
                    }

                    if (currentToast != null)
                        currentToast.show();
                } else {
                    taps = 0;
                }

                lastTap = thisTap;
            }
        });

        TextView version = v.findViewById(R.id.about_text_version);
        version.setText(versionName);

        LinearLayout license = v.findViewById(R.id.about_layout_license);
        LinearLayout changelog = v.findViewById(R.id.about_layout_changelog);
        LinearLayout source = v.findViewById(R.id.about_layout_source);
        LinearLayout faq = v.findViewById(R.id.about_layout_faq);

        license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseAboutFragment.this.openURI(MIT_URI);
            }
        });
        changelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseAboutFragment.this.openURI(CHANGELOG_URI);
            }
        });
        source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseAboutFragment.this.openURI(GITHUB_URI);
            }
        });
        faq.setOnClickListener((View view) -> this.openURI(FAQ_URI));

        LinearLayout author1Layout = v.findViewById(R.id.aboutLayoutAuthor1);
        author1Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseAboutFragment.this.openURI(AUTHOR1_GITHUB);
            }
        });

        LinearLayout contributors = v.findViewById(R.id.about_layout_contributors);
        contributors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseAboutFragment.this.openURI(CONTRIBUTORS_URI);
            }
        });

        LinearLayout supportLayout = v.findViewById(R.id.about_layout_support);
        supportLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseAboutFragment.this.openURI(SUPPORT_URI);
            }
        });

        return v;
    }

    private void enableSpecialFeatures() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder
                .setTitle(R.string.about_title_special_features)
                .setMessage(R.string.about_dialog_special_features)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast
                                .makeText(BaseAboutFragment.this.getActivity(),
                                        R.string.about_toast_special_features, Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()
                .show();
    }

    public void openURI(String uri) {
        Intent openURI = new Intent(Intent.ACTION_VIEW);
        openURI.setData(Uri.parse(uri));
        this.startActivity(openURI);
    }

    public void copyToClipboard(String uri) {
        ClipboardManager clipboard = (ClipboardManager) this
                .getActivity()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("MyZOE", uri);
        clipboard.setPrimaryClip(clip);
        Toast
                .makeText(this.getActivity(),
                        this.getString(R.string.about_toast_copied_to_clipboard),
                        Toast.LENGTH_SHORT)
                .show();
    }
}
