package mobdoki.client;

import java.net.URLEncoder;
import java.util.ArrayList;

import mobdoki.client.activity.user.LogInActivity;
import mobdoki.client.connection.HttpGetJSONConnection;
import mobdoki.client.connection.UserInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class MessageService extends Service {
	
	private NotificationManager notificationManager;
	ArrayList<Messenger> clients = new ArrayList<Messenger>();
	private Thread thread;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		thread = new Thread(null, new ServiceWorker(), "MessageService");
		thread.start();
	}
	
	private boolean isRun = true;
	
	private class ServiceWorker implements Runnable {
		
		HttpGetJSONConnection download;
		
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.arg1==1) {
					if (download.isOK() && download.getInt("count")>0) {		// ha van a legutobbi check ota uj uzenet:
						diplayNotificationMessage("MobDoki: Új üzenet érkezett.");			// uj uzenet jelzese						
						UserInfo.putString("lastmailcheck", download.getString("date"));	// a most lekerdezettnel ujabbakat kell ezutan figyelni
						
						Message m = Message.obtain();
						for (Messenger c : clients) {							// uj uzenet jelzese a feliratkozottaknak
							try {
								c.send(m);
							} catch (Exception e) {}
						}
						if (clients.size()==0) stopSelf();						// szerviz leallitasa ha nincs feliratkozo (nem kell tobb uj uzenetet jelezni)
					}					
				}
			}
		};
		
		public void run() {
			String url;
			while (isRun) {
				url = "HasNewMessage?ssid=" + UserInfo.getSSID() + "&date=" + URLEncoder.encode(UserInfo.getString("lastmailcheck"));
				download = new HttpGetJSONConnection(url, handler);
				download.run();
				try {
					Thread.sleep(30000);				// ellenorzes 30 sec-enkent
				} catch (InterruptedException e) {}
			}
		}
	}
	
	@Override
	public void onDestroy() {
		isRun=false;
		super.onDestroy();
	}
	
	private void diplayNotificationMessage(String message) {
		Notification notification = new Notification(R.drawable.notification_mail, message, System.currentTimeMillis());
		
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, LogInActivity.class), 0);

        notification.setLatestEventInfo(this, "Message Service", message, contentIntent);

        notificationManager.notify(R.string.app_message_notification_id, notification);
	}

	
	public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    // Feliratkozottaktol erkezo uzenet
	class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:			// Feliratkozas ertesitesre
                    clients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:			// Leiratkozas
                    clients.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
	final Messenger serviceMessenger = new Messenger(new IncomingMessageHandler());
	
	@Override
	public IBinder onBind(Intent intent) {
		return serviceMessenger.getBinder();
	}
}
