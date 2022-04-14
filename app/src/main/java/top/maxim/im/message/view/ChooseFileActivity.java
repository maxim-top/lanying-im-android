
package top.maxim.im.message.view;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.maxim.im.R;
import top.maxim.im.common.base.BaseTitleActivity;
import top.maxim.im.common.bean.FileBean;
import top.maxim.im.common.utils.TimeUtils;
import top.maxim.im.common.view.Header;
import top.maxim.im.common.view.recyclerview.BaseRecyclerAdapter;
import top.maxim.im.common.view.recyclerview.BaseViewHolder;
import top.maxim.im.common.view.recyclerview.DividerItemDecoration;

/**
 * Description : 选择文件
 */
public class ChooseFileActivity extends BaseTitleActivity {

    private RecyclerView mExListView;

    private DocFileAdapter mAdapter;

    private ProgressBar progressBar;

    public static final int FILE_UNSELECTED = -1;

    public static final int FILE_SELECTED = 1;

    private List<FileBean> mChooseFile = new ArrayList<>();

    private final int CHOOSE_MAX_COUNT = 1;

    public static final String CHOOSE_FILE_DATA = "chooseFileData";

    public static void openChooseFileActivity(Context context, int requestCode) {
        Intent intent = new Intent(context, ChooseFileActivity.class);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected Header onCreateHeader(RelativeLayout headerContainer) {
        Header.Builder builder = new Header.Builder(this, headerContainer);
        builder.setBackIcon(R.drawable.header_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        builder.setRightText(R.string.confirm, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(CHOOSE_FILE_DATA, (Serializable)mChooseFile);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        builder.setTitle(R.string.choose_file);
        return builder.build();
    }

    @Override
    protected View onCreateView() {
        View view = View.inflate(this, R.layout.activity_choose_file_view, null);
        mExListView = view.findViewById(R.id.elv_doc_file);
        progressBar = view.findViewById(R.id.file_progress);
        mExListView.setLayoutManager(new LinearLayoutManager(this));
        mExListView.addItemDecoration(new DividerItemDecoration(this, R.color.guide_divider));
        mExListView.setAdapter(mAdapter = new DocFileAdapter(this));
        setViewListener();
        return view;
    }

    /**
     * 初始化监听
     */
    @Override
    protected void setViewListener() {
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileBean bean = mAdapter.getItem(position);
                if (bean == null) {
                    return;
                }
                if (bean.getStatus() == FILE_UNSELECTED) {
                    if (mChooseFile.size() >= CHOOSE_MAX_COUNT) {
                        return;
                    }
                    bean.setStatus(FILE_SELECTED);
                    mChooseFile.add(bean);
                } else {
                    bean.setStatus(FILE_UNSELECTED);
                    mChooseFile.remove(bean);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void initDataForActivity() {
        super.initDataForActivity();
        String[] selectType = new String[] {
                ".doc", ".docx", ".xls", "xlsx", ".pdf", ".ppt", ".pptx", ".pages", ".numbers",
                ".txt", ".rar", ".zip", ".apk"
        };
        Observable<List<FileBean>> observable = getDocFiles(this, selectType);
        if (observable == null) {
            return;
        }
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<FileBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        showDocFiles(null);
                    }

                    @Override
                    public void onNext(List<FileBean> fileBeans) {
                        showDocFiles(fileBeans);
                    }
                });
    }

    private void showDocFiles(List<FileBean> fileBeans) {
        if (fileBeans != null && fileBeans.size() > 0) {
            if (mAdapter != null) {
                progressBar.setVisibility(View.GONE);
                mAdapter.replaceList(fileBeans);
            }
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * 获取文件
     * 
     * @param context
     * @param docMimeTypes
     * @return
     */
    private Observable<List<FileBean>> getDocFiles(Context context, final String... docMimeTypes) {
        if (docMimeTypes == null || docMimeTypes.length == 0) {
            return Observable.empty();
        }
        return Observable.just(context).map(new Func1<Context, List<FileBean>>() {
            @Override
            public List<FileBean> call(Context context) {
                Uri mImageUri = MediaStore.Files.getContentUri("external");
                ContentResolver mContentResolver = context.getContentResolver();
                String[] queryColumn = {
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DATE_MODIFIED,
                        MediaStore.Files.FileColumns.SIZE
                };
                StringBuilder selection = new StringBuilder();
                // 文件mimeType在安卓系统的数据表有时候为空 所以按照后缀名搜索
                for (int i = 0; i < docMimeTypes.length; i++) {
                    if (i != 0) {
                        selection.append(" or ");
                    }
                    selection.append(MediaStore.Files.FileColumns.DATA).append(" ").append("LIKE")
                            .append(" '%").append(docMimeTypes[i]).append("'");
                }
                String order = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC ";
                Cursor mCursor = mContentResolver.query(mImageUri, queryColumn,
                        selection.toString(), null, order);
                try {
                    if (mCursor == null) {
                        return null;
                    }

                    List<FileBean> files = new ArrayList<>();

                    while (mCursor.moveToNext()) {
                        long size = mCursor.getLong(2);
                        if (size > 0) {
                            FileBean bean = new FileBean();
                            String path = mCursor.getString(0);
                            if (TextUtils.isEmpty(path)) {
                                continue;
                            }
                            int last = path.lastIndexOf("/") + 1;
                            if (last != -1 && last < path.length()) {
                                String title = path.substring(last, path.length());
                                bean.setDesc(title);
                                bean.setPath(path);
                                bean.setCreateTime(mCursor.getLong(1));
                                // 此处没有使用size 是因为有些文件size和file.length有时候不相等。导致一些不必要的bug
                                bean.setSize(new File(bean.getPath()).length());
                                bean.setStatus(FILE_UNSELECTED);
                                if (title.endsWith(".doc") || title.endsWith(".docx")) {
                                    bean.setSuffix("doc");
                                } else if (title.endsWith(".txt")) {
                                    bean.setSuffix("txt");
                                } else if (title.endsWith(".xls") || title.endsWith(".xlsx")) {
                                    bean.setSuffix("xls");
                                } else if (title.endsWith(".pdf")) {
                                    bean.setSuffix("pdf");
                                } else if (title.endsWith(".ppt") || title.endsWith(".pptx")) {
                                    bean.setSuffix("ppt");
                                } else if (title.endsWith(".rar")) {
                                    bean.setSuffix("rar");
                                } else if (title.endsWith(".zip")) {
                                    bean.setSuffix("zip");
                                } else if (title.endsWith(".apk")) {
                                    bean.setSuffix("apk");
                                }
                                files.add(bean);
                            }
                        }
                    }
                    return files;
                } catch (Exception e) {
                    Log.e("ChooseFileActivity", e.getMessage());
                } finally {
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                }
                return null;
            }
        });
    }

    /**
     * 展示文件的adapter
     */
    private class DocFileAdapter extends BaseRecyclerAdapter<FileBean> {

        public DocFileAdapter(Context context) {
            super(context);
        }

        @Override
        protected int onCreateViewById(int viewType) {
            return R.layout.item_file_view;
        }

        @Override
        protected void onBindHolder(BaseViewHolder holder, int position) {
            CheckBox cbChoose = holder.findViewById(R.id.cb_doc_file_choice);
            ImageView ivDocIcon = holder.findViewById(R.id.iv_doc_icon);
            TextView tvDocTitle = holder.findViewById(R.id.tv_doc_title);
            TextView tvDocSize = holder.findViewById(R.id.tv_doc_size);
            TextView tvDocTime = holder.findViewById(R.id.tv_doc_create_time);

            FileBean bean = getItem(position);

            if (bean != null) {
                cbChoose.setChecked(bean.getStatus() != FILE_UNSELECTED);
                tvDocTitle.setText(bean.getDesc());
                String time = TimeUtils.millis2String(mContext, bean.getCreateTime() * 1000);
                tvDocTime.setText(!TextUtils.isEmpty(time) ? time : "");
                String size = Formatter.formatFileSize(mContext, bean.getSize());
                tvDocSize.setText(size);
                ivDocIcon.setImageResource(R.drawable.chat_file_default_icon);
            }
        }
    }
}
