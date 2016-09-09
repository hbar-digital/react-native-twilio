package com.rogchap.react.modules.twilio;

import android.support.annotation.Nullable;

import android.Manifest;
import android.util.Log;
import android.app.PendingIntent;
import android.content.Intent;

import android.content.Context;
import android.content.Intent;
// import android.os.Parcelable;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.app.PendingIntent;

import android.media.AudioManager;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;
import com.facebook.react.modules.core.DeviceEventManagerModule;


import com.twilio.client.Connection;
import com.twilio.client.ConnectionListener;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;
import com.twilio.client.Twilio;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;


public class TwilioModule extends ReactContextBaseJavaModule implements DeviceListener, ConnectionListener {

    private static final String TAG = TwilioModule.class.getName();

    private ReactContext reactContext;
    private Device device;
    private Connection connection;
    private Promise connectionPromise;


    public TwilioModule(ReactApplicationContext reactContext) {
        super(reactContext);

        this.reactContext = reactContext;
    }

    // private void sendEvent(String eventName, @Nullable WritableMap params) {
    //     getReactApplicationContext()
    //             .getJSModule(RCTNativeAppEventEmitter.class)
    //             .emit(eventName, params);
    //     Log.d(TAG, "event sent " + eventName);
    // }

    @Override
    public String getName() {
        return "Twilio";
    }

    @ReactMethod
    public void initWithToken(final String token, final Promise promise) {

        if(!Twilio.isInitialized()) {

            Twilio.initialize(reactContext, new Twilio.InitListener() {

                @Override
                public void onInitialized() {
                    Twilio.setLogLevel(Log.DEBUG);

                    createDevice(token, promise);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, e.getMessage());

                    promise.reject(e);
                }
            });
        } else {
            createDevice(token, promise);
        }
    }

    private void createDevice(String capabilityToken, Promise promise) {
        try {
            if (device == null) {
                device = Twilio.createDevice(capabilityToken, this);
            } else {
                device.updateCapabilityToken(capabilityToken);
            }

            promise.resolve(Arguments.createMap());
        } catch (Exception e) {
            Log.e(TAG, "An error has occured updating or creating a Device: \n" + e.toString());

            promise.reject(e);
        }
    }

    @ReactMethod
    public void connect(ReadableMap params, Promise promise) {
        if (device != null) {
            connectionPromise = promise;
            connection = device.connect(toMap(params), this);
        } else {
            promise.reject("device is null");
        }
    }

    @ReactMethod
    public void disconnect(Promise promise) {
        if (connection != null) {
          // connectionPromise = promise;

          connection.disconnect();
          promise.resolve(Arguments.createMap());
        }
    }

    @ReactMethod
    public void setSpeaker(boolean enabled) {
        AudioManager audioManager = (AudioManager) getCurrentActivity().getBaseContext().getSystemService(Context.AUDIO_SERVICE);

        audioManager.setSpeakerphoneOn(enabled);
    }




    public static Map<String, String> toMap(ReadableMap readableMap) {
        if (readableMap == null) {
            return null;
        }

        com.facebook.react.bridge.ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        if (!iterator.hasNextKey()) {
            return null;
        }

        Map<String, String> result = new HashMap<>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            result.put(key, readableMap.getString(key));
        }

        return result;
    }


    public static void sendEvent(ReactContext reactContext, String eventName, Object params) {
        if (reactContext != null)
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        else
            Log.e(TAG, "Could not submit event for a null context...");
    }


    /* Device Listener */
    @Override
    public void onStartListening(Device device) {
        Log.d(TAG, "Device has started listening for incoming connections");
    }

    @Override
    public void onStopListening(Device device) {
        Log.d(TAG, "Device has stopped listening for incoming connections");
    }

    @Override
    public void onStopListening(Device device, int errorCode, String error) {
        Log.e(TAG, String.format("Device has encountered an error and has stopped listening for incoming connections: %s", error));
    }

    @Override
    public boolean receivePresenceEvents(Device device) {
        return false;
    }

    @Override
    public void onPresenceChanged(Device device, PresenceEvent presenceEvent) {
    }



    /* ConnectionListener */

  @Override
  public void onConnecting(Connection connection) {
    Log.d(TAG, "onConnecting <------------------");
  }

  @Override
  public void onConnected(Connection connection) {
    Log.d(TAG, "onConnected <------------------");

    WritableMap params = Arguments.createMap();
    params.putString("CallSid", connection.getParameters().get(Connection.IncomingParameterCallSIDKey));

    sendEvent(reactContext, "connectionDidConnect", params);
    // connectionPromise.resolve(Arguments.createMap());
  }

  @Override
  public void onDisconnected(Connection connection) {
    Log.d(TAG, "onDisconnected <------------------");
    if (connection == this.connection) {
        // connectionPromise.resolve(Arguments.createMap());
        this.connection = null;
    }
  }

  @Override
  public void onDisconnected(Connection connection, int errorCode, String errorMessage) {
    Log.d(TAG, "onDisconnected error <------------------");
    // Map errors = new HashMap();
    // errors.put("err", errorMessage);
    // sendEvent("connectionDidFail", errors);
  }

}
