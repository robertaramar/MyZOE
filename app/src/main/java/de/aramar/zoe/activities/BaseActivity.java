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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public abstract class BaseActivity extends ThemedActivity {
    private ScreenOffReceiver screenOffReceiver;
    private BroadcastReceivedCallback broadcastReceivedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.screenOffReceiver = new ScreenOffReceiver();
        this.registerReceiver(this.screenOffReceiver, this.screenOffReceiver.filter);
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(this.screenOffReceiver);

        super.onDestroy();
    }

    private void destroyIfNotMain() {
        if (this.getClass() != MainActivity.class)
            this.finish();
    }

    public void setBroadcastCallback(BroadcastReceivedCallback cb) {
        this.broadcastReceivedCallback = cb;
    }

    public class ScreenOffReceiver extends BroadcastReceiver {
        public IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if (BaseActivity.this.broadcastReceivedCallback != null)
                    BaseActivity.this.broadcastReceivedCallback.onReceivedScreenOff();

                BaseActivity.this.destroyIfNotMain();
            }
        }
    }

    interface BroadcastReceivedCallback {
        void onReceivedScreenOff();
    }
}
