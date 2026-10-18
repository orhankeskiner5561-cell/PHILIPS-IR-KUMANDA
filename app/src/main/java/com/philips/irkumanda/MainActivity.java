package com.philips.irkumanda;
import android.app.Activity;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.util.ArrayList;
public class MainActivity extends Activity {
  ConsumerIrManager ir; TextView status; int toggle=0;
  @Override public void onCreate(Bundle b){super.onCreate(b);ir=(ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(14,14,14,14);root.setBackgroundColor(Color.rgb(242,244,248));ScrollView s=new ScrollView(this);s.addView(root);setContentView(s);
    TextView title=new TextView(this);title.setText("PHILIPS IR KUMANDA");title.setTextSize(24);title.setTextColor(Color.BLACK);title.setGravity(Gravity.CENTER);root.addView(title);
    status=new TextView(this);status.setPadding(0,12,0,20);status.setGravity(Gravity.CENTER);root.addView(status);status.setText(ir!=null&&ir.hasIrEmitter()?"Kızılötesi verici bulundu • RC5 test":"Android kızılötesi verici bulamadı");
    row(root,new String[]{"Aç/Kapat","Kaynak","Sessiz"},new int[]{12,56,13});
    row(root,new String[]{"Menü","Yukarı","Geri"},new int[]{82,80,10});
    row(root,new String[]{"Sol","OK","Sağ"},new int[]{85,87,86});
    row(root,new String[]{"Bilgi","Aşağı","Rehber"},new int[]{15,81,53});
    row(root,new String[]{"Ses +","Ses -","Kanal +","Kanal -"},new int[]{16,17,32,33});
    row(root,new String[]{"1","2","3"},new int[]{1,2,3});row(root,new String[]{"4","5","6"},new int[]{4,5,6});row(root,new String[]{"7","8","9"},new int[]{7,8,9});row(root,new String[]{"0"},new int[]{0});
    TextView info=new TextView(this);info.setText("TEST: Önce ses, kanal ve aç/kapat tuşlarını deneyin. Bu sürümde uydu tarama ve favori kanal düzenleme henüz bulunmuyor.");info.setPadding(0,20,0,10);root.addView(info);
  }
  void row(LinearLayout root,String[] labels,int[] codes){LinearLayout line=new LinearLayout(this);line.setOrientation(0);root.addView(line);for(int i=0;i<labels.length;i++){final int code=codes[i];final String name=labels[i];Button btn=new Button(this);btn.setText(name);btn.setAllCaps(false);btn.setTextSize(12);btn.setTextColor(Color.BLACK);btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,70,1);p.setMargins(2,2,2,2);line.addView(btn,p);btn.setOnClickListener(v->send(name,code));}}
  void send(String name,int command){if(ir==null||!ir.hasIrEmitter()){status.setText("IR vericisi bulunamadı");return;}try{
    int field=command<64?1:0;int bits=(1<<13)|(field<<12)|((toggle&1)<<11)|((command&63));toggle^=1;
    ArrayList<Integer> runs=new ArrayList<>();int state=1,length=0;
    for(int i=13;i>=0;i--){int bit=(bits>>i)&1;int first=bit==1?1:0;int second=1-first;int[] halves={first,second};for(int half:halves){if(half==state)length+=889;else{runs.add(length);state=half;length=889;}}}
    runs.add(length);if(runs.size()%2==1)runs.add(889);int[] pattern=new int[runs.size()];for(int i=0;i<pattern.length;i++)pattern[i]=runs.get(i);
    ir.transmit(36000,pattern);status.setText("Gönderildi: "+name+" (RC5 "+command+")");
  }catch(Exception e){status.setText("Gönderim hatası: "+e.getMessage());}}
}
