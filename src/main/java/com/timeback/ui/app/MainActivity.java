package com.timeback.ui.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Single Activity — Navigation Component로 Fragment를 교체한다.
 * Jetpack Compose 대신 Fragment + XML 방식 사용 (Java 호환).
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView(R.layout.activity_main);
        // NavHostFragment를 호스트로 사용
        // NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
        //         .findFragmentById(R.id.nav_host_fragment);
        // navController = navHostFragment.getNavController();
    }
}
