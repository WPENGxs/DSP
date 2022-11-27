package com.wpeng.dsp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.zjy.audiovisualize.view.AudioVisualizeView;

import java.util.List;

public class AudioActivity extends AppCompatActivity {
    private AudioVisualizeView AudioVisualize;
    private boolean isStarted=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        AudioVisualize = findViewById(R.id.audio_visualize_view);
        Button start=findViewById(R.id.start);
        Button stop=findViewById(R.id.stop);
        getPermissions();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isStarted) {
                    AudioVisualize.doPlay(R.raw.sound);
                    Toast.makeText(AudioActivity.this,"Play raw sound(default)",Toast.LENGTH_SHORT).show();
                    isStarted=true;
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AudioVisualize != null){
                    AudioVisualize.release();
                    isStarted=false;
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if (AudioVisualize != null) {
            AudioVisualize.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (AudioVisualize != null) {
            AudioVisualize.release();
        }
    }

    public void getPermissions(){
        XXPermissions.with(AudioActivity.this)
                // 申请单个权限
                .permission(Permission.RECORD_AUDIO)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        //Toast.makeText(AudioActivity.this,"获取权限成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            Toast.makeText(AudioActivity.this,"被拒绝授权，请手动授予录音权限",Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            //XXPermissions.startPermissionActivity(AudioActivity.this, permissions);
                        } else {
                            Toast.makeText(AudioActivity.this,"获取录音权限失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        /*XXPermissions.with(AudioActivity.this)
                // 申请单个权限
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        //Toast.makeText(AudioActivity.this,"获取权限成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            Toast.makeText(AudioActivity.this,"被拒绝授权，请手动授予读写权限",Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            //XXPermissions.startPermissionActivity(AudioActivity.this, permissions);
                        } else {
                            Toast.makeText(AudioActivity.this,"获取读写权限失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
    }
}