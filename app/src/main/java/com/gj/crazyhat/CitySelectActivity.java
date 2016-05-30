package com.gj.crazyhat;


import android.app.Activity;
import android.os.Bundle;
import com.gj.crazyhat.city.CityManager;

public class CitySelectActivity extends Activity {

    private CityManager mCityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_select);

        mCityManager = new CityManager(this);
    }



}
