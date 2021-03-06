package pop.hl.com.poplibrary.update;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import pop.hl.com.poplibrary.bean.DownloadBean;
import pop.hl.com.poplibrary.utils.AppUtil;
import rx.Subscription;

/**
 * Author:hl
 * Time:  2018/5/24 11:30
 * Des:   This is DownLoadIntentService
 */
public class DownLoadIntentService extends IntentService {
    private static final String ACTION_DOWNLOAD = "intentservice.action.download";

    private static final String DOWNLOAD_URL = "downloadUrl";
    private static final String APK_PATH = "apkPath";
    private static final String FILE_PROVIDER = "fileProvider";
    private static final String NOTIFY_ICON = "notifyIcon";
    private static final String APP_NAME = "appName";
    private static final String B_FORCE = "bforceUpdate";
    private static final String InfoChannel = "InfoChannelDown";

    private Subscription subscription = null;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    ///< 保存响铃音量，下载的时候静音；结束或恢复
    private int volNum;

    public DownLoadIntentService() {
        super("DownLoadIntentService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void startUpdateService(Context _context, String _fileProvider, int _notifyIcon, String _url, String _apkPath, boolean bforce) {
        Intent intent = new Intent(_context, DownLoadIntentService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(DOWNLOAD_URL, _url);
        intent.putExtra(APK_PATH, _apkPath);
        intent.putExtra(FILE_PROVIDER, _fileProvider);
        intent.putExtra(NOTIFY_ICON, _notifyIcon);
        intent.putExtra(APP_NAME, AppUtil.getAppName(_context));
        intent.putExtra(B_FORCE, bforce);
        _context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                ///< @a 烦人的声音 - 这里不通知了
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(InfoChannel, InfoChannel, NotificationManager.IMPORTANCE_HIGH);
                    //  mChannel.setDescription(description);
                    //  mChannel.enableLights(true);
                    //  mChannel.setLightColor(Color.RED);
                    //  mChannel.enableVibration(true);
                    //  mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    mChannel.setSound(null, null);
                    mChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
                    notificationManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(this, InfoChannel);
                int notifyIcon = intent.getIntExtra(NOTIFY_ICON, -1);
                String appName =intent.getStringExtra(APP_NAME);
                if (-1 == notifyIcon){
                    builder.setWhen(System.currentTimeMillis())
                            .setContentTitle(appName)
                            .setAutoCancel(true)
                            .setContentText("版本更新");
                }else{
                    builder.setSmallIcon(notifyIcon)
                            .setWhen(System.currentTimeMillis())
                            .setContentTitle(appName)
                            .setAutoCancel(true)
                            .setContentText("版本更新");
                }
                ///< 仅仅响一次
                builder.setOnlyAlertOnce(true);
                //builder.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE);
                notificationManager.notify(0, builder.build());

                //
                //                ///< 静音，不然不停响
                //                volNum = AudioManagerUtils.getSoundDecibel(AudioManager.STREAM_NOTIFICATION);
                //                AudioManagerUtils.setAudioSound(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_PLAY_SOUND);

                String url = intent.getStringExtra(DOWNLOAD_URL);
                String apkPath = intent.getStringExtra(APK_PATH);
                String fileProvider = intent.getStringExtra(FILE_PROVIDER);
                boolean bForce = intent.getBooleanExtra(B_FORCE, false);
                handleUpdate(url, apkPath, fileProvider, bForce);
            }
        }
    }

    /**
     * 利用回调
     *
     * @param url
     * @param apkPath
     */
    private void handleUpdate(String url, String apkPath, String _fileProvider, final boolean bforce) {
        subscription = UpdateManager.downloadApk(this, _fileProvider, url, apkPath, new ProgressListener() {
            @Override
            public void onProgress(long progress, long total, boolean done) {
                ///< @a 烦人的声音 - 这里不通知了
                int progresss = (int) Math.round(progress / (double) total * 100);
                builder.setContentInfo(String.valueOf(progress) + "%").setProgress(100, progresss, false);
                notificationManager.notify(0, builder.build());
                if (progress >= total) {
                    ///< 恢复Audio
                    //AudioManagerUtils.setAudioSound(AudioManager.STREAM_NOTIFICATION, volNum, AudioManager.FLAG_PLAY_SOUND);
                    notificationManager.cancel(0);
                }
                ///< 本来打算用RxBus，但RxBus不能在子线程中
                ///< 通过EventBus发布进度信息
                EventBus.getDefault().post(new DownloadBean(total, progress, -1, "进度信息"));
            }

            @Override
            public void onFinish(int status, String msg) {
                ///< @a 烦人的声音 - 这里不通知了
                //                ///< 恢复Audio
                //                AudioManagerUtils.setAudioSound(AudioManager.STREAM_NOTIFICATION, volNum, AudioManager.FLAG_PLAY_SOUND);
                ///< 取消状态栏
                notificationManager.cancel(0);
                if (bforce){
                    ///< 通过EventBus发布进度信息 - 失败发送1状态，成功发送0状态，读写错误发送2状态
                    EventBus.getDefault().post(new DownloadBean(0, 0, status, msg));
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ///< @a 烦人的声音 - 这里不通知了
        //        if (null != notificationManager) {
        //            notificationManager.cancel(0);
        //        }
        //        ///< 取消网络订阅关系
        //        UpdateManager.unSubscribe(subscription);
    }
}
