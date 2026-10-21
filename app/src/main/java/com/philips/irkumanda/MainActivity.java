package com.philips.irkumanda;
import android.app.Activity;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;

public class MainActivity extends Activity {
 ConsumerIrManager ir;
 TextView status, scanState;
 Spinner keyPicker;
 int scanProfile=0, scanCommand=0, lastProfile=-1, lastCommand=-1, toggle=0;
 String[] protocols={"Philips RC5 TV 36k","Philips RC5 TV 38k","Philips RC5 adres 3 36k","Philips RC6 TV 36k","Philips RC6 adres 3 36k","NEC adres 00FF 38k","NEC adres 20DF 38k","Samsung 07 38k","Sony SIRC 40k"};
 String[] keys={"GÜÇ","SESSİZ","SES +","SES -","KANAL +","KANAL -","MENÜ","AYARLAR","KAYNAK","GERİ","EXIT","OK","YUKARI","AŞAĞI","SOL","SAĞ","FAVORİ","KANAL LİSTESİ","REHBER","TV","KIRMIZI","YEŞİL","SARI","MAVİ","0","1","2","3","4","5","6","7","8","9"};
 String selected="GERİ";
 Vibrator vibrator;
 int bg=Color.rgb(17,29,47), panel=Color.rgb(32,49,73);
 @Override public void onCreate(Bundle b){
  super.onCreate(b);ir=(ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(12),dp(14),dp(12),dp(20));root.setBackgroundColor(bg);
  scroll.addView(root);setContentView(scroll);
  label(root,"EVREN • AKILLI TV KUMANDASI",22,Color.WHITE,Gravity.CENTER);
  status=label(root,ir!=null&&ir.hasIrEmitter()?"IR verici hazır":"Bu telefonda IR verici algılanmadı",13,0xffa6c7e4,Gravity.CENTER);
  section(root,"TELEVİZYON KUMANDASI");
  buttonRow(root,new String[]{"GÜÇ","KAYNAK","SESSİZ"},new int[]{0xffad3445,panel,panel});
  buttonRow(root,new String[]{"MENÜ","AYARLAR","EXIT"},null);
  buttonRow(root,new String[]{"GERİ","YUKARI","FAVORİ"},null);
  buttonRow(root,new String[]{"SOL","OK","SAĞ"},null);
  buttonRow(root,new String[]{"REHBER","AŞAĞI","KANAL LİSTESİ"},null);
  buttonRow(root,new String[]{"SES +","SESSİZ","KANAL +"},null);
  buttonRow(root,new String[]{"SES -","TV","KANAL -"},null);
  buttonRow(root,new String[]{"KIRMIZI","YEŞİL","SARI","MAVİ"},new int[]{0xffa93445,0xff238c5c,0xffb99720,0xff2869b7});
  section(root,"SAYISAL TUŞLAR");
  buttonRow(root,new String[]{"1","2","3"},null);
  buttonRow(root,new String[]{"4","5","6"},null);
  buttonRow(root,new String[]{"7","8","9"},null);
  buttonRow(root,new String[]{"FAVORİ","0","EXIT"},null);
  section(root,"HER TUŞ İÇİN KOD / PROTOKOL BULUCU");
  label(root,"Bir tuş seçin. İLERİ / GERİ kodu TV'ye gönderir. Doğru tepkiyi görünce KAYDET'e basın.",14,0xffc6d5e4,Gravity.LEFT);
  keyPicker=new Spinner(this);
  ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,keys);
  keyPicker.setAdapter(adapter);root.addView(keyPicker);
  for(int i=0;i<keys.length;i++)if(keys[i].equals("GERİ"))keyPicker.setSelection(i);
  keyPicker.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
   public void onNothingSelected(android.widget.AdapterView<?> a){}
   public void onItemSelected(android.widget.AdapterView<?> a,View v,int pos,long id){selected=keys[pos];refresh();}
  });
  scanState=label(root,"",14,0xffe6f2ff,Gravity.LEFT);
  buttonRow(root,new String[]{"◀ GERİ","KAYDET","İLERİ ▶"},new int[]{0xff27617a,0xff238c5c,0xff27617a});
  label(root,"İLERİ yeni kodu hemen gönderir; GERİ bir önceki kodu yeniden gönderir. Kodlar bitince otomatik sonraki protokole geçilir. KAYDET seçili tuşun son gönderilen kodunu saklar.",12,0xffa6b7c9,Gravity.LEFT);
  label(root,"Desteklenen tarama: Philips RC5 / RC6, NEC, Samsung ve Sony SIRC. Her TV modelinin bütün komutları garanti değildir. IR yalnızca kod gönderir; TV'den doğru çalıştığı bilgisini otomatik alamaz.",12,0xffa6b7c9,Gravity.LEFT);
  refresh();
 }
 TextView label(LinearLayout root,String value,int size,int color,int gravity){
  TextView t=new TextView(this);t.setText(value);t.setTextColor(color);t.setTextSize(size);t.setGravity(gravity);t.setPadding(dp(6),dp(9),dp(6),dp(9));root.addView(t);return t;
 }
 void section(LinearLayout root,String name){TextView t=label(root,name,14,0xff9fc9ed,Gravity.LEFT);t.setTypeface(null,Typeface.BOLD);}
 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
 void buttonRow(LinearLayout root,String[] names,int[] colors){
  LinearLayout row=new LinearLayout(this);root.addView(row);
  for(int i=0;i<names.length;i++){
   String name=names[i];TextView b=new TextView(this);b.setText(name);b.setTextColor(Color.WHITE);b.setTypeface(null,Typeface.BOLD);
   b.setTextSize(names.length>3?12:13);b.setGravity(Gravity.CENTER);
   GradientDrawable shape=new GradientDrawable();shape.setColor(colors!=null?colors[i]:panel);shape.setCornerRadius(dp(12));b.setBackground(shape);
   LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(55),1);p.setMargins(dp(3),dp(4),dp(3),dp(4));row.addView(b,p);
   b.setOnClickListener(v->{vibrate();handle(name);});
  }
 }
 int limit(int profile){return profile<=4?128:profile==8?128:256;}
 void refresh(){
  if(scanState==null)return;
  int saved=getPreferences(0).getInt("code_"+selected,-1);
  String savedText=saved<0?"Henüz kaydedilmedi":protocols[saved/256]+" • Komut "+(saved%256);
  scanState.setText("Tuş: "+selected+"\nProtokol: "+protocols[scanProfile]+"\nKod: "+scanCommand+" / "+(limit(scanProfile)-1)+"\nKayıt: "+savedText);
 }
 void vibrate(){try{if(vibrator!=null&&vibrator.hasVibrator()){if(android.os.Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(25,VibrationEffect.DEFAULT_AMPLITUDE));else vibrator.vibrate(25);}}catch(Exception ignored){}}
 void stepCode(int direction){
  scanCommand+=direction;
  if(scanCommand>=limit(scanProfile)){scanProfile=(scanProfile+1)%protocols.length;scanCommand=0;}
  if(scanCommand<0){scanProfile=(scanProfile+protocols.length-1)%protocols.length;scanCommand=limit(scanProfile)-1;}
  lastProfile=scanProfile;lastCommand=scanCommand;
  transmit("Kod arama: "+selected,scanCommand,scanProfile);refresh();
 }
 void handle(String name){
  if(name.equals("İLERİ ▶")){stepCode(1);return;}
  if(name.equals("◀ GERİ")){stepCode(-1);return;}
  if(name.equals("KAYDET")){
   if(lastProfile!=scanProfile||lastCommand!=scanCommand){Toast.makeText(this,"Önce İLERİ veya GERİ ile kod gönderin",Toast.LENGTH_SHORT).show();return;}
   getPreferences(0).edit().putInt("code_"+selected,scanProfile*256+scanCommand).apply();
   Toast.makeText(this,selected+" kodu kaydedildi",Toast.LENGTH_SHORT).show();refresh();return;
  }
  int saved=getPreferences(0).getInt("code_"+name,-1);
  if(saved<0){selected=name;for(int i=0;i<keys.length;i++)if(keys[i].equals(name)){keyPicker.setSelection(i);break;}transmit(name+" (varsayılan)",defaultCommand(name),scanProfile);refresh();return;}
  transmit(name,saved%256,saved/256);
 }
 int defaultCommand(String name){
  switch(name){
   case "GÜÇ":return 12;case "SESSİZ":return 13;case "SES +":return 16;case "SES -":return 17;
   case "KANAL +":return 32;case "KANAL -":return 33;case "MENÜ":return 82;
   case "YUKARI":return 80;case "AŞAĞI":return 81;case "SOL":return 85;case "SAĞ":return 86;
   case "OK":return 87;case "KAYNAK":return 56;case "REHBER":return 53;
   case "GERİ":return 10;case "EXIT":return 10;case "FAVORİ":return 54;
   case "KANAL LİSTESİ":return 53;case "AYARLAR":return 82;case "TV":return 63;
   default:try{return Integer.parseInt(name);}catch(Exception e){return 0;}
  }
 }
 void addPulse(ArrayList<Integer> levels,ArrayList<Integer> widths,int level,int width){
  int last=levels.size()-1;if(last>=0&&levels.get(last)==level)widths.set(last,widths.get(last)+width);
  else{levels.add(level);widths.add(width);}
 }
 void rc6Bit(ArrayList<Integer> levels,ArrayList<Integer> widths,int bit,int half){
  addPulse(levels,widths,bit==1?1:0,half);addPulse(levels,widths,bit==1?0:1,half);
 }
 int[] pulses(ArrayList<Integer> levels,ArrayList<Integer> widths){
  if(!levels.isEmpty()&&levels.get(0)==0){levels.remove(0);widths.remove(0);}
  if(!levels.isEmpty()&&levels.get(levels.size()-1)==1)addPulse(levels,widths,0,444);
  int[] a=new int[widths.size()];for(int i=0;i<a.length;i++)a[i]=widths.get(i);return a;
 }
 int[] rc5Pattern(int cmd,int t,int profile){
  int address=profile==2?3:0;int field=cmd<64?1:0;
  int frame=(1<<13)|(field<<12)|((t&1)<<11)|(address<<6)|(cmd&63);
  ArrayList<Integer> l=new ArrayList<>(),w=new ArrayList<>();
  for(int i=13;i>=0;i--){int bit=(frame>>i)&1;addPulse(l,w,bit==1?0:1,889);addPulse(l,w,bit==1?1:0,889);}
  return pulses(l,w);
 }
 int[] rc6Pattern(int cmd,int t,int profile){
  ArrayList<Integer> l=new ArrayList<>(),w=new ArrayList<>();
  addPulse(l,w,1,2664);addPulse(l,w,0,888);rc6Bit(l,w,1,444);
  for(int i=0;i<3;i++)rc6Bit(l,w,0,444);
  rc6Bit(l,w,t&1,888);int address=profile==4?3:0;
  for(int i=7;i>=0;i--)rc6Bit(l,w,(address>>i)&1,444);
  for(int i=7;i>=0;i--)rc6Bit(l,w,(cmd>>i)&1,444);
  return pulses(l,w);
 }
 void pulseDistance(ArrayList<Integer> l,ArrayList<Integer> w,int bit,int mark,int zero,int one){
  addPulse(l,w,1,mark);addPulse(l,w,0,bit==0?zero:one);
 }
 int[] necPattern(int cmd,int profile){
  ArrayList<Integer> l=new ArrayList<>(),w=new ArrayList<>();
  addPulse(l,w,1,9000);addPulse(l,w,0,4500);
  int address=profile==5?0x00FF:profile==6?0x20DF:0xE0E0;
  int[] bytes={address&255,(address>>>8)&255,cmd&255,(~cmd)&255};
  for(int value:bytes)for(int i=0;i<8;i++)pulseDistance(l,w,(value>>>i)&1,560,560,1690);
  addPulse(l,w,1,560);addPulse(l,w,0,560);return pulses(l,w);
 }
 int[] sonyPattern(int cmd){
  ArrayList<Integer> l=new ArrayList<>(),w=new ArrayList<>();
  addPulse(l,w,1,2400);addPulse(l,w,0,600);
  int value=cmd&127;
  for(int i=0;i<12;i++){addPulse(l,w,1,((value>>i)&1)==1?1200:600);addPulse(l,w,0,600);}
  return pulses(l,w);
 }
 void transmit(String name,int command,int profile){
  if(ir==null||!ir.hasIrEmitter()){status.setText("Bu cihazda IR verici yok");return;}
  int t=toggle;toggle^=1;
  int[] pattern=profile<=2?rc5Pattern(command,t,profile):profile<=4?rc6Pattern(command,t,profile):profile<=7?necPattern(command,profile):sonyPattern(command);
  int frequency=profile==1?38000:profile<=4?36000:profile==8?40000:38000;
  new Thread(()->{
   try{
    for(int i=0;i<2;i++){ir.transmit(frequency,pattern);if(i==0)Thread.sleep(125);}
    runOnUiThread(()->status.setText("Gönderildi: "+name+" • "+protocols[profile]+" • "+command));
   }catch(Exception e){runOnUiThread(()->status.setText("IR hatası: "+e.getMessage()));}
  }).start();
 }
}
