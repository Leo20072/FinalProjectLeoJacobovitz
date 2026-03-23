package com.example.finalprojectleojacobovitz;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

import java.util.Calendar;

public class SettingActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "AlarmApp";
    private static final String CHANNEL_ID = "AlarmNotificationChannel";

    private static final int DAILY_ALARM_REQUEST_CODE = 0;
    private static final int TEST_ALARM_REQUEST_CODE = 1;
    private TextView btn_back;
    Button btnEditProfile;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> finish());
        createNotificationChannel();

        Button btnSetDaily = findViewById(R.id.btnSetDaily);
        btnSetDaily.setOnClickListener(v -> {
            if (checkPermissions()) {
                scheduleDailyAlarm();
            } else {
                requestPermissions();
            }
        });

        Button btnTestNow = findViewById(R.id.btnTestNow);
        btnTestNow.setOnClickListener(v -> {
            if (checkPermissions()) {
                scheduleTestAlarm();
            } else {
                requestPermissions();
            }
        });

        Button btnCancelAlarm = findViewById(R.id.btnCancelAlarm);
        btnCancelAlarm.setOnClickListener(v -> {
            cancelDailyAlarm();
        });

        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "הרשאת התראות ניתנה", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "יש לאשר התראות כדי לקבל תזכורות", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scheduleDailyAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // הפניה ל-BroadcastReceiver שלנו במקום ל-Activity
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                DAILY_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );

            Log.d(TAG, "Daily alarm scheduled for: " + calendar.getTime());
            Toast.makeText(this, "תזכורת קריאה הוגדרה ל-10:00 בבוקר", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleTestAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                TEST_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + 5000;

        if (alarmManager != null) {
            // התיקון: בדיקה והפניה למסך ההגדרות הנכון
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // אנדרואיד 12 ומעלה
                if (!alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "מעביר אותך להגדרות לאישור תזמון מדויק...", Toast.LENGTH_LONG).show();

                    // פותח בדיוק את המסך שבו מאשרים את ההרשאה הזו לאפליקציה שלך
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    settingsIntent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(settingsIntent);

                    return; // עוצר את הפעולה עד שהמשתמש יאשר ויחזור
                }
            }

            // אם יש הרשאה, ממשיכים כרגיל
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );

            Log.d(TAG, "Test alarm set for 5 seconds from now");
            Toast.makeText(this, "התראת ניסיון תופיע בעוד 5 שניות...", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelDailyAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                DAILY_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Daily alarm cancelled");
            Toast.makeText(this, "תזכורת הקריאה בוטלה", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "תזכורות קריאה",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}