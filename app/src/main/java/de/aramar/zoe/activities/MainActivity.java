package de.aramar.zoe.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import de.aramar.zoe.R;
import de.aramar.zoe.security.LoginController;

public class MainActivity extends BaseActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        DrawerLayout drawer = this.findViewById(R.id.drawer_layout);
        NavigationView navigationView = this.findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        this.mAppBarConfiguration =
                new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_location, R.id.nav_login,
                        R.id.nav_slideshow, R.id.nav_share, R.id.nav_send)
                        .setDrawerLayout(drawer)
                        .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController,
                this.mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        if (!LoginController
                .getLoginController(this.getApplication())
                .getLoginSaveCredentials()) {
            navController.navigate(R.id.nav_login);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this
                .getMenuInflater()
                .inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController,
                this.mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            this.startActivity(aboutIntent);
            return true;
        }
        if (id == R.id.action_settings) {
            this.startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
