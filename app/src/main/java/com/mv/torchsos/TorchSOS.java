package com.mv.torchsos;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class TorchSOS extends TileService {

    private CameraManager cameraManager;
    private String cameraId;
    private Handler handler = new Handler();
    private Runnable turnOffFlash;
    private Runnable turnOnFlash;
    static boolean SOS_flashing = false;
    int shortDelay = 300; // Millisec for short flash
    int longDelay = 900; // Millisec for long flash
    int endDelay = 3000; // Millisec
    int nullDelay = 0; // Millisec for null flash
    int[] sosPattern = {shortDelay, shortDelay, shortDelay, shortDelay, shortDelay, shortDelay, shortDelay, longDelay, shortDelay, longDelay, shortDelay, longDelay, shortDelay, shortDelay, shortDelay, shortDelay, shortDelay, shortDelay, endDelay, nullDelay};
    int patternIndex = 0;

    @Override
    public void onStartListening() {
        super.onStartListening();
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile qsTile = getQsTile();
        if (qsTile.getState() == Tile.STATE_ACTIVE) {
            stopFlashing();
        } else {
            startFlashing();
        }
    }

    private void startFlashing() {
        Tile qsTile = getQsTile();
        qsTile.setState(Tile.STATE_ACTIVE);
        qsTile.updateTile();
        SOS_flashing = true;
        patternIndex = 0; // Reset the pattern index
        postNextFlash(true); // Start with the flash on
    }

    private void postNextFlash(boolean turnOn) {
        if(sosPattern[patternIndex]==nullDelay){
            patternIndex = 0;
            postNextFlash(true);
        }
        else{
            handler.postDelayed(turnOn ? turnOnFlash : turnOffFlash, sosPattern[patternIndex]);
            patternIndex = (patternIndex + 1) % sosPattern.length; // Move to the next element in the pattern
        }
    }

    private void stopFlashing() {
        SOS_flashing = false;
        Tile qsTile = getQsTile();
        handler.removeCallbacks(turnOnFlash);
        handler.removeCallbacks(turnOffFlash);
        try {
            cameraManager.setTorchMode(cameraId, false);
            qsTile.setState(Tile.STATE_INACTIVE);
            qsTile.updateTile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Initialize the Runnables
    {
        turnOnFlash = new Runnable() {
            @Override
            public void run() {
                if (SOS_flashing) {
                    try {
                        cameraManager.setTorchMode(cameraId, true);
                        postNextFlash(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        turnOffFlash = new Runnable() {
            @Override
            public void run() {
                if (SOS_flashing) {
                    try {
                        cameraManager.setTorchMode(cameraId, false);
                        postNextFlash(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

}
