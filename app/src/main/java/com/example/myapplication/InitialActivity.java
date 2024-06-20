package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InitialActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText storeNameField, authCodeField, usernameField, passwordField, nameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        dbHelper = new DBHelper(this);

        storeNameField = findViewById(R.id.storeNameField);
        authCodeField = findViewById(R.id.authCodeField);
        usernameField = findViewById(R.id.nameField1);
        passwordField = findViewById(R.id.passwordField1);
        nameField = findViewById(R.id.nameField); // 이름 입력 필드 추가

        Button createStoreButton = findViewById(R.id.createStoreButton);
        createStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createStore();
            }
        });
    }

    private void createStore() {
        String storeName = storeNameField.getText().toString();
        String authCode = authCodeField.getText().toString();
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        String name = nameField.getText().toString(); // 이름 가져오기

        if (authCode.length() == 5 && authCode.matches("\\d{5}") && !dbHelper.storeCodeExists(authCode) && !dbHelper.storeNameExists(storeName)) {
            if (!dbHelper.isUsernameTaken(username)) {
                dbHelper.addStore(storeName, authCode);
                dbHelper.addUser(username, password, "방장", name, storeName, authCode); // 이름 추가
                Toast.makeText(this, "가게가 성공적으로 생성되었습니다.", Toast.LENGTH_SHORT).show();
                // 가게 생성 후 로그인 화면으로 이동
                Intent intent = new Intent(InitialActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "사용자 이름이 이미 존재합니다. 다른 이름을 선택하세요.", Toast.LENGTH_SHORT).show();
            }
        } else if (dbHelper.storeCodeExists(authCode)) {
            Toast.makeText(this, "동일한 인증번호를 가진 가게가 존재합니다. 다른 것을 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if (dbHelper.storeNameExists(storeName)) {
            Toast.makeText(this, "동일한 가게이름을 가진 가게가 존재합니다. 다른 것을 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "유효한 5자리 인증번호를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
