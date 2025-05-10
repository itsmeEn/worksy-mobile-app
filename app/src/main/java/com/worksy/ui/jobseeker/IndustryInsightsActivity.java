package com.worksy.ui.jobseeker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.worksy.R;
import android.view.View;
import android.widget.Button; // Import the Button class

public class IndustryInsightsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_industry_insights);

        // Set up the back button click listener to finish the activity.
        Button backButton = (Button) findViewById(R.id.backButton); // Cast to Button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}