package com.therabbitmage.android.beacon.receiver.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.therabbitmage.android.beacon.R;
import com.therabbitmage.android.beacon.service.BeaconService;

public class BeaconButtonWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		if(context == null){
			return;
		}
		
		if(appWidgetManager == null){
			return;
		}
		
		final int N = appWidgetIds.length;
		
		for(int i = 0; i < N; i++){
			
			int appWidgetId = appWidgetIds[i];
			
			Intent intent = new Intent(context, BeaconService.class);
			intent.setAction(BeaconService.ACTION_BEGIN);
			PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.beacon_widget_button);
			views.setOnClickPendingIntent(R.id.beacon_button, pendingIntent);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}


}
