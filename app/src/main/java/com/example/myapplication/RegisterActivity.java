package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    private EditText etId, etPass, etName, etStoreName, etCode;
    private DBHelper dbHelper;
    private Button btnValidateCode, btnValidateEmail, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etId = findViewById(R.id.et_id);
        etPass = findViewById(R.id.et_pass);
        etName = findViewById(R.id.et_name);
        etStoreName = findViewById(R.id.et_storename);
        etCode = findViewById(R.id.et_code);
        btnValidateCode = findViewById(R.id.btn_validate_code);
        btnValidateEmail = findViewById(R.id.btn_validate_email);
        btnRegister = findViewById(R.id.btn_register);

        dbHelper = new DBHelper(this);

        btnValidateCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String storeName = etStoreName.getText().toString();
                String code = etCode.getText().toString();
                if (verifyCodeAndStore(storeName, code)) {
                    Toast.makeText(RegisterActivity.this, "인증 성공!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "인증 실패!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnValidateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = etId.getText().toString();
                if (!dbHelper.isUsernameTaken(id)) {
                    Toast.makeText(RegisterActivity.this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "아이디가 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = etId.getText().toString();
                String pass = etPass.getText().toString();
                String name = etName.getText().toString();
                String storeName = etStoreName.getText().toString();
                String code = etCode.getText().toString();

                if (verifyCodeAndStore(storeName, code)) {
                    if (!dbHelper.isUsernameTaken(id)) {
                        if (dbHelper.addUser(id, pass, "알바생", name, storeName, code)) {
                            Toast.makeText(RegisterActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "회원가입 실패!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "사용자 이름이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "인증 실패!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean verifyCodeAndStore(String storeName, String code) {
        boolean codeExists = dbHelper.storeCodeExists(code);
        String foundStoreName = dbHelper.findStoreNameByCode(code);
        return codeExists && storeName.equals(foundStoreName);
    }
}
