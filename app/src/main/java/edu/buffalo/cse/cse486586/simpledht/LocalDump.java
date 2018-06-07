package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

/**
 * Created by sourabh on 4/8/18.
 */

public abstract class LocalDump implements View.OnClickListener {

    //taken from On test click listener
    private static final String TAG = LocalDump.class.getName();

    private final TextView mTextView;
    private final ContentResolver mContentResolver;
    private final Uri mUri;

    public LocalDump(TextView _tv, ContentResolver _cr) {
        mTextView = _tv;
        mContentResolver = _cr;
        mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    public void onClick(View v) {
        new LocalDump.Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class Task extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            return null;
        }

        protected void onProgressUpdate(String... strings) {
            mTextView.append(strings[0]);

        }
    }
}
