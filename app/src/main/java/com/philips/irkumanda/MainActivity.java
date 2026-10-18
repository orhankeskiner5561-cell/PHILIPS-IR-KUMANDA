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
  ConsumerIrManager ir; TextView status; int toggle=0; int selectedProfile=0; int candidate=0; boolean pairing=false;
  final String[] learnNames={"BACK / GERİ","EXIT / ÇIKIŞ","TV / CANLI YAYIN"};
  final String[] learnKeys={"learn_back","learn_exit","learn_tv"};
  int learnTarget=0, learnCode=10;
  TextView learnState;
  final String[] profiles={"RC5 TV / normal","RC5 TV / ters Manchester","RC5 TV / 38 kHz","RC5 adres 3 / normal","RC5 adres 3 / ters Manchester","RC6 mode 0 / TV adres 0","RC6 mode 0 / TV adres 3"};
  @Override public void onCreate(Bundle b){super.onCreate(b);ir=(ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(14,14,14,14);root.setBackgroundColor(Color.rgb(242,244,248));ScrollView s=new ScrollView(this);s.addView(root);setContentView(s);
    TextView title=new TextView(this);title.setText("PHILIPS IR KUMANDA");title.setTextSize(24);title.setTextColor(Color.BLACK);title.setGravity(Gravity.CENTER);root.addView(title);
    status=new TextView(this);status.setPadding(0,12,0,20);status.setGravity(Gravity.CENTER);status.setTextColor(Color.DKGRAY);root.addView(status);status.setText(ir!=null&&ir.hasIrEmitter()?"Kızılötesi verici bulundu • RC5 test":"Android kızılötesi verici bulamadı");
    selectedProfile=getPreferences(0).getInt("ir_profile",0);if(selectedProfile<0||selectedProfile>=profiles.length)selectedProfile=0;
    section(root,"KIZILÖTESİ TV EŞLEŞTİRME");
    TextView guide=new TextView(this);guide.setText("TV açıkken telefonun IR ucunu TV'ye tutun. 'Dene' Ses + komutu gönderir. Ses gerçekten artarsa ÇALIŞTI'ya basın; yalnız kırmızı ışık yanması yeterli değildir.");guide.setTextColor(Color.DKGRAY);guide.setTextSize(14);root.addView(guide);
    TextView pairState=new TextView(this);pairState.setTextColor(Color.rgb(25,70,110));pairState.setTextSize(15);pairState.setPadding(dp(6),dp(8),0,dp(8));root.addView(pairState);
    pairState.setText("Kayıtlı profil: "+profiles[selectedProfile]);
    LinearLayout pairRow=new LinearLayout(this);root.addView(pairRow);
    TextView test=pairButton("DENE",pairRow),works=pairButton("ÇALIŞTI",pairRow),next=pairButton("SONRAKİ",pairRow);
    test.setOnClickListener(v->{pairing=true;pairState.setText("Denenen "+(candidate+1)+"/"+profiles.length+": "+profiles[candidate]);transmit("Eşleştirme Ses +",16,candidate);});
    next.setOnClickListener(v->{pairing=false;candidate=(candidate+1)%profiles.length;pairState.setText("Sıradaki "+(candidate+1)+"/"+profiles.length+": "+profiles[candidate]);});
    works.setOnClickListener(v->{if(!pairing){pairState.setText("Önce DENE tuşuna basın.");return;}selectedProfile=candidate;getPreferences(0).edit().putInt("ir_profile",selectedProfile).apply();pairState.setText("Kaydedildi: "+profiles[selectedProfile]);pairing=false;});
    section(root,"BACK / EXIT / TV KOD EŞLEŞTİRME");
    TextView learnHelp=new TextView(this);
    learnHelp.setText("Önce işlevi seçin. DENE tek bir RC5 komutu yollar. TV gerçekten doğru işlevi yaparsa KAYDET'e basın. Diğer kodlara SONRAKİ ile geçin. Her tuşun kodu ayrı saklanır.");
    learnHelp.setTextColor(Color.DKGRAY);root.addView(learnHelp);
    Spinner targetSpinner=new Spinner(this);
    ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,learnNames);
    targetSpinner.setAdapter(adapter);root.addView(targetSpinner);
    learnState=new TextView(this);learnState.setTextSize(15);learnState.setTextColor(Color.rgb(25,70,110));
    learnState.setPadding(dp(6),dp(8),0,dp(8));root.addView(learnState);
    targetSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
      public void onNothingSelected(android.widget.AdapterView<?> p){}
      public void onItemSelected(android.widget.AdapterView<?> p,android.view.View v,int pos,long id){
        learnTarget=pos;learnCode=getPreferences(0).getInt(learnKeys[pos],pos==0?10:pos==1?83:63);
        refreshLearn();
      }
    });
    LinearLayout learnRow=new LinearLayout(this);root.addView(learnRow);
    TextView learnTry=pairButton("DENE",learnRow),learnSave=pairButton("KAYDET",learnRow),learnNext=pairButton("SONRAKİ",learnRow);
    learnTry.setOnClickListener(v->transmit("Kod denemesi: "+learnNames[learnTarget],learnCode,selectedProfile));
    learnSave.setOnClickListener(v->{getPreferences(0).edit().putInt(learnKeys[learnTarget],learnCode).apply();refreshLearn();Toast.makeText(this,"Kod kaydedildi: "+learnCode,Toast.LENGTH_SHORT).show();});
    learnNext.setOnClickListener(v->{learnCode=(learnCode+1)%128;refreshLearn();});
    section(root,"TEMEL KONTROLLER");
    row(root,new String[]{"Güç","Kaynak","Sessiz"},new int[]{12,56,13});
    section(root,"YÖN TUŞLARI");
    row(root,new String[]{"Menü","▲ Yukarı","BACK / Geri"},new int[]{82,80,-10});
    row(root,new String[]{"◀ Sol","OK","Sağ ▶"},new int[]{85,87,86});
    row(root,new String[]{"Bilgi","▼ Aşağı","Rehber"},new int[]{15,81,53});
    row(root,new String[]{"Önceki","EXIT / Çıkış","TV / Canlı Yayın"},new int[]{10,-11,-12});
    section(root,"TV VE UYDU KONTROLLERİ");
    row(root,new String[]{"TV / Radyo","Favoriler","LIST"},new int[]{-12,54,53});
    row(root,new String[]{"SMART TV","SOURCES","TV GUIDE"},new int[]{-4,56,53});
    row(root,new String[]{"OPTIONS","Kırmızı","Yeşil"},new int[]{-5,-6,-7});
    row(root,new String[]{"Sarı","Mavi",""},new int[]{-8,-9,-1});
    row(root,new String[]{"Kurulum / Menü","Kanal Arama","Sesli Arama"},new int[]{82,-2,-3});
    section(root,"SES VE KANAL");
    row(root,new String[]{"Ses +","Ses −","Kanal +","Kanal −"},new int[]{16,17,32,33});
    section(root,"SAYISAL TUŞLAR");
    row(root,new String[]{"1","2","3"},new int[]{1,2,3});
    row(root,new String[]{"4","5","6"},new int[]{4,5,6});
    row(root,new String[]{"7","8","9"},new int[]{7,8,9});
    row(root,new String[]{"","0",""},new int[]{-1,0,-1});
    TextView info=new TextView(this);info.setText("TEST: Önce ses, kanal ve aç/kapat tuşlarını deneyin. BACK, EXIT ve TV işlevlerini üstteki kod eşleştirme alanında tek tek deneyip kaydedin. Yeni özel tuşlar için kodlar modele göre değişebilir. Kanal arama için TV menüsünü açıp kurulum bölümüne gidin. Sesli arama için TV ile ayrı mikrofon bağlantısı gerekir.");info.setPadding(0,20,0,10);info.setTextColor(Color.DKGRAY);root.addView(info);
  }
  void refreshLearn(){
    if(learnState!=null)learnState.setText(learnNames[learnTarget]+" — Denenecek RC5 kodu: "+learnCode+" / 127 • Kayıtlı: "+getPreferences(0).getInt(learnKeys[learnTarget],-1));
  }
  TextView pairButton(String text,LinearLayout row){
    TextView b=new TextView(this);b.setText(text);b.setTextColor(Color.WHITE);b.setTextSize(13);b.setTypeface(null,Typeface.BOLD);b.setGravity(Gravity.CENTER);
    GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(35,91,109));bg.setCornerRadius(dp(9));b.setBackground(bg);
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(50),1);p.setMargins(dp(3),dp(5),dp(3),dp(5));row.addView(b,p);return b;
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
      else if(code==-2){btn.setClickable(true);btn.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("Uydu kanal arama").setMessage("TV menüsünden Kurulum / Kanallar / Uydu kurulumu bölümüne gidin. Bu düğme otomatik kanal taraması başlatmaz.").setPositiveButton("Menüyü aç",(d,w)->send("Menü",82)).setNegativeButton("Kapat",null).show());}
      else if(code==-3){btn.setClickable(true);btn.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("Google sesli arama").setMessage("Kızılötesi bağlantı telefon mikrofonunun sesini televizyona taşıyamaz. TV destekliyorsa Bluetooth kumanda veya ağ üzerinden mikrofon bağlantısı gerekir.").setPositiveButton("Tamam",null).show());}
      else if(code==-10||code==-11||code==-12){btn.setClickable(true);btn.setOnClickListener(v->send(name,getPreferences(0).getInt(learnKeys[-code-10],code==-10?10:code==-11?83:63)));}
      else if(code<=-4&&code>=-9){btn.setClickable(true);btn.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle(name+" kodu").setMessage("Bu özel tuşun televizyonunuz için doğru IR kodu henüz doğrulanmadı. Yanlış komut göndermemek için şimdilik devre dışı.").setPositiveButton("Tamam",null).show());}
      else{btn.setVisibility(android.view.View.INVISIBLE);}
    }
  }
  // Philips TV RC5: two start bits, toggle, address 0 (TV), six command bits.
  // ConsumerIrManager expects alternating MARK and SPACE durations, beginning with MARK.
  int[] rc5Pattern(int command,int toggleBit,int profile){
    int field=(command<64)?1:0;
    int address=(profile==3||profile==4)?3:0;
    int frame=(1<<13)|(field<<12)|((toggleBit&1)<<11)|(address<<6)|(command&63);
    ArrayList<Integer> levels=new ArrayList<>(),widths=new ArrayList<>();
    boolean inverted=(profile==1||profile==4);
    for(int i=13;i>=0;i--){
      int bit=(frame>>i)&1;
      int first=((bit==1)^inverted)?0:1;
      addPulse(levels,widths,first,889);
      addPulse(levels,widths,1-first,889);
    }
    // Android ConsumerIrManager must begin with a MARK. RC5's leading idle
    // SPACE is not part of the modulated signal; remove it before transmission.
    if(!levels.isEmpty()&&levels.get(0)==0){levels.remove(0);widths.remove(0);}
    // ConsumerIrManager expects MARK/SPACE pairs, including final SPACE.
    if(widths.size()%2!=0)widths.add(889);
    int[] out=new int[widths.size()];
    for(int i=0;i<out.length;i++)out[i]=widths.get(i);
    return out;
  }
  // RC6 mode 0: 6T mark/2T space leader, 1T mark/1T space start,
  // mode bits 000, double-width trailer toggle, 8-bit address and 8-bit command.
  // T=444 us, Manchester 1=mark-space and 0=space-mark.
  int[] rc6Pattern(int command,int toggleBit,int profile){
    ArrayList<Integer> pulses=new ArrayList<>();
    // Explicit alternating mark/space durations; merge adjacent equal levels.
    ArrayList<Integer> levels=new ArrayList<>();
    ArrayList<Integer> widths=new ArrayList<>();
    addPulse(levels,widths,1,2664);addPulse(levels,widths,0,888);
    rc6Bit(levels,widths,1,444);
    // Mode 0 (000)
    for(int i=0;i<3;i++)rc6Bit(levels,widths,0,444);
    // Trailer bit, twice the usual width
    rc6Bit(levels,widths,toggleBit&1,888);
    int address=profile==6?3:0;
    for(int i=7;i>=0;i--)rc6Bit(levels,widths,(address>>i)&1,444);
    for(int i=7;i>=0;i--)rc6Bit(levels,widths,(command>>i)&1,444);
    // Ensure the Android pulse array begins with MARK and ends with SPACE.
    if(!levels.isEmpty()&&levels.get(0)==0){levels.remove(0);widths.remove(0);}
    // Ensure ending SPACE and a safe trailing gap for transmit.
    if(levels.get(levels.size()-1)==1)addPulse(levels,widths,0,444);
    for(int width:widths)pulses.add(width);
    int[] out=new int[pulses.size()];
    for(int i=0;i<out.length;i++)out[i]=pulses.get(i);
    return out;
  }
  void addPulse(ArrayList<Integer> levels,ArrayList<Integer> widths,int level,int width){
    int last=levels.size()-1;
    if(last>=0&&levels.get(last)==level)widths.set(last,widths.get(last)+width);
    else{levels.add(level);widths.add(width);}
  }
  void rc6Bit(ArrayList<Integer> levels,ArrayList<Integer> widths,int bit,int half){
    addPulse(levels,widths,bit==1?1:0,half);
    addPulse(levels,widths,bit==1?0:1,half);
  }
  void send(String name,int command){transmit(name,command,selectedProfile);}
  void transmit(String name,int command,int profile){
    if(ir==null||!ir.hasIrEmitter()){status.setText("IR vericisi bulunamadı");return;}
    final int currentToggle=toggle;toggle^=1;
    final int[] pattern=profile>=5?rc6Pattern(command,currentToggle,profile):rc5Pattern(command,currentToggle,profile);
    new Thread(()->{
      try{
        for(int n=0;n<3;n++){
          ir.transmit(profile>=5?36000:(profile==2?38000:36000),pattern);
          if(n<2)Thread.sleep(Math.max(20,114-(profile>=5?35:25)));
        }
        runOnUiThread(()->status.setText("IR gönderildi: "+name+" — "+profiles[profile]));
      }catch(Exception e){runOnUiThread(()->status.setText("IR hatası: "+e.getMessage()));}
    }).start();
  }
}
