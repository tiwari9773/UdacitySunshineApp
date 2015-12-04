package in.udacity.learning.custom_view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import in.udacity.learning.constant.AppConstant;
import in.udacity.learning.shunshine.app.R;

/**
 * Created by Lokesh on 05-12-2015.
 */
public class LocationEditTextPreference extends EditTextPreference {
    private String TAG = LocationEditTextPreference.class.getName();
    /*Define minimum length of edittext*/
    private static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;
    private int minLength;

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.location_edit_text_preference, 0, 0);
        try {
            minLength = a.getInteger(R.styleable.location_edit_text_preference_min_length, DEFAULT_MINIMUM_LOCATION_LENGTH);

            if (AppConstant.DEBUG) {
                Log.d(TAG, minLength + "");
            }
        } finally {
            a.recycle();
        }

    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Dialog d = getDialog();
                if (d instanceof AlertDialog) {
                    AlertDialog ad = (AlertDialog) d;
                    Button postviteButton = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (s.length() < minLength) {
                        postviteButton.setEnabled(false);
                    } else {
                        postviteButton.setEnabled(true);

                    }
                }

            }
        });

    }
}
