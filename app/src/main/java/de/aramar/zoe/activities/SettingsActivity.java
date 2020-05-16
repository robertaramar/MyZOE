package de.aramar.zoe.activities;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.Toast;

import de.aramar.zoe.R;
import de.aramar.zoe.ui.settings.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View-Elemente aus XML-Layout Datei erzeugen lassen
        setContentView(R.layout.activity_settings);

        // Initialisieren der App Bar und Aktivieren des Up-Buttons
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings_activity);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.action_settings));

        ViewStub stub = findViewById(R.id.container_stub);
        stub.inflate();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}