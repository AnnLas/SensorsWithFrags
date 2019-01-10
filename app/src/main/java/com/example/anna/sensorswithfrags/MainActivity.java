package com.example.anna.sensorswithfrags;

import android.os.Bundle;

import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {



    private Fragment gyroFragment;
    private Fragment acceleroFragment;
    private FragmentManager fragmentManager;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected( MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentManager.beginTransaction().hide(gyroFragment).show(acceleroFragment).commit();
                    return true;
                case R.id.navigation_dashboard:
                    if (gyroFragment == null) {
                        gyroFragment = new GyroFragment();
                        fragmentManager.beginTransaction().hide(acceleroFragment).add(R.id.fragment_container, gyroFragment).commit();
                    } else
                        fragmentManager.beginTransaction().hide(acceleroFragment).show(gyroFragment).commit();
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        acceleroFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        gyroFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (acceleroFragment == null) {
            acceleroFragment = new AcceleroFragment();
            fragmentManager.beginTransaction().add(R.id.fragment_container, acceleroFragment).commit();
        }
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }


}
