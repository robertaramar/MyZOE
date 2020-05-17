package de.aramar.zoe.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.auth0.android.jwt.JWT;

import java.text.DateFormat;
import java.util.Locale;

import de.aramar.zoe.R;
import de.aramar.zoe.security.LoginController;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private LoginController loginController;

    private LoginViewModel loginViewModel;

    private EditText countryEdit;

    private EditText usernameEdit;

    private EditText passwordEdit;

    private TextView statusTextView;

    private TextView gigyaExpiresTextView;

    private TextView kamereonExpiresTextView;

    private Switch switchSaveCredentials;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        this.loginController = LoginController.getLoginController(this
                .requireActivity()
                .getApplication());
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        this.statusTextView = root.findViewById(R.id.login_value_status);
        this.gigyaExpiresTextView = root.findViewById(R.id.login_value_gigya_expires);
        this.kamereonExpiresTextView = root.findViewById(R.id.login_value_kamereon_expires);

        root
                .findViewById(R.id.login_button_login)
                .setOnClickListener(this);
        final Button refreshButton = root.findViewById(R.id.login_button_refresh);
        refreshButton.setOnClickListener(this);
        refreshButton.setEnabled(false);

        this.loginViewModel
                .getLiveSecurityDataContainer()
                .observe(this.getViewLifecycleOwner(), securityData -> {
                    LoginFragment.this.statusTextView.setText(securityData.getText());
                    refreshButton.setEnabled(securityData
                            .getStatus()
                            .getLevel() > 0);
                    switch (securityData.getStatus()) {
                        case GIGYA_JWT_AVAILABLE:
                        case KAMEREON_JWT_AVAILABLE:
                            if (securityData.getGigyaJwt() != null) {
                                JWT gigyaJwt = new JWT(securityData.getGigyaJwt());
                                LoginFragment.this.gigyaExpiresTextView.setText(DateFormat
                                        .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                        .format(gigyaJwt.getExpiresAt()));
                            }
                            if (securityData.getKamereonJwt() != null) {
                                JWT kamereonJwt = new JWT(securityData.getKamereonJwt());
                                LoginFragment.this.kamereonExpiresTextView.setText(DateFormat
                                        .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                        .format(kamereonJwt.getExpiresAt()));
                            }
                            break;
                        default:
                            break;
                    }
                });

        this.countryEdit = root.findViewById(R.id.login_value_country);
        this.usernameEdit = root.findViewById(R.id.login_value_username);
        this.passwordEdit = root.findViewById(R.id.login_value_password);

        this.countryEdit.setText(this.loginController.getLoginCountry());
        this.usernameEdit.setText(this.loginController.getLoginUsername());
        this.passwordEdit.setText(this.loginController.getLoginPassword());

        this.switchSaveCredentials = root.findViewById(R.id.login_save_credentials);
        this.switchSaveCredentials.setChecked(this.loginController.getLoginSaveCredentials());

        return root;
    }

    private void onLoginButton(View loginButton) {
        String loginCountry = this.countryEdit
                .getText()
                .toString();
        String loginUsername = this.usernameEdit
                .getText()
                .toString();
        String loginPassword = this.passwordEdit
                .getText()
                .toString();
        if (this.switchSaveCredentials.isChecked()) {
            this.loginController.setCredentials(loginCountry, loginUsername, loginPassword, true);
            Toast
                    .makeText(this.getContext(), "Login information saved ...", Toast.LENGTH_SHORT)
                    .show();
        } else {
            this.loginController.setCredentials(loginCountry, loginUsername, loginPassword, false);
            this.loginController.clearCredentials();
        }
        this.statusTextView.setText("");
        this.gigyaExpiresTextView.setText("");
        this.kamereonExpiresTextView.setText("");
        Locale locale = Locale.forLanguageTag(loginCountry.replace("_", "-"));
        this.loginViewModel.loadConfig(locale);
    }

    private void onRefreshButton(View refreshButton) {
        Toast
                .makeText(this.getContext(), "Refresh pressed", Toast.LENGTH_LONG)
                .show();
        this.gigyaExpiresTextView.setText("");
        this.kamereonExpiresTextView.setText("");
        this.loginViewModel.refreshJwt();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button_login:
                this.onLoginButton(view);
                break;
            case R.id.login_button_refresh:
                this.onRefreshButton(view);
                break;
        }
    }
}