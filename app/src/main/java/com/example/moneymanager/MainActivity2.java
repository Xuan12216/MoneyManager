package com.example.moneymanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.moneymanager.Fragment.CalendarFragment;
import com.example.moneymanager.Fragment.FlagFragment;
import com.example.moneymanager.Fragment.HomeFragment;
import com.example.moneymanager.Fragment.PersonFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        BottomNavigationView navigationView = findViewById(R.id.bottomNavigationView);

        int reload = getIntent().getIntExtra("Reload",-1);

        if (reload == 1) {
            MenuItem flagItem = navigationView.getMenu().findItem(R.id.flag);
            flagItem.setChecked(true);
            replaceFragment(new FlagFragment());
        }
        else replaceFragment(new HomeFragment());

        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 在這裡處理按鈕的點擊事件
                switch (item.getItemId()) {
                    case R.id.home:
                        replaceFragment(new HomeFragment());
                        return true;
                    case R.id.calendar:
                        replaceFragment(new CalendarFragment());
                        return true;
                    case R.id.flag:
                        replaceFragment(new FlagFragment());
                        return true;
                    case R.id.person:
                        replaceFragment(new PersonFragment());
                        return true;
                }
                return false;
            }
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}