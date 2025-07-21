package com.example.gdrivepasswordvault;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MainAccessibilityService extends AccessibilityService {
    private static final String TAG = "MainAccessibilityService";
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

        // Set the type of events that this service wants to listen to. Others
        // aren't passed to this service.
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;


        // Set the type of feedback your service provides.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        // Default services are invoked only if no package-specific services are
        // present for the type of AccessibilityEvent generated. This service is
        // app-specific, so the flag isn't necessary. For a general-purpose service,
        // consider setting the DEFAULT flag.

        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }
}
