
package top.maxim.im.common.utils;

import android.os.SystemClock;
import android.view.View;

import java.util.Arrays;

/**
 * Description : 多次点击
 */
public class ClickTimeUtils {

    /* 间隔时间 */
    private static long mTimeBetween = 1000;

    public static void setClickTimes(View view, final int times, final IClick click) {
        final long[] hits = new long[times];
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(hits, 1, hits, 0, times - 1);
                hits[times - 1] = SystemClock.uptimeMillis();

                if (hits[times - 1] - hits[0] <= mTimeBetween) {
                    Arrays.fill(hits, 0); // 数据全部置零
                    if (click != null) {
                        click.onClick(); // 设置事件的回调
                    }
                }
            }
        });
    }

    public interface IClick {
        void onClick();
    }
}
