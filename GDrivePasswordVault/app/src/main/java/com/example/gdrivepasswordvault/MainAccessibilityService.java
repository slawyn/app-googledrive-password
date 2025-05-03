package com.example.gdrivepasswordvault;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MainAccessibilityService extends AccessibilityService {
    private static final String TAG = "MainService";
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d(TAG, accessibilityEvent.getPackageName() +"[" + accessibilityEvent.getEventType() +"]");
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            AccessibilityNodeInfo currentNode = accessibilityEvent.getSource();
            if(currentNode != null)
            {
                if(currentNode.getClassName().equals("android.widget.EditText"))
                {
                    Log.d(TAG, currentNode.getText().toString());

                    /*
                    int i = currentNode.getChildCount();
                    while(--i>=0)
                    {

                        AccessibilityNodeInfo child = currentNode.getChild(i);
                        Log.d("AccessibilityService", (String) child.getClassName());
                        if(child.getClassName().equals("android.widget.TextView"))
                        {
                            Log.d("AccessibilityService", (String) child.getText());
                        }
                    }*/
                }
                else
                {
                    Log.d(TAG, (String) currentNode.getClassName());
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        /*
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.eventTypes=AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = null;
        setServiceInfo(info);
        */

    }
}
