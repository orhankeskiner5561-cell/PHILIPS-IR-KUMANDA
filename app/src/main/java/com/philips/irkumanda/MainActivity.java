package com.philips.irkumanda;
import android.app.Activity;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;
import java.util.ArrayList;
public class MainActivity extends Activity {
  ConsumerIrManager ir; TextView status; int toggle=0;
  @Override public void onCreate(Bundle b){super.onCreate(b);ir=(ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(14,14,14,14);root.setBackgroundColor(Color.rgb(242,244,248));ScrollView s=new ScrollView(this);s.addView(root);setContentView(s);
    TextView title=new TextView(this);title.setText("PHILIPS IR KUMANDA");title.setTextSize(24);title.setTextColor(Color.BLACK);title.setGravity(Gravity.CENTER);root.addView(title);
    status=new TextView(this);status.setPadding(0,12,0,20);status.setGravity(Gravity.CENTER);status.setTextColor(Color.DKGRAY);root.addView(status);status.setText(ir!=null&&ir.hasIrEmitter()?"Kızılötesi verici bulundu • RC5 test":"Android kızılötesi verici bulamadı");
    section(root,"TEMEL KONTROLLER");
    row(root,new String[]{"⏻ Güç","Kaynak","Sessiz"},new int[]{12,56,13});
    section(root,"YÖN TUŞLARI");
    row(root,new String[]{"Menü","▲ Yukarı","Geri"},new int[]{82,80,10});
    row(root,new String[]{"◀ Sol","OK","Sağ ▶"},new int[]{85,87,86});
    row(root,new String[]{"Bilgi","▼ Aşağı","Rehber"},new int[]{15,81,53});
    section(root,"SES VE KANAL");
    row(root,new String[]{"Ses +","Ses −","Kanal +","Kanal −"},new int[]{16,17,32,33});
    section(root,"SAYISAL TUŞLAR");
    row(root,new String[]{"1","2","3"},new int[]{1,2,3});
    row(root,new String[]{"4","5","6"},new int[]{4,5,6});
    row(root,new String[]{"7","8","9"},new int[]{7,8,9});
    row(root,new String[]{"","0",""},new int[]{-1,0,-1});
    TextView info=new TextView(this);info.setText("TEST: Önce ses, kanal ve aç/kapat tuşlarını deneyin. Bu sürümde uydu tarama ve favori kanal düzenleme henüz bulunmuyor.");info.setPadding(0,20,0,10);info.setTextColor(Color.DKGRAY);root.addView(info);
  }
  int dp(float value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
  void section(LinearLayout root,String text){
    TextView header=new TextView(this);header.setText(text);header.setTextSize(12);
    header.setTextColor(Color.rgb(70,80,100));header.setTypeface(null,Typeface.BOLD);
    header.setPadding(dp(5),dp(14),0,dp(5));root.addView(header);
  }
  void row(LinearLayout root,String[] labels,int[] codes){
    LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);root.addView(line);
    for(int i=0;i<labels.length;i++){
      final int code=codes[i];final String name=labels[i];
      TextView btn=new TextView(this);
      btn.setText(name);btn.setTextSize(14);btn.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
      btn.setTextColor(Color.WHITE);btn.setGravity(Gravity.CENTER);btn.setIncludeFontPadding(true);
      GradientDrawable bg=new GradientDrawable();
      bg.setColor(code==12?Color.rgb(166,45,56):Color.rgb(37,62,102));
      bg.setCornerRadius(dp(12));btn.setBackground(bg);
      LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(52),1);
      p.setMargins(dp(3),dp(4),dp(3),dp(4));line.addView(btn,p);
      if(code>=0){btn.setClickable(true);btn.setOnClickListener(v->send(name,code));}
      else{btn.setVisibility(android.view.View.INVISIBLE);}
    }
  }
  // Philips TV RC5: two start bits, toggle, address 0 (TV), six command bits.
  // ConsumerIrManager expects alternating MARK and SPACE durations, beginning with MARK.
  int[] rc5Pattern(int command,int toggleBit){
    int field=(command<64)?1:0;
    int frame=(1<<13)|(field<<12)|((toggleBit&1)<<11)|(command&63);
    ArrayList<Integer> durations=new ArrayList<>();
    int current=1, duration=0;
    for(int i=13;i>=0;i--){
      int bit=(frame>>i)&1;
      // RC5 Manchester: logical 1 is space then mark; logical 0 is mark then space.
      int[] halves=bit==1?new int[]{0,1}:new int[]{1,0};
      for(int level:halves){
        if(level==current){duration+=889;}
        else{
          if(duration>0)durations.add(duration);
          current=level;duration=889;
        }
      }
    }
    if(duration>0)durations.add(duration);
    // Trailing MARK requires a SPACE; trailing SPACE is already a valid final interval.
    if(durations.size()%2==1)durations.add(889);
    int[] result=new int[durations.size()];
    for(int i=0;i<result.length;i++)result[i]=durations.get(i);
    return result;
  }
  void send(String name,int command){
    if(ir==null||!ir.hasIrEmitter()){status.setText("IR vericisi bulunamadı");return;}
    try{
      final int currentToggle=toggle;toggle^=1;
      final int[] pattern=rc5Pattern(command,currentToggle);
      new Thread(()->{
        try{
          // A short press on a Philips RC5 remote repeats the same frame.
          for(int n=0;n<3;n++){
            ir.transmit(36000,pattern);
            if(n<2)Thread.sleep(114);
          }
          runOnUiThread(()->status.setText("RC5 TV adres 0 gönderildi: "+name+" ("+command+")"));
        }catch(Exception e){runOnUiThread(()->status.setText("Gönderim hatası: "+e.getMessage()));}
      }).start();
    }catch(Exception e){status.setText("Gönderim hatası: "+e.getMessage());}
  }
}
