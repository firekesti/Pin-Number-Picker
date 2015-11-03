package net.firekesti.pinnumberpicker.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import net.firekesti.pinnumberpicker.OnFinalNumberDoneListener;
import net.firekesti.pinnumberpicker.PinNumberPicker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PinNumberPicker.loadResources(this);

        final PinNumberPicker numberPicker1 = (PinNumberPicker) findViewById(R.id.picker1);
        final PinNumberPicker numberPicker2 = (PinNumberPicker) findViewById(R.id.picker2);
        final PinNumberPicker numberPicker3 = (PinNumberPicker) findViewById(R.id.picker3);
        final PinNumberPicker numberPicker4 = (PinNumberPicker) findViewById(R.id.picker4);

        /**
         * To remove the arrows from the layout, comment out this block. Then, in the sample app's
         * dimens.xml, comment out the dimen that sets the height to "pin_number_picker_height_with_arrows"
         */
        numberPicker1.setArrowsEnabled(true);
        numberPicker2.setArrowsEnabled(true);
        numberPicker3.setArrowsEnabled(true);
        numberPicker4.setArrowsEnabled(true);

        numberPicker1.setValueRange(0, 9);
        numberPicker1.setNextNumberPicker(numberPicker2);

        numberPicker2.setValueRange(0, 9);
        numberPicker2.setNextNumberPicker(numberPicker3);

        numberPicker3.setValueRange(0, 9);
        numberPicker3.setNextNumberPicker(numberPicker4);


        numberPicker4.setValueRange(0, 9);
        numberPicker4.setOnFinalNumberDoneListener(new OnFinalNumberDoneListener() {
            @Override
            public void onDone() {
                String pin = "" + numberPicker1.getValue() + numberPicker2.getValue() +
                        numberPicker3.getValue() + numberPicker4.getValue();
                Toast.makeText(MainActivity.this, "PIN is " + pin, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
