package com.example.rssreader;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Simulate a back press to return to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // kličemo metodo nadrazreda, da zagotovimo da je ustvarjeno vse potrebno iz nadrazreda
        setContentView(R.layout.activity_detail); // nastavimo layout

        ActionBar actionBar = getSupportActionBar(); // kličemo actionbar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // ne prikažemo naslova
            actionBar.setDisplayHomeAsUpEnabled(true); // prikažemo gumb za nazaj
        }


        ImageView imageView = findViewById(R.id.detail_image);
        TextView titleView = findViewById(R.id.detail_title);
        TextView descriptionView = findViewById(R.id.detail_description);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String imageUrl = intent.getStringExtra("imageUrl");
        String description = intent.getStringExtra("description");

        titleView.setText(title);
        descriptionView.setText(description);
        Picasso.get().load(imageUrl).into(imageView);
    }
}
