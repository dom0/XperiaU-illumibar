package it.alcacoop.xunotifier;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.sonyericsson.illumination.IlluminationIntent;

public class NotificationsListenerService extends AccessibilityService {
  private final String tag = "NotificationService";
  PowerManager powermanager;
  boolean debug = false;

  public NotificationsListenerService() {
    debug(tag, "SERVICE STARTED");
  }


  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    String pkgname = String.valueOf(event.getPackageName());
    debug(tag, "RECEIVED NOTIFICATION FROM: " + pkgname);

    if (powermanager.isScreenOn() && (!pkgname.equals("com.android.phone"))) {
      debug(tag, "ISSCREENON");
      return;
    }
    if (pkgname.equals("com.google.android.gm")) { // GMAIL
      start_led_pulse(0xFFFF0000);
    } else if (pkgname.equals("com.google.android.talk")) { // HANGOUT
      start_led_pulse(0xFF00FF00);
    } else if (pkgname.equals("com.sonyericsson.conversations")) { // SMS
      start_led_pulse(0xFF00FF00);
    } else if (pkgname.equals("com.google.android.gms")) { // GOOGLE PLAY GAMES
      start_led_pulse(0xFFFFFF00);
    } else if (pkgname.equals("com.android.phone")) { // MISSED CALL

      List<CharSequence> notificationList = event.getText();
      for (int i = 0; i < notificationList.size(); i++) {
        debug(tag, "TEXT: " + notificationList.get(i));
      }
      String s = notificationList.get(0).toString().toLowerCase();
      if (s.contains("persa"))
        start_led_pulse(0xFFFF00FF);
    }
  }

  private void start_led_pulse(int color) {
    Intent intent = new Intent(IlluminationIntent.ACTION_START_LED_PULSE);
    intent.putExtra(IlluminationIntent.EXTRA_LED_PULSE_ON_TIME, 600);
    intent.putExtra(IlluminationIntent.EXTRA_LED_PULSE_OFF_TIME, 600);
    intent.putExtra(IlluminationIntent.EXTRA_LED_NO_OF_PULSES, 0);
    intent.putExtra(IlluminationIntent.EXTRA_LED_ID, IlluminationIntent.VALUE_BUTTON_RGB);
    intent.putExtra(IlluminationIntent.EXTRA_LED_COLOR, color);
    intent.putExtra(IlluminationIntent.EXTRA_PACKAGE_NAME, "it.alcacoop.xunotifier");
    startService(intent);
  }

  private void stop_led_pulse() {
    Intent intent = new Intent(IlluminationIntent.ACTION_STOP_LED);
    intent.putExtra(IlluminationIntent.EXTRA_LED_ID, IlluminationIntent.VALUE_BUTTON_RGB);
    intent.putExtra(IlluminationIntent.EXTRA_PACKAGE_NAME, "it.alcacoop.xunotifier");
    startService(intent);
  }

  @Override
  public void onInterrupt() {
  }


  @Override
  public void onServiceConnected() {
    debug(tag, "Service connected");

    powermanager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
    final IntentFilter theFilter = new IntentFilter();
    theFilter.addAction(Intent.ACTION_SCREEN_ON);
    theFilter.addAction(Intent.ACTION_SCREEN_OFF);
    BroadcastReceiver mPowerKeyReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String strAction = intent.getAction();
        if (strAction.equals(Intent.ACTION_SCREEN_ON)) {
          debug(tag, "SCREEN ON");
          stop_led_pulse();
        }
      }
    };
    getApplicationContext().registerReceiver(mPowerKeyReceiver, theFilter);
  }

  private void debug(String tag, String message) {
    if (debug)
      Log.d(tag, message);
  }
}
