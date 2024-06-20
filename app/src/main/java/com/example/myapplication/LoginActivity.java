package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private EditText etId, etPass;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etId = findViewById(R.id.et_id);
        etPass = findViewById(R.id.et_pass);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnRegister = findViewById(R.id.btn_register);

        dbHelper = new DBHelper(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = etId.getText().toString();
                String pass = etPass.getText().toString();

                if (dbHelper.isValidUser(id, pass)) {
                    // 로그인 성공 시 사용자 이름을 가져옴
                    String name = dbHelper.getUserName(id);
                    // Login successful
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("USERNAME", id);
                    intent.putExtra("NAME", name);
                    startActivity(intent);
                    finish();
                } else {
                    // Login failed
                    Toast.makeText(LoginActivity.this, "Invalid ID or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
