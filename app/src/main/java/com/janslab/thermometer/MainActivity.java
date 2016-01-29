package com.janslab.thermometer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Kofi Gyan on 1/29/2016.
 */
public class MainActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.dummy_thermometer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),DummyThermometerActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.thermometer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ThermometerActivity.class);
                startActivity(intent);

            }
        });

        findViewById(R.id.light_sensor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LightSensorMeterActivity.class);
                startActivity(intent);

            }
        });



    }
}
