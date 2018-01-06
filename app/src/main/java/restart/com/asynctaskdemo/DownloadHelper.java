package restart.com.asynctaskdemo;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 1、download方法 url localPath listener
 * 2、listener：Start，success fail progress
 * 3、用asynctask封装
 * Created by Administrator on 2018/1/6.
 */

public class DownloadHelper {
    public static void download(String url, String localPath, OnDownloadListener listener) {
        DownloadAsyncTask task = new DownloadAsyncTask(url, localPath, listener);
        task.execute();
    }

    public static class DownloadAsyncTask extends AsyncTask<String, Integer, Boolean> {

        private String mFilePath;
        private String mUrl;
        private OnDownloadListener mListener;

        public DownloadAsyncTask(String mFilePath, String mUrl, OnDownloadListener mListener) {
            this.mFilePath = mFilePath;
            this.mUrl = mUrl;
            this.mListener = mListener;
        }

        /**
         * 在异步任务之前  主线程
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            可操作UI 准备工作
//            button.setText(R.string.downloading);
//            textView.setText(R.string.downloading);
//            progressBar.setProgress(INIT_PROGRESS);
            if (mListener != null) {
                mListener.onStart();
            }
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
//            if (strings != null && strings.length > 0) {
            String apkUrl = mUrl;

            try {
//                    构造URL
                URL url = new URL(apkUrl);
                //构造连接并打开
                URLConnection urlConnection = url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
//                    获得下载内容的总长度
                int contentLength = urlConnection.getContentLength();

//                   下载地址准备
//                    mFilePath = Environment.getExternalStorageDirectory()
//                            + File.separator + FILE_NAME;
                //对下载地址进行处理
                File apkFile = new File(mFilePath);
                if (apkFile.exists()) {
                    boolean result = apkFile.delete();
                    if (!result) {
                        if (mListener != null) {
                            mListener.onFail(-1, apkFile, "文件删除失败");
                        }
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
                    outputStream.write(bytes, 0, length);
                    downloadSize += length;
//                        发送进度
                    publishProgress(downloadSize * 100 / contentLength);
                }

                inputStream.close();
                outputStream.close();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                if (mListener != null) {
                    mListener.onFail(-1, new File(mFilePath), e.getMessage());
                }
                return false;
            }

//            } else {
//                return false;
//            }
            if (mListener != null) {
                mListener.onSuccess(0, new File(mFilePath));
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //也是在主线程中，可以执行结果处理
//            button.setText(aBoolean?"下载完成":"下载失败");
//            textView.setText(aBoolean?"下载完成"+mFilePath:"下载失败");
            if (mListener != null) {
                if (aBoolean) {
                    mListener.onSuccess(0, new File(mFilePath));
                } else {
                    mListener.onFail(-1, new File(mFilePath), "下载失败");
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
//            ui线程  收到进度 处理
//            if (values != null && values.length>0) {
//                progressBar.setProgress(values[0]);
//            }
            if (mListener != null) {
                mListener.onProgress(values[0]);
            }

        }

    }

    public interface OnDownloadListener {
        void onStart();

        void onSuccess(int code, File file);

        void onFail(int code, File file, String message);

        void onProgress(int progress);

        abstract class SimpleDownloadListener implements OnDownloadListener{
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int progress) {

            }
        }
    }
}
