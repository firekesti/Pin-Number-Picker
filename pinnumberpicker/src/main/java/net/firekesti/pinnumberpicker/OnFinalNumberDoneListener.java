package net.firekesti.pinnumberpicker;

import android.util.Log;

/**
 * A listener that triggers when the user is done with their numerical input.
 */
public class OnFinalNumberDoneListener implements PinNumberPicker.DoneListener {
    @Override
    public void onDone() {
        Log.d("PinNumberPicker", "Number Picking is done!");
    }
}
