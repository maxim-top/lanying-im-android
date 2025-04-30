
package top.maxim.im.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;

import top.maxim.im.R;
import top.maxim.im.message.utils.MessageConfig;

public class PasswordActivity extends AppCompatActivity {

    private final int NUM_CHARS = 4;
    private EditText[] passwordEt = new EditText[NUM_CHARS];
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable[] hideRunnables = new Runnable[NUM_CHARS];
    private String[] inputCache = new String[NUM_CHARS]; // 用于缓存真实输入值

    public static void open(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, PasswordActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_mode_password);

        // 初始化控件
        passwordEt[0] = findViewById(R.id.et_password_1);
        passwordEt[1] = findViewById(R.id.et_password_2);
        passwordEt[2] = findViewById(R.id.et_password_3);
        passwordEt[3] = findViewById(R.id.et_password_4);

        setupListeners();
    }

    private void setupListeners() {
        for (int i = 0; i < NUM_CHARS; i++) {
            final int index = i;
            passwordEt[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 输入完成后自动跳转
                    if (s.length() == 1 && index < NUM_CHARS - 1) {
                        passwordEt[index + 1].requestFocus();
                        // 取消之前的隐藏任务
                        if (hideRunnables[index] != null) {
                            handler.removeCallbacks(hideRunnables[index]);
                        }

                        // 延迟隐藏真实字符
                        hideRunnables[index] = () -> {
                            passwordEt[index].removeTextChangedListener(this);
                            passwordEt[index].setText("&");
                            passwordEt[index].addTextChangedListener(this);
                        };

                        handler.postDelayed(hideRunnables[index], 200);
                    }
                    if (count > 0) {
                        inputCache[index] = s.subSequence(start, start + count).toString();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            // 修正退格键处理逻辑
            passwordEt[i].setOnKeyListener((v, keyCode, event) -> {
                // 将 View 强制转换为 EditText
                if (v instanceof EditText) {
                    EditText et = (EditText) v;
                    if (keyCode == KeyEvent.KEYCODE_DEL
                            && event.getAction() == KeyEvent.ACTION_UP
                            && et.getText().length() == 0
                            && index > 0) {
                        passwordEt[index - 1].requestFocus();
                        return true;
                    }
                }
                return false;
            });
        }
        // 按钮点击
        findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String password = getPassword();
            if (validatePassword(password)) {
                // 处理密码验证逻辑
                Intent intent = new Intent();
                intent.putExtra("password", password);
                setResult(RESULT_OK, intent);
                finish(); // 关闭当前页面
            }
        });

        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }

    private String getPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NUM_CHARS; i++) {
            sb.append(inputCache[i].toString());
        }
        return sb.toString();
    }

    private boolean validatePassword(String password) {
        // 基础验证示例
        if (password.length() != 4) {
            Toast.makeText(this, "密码必须4位", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}