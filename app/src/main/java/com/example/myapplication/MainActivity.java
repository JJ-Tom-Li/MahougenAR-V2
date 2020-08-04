package com.example.myapplication;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.SubMenu;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    /**Variables declaration*/
    private MahougenView mahougenView; //The Mahougen view
    private SeekBar sbMP;              //The MP seekBar
    private TextView tvMP;             //The TextView of MP
    private ArrayList<String> images = new ArrayList<String>(); //The namelist of images from sdcard
    private String imageName;
    ArrayAdapter<String> imageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mahougen__drawing_);

        // ask permission to read and write sdcard
        askPermissions();

        //Create the Mahougen dir
        CreateMahougenDir();
        //update image list
        updateImageList();
        //Create color selection spinners.
        createColorSpinner();

        // find the views
        mahougenView = (MahougenView)findViewById(R.id.mahougenView);
        sbMP = (SeekBar)findViewById(R.id.seekBarMP);
        tvMP=(TextView)findViewById(R.id.textView);

        //set the textView tMP
        tvMP.setText("MP:10");
        //set the seekBar
        sbMP.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Set the MP.
                //The MP is the vertexCount in MahougenView.
                tvMP.setText("MP:"+(progress+3));
                mahougenView.changeMP(progress+3);
                //re-initialize the mahougenView
                mahougenView.clear();
                mahougenView=(MahougenView)findViewById(R.id.mahougenView);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void askPermissions() {
        /**ask the permission for R/W sdcard.*/
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    public void CreateMahougenDir()
    {
        //File sdFile = android.os.Environment.getExternalStorageDirectory(); //The dir of sdcard
        String path = this.getFilesDir()+"/mahougens/"; //sdFile.getPath() + File.separator + "mahougens"; //The path of mahougen dir
        File dirFile = new File(path);
        if(!dirFile.exists())//如果資料夾不存在
            dirFile.mkdir();//建立資料夾
    }
    public void OnResetClick(View v)
    {
        /**reset the mahougenView*/
        //clear
        mahougenView.clear();
        //show the clear Toast
        Toast.makeText(this,"Clear", Toast.LENGTH_LONG)
                .show();
    }

    public File Save()
    {
        /**Save the mahougen image into the sdcard.
         * If there is no the directory of "Mahougens",create one.
         * This app will get the mahougen images from the directory,so
         * you can put your own images(e.g. .png or .jpg ) into the directory and create a 2D AR.
         * (This should be a bug,but I want to keep it as a hidden function XD.)
         * */
        try{
            //File sdFile = android.os.Environment.getExternalStorageDirectory(); //The dir of sdcard
            String path = this.getFilesDir()+"/mahougens/"; //sdFile.getPath() + File.separator + "mahougens"; //The path of mahougen dir
            System.out.println(path);
            imageName= System.currentTimeMillis()+".png"; //Use the time as the file name.

            File file = new File(path, imageName);
            OutputStream stream = new FileOutputStream(file);
            mahougenView.saveBitmap(stream); //Put the bitmap into file stream.
            stream.close();

            //Show Toast
            Toast.makeText(this,"save success", Toast.LENGTH_LONG)
                    .show();
            return file;
        }catch(Exception e){
            //Show error
            Toast.makeText(this,"save failed", Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }
        return null;
    }

    public void OnShareClick(View v)
    {
        /**Share the mahougen through other SNS.*/
        try {
            // get file directory.
            final File pictureFile = Save();
            // invoke an intent with ACTION_SEND
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            //Set the image type as png
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    this,
                    getPackageName()+".provider",
                    pictureFile));
            startActivity(Intent.createChooser(shareIntent, "share" /*getString(R.string.share)*/));
        }catch (Exception e){
            Toast.makeText(this,"share failed", Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
        }
    }

    public void OnSummonClick(View v) {
        /**First showing the AlertDialog and let user to choose
         *  either use the mahougen or select others from sdcard.
         *  Both choice will show the tutorial of the generating of AR mahougen.*/

        final String[] chioce = new String[]{"用這張!", "從資料夾選取"}; //"Choose this" or "Select from directory"

        AlertDialog showChoice = new AlertDialog.Builder(MainActivity.this)
                .setTitle("選擇魔法陣來源")
                .setItems(chioce, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (chioce[i] == "用這張!") {
                            //Use the mahougen you draw
                            Save();//save mahougen
                            showTutorial();
                        } else if (chioce[i] == "從資料夾選取") {
                            //Use the mahougen(or other images) from the dir
                            AlertDialog imageChioce = new AlertDialog.Builder(MainActivity.this)
                                    .setAdapter(imageAdapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //get the image name from the list
                                            imageName = imageAdapter.getItem(i).toString();
                                            showTutorial();
                                        }
                                    })
                                    .show();
                        }

                    }
                })
                .show();
    }

    public void showTutorial()
    {
        /**Showing the tutorial of the generating.*/
        AlertDialog showTheTutorial = new AlertDialog.Builder(MainActivity.this)
                .setTitle("即將生成魔法陣")
                .setMessage("生成魔法陣時，請依照以下步驟:\n" +
                        "1.找一張辨識度高的相片或卡片(悠遊卡)，做為目標物\n" +
                        "2.將相機畫面對準對焦至目標物，盡量填滿整個相機畫面\n" +
                        "3.按下正下方的相機圖示，魔法陣將會生成\n" +
                        "4.成為大魔法師吧!\n")
                .setPositiveButton("生成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        //Choose the generate
//                        Toast.makeText(MainActivity.this, "即將生成，請稍等...", Toast.LENGTH_LONG).show();
//                        //Create the intent and sent the image name to the UserDefinedTargets activity.
//                        Intent intent = new Intent();
//                        intent.setClassName(getPackageName(), getPackageName() + ".app.UserDefinedTargets.UserDefinedTargets");
//                        intent.putExtra("imageName",imageName);
//                        //go to the AR ui
//                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Choose to cancel
                        Toast.makeText(MainActivity.this, "取消", Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }
    public boolean createColorSpinner(){
        Spinner mahougenColorSpinner = (Spinner) findViewById(R.id.mahougen_color_spinner);
        Spinner backgroundColorSpinner = (Spinner) findViewById(R.id.background_color_spinner);
        /**
         * Here I declare two color name strings because, if the two spinner declared by the same adapter,
         * the color of mahougen and background will both be black while opening this app.
        */
        final String mcolor_cnames[] = {"白色","黑色","灰色","藍色","紅色"}; //Chinese color names.(For mahougen spinner)
        final String mcolor_enames[] = {"WHITE","BLACK","GRAY","BLUE","RED"}; //English color names.(For color changing functions.)
        final String bcolor_cnames[] = {"黑色","白色","灰色","藍色","紅色"}; //Chinese color names.(For background spinner)
        final String bcolor_enames[] = {"BLACK","WHITE","GRAY","BLUE","RED"}; //English color names.(For color changing functions.)

        ArrayAdapter madapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, mcolor_cnames);
        ArrayAdapter badapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, bcolor_cnames);

        mahougenColorSpinner.setAdapter(madapter);
        backgroundColorSpinner.setAdapter(badapter);

        //Set spinners onSelected function.
        mahougenColorSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView adapterView, View view, int position, long id){
                System.out.println("mahougen color:"+adapterView.getSelectedItem().toString());
                mahougenView.changeLineColor(mcolor_enames[position]);
            }
            public void onNothingSelected(AdapterView arg0) {

            }
        });
        backgroundColorSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView adapterView, View view, int position, long id){
                System.out.println("background color:"+adapterView.getSelectedItem().toString());
                mahougenView.changeBackgroundColor(bcolor_enames[position]);
            }
            public void onNothingSelected(AdapterView arg0) {

            }
        });
        return true;
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        /** select to change background color or line color*/
//        //The manu of background color
//        SubMenu subMenuBackground = menu.addSubMenu(Menu.NONE, Menu.FIRST, Menu.NONE, "改變背景顏色");
//        subMenuBackground.add(Menu.NONE, Menu.FIRST+1, Menu.NONE,"黑色");
//        subMenuBackground.add(Menu.NONE, Menu.FIRST+2, Menu.NONE,"白色");
//        subMenuBackground.add(Menu.NONE, Menu.FIRST+3, Menu.NONE,"灰色");
//        subMenuBackground.add(Menu.NONE, Menu.FIRST+4, Menu.NONE,"藍色");
//        subMenuBackground.add(Menu.NONE, Menu.FIRST+5, Menu.NONE,"紅色");
//
//        //The manu of line color
//        SubMenu subMenuLine = menu.addSubMenu(Menu.NONE+1, Menu.FIRST+6, Menu.NONE, "改變線條顏色");
//        subMenuLine.add(Menu.NONE+1, Menu.FIRST+7, Menu.NONE,"黑色");
//        subMenuLine.add(Menu.NONE+1, Menu.FIRST+8, Menu.NONE,"白色");
//        subMenuLine.add(Menu.NONE+1, Menu.FIRST+9, Menu.NONE,"灰色");
//        subMenuLine.add(Menu.NONE+1, Menu.FIRST+10, Menu.NONE,"藍色");
//        subMenuLine.add(Menu.NONE+1, Menu.FIRST+11, Menu.NONE,"紅色");
//
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        /**If user choose the color in the menu ,change it.*/
//        switch(item.getItemId()){
//            case Menu.FIRST+1:
//                mahougenView.changeBackgroundColor("BLACK");
//                break;
//            case Menu.FIRST+2:
//                mahougenView.changeBackgroundColor("WHITE");
//                break;
//            case Menu.FIRST+3:
//                mahougenView.changeBackgroundColor("GRAY");
//                break;
//            case Menu.FIRST+4:
//                mahougenView.changeBackgroundColor("BLUE");
//                break;
//            case Menu.FIRST+5:
//                mahougenView.changeBackgroundColor("RED");
//                break;
//            case Menu.FIRST+7:
//                mahougenView.changeLineColor("BLACK");
//                break;
//            case Menu.FIRST+8:
//                mahougenView.changeLineColor("WHITE");
//                break;
//            case Menu.FIRST+9:
//                mahougenView.changeLineColor("GRAY");
//                break;
//            case Menu.FIRST+10:
//                mahougenView.changeLineColor("BLUE");
//                break;
//            case Menu.FIRST+11:
//                mahougenView.changeLineColor("RED");
//                break;
//        }
//        if(item.getItemId()!= Menu.FIRST&&item.getItemId()!= Menu.FIRST+6)
//            //If the chosen item is not the "Change background" or "Change line"
//            Toast.makeText(this,"顏色已修改", Toast.LENGTH_LONG).show();
//        return super.onOptionsItemSelected(item);
//    }


    public void updateImageList() {
        /** load the song list form sdcard*/
        String path = this.getFilesDir()+"/mahougens/"; //The path of mahougen dir
        File home = new File(path);
        /*check if there is any file*/
        if (home.listFiles( new FileExtensionFilter()).length > 0) {
            for (File file : home.listFiles( new FileExtensionFilter())) {
                //add png file to image list
                images.add(file.getName());
            }

            /*put the image list into ListView*/
            imageAdapter = new ArrayAdapter<String>
                    (this,R.layout.support_simple_spinner_dropdown_item,images);

        }
    }

    public class FileExtensionFilter implements FilenameFilter {
        /** check if file is end with ".png" or ".PNG"*/
        public boolean accept(File dir, String name) {
            return (name.endsWith(".png") || name.endsWith(".PNG")||name.endsWith(".jpg")||name.endsWith(".JPG"));
        }
    }
}