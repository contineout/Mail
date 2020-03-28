package com.example.ttett;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.ttett.Adapter.SelectAdapter;
import com.example.ttett.Entity.EmailMessage;
import com.example.ttett.Service.MailService;
import com.example.ttett.bean.MessageEvent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectMailActivity extends AppCompatActivity implements View.OnClickListener {
    private BottomNavigationView bottomNavigationView;
    private Toolbar mToolbar;
    private RecyclerView selectRv;
    private List<EmailMessage> emailMessages;
    private SelectAdapter selectAdapter;
    private String TAG = "SelectMailActivity";
    private TextView tvCancel,tvSelectCount,tvAllSelect;
    private Map<Integer, Boolean> checkStatus;
    private int trueCount;
    MailService mailService = new MailService(this);
    private List<Integer> id_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_mail);

        mToolbar = findViewById(R.id.select_toolbar);
        selectRv = findViewById(R.id.select_rv);
        tvAllSelect = findViewById(R.id.All_select);
        tvCancel = findViewById(R.id.select_cancel);
        tvSelectCount = findViewById(R.id.select_count);
        bottomNavigationView = findViewById(R.id.select_Bnv);

        EventBus.getDefault().register(this);

        setSupportActionBar(mToolbar);
        Bundle bundle = getIntent().getExtras();
        emailMessages = bundle.getParcelableArrayList("emailMessages");
        checkStatus = new HashMap<>();
        for(int i =0;i < emailMessages.size();i++){
            checkStatus.put(i,false);
        }

        selectRv.setLayoutManager(new LinearLayoutManager(this));
        selectAdapter = new SelectAdapter(this,emailMessages,checkStatus);
        selectRv.setAdapter(selectAdapter);


        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setEnabled(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(onBottomNavigationItemSelectedListener);

        tvAllSelect.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    /**
     * 获取选择的item状态和messageId
     * @param event
     */
    @SuppressLint("ResourceType")
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void bottomNavigationViewCheck(MessageEvent event){
        if(event.getMessage().equals("check_status")){
            checkStatus = event.getCheckStatus();
            switch_Bnv(checkStatus);
        }
    }

    /**
     * 切换buttomNavigation图标
     * @param checkStatus
     */
    private void switch_Bnv(Map<Integer, Boolean> checkStatus){
        MenuItem item1 = bottomNavigationView.getMenu().getItem(0);
        MenuItem item2 = bottomNavigationView.getMenu().getItem(1);
        MenuItem item3 = bottomNavigationView.getMenu().getItem(2);
        MenuItem item4 = bottomNavigationView.getMenu().getItem(3);
        trueCount = 0 ;
        id_item = new ArrayList<Integer>();
        for(Map.Entry<Integer,Boolean> entry:checkStatus.entrySet()){
            if(entry.getValue()){
                id_item.add(emailMessages.get(entry.getKey()).getId());
                trueCount++;
            }
        }

        if(trueCount!=0){
            tvSelectCount.setText("已选择"+trueCount+"封");
            item1.setIcon(R.mipmap.set_star_check);
            item2.setIcon(R.mipmap.set_unread_check);
            item3.setIcon(R.mipmap.trash);
            item4.setIcon(R.mipmap.set_tran_check);
            item1.setEnabled(true);
            item2.setEnabled(true);
            item3.setEnabled(true);
            item4.setEnabled(true);
        }else {
            tvSelectCount.setText("多选");
            item1.setIcon(R.mipmap.set_star_uncheck);
            item2.setIcon(R.mipmap.set_unread_uncheck);
            item3.setIcon(R.mipmap.set_delete_uncheck);
            item4.setIcon(R.mipmap.set_tran_uncheck);
            item1.setEnabled(false);
            item2.setEnabled(false);
            item3.setEnabled(false);
            item4.setEnabled(false);
        }

        if(trueCount == checkStatus.size()){
            tvAllSelect.setText("取消全选");
        }else{
            tvAllSelect.setText("全选");
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener onBottomNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.set_star:
                    if(id_item != null){
                        for(int id:id_item){
                            mailService.updateisStar(id);
                        }
                    }
                    finish();
                    break;
                case R.id.set_unread:
                    if(id_item != null){
                        for(int id:id_item){
                            mailService.updateReadMessage(id);
                        }
                    }
                    finish();
                    break;
                case R.id.set_delete:
                    if(id_item != null){
                        for(int id:id_item){
                            mailService.updateisDelete(id);
                        }
                    }
                    finish();
                    break;
                case R.id.set_tran:

                    return true;
                default:
                    break;

            }
            return false;
        }
    };


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.select_cancel:
                finish();
                break;
            case R.id.All_select:
                if(trueCount == checkStatus.size()){
                    for(int i =0;i < checkStatus.size();i++){
                        checkStatus.put(i,false);
                    }
                }else{
                    for(int i =0;i < checkStatus.size();i++){
                        checkStatus.put(i,true);
                    }
                }
                selectAdapter.notifyDataSetChanged();
                break;
                default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
