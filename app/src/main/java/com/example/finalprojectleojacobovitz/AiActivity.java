package com.example.finalprojectleojacobovitz;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalprojectleojacobovitz.gemini.GeminiCallback;
import com.example.finalprojectleojacobovitz.gemini.GeminiManager;

public class AiActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    private EditText etQuestion;
    private Button btnAsk, btnHome;
    private TextView tvAnswer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);
        etQuestion = findViewById(R.id.etQuestion);
        tvAnswer = findViewById(R.id.tvAnswer);
        btnAsk = findViewById(R.id.btnAsk);
        btnAsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnHome = findViewById(R.id.btnHome);
                btnHome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

                String q = etQuestion.getText().toString();
                if(q.equals(""))
                    q = "מה הספר הכי נפוץ בישראל";

                String prompt = q + "תשובה עד 50 מילים";
                //String prompt = "What is the capital of France?";
                GeminiManager.getInstance().sendMessage(prompt, new GeminiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        runOnUiThread(() ->
                                {
                                    tvAnswer.setText(response);
                                }
                        );
                    }

/*                    @Override
                    public void onError(Throwable e) {
                        //runOnUiThread(() ->System.out.println("שגיאה: " + e.getMessage()));
                        runOnUiThread(() ->Log.e(TAG, "שגיאה: " + e.getMessage()));
                        //Toast.makeText(MainActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();


                    }*/

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Gemini error", e); // prints full stack trace, not just message
                        Toast.makeText(AiActivity.this,
                                "Error: " + e.getClass().getName() + " / " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }


                    @Override
                    public void onError(Exception e) {
                        //runOnUiThread(() -> System.out.println("שגיאה: " + e.getMessage()));
                        runOnUiThread(() ->Log.e(TAG, "שגיאה: " + e.getMessage()));

                        //Toast.makeText(MainActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

}