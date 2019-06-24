
package top.maxim.im.common.utils.video;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.util.List;

import top.maxim.im.R;

/**
 * Description :长按录制，点击拍照页面 Created by zhuxinhong on 2017/3/14. Job
 * number：135198 Phone ：13520295328 Email：zhuxinhong@syswin.com Person in charge
 * : zhuxinhong Leader：zhuxinhong
 */
public class PhotoRecorderActivity extends Activity implements IRecordView {
    public static final String EXTRA_CAMERA_PATH = "camera_path";

    public static final String EXTRA_VIDEO_PATH = "video_path";

    public static final String EXTRA_VIDEO_DURATION = "duration";

    public static final String EXTRA_RECORD_TYPE = "record_type";

    private VideoMediaRecorder mRecorder = new VideoMediaRecorder();

    private VideoRecorderLayout mRecorderLayout;

    /**
     * 1 up,2 left ,3 down,4 right
     */
    private int mOrient = 1;

    private SensorManager mSensorManager = null;

    private Sensor mAccelerometer;

    private Sensor mMagnetic;

    private float[] accelerometerValues = new float[3];

    private float[] magneticFieldValues = new float[3];

    SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
            }
            float[] values = new float[3];
            float[] R = new float[9];
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
            SensorManager.getOrientation(R, values);
            // values[0] = (float) Math.toDegrees(values[0]);
            double y = (float)Math.toDegrees(values[1]);
            double z = (float)Math.toDegrees(values[2]);

            final int orient = mOrient;
            if (z > 30 && z < 150) {
                if (y > -30 && y < 30) {
                    mOrient = 4;
                }
            } else if (z < -30 && z > -150) {
                if (y > -30 && y < 30) {
                    mOrient = 2;
                }
            } else if (y > 25 && y < 150) {
                mOrient = 3;
            } else {
                mOrient = 1;
            }
            if (mOrient != orient) {
                orientChanged(orient, mOrient);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video_record_layout);
        initRecorderLayout();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            Log.i("PhotoRecorderActivity", "mSensorManager is null");
            return;
        }
        List<Sensor> senorList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (senorList != null && senorList.size() > 0) {
            mAccelerometer = senorList.get(0);
        }
        senorList = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if (senorList != null && senorList.size() > 0) {
            mMagnetic = senorList.get(0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * 初始化录制UI布局
     */
    private void initRecorderLayout() {
        mRecorderLayout = (VideoRecorderLayout)findViewById(R.id.sv_recorder_layout);
        mRecorderLayout.setRecordView(PhotoRecorderActivity.this);
        mRecorderLayout.setRecorder(mRecorder);
    }

    @Override
    protected void onResume() {
        mSensorManager.registerListener(mSensorListener, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorListener, mMagnetic,
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();

    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        mRecorder.endRecord(null);
        super.onDestroy();
    }

    @Override
    public void onCloseRecord() {
        setResult(RESULT_CANCELED);
        onBackPressed();
    }

    @Override
    public void onFinished(int type, String path, int width, int height, int duration) {
        Intent it = new Intent();
        it.putExtra(EXTRA_RECORD_TYPE, type);
        if (type == 1) {
            it.putExtra(EXTRA_VIDEO_PATH, path);
            it.putExtra(EXTRA_VIDEO_DURATION, duration);
        } else {
            it.putExtra(EXTRA_CAMERA_PATH, path);
        }
        it.putExtra("width", width);
        it.putExtra("height", height);
        setResult(RESULT_OK, it);
        // 刷新本地相册
        // CameraUtils.refreshingMediaScanner(this, path);
        onBackPressed();
    }

    @Override
    public void orientChanged(int originOrient, int orient) {
        mRecorderLayout.requestOrient(originOrient, orient);
    }

    @Override
    public void onBackPressed() {
        finish();
        // overridePendingTransition(0, R.anim.out_left_out);
    }
}
