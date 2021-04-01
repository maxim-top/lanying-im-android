package top.maxim.im.rtc.view;

import android.content.Context;
import android.util.AttributeSet;

import org.webrtc.SurfaceViewRenderer;

public class RTCRenderView extends SurfaceViewRenderer {

    public RTCRenderView(Context context) {
        super(context);
    }

    public RTCRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
