package com.worksy.ui.jobseeker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.worksy.R;

public class CareerAssessmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career_assessment);
        // Add logic for career assessment here
    }

    public static class IndustryInsightsActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_industry_insights);
            // Add logic for industry insights here
        }
    }
}
