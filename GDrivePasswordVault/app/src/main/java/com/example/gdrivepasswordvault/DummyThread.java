package com.example.gdrivepasswordvault;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

public class DummyThread extends Thread{

        private Object sync;
        private boolean mRunning;
        private MainActivity mActivity;
        public DummyThread(MainActivity act){
            super();
            mActivity = act;
        }

        @Override
        public void run() {


            mRunning = true;
            sync = new Object();
            long mLastMilliseconds = System.currentTimeMillis();
            long mCurrentMilliseconds;
            long mTotal = 0;
            long mTimeout = 0;
            while (mRunning) {
                mCurrentMilliseconds = System.currentTimeMillis();
                mTotal = mTotal + (mCurrentMilliseconds - mLastMilliseconds);
            }
        }

        private void waitOnEvent(){
        synchronized (sync){
            try {
                sync.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

        public void generateEvent(){
        synchronized (sync) {
            sync.notify();
        }
    }

}
