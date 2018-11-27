package in.arjsna.audiorecorder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.widget.LinearLayout;

import in.arjsna.audiorecorder.R;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;



public class MainScreen extends AppCompatActivity {

    CardView recordAudio,textSummarization,voiceCategorization;

    Intent i1,i2,i3,i4,i5,i6,i7,i8,i9;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainscreen);

        ll = (LinearLayout) findViewById(R.id.ll);




        recordAudio = (CardView)findViewById(R.id.record_audio);
        i1 = new Intent(this,MainActivity.class);
        recordAudio.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(i1);
            }
        });


        textSummarization = (CardView)findViewById(R.id.text_summarization);
        i2 = new Intent(this,TextSummarization.class);
        textSummarization.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(i2);
            }
        });

        voiceCategorization = (CardView)findViewById(R.id.voice_categorization);
        i3 = new Intent(this,VoiceCategorization.class);
        voiceCategorization.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(i2);
            }
        });


    }


}