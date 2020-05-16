/*
 * Copyright (C) 2017-2018 Jakob Nixdorf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.aramar.zoe.activities;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import de.aramar.zoe.utilities.Tools;

public abstract class ThemedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setLocale();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        this.setLocale();

        super.onResume();
    }

    public void setLocale() {
        Locale locale = Tools.getSystemLocale();
        Locale.setDefault(locale);

        Resources resources = this.getBaseContext().getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;

        // TODO: updateConfiguration is marked as deprecated. Replace with android.content.Context.createConfigurationContext
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
