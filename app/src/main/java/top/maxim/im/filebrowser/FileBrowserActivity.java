package top.maxim.im.filebrowser;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import top.maxim.im.R;
import top.maxim.im.common.utils.AppContextUtils;
import top.maxim.im.common.utils.FileConfig;

/**
 * Created by zhengmin on 2018/12/17.
 */

public class FileBrowserActivity extends ListActivity {
    private static final String TAG = FileBrowserActivity.class.getSimpleName() + "--->";
    private String rootPath;
    private List<String> pathList;
    private List<String> itemsList;
    private TextView curPathTextView;
    private File priDir;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        initView();
        initFile();
    }

    private void initView() {
        curPathTextView = (TextView) findViewById(R.id.curPath);
    }

    private void initFile() {
        rootPath = getFilesDir().getParent();
        getFileDir(rootPath);
        priDir = new File(FileConfig.DIR_APP_CRASH_LOG);
        if (!priDir.exists()) {
            priDir.mkdirs();
        }
    }


    private void getFileDir(String filePath) {
        curPathTextView.setText(filePath);
        itemsList = new ArrayList<>();
        pathList = new ArrayList<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (!filePath.equals(rootPath)) {
            itemsList.add("b1");
            pathList.add(rootPath);
            itemsList.add("b2");
            pathList.add(file.getParent());
        }
        if (files == null) {
            Toast.makeText(this, "所选SD卡为空！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            // if (checkSpecificFile(f)) {
            itemsList.add(f.getName());
            pathList.add(f.getPath());
            //   }
        }
        setListAdapter(new FileAdapter(this, itemsList, pathList));
    }

    public String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        if (start != -1) {
            return pathandname.substring(start + 1, pathandname.length());
        } else {
            return null;
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File file = new File(pathList.get(position));
        if (file.isDirectory()) {
            getFileDir(file.getPath());
        } else {
            copySdcardFile(file.getPath(), priDir + "/" + getFileName(file.getPath()));
            //Toast.makeText(this, file.getPath(), Toast.LENGTH_SHORT).show();
        }
    }

    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static void copySdcardFile(String fromFile, String toFile) {
        InputStream fosfrom = null;
        OutputStream fosto = null;
        try {
             fosfrom = new FileInputStream(fromFile);
             fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }

            Toast.makeText(AppContextUtils.getAppContext(), "文件已拷贝至" + toFile, Toast.LENGTH_SHORT)
                    .show();

        } catch (Exception ex) {
        }
        finally {

            try {
                if (fosfrom != null) {
                    fosfrom.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fosto != null) {
                    fosto.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
