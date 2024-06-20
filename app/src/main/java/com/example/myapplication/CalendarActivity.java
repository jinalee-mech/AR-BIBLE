package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {
    private String selectedDate;
    private String currentUserName;
    private CalendarView calendarView;
    private Button addButton;
    private TextView dateTextView;
    private LinearLayout scheduleLayout;
    private Spinner scheduleSpinner;
    private Map<String, List<String>> scheduleMap = new HashMap<>();
    private static Map<String, SubstituteRequest> substituteRequests = new HashMap<>();
    private static final String SCHEDULE_FILE = "schedule_data.dat";
    private static final String SUBSTITUTE_REQUEST_FILE = "substitute_requests.dat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        dateTextView = findViewById(R.id.dateTextView);
        scheduleLayout = findViewById(R.id.scheduleLayout);
        addButton = findViewById(R.id.addButton);
        scheduleSpinner = findViewById(R.id.scheduleSpinner);

        // 로그인한 사용자 이름과 아이디 받기
        Intent intent = getIntent();
        currentUserName = intent.getStringExtra("NAME");

        // 스케줄 유형을 스피너에 추가
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.schedule_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);

        loadScheduleMap();
        loadSubstituteRequests();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                dateTextView.setText(selectedDate);
                updateScheduleLayout();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String schedule = scheduleSpinner.getSelectedItem().toString();
                if (!schedule.isEmpty()) {
                    addSchedule(selectedDate, schedule);
                    updateScheduleLayout();
                    saveScheduleMap();
                }
            }
        });

        Button homeButton = findViewById(R.id.button_home);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarActivity.this, HomeActivity.class);
                intent.putExtra("NAME", currentUserName);
                startActivity(intent);
            }
        });

        // 로그인 후 알림을 확인
        checkSubstituteRequests();
    }

    private void addSchedule(String date, String schedule) {
        String scheduleWithUser = schedule + " (" + currentUserName + ")";
        List<String> schedules = scheduleMap.getOrDefault(date, new ArrayList<>());
        schedules.add(scheduleWithUser);
        scheduleMap.put(date, schedules);
    }

    private void updateScheduleLayout() {
        scheduleLayout.removeAllViews();
        List<String> schedules = scheduleMap.get(selectedDate);
        if (schedules != null && !schedules.isEmpty()) {
            for (String schedule : schedules) {
                TextView scheduleTextView = new TextView(this);
                scheduleTextView.setText(schedule);
                scheduleTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showScheduleOptionsDialog(schedule);
                    }
                });
                scheduleLayout.addView(scheduleTextView);
            }
        } else {
            TextView noScheduleTextView = new TextView(this);
            noScheduleTextView.setText("No schedules for this date.");
            scheduleLayout.addView(noScheduleTextView);
        }
    }

    private void showScheduleOptionsDialog(String schedule) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("스케줄 옵션")
                .setItems(new String[]{"대타 요청", "스케줄 삭제"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            requestSubstitute(selectedDate);
                            break;
                        case 1:
                            deleteSchedule(selectedDate, schedule);
                            break;
                    }
                })
                .show();
    }

    private void deleteSchedule(String date, String schedule) {
        List<String> schedules = scheduleMap.get(date);
        if (schedules != null) {
            schedules.remove(schedule);
            if (schedules.isEmpty()) {
                scheduleMap.remove(date);
            }
            Toast.makeText(this, "스케줄이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            updateScheduleLayout();
            saveScheduleMap();
        }
    }

    private void requestSubstitute(String date) {
        if (scheduleMap.containsKey(date)) {
            SubstituteRequest request = new SubstituteRequest(currentUserName, date, scheduleSpinner.getSelectedItem().toString());
            substituteRequests.put(date, request);
            saveSubstituteRequests(); // 대타 요청을 저장
            Toast.makeText(this, "대타 요청이 전송되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "대타 요청할 스케줄이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSubstituteRequests() {
        for (Map.Entry<String, SubstituteRequest> entry : substituteRequests.entrySet()) {
            SubstituteRequest request = entry.getValue();
            if ("requested".equals(request.getStatus()) && !request.getRequester().equals(currentUserName)) {
                new AlertDialog.Builder(this)
                        .setTitle("대타 요청")
                        .setMessage(request.getDate() + "에 " + request.getRequester() + "님의 " + request.getScheduleType() + " 대타 요청이 있습니다. 수락하시겠습니까?")
                        .setPositiveButton("수락", (dialog, which) -> {
                            request.setAcceptor(currentUserName);
                            request.setStatus("accepted");
                            Toast.makeText(CalendarActivity.this, "대타 요청을 수락했습니다.", Toast.LENGTH_SHORT).show();
                            updateScheduleWithSubstitute(request.getDate(), request.getScheduleType(), currentUserName);
                            saveSubstituteRequests();
                            saveScheduleMap();
                        })
                        .setNegativeButton("거절", (dialog, which) -> dialog.dismiss())
                        .show();
            } else if ("accepted".equals(request.getStatus()) && request.getRequester().equals(currentUserName)) {
                Toast.makeText(this, request.getDate() + "에 대한 대타 요청이 수락되었습니다. 수락자: " + request.getAcceptor(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateScheduleWithSubstitute(String date, String scheduleType, String substituteName) {
        List<String> schedules = scheduleMap.get(date);
        if (schedules != null) {
            for (int i = 0; i < schedules.size(); i++) {
                if (schedules.get(i).contains(scheduleType)) {
                    schedules.set(i, scheduleType + " (" + substituteName + ")");
                    updateScheduleLayout();
                    saveScheduleMap();
                    return;
                }
            }
        }
    }

    private static class SubstituteRequest implements java.io.Serializable {
        private String requester;
        private String acceptor;
        private String date;
        private String scheduleType;
        private String status;

        public SubstituteRequest(String requester, String date, String scheduleType) {
            this.requester = requester;
            this.date = date;
            this.scheduleType = scheduleType;
            this.status = "requested";
        }

        public String getRequester() {
            return requester;
        }

        public String getAcceptor() {
            return acceptor;
        }

        public void setAcceptor(String acceptor) {
            this.acceptor = acceptor;
        }

        public String getDate() {
            return date;
        }

        public String getScheduleType() {
            return scheduleType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    private void saveScheduleMap() {
        try (FileOutputStream fos = openFileOutput(SCHEDULE_FILE, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(scheduleMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadScheduleMap() {
        try (FileInputStream fis = openFileInput(SCHEDULE_FILE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            scheduleMap = (HashMap<String, List<String>>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            scheduleMap = new HashMap<>();
        }
    }

    private void saveSubstituteRequests() {
        try (FileOutputStream fos = openFileOutput(SUBSTITUTE_REQUEST_FILE, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(substituteRequests);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSubstituteRequests() {
        try (FileInputStream fis = openFileInput(SUBSTITUTE_REQUEST_FILE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            substituteRequests = (HashMap<String, SubstituteRequest>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            substituteRequests = new HashMap<>();
        }
    }
}
