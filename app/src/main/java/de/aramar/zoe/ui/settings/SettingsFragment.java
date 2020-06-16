package de.aramar.zoe.ui.settings;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Arrays;

import de.aramar.zoe.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private class EditTextListener implements EditTextPreference.OnBindEditTextListener {
        /**
         * number of allowed digits
         */
        private int digits;

        /**
         * minimum value allowed
         */
        private int min;

        /**
         * maximum value allowed
         */
        private int max;

        public EditTextListener(int digits, int min, int max) {
            this.digits = digits;
            this.min = min;
            this.max = max;
        }

        @Override
        public void onBindEditText(@NonNull EditText editText) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER); // set only numbers allowed to input
            editText.selectAll(); // select all text
            int maxLength = this.digits;
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(
                    maxLength), (source, start, end, dest, dstart, dend) -> {
                try {
                    int input = Integer.parseInt(dest.toString() + source.toString());
                    if (this.isInRange(this.min, this.max, input))
                        return null;
                } catch (NumberFormatException nfe) {
                }
                return "";
            }});
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        this.setPreferencesFromResource(R.xml.preferences, rootKey);

        EditTextPreference.OnBindEditTextListener listenerPercentage =
                new EditTextListener(3, 0, 100);

        Arrays
                .asList("notification_level_full", "notification_level_medium",
                        "notification_level_low")
                .stream()
                .map(pref_key -> {
                    return (EditTextPreference) this.findPreference(pref_key);
                })
                .forEach(preference -> {
                    preference.setOnBindEditTextListener(listenerPercentage);
                    preference.setSummaryProvider(
                            EditTextPreference.SimpleSummaryProvider.getInstance());
                });

        EditTextPreference hvac_temperature = this.findPreference("hvac_temperature");
        hvac_temperature.setOnBindEditTextListener(new EditTextListener(2, 0, 26));
        hvac_temperature.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
    }
}