package com.mv.torchsos;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

public class TorchBlink extends TileService {


    private CameraManager cameraManager;
    private String cameraId;
    private Handler handler = new Handler();
    private Runnable turnOffFlash;
    private Runnable turnOnFlash;
    static boolean SOS_flashing = false;
    int delay = 300; // Millisec

    @Override
    public void onStartListening() {
        super.onStartListening();
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0]; // Usually, 0 is the back-facing camera
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile qsTile = getQsTile();
        Log.d("QWER", "TileState = " + qsTile.getState());
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
        final int[] i = {0};
        SOS_flashing = true;
        turnOnFlash = new Runnable() {
            @Override
            public void run() {
                if (SOS_flashing) {
                    try {
                        i[0]++;
                        Log.w("QWER", "Flash ON, SOS_flashing = " + SOS_flashing + "    i = " + i[0]);
                        cameraManager.setTorchMode(cameraId, true); // Turn ON the flash
                        handler.postDelayed(turnOffFlash, delay);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.w("QWER", "Quitting");
                        cameraManager.setTorchMode(cameraId, false); // Ensure the flash is turned off
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
                        cameraManager.setTorchMode(cameraId, false); // Turn OFF the flash
                        handler.postDelayed(turnOnFlash, delay);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        handler.post(turnOnFlash);
    }



    private void stopFlashing() {
        SOS_flashing = false;
        Tile qsTile = getQsTile();
        handler.removeCallbacks(turnOnFlash);
        handler.removeCallbacks(turnOffFlash);
        try {
            cameraManager.setTorchMode(cameraId, false); // Ensure the flash is turned off
            SOS_flashing = false;
            qsTile.setState(Tile.STATE_INACTIVE);
            qsTile.updateTile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
