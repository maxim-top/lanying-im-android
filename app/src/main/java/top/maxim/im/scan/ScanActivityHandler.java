/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.maxim.im.scan;

import android.os.Handler;
import android.os.Message;

import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.util.Map;

import top.maxim.im.scan.camera.CameraManager;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 */
public final class ScanActivityHandler extends Handler {

    private final QrcodeScanner activity;

    private final DecodeThread decodeThread;

    private State state;

    private final CameraManager cameraManager;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public ScanActivityHandler(QrcodeScanner activity, Map<DecodeHintType, ?> baseHints,
            String characterSet, CameraManager cameraManager) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case QrcodeScanner.restart_preview:
                restartPreviewAndDecode();
                break;
            case QrcodeScanner.decode_succeeded:
                state = State.SUCCESS;
                activity.handleDecode((Result)message.obj);
                // sendEmptyMessageDelayed(QrcodeScanner.restart_preview, 5000);
                break;
            case QrcodeScanner.decode_failed:
                // We're decoding as fast as possible, so when one decode fails,
                // start another.
                state = State.PREVIEW;
                activity.bitmapBright(message.arg1);
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), QrcodeScanner.decode);
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), QrcodeScanner.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause()
            // will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(QrcodeScanner.restart_preview);
        removeMessages(QrcodeScanner.decode_succeeded);
        removeMessages(QrcodeScanner.decode_failed);
    }

    public void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), QrcodeScanner.decode);
        }
    }

}
