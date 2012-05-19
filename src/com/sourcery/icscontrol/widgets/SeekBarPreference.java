
package com.sourcery.icscontrol.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.sourcery.icscontrol.R;

    public class SeekBarPreference extends Preference
        implements OnSeekBarChangeListener {

    public int minimum;
    public int maximum;
    public int interval;
    public int defaultValue;

    private TextView monitorBox;
    private SeekBar bar;

    private OnPreferenceChangeListener changer;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

     TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
     maximum = a.getInt(R.styleable.SeekBarPreference_seekbar_max, 100);
     minimum = a.getInt(R.styleable.SeekBarPreference_seekbar_min, 0);
     interval = a.getInt(R.styleable.SeekBarPreference_seekbar_interval, 1);
        // since we can no longer guarantee 60 is a valid default we do a little math to find a valid value 
     defaultValue = a.getInt(R.styleable.SeekBarPreference_seekbar_default_value, (int) (((maximum - minimum) / 4) * 3));
    }

    @Override
    protected View onCreateView(ViewGroup parent) {

        View layout = View.inflate(getContext(), R.layout.slider_preference, null);

        monitorBox = (TextView) layout.findViewById(R.id.monitor_box);
        bar = (SeekBar) layout.findViewById(R.id.seek_bar);
        bar.setOnSeekBarChangeListener(this);
        bar.setProgress(defaultValue - minimum);
        if (minimum == 0 && maximum == 100) monitorBox.setText(defaultValue + "%");
        else monitorBox.setText(defaultValue + "");

        return layout;
    }

    public int getDefaultValue() {
        return defaultValue;
    }
    
    public void setInitValue(int progress) {
        defaultValue = progress - minimum;
    }
    
    public void setValue(int value) {
        if(bar != null)
            bar.setProgress(value - minimum);
    }

    public void setMinimum(int min_value) {
        minimum = min_value;
    }

    public void setMaximum(int max_value) {
        maximum = max_value;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public void setInterval(int interval_value) {
        interval = interval_value;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return super.onGetDefaultValue(a, index);
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        changer = onPreferenceChangeListener;
        super.setOnPreferenceChangeListener(onPreferenceChangeListener);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progress = Math.round(((float) progress) / interval) * interval;

        // because you never know
        if (progress > maximum) progress = maximum;
        if (progress < minimum) progress = minimum;

        seekBar.setProgress(progress - minimum);
        if (minimum == 0 && maximum == 100) monitorBox.setText(progress - minimum + "%");
        else monitorBox.setText(progress - minimum + "");
        changer.onPreferenceChange(this, Integer.toString(progress - minimum));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
