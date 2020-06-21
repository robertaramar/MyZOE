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

package de.aramar.zoe.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.util.Log;

import com.android.volley.VolleyError;

import org.apache.commons.collections4.CollectionUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.aramar.zoe.R;
import de.aramar.zoe.data.kamereon.error.ErrorBody;
import de.aramar.zoe.data.kamereon.error.ErrorEnvelope;
import de.aramar.zoe.network.Mapper;

public class Tools {

    /* Get a color based on the current theme */
    private static int getThemeColor(Context context, int colorAttr) {
        Resources.Theme theme = context.getTheme();
        TypedArray arr = theme.obtainStyledAttributes(new int[]{colorAttr});

        int colorValue = arr.getColor(0, -1);
        arr.recycle();

        return colorValue;
    }

    /* Create a ColorFilter based on the current theme */
    public static ColorFilter getThemeColorFilter(Context context, int colorAttr) {
        return new PorterDuffColorFilter(getThemeColor(context, colorAttr), PorterDuff.Mode.SRC_IN);
    }

    public static Locale getSystemLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Resources.getSystem()
                    .getConfiguration()
                    .getLocales()
                    .get(0);
        } else {
            return Resources.getSystem()
                    .getConfiguration().locale;
        }
    }

    public static String getLocalizedTimestamp(String stringTimestamp) {
        String formattedTimestamp = stringTimestamp;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try { // Fixes bug #3
                DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .withLocale(Locale.getDefault())
                        .withZone(ZoneId.systemDefault());
                if (stringTimestamp.contains("Z")) { // Ph2 seems to report zulu time
                    Instant instant = Instant.parse(stringTimestamp);
                    formattedTimestamp = formatter.format(instant);
                } else { // Ph1 seems to report offset date time
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(stringTimestamp);
                    formattedTimestamp = formatter.format(offsetDateTime);
                }
            } catch (Exception e) {
                Log.d(Tools.class.getCanonicalName(), "Cannot parse timestamp of " + stringTimestamp);
            }
        }
        return formattedTimestamp;
    }

    /**
     * @param throwable A throwable which information shall be displayed
     * @param context   A context to be used to address the UI
     */
    public static void displayError(Throwable throwable, Context context) {
        ErrorBody errorBody = new ErrorBody();
        // If all goes wrong, it' an unknown error ;-)
        errorBody.setError(context.getString(R.string.error_alert_unknown));
        if (throwable instanceof VolleyError) {
            VolleyError volleyError = (VolleyError) throwable;
            if (volleyError.networkResponse != null) {
                errorBody.setStatus(volleyError.networkResponse.statusCode);

                if (volleyError.networkResponse.data != null) {
                    String jsonEnvelope = new String(volleyError.networkResponse.data);
                    ErrorEnvelope errorEnvelope = Mapper.object(jsonEnvelope, ErrorEnvelope.class);
                    errorBody = CollectionUtils.emptyIfNull(errorEnvelope.getErrors())
                            .stream()
                            .filter(Objects::nonNull)
                            .map(error -> {
                                String text = error.getErrorMessage()
                                        .replaceAll("\\\\\"", "\"");
                                // if it's not JSON in the error-message, make one up
                                if (!text.startsWith("{")) {
                                    ErrorBody tmpErrorBody = new ErrorBody();
                                    tmpErrorBody.setStatus(volleyError.networkResponse.statusCode);
                                    tmpErrorBody.setError(text);
                                    text = Mapper.string(tmpErrorBody);
                                }
                                return text;
                            })
                            .map(jsonBody -> {
                                return Mapper.object(jsonBody, ErrorBody.class);
                            })
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(errorBody);
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (errorBody.getStatus() != null) {
            stringBuilder.append(
                    String.format(context.getString(R.string.error_alert_line_status), errorBody.getStatus()));
        }
        if (errorBody.getError() != null) {
            stringBuilder.append((stringBuilder.length() > 0) ? "\n" : "");
            stringBuilder.append(
                    String.format(context.getString(R.string.error_alert_line_error), errorBody.getError()));
        }
        if (errorBody.getAdditionalProperties() != null && errorBody.getAdditionalProperties()
                .size() > 0) {
            Object errorObject = errorBody.getAdditionalProperties()
                    .get("errors");
            if (errorObject != null && errorObject instanceof ArrayList) {
                List<HashMap<String, String>> errorList = (List) errorObject;
                HashMap<String, String> errorMap = errorList.get(0);
                errorMap.forEach((key, value) -> {
                    stringBuilder.append((stringBuilder.length() > 0) ? "\n" : "");
                    stringBuilder.append(key + " : " + value);
                });
            } else {
                errorBody.getAdditionalProperties()
                        .forEach((key, value) -> {
                            stringBuilder.append((stringBuilder.length() > 0) ? "\n" : "");
                            stringBuilder.append(key + " : " + value);

                        });
            }
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.error_alert_title);
        alert.setMessage(stringBuilder);
        alert.setPositiveButton("OK", null);
        alert.show();
    }
}
