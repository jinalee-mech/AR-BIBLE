package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NoticeActivity extends AppCompatActivity {
    private LinearLayout noticeContainer;
    private EditText searchField;
    private List<Notice> notices;
    private String currentUserName;
    private static final String FILE_NAME = "notices.dat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        noticeContainer = findViewById(R.id.notice_container);
        searchField = findViewById(R.id.search_field);

        // Get the current user's name from the intent
        Intent intent = getIntent();
        currentUserName = intent.getStringExtra("NAME");

        // Load notices from file
        loadNotices();

        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString();
            List<Notice> filteredNotices = filterNotices(notices, query);
            displayNotices(filteredNotices);
        });

        Button addNoticeButton = findViewById(R.id.add_notice_button);
        addNoticeButton.setOnClickListener(v -> showAddNoticeDialog());

        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(NoticeActivity.this, HomeActivity.class);
            homeIntent.putExtra("NAME", currentUserName);
            startActivity(homeIntent);
        });

        displayNotices(notices);
    }

    private void displayNotices(List<Notice> notices) {
        noticeContainer.removeAllViews();
        for (Notice notice : notices) {
            LinearLayout noticeView = new LinearLayout(this);
            noticeView.setOrientation(LinearLayout.VERTICAL);
            noticeView.setPadding(16, 16, 16, 16);

            TextView titleTextView = new TextView(this);
            titleTextView.setText(notice.getTitle());
            titleTextView.setTextSize(18);
            titleTextView.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView contentTextView = new TextView(this);
            contentTextView.setText(notice.getContent());
            contentTextView.setTextSize(16);

            TextView authorTextView = new TextView(this);
            authorTextView.setText("작성자: " + notice.getAuthor());
            authorTextView.setTextSize(14);

            noticeView.addView(titleTextView);
            noticeView.addView(contentTextView);
            noticeView.addView(authorTextView);

            noticeView.setOnClickListener(v -> showOptionsDialog(notice));

            noticeContainer.addView(noticeView);
        }
    }

    private List<Notice> filterNotices(List<Notice> notices, String query) {
        List<Notice> filteredNotices = new ArrayList<>();
        for (Notice notice : notices) {
            if (notice.getTitle().contains(query) || notice.getContent().contains(query)) {
                filteredNotices.add(notice);
            }
        }
        return filteredNotices;
    }

    private void showAddNoticeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_notice);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); // 팝업창 크기 조절

        EditText titleField = dialog.findViewById(R.id.notice_title);
        EditText contentField = dialog.findViewById(R.id.notice_content);

        Button saveButton = dialog.findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            String title = titleField.getText().toString();
            String content = contentField.getText().toString();

            if (!title.isEmpty() && !content.isEmpty()) {
                notices.add(new Notice(title, content, currentUserName));
                displayNotices(notices);
                saveNotices();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showOptionsDialog(Notice notice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("옵션 선택")
                .setItems(new String[]{"수정", "삭제"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditNoticeDialog(notice);
                    } else if (which == 1) {
                        showDeleteNoticeDialog(notice);
                    }
                })
                .show();
    }

    private void showEditNoticeDialog(Notice notice) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_notice);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); // 팝업창 크기 조절

        EditText titleField = dialog.findViewById(R.id.notice_title);
        EditText contentField = dialog.findViewById(R.id.notice_content);

        titleField.setText(notice.getTitle());
        contentField.setText(notice.getContent());

        Button saveButton = dialog.findViewById(R.id.save_button);
        saveButton.setText("수정");
        saveButton.setOnClickListener(v -> {
            String title = titleField.getText().toString();
            String content = contentField.getText().toString();

            if (!title.isEmpty() && !content.isEmpty()) {
                notice.setTitle(title);
                notice.setContent(content);
                displayNotices(notices);
                saveNotices();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showDeleteNoticeDialog(Notice notice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("공지 삭제")
                .setMessage("이 공지를 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    notices.remove(notice);
                    displayNotices(notices);
                    saveNotices();
                    Toast.makeText(this, "공지 삭제 완료", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void saveNotices() {
        try (FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(notices);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNotices() {
        try (FileInputStream fis = openFileInput(FILE_NAME);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            notices = (ArrayList<Notice>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            notices = new ArrayList<>();
        }
    }

    private static class Notice implements java.io.Serializable {
        private String title;
        private String content;
        private String author;

        public Notice(String title, String content, String author) {
            this.title = title;
            this.content = content;
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }
}
