package restart.com.asynctaskdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/*
* 1、网络上请求数据：申请网络权限 读写存储权限
* 2、布局我们的layout
* 3、下载支线我们要做什么？UI
* 4、下载中我们要做什么？ 数据
* 5、下载后我们要做什么？UI
*
* */
public class MainActivity extends FragmentActivity {

    public static final String TAG = "TAG";
    public static final int MY_PERMISSION_CODE = 10086;
    public static final int INIT_PROGRESS = 0;
    public static final String APK_URL = "http://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk";
    public static final String FILE_NAME = "mimooc.apk";
    private boolean permissionFlag = false;
    private ProgressBar progressBar;
    private Button button;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //检查权限
        boolean isAllGranted = checkPermissionALLGranted(new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });
        if (isAllGranted) {
            permissionFlag = true;
        }
        if (!isAllGranted) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, MY_PERMISSION_CODE);
        }
        if (!permissionFlag) {
            return;
        }


//         初始化视图
        initView();
        //设置点击监听
        setListener();
        //初始化ui
        setData();
    }

    private void setData() {
        progressBar.setProgress(INIT_PROGRESS);
        button.setText(R.string.click_download);
        textView.setText(R.string.download_text);
    }

    private void setListener() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                下载的事情
                // TODO: 2018/1/6 下载任务
                DownloadAsyncTask asyncTask = new DownloadAsyncTask();
                asyncTask.execute(APK_URL);
            }
        });
    }

    /**
     * 初始化视图
     */
    private void initView() {
        progressBar = findViewById(R.id.progressBar);
        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

    }

    public class DownloadAsyncTask extends AsyncTask<String, Integer, Boolean> {

        private String mFilePath;

        /**
         * 在异步任务之前  主线程
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            可操作UI 准备工作
            button.setText(R.string.downloading);
            textView.setText(R.string.downloading);
            progressBar.setProgress(INIT_PROGRESS);
        }

        /**
         * 在另外一个线程中处理事件
         *
         * @param strings 可变参数
         * @return 结果
         */
        @Override
        protected Boolean doInBackground(String... strings) {
//            publishProgress();抛出进度
            if (strings != null && strings.length > 0) {
                String apkUrl = strings[0];

                try {
//                    构造URL
                    URL url = new URL(apkUrl);
                    //构造连接并打开
                    URLConnection urlConnection = url.openConnection();

                    InputStream inputStream = urlConnection.getInputStream();
//                    获得下载内容的总长度
                    int contentLength = urlConnection.getContentLength();

//                   下载地址准备
                    mFilePath = Environment.getExternalStorageDirectory()
                            + File.separator + FILE_NAME;
                    //对下载地址进行处理
                    File apkFile = new File(mFilePath);
                    if (apkFile.exists()) {
                        boolean result = apkFile.delete();
                        if (!result) {
                            return false;
                        }
                    }

                    //已下载大小
                    int downloadSize = 0;

                    byte[] bytes = new byte[1024];

                    int length;
//                  创建一个输出管道
                    OutputStream outputStream = new FileOutputStream(mFilePath);
                    while ((length = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes,0,length);
                        downloadSize += length;
//                        发送进度
                        publishProgress(downloadSize*100/contentLength);
                    }

                    inputStream.close();
                    outputStream.close();


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

            } else {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //也是在主线程中，可以执行结果处理
            button.setText(aBoolean?"下载完成":"下载失败");
            textView.setText(aBoolean?"下载完成"+mFilePath:"下载失败");
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
//            ui线程  收到进度 处理
            if (values != null && values.length>0) {
                progressBar.setProgress(values[0]);
            }

        }

    }

    //检查权限
    /*
    * 权限检查：
    * */
    private boolean checkPermissionALLGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限结果返回处理
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_CODE) {
            boolean isAllGranted = true;
//            判断所有的权限是否都已授予
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                //如果全部授予了
                permissionFlag = true;

            } else {
                //如果没有则告诉用户要申请的原因
//                openAppDetaisl();
            }

        }
    }

}
