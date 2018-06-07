package edu.buffalo.cse.cse486586.simpledht;

import android.os.Bundle;
import android.app.Activity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;

public class SimpleDhtActivity extends Activity {


    //Static variables ( Referenced from PA1)
    static final String TAG = SimpleDhtActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_dht_main);

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        findViewById(R.id.button3).setOnClickListener(new OnTestClickListener(tv, getContentResolver()));

        /*todo :
        * 1. Implement a class called LocalDump  (should  implements OnClickListener just like OnTestClickListener class)
        * 2. Implement a class called GlobalDump (should  implements OnClickListener just like OnTestClickListener class)
        * */
        //findViewById(R.id.button1).setOnClickListener(new LocalDump(tv,getContetn));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_simple_dht_main, menu);
        return true;
    }


}
