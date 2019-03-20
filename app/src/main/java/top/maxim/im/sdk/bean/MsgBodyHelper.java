
package top.maxim.im.sdk.bean;

import android.util.SparseArray;

import im.floo.floolib.BMXMessage;

/**
 * Description : 消息体分类 Created by Mango on 2018/11/11.
 */
public final class MsgBodyHelper {

    private static SparseArray<BMXMessage.ContentType> sType2ClzMap = new SparseArray<>();

    static {
        sType2ClzMap.put(BMXMessage.ContentType.Text.swigValue(), BMXMessage.ContentType.Text);
        sType2ClzMap.put(BMXMessage.ContentType.Image.swigValue(), BMXMessage.ContentType.Image);
        sType2ClzMap.put(BMXMessage.ContentType.Video.swigValue(), BMXMessage.ContentType.Video);
        sType2ClzMap.put(BMXMessage.ContentType.Voice.swigValue(), BMXMessage.ContentType.Voice);
        sType2ClzMap.put(BMXMessage.ContentType.File.swigValue(), BMXMessage.ContentType.File);
        sType2ClzMap.put(BMXMessage.ContentType.Location.swigValue(),
                BMXMessage.ContentType.Location);
    }


    public static BMXMessage.ContentType getContentBodyClass(int contentType) {
        return sType2ClzMap.get(contentType);
    }
}
