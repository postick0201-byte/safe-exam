package kr.co.safetyexam.app;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(ExamGuardPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
