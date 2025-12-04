package com.example.tapetrove.Activity.Home;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.tapetrove.Activity.Authentication.SignInActivity;
import com.example.tapetrove.Activity.Profile.ProfileActivity;
import com.example.tapetrove.Activity.Search.SearchActivity;
import com.example.tapetrove.Api.ApiResponse;
import com.example.tapetrove.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // --- PERBAIKAN: Pemeriksaan sesi pengguna dilakukan pertama kali ---
        if (currentUser == null) {
            // Jika tidak ada pengguna yang login, langsung ke SignInActivity dan hentikan eksekusi lebih lanjut.
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
            return; // Penting untuk menghentikan eksekusi onCreate lebih lanjut
        }

        // --- Logika normal hanya berjalan jika pengguna sudah login ---
        Toast.makeText(MainActivity.this, "Selamat datang " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (item.getItemId() == R.id.search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // Handle intent dari SearchAdapter, hanya jika ini bukan cold start (savedInstanceState == null)
        Intent intent = getIntent();
        if (savedInstanceState == null && intent != null && intent.getExtras() != null && intent.hasExtra("movie")) {
            ApiResponse.Movie movie = (ApiResponse.Movie) intent.getSerializableExtra("movie");
            if (movie != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("film", movie);
                replaceFragmentWithBundle(new PeminjamanFragment(), bundle);
            }
        } else {
            // Jika tidak ada intent khusus, muat fragment default (HomeFragment)
            replaceFragment(new HomeFragment());
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && currentFragment instanceof PeminjamanFragment) {
            findViewById(R.id.bottomNavigation).setVisibility(View.GONE);
        } else {
            findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void replaceFragmentWithBundle(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }
}
