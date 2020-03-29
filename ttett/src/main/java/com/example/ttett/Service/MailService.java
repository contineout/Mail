package com.example.ttett.Service;

import android.content.Context;
import android.util.Log;

import com.example.ttett.Dao.MailDao;
import com.example.ttett.Entity.Email;
import com.example.ttett.Entity.EmailMessage;
import com.example.ttett.util.RecipientMessage;

import java.util.List;

import javax.mail.Message;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MailService {

    private Context mContext;
    public MailService(Context context) {
        this.mContext = context;
    }

    /**
     * 判断message_id是否有重复，若没有则保存
     * @param emailMessages
     * @return
     */
    public Boolean SaveMessage( List<EmailMessage> emailMessages) {
        MailDao mailDao = new MailDao(mContext);
        int i = 0;
        for (EmailMessage emailMessage : emailMessages) {
            if (!mailDao.isExistMail(emailMessage.getMessage_id())) {
                mailDao.InsertMessages(emailMessage);
                i+=1;
            }
        }
        return i > 0;
    }

    /**
     * 判断有没有新邮件
     * @param email
     * @param messages
     * @return
     */
    public Message[] isNewMessage(Email email, Message[] messages) {
        MailDao mailDao = new MailDao(mContext);
        int MessageCount = mailDao.QueryMessageCount(email);
        Log.d(TAG,"MessageCount"+MessageCount);
        int RecipientMessageCount = messages.length;
        if(MessageCount == 0){
            return messages;
        }
        if(MessageCount < RecipientMessageCount){
            int Count = RecipientMessageCount - MessageCount;
            Message[] temp = new Message[Count];
            for(int i = 0;i < Count; i++){
                temp[i] = messages[MessageCount+i];
            }
            return temp;
        }
        return null;
    }

    /**
     * 同步邮件
     * @param email
     * @return
     */
    public void SynchronizeMessage(Email email){
        RecipientMessage recipientMessage = new RecipientMessage();
        List<EmailMessage> emailMessages;
        switch (email.getType()){
            case "sina.com":
                emailMessages = recipientMessage.SinaRecipient(email,mContext);
                if(SaveMessage(emailMessages)){
                    Log.d(TAG,"sina有新邮件保存成功");
                }
                break;
            case "qq.com":
                emailMessages = recipientMessage.QQRecipient(email,mContext);
                if(SaveMessage(emailMessages)){
                    Log.d(TAG,"sina有新邮件保存成功");
                }
                break;
        }
    }

    /**
     * 查询sqlite所有邮件
     * @param email
     * @return
     */
    public List<EmailMessage> queryAllMessage(Email email){
        MailDao mailDao = new MailDao(mContext);
        return mailDao.QueryAllMessage(email);
    }

    /**
     * 查询sqlite所有已发送邮件
     * @param email
     * @return
     */
    public List<EmailMessage> querySendedMessage(Email email){
        MailDao mailDao = new MailDao(mContext);
        return mailDao.QuerySendedMessage(email);
    }

    /**
     * 查询sqlite所有未发送邮件
     * @param email
     * @return
     */
    public List<EmailMessage> queryDraftsMessage(Email email){
        MailDao mailDao = new MailDao(mContext);
        return mailDao.QueryDraftMessage(email);
    }

    /**
     * 批量修改isRead标志位
     * @param id_item
     */
    public void updateReadMessage(List<Integer> id_item,boolean setUnread) {
        MailDao mailDao = new MailDao(mContext);
        for(int id:id_item){
            if(mailDao.isExistMessage(id)){
               if(setUnread){
                   mailDao.updateunRead(id);
               }else{
                   mailDao.updateRead(id);
               }
            }
        }
    }

    /**
     * 批量修改isStar标志位
     * @param id_item
     */
    public void updateStarMessage(List<Integer> id_item,boolean setStar) {
        MailDao mailDao = new MailDao(mContext);
        for(int id:id_item){
            if(mailDao.isExistMessage(id)){
                if(setStar){
                    mailDao.updateStar(id);
                }else{
                    mailDao.updateUnStar(id);
                }
            }
        }
    }

    /**
     * 点击取消设置isRead = 1
     * @param id
     */
    public void updateReadMessage(int id) {
        MailDao mailDao = new MailDao(mContext);
        if(mailDao.isExistMessage(id)){
            mailDao.updateRead(id);
        }
    }

    /**
     * 查询选中已读邮件数量 isRead = 1
     * @param id_item
     * @return
     */

    public int queryReadCount(List<Integer> id_item){
        MailDao mailDao = new MailDao(mContext);
        int ReadCount = 0;
        for(int id:id_item){
            if(mailDao.isExistMessage(id)){
                ReadCount+=mailDao.queryisRead(id);
            }
        }
        return ReadCount;
    }

    public int queryStarCount(List<Integer> id_item){
        MailDao mailDao = new MailDao(mContext);
        int StarCount = 0;
        for(int id:id_item){
            if(mailDao.isExistMessage(id)){
                StarCount+=mailDao.queryisStar(id);
            }
        }
        return StarCount;
    }


    /**
     * 修改删除
     * @param id
     */
    public void updateisDelete(int id) {
        MailDao mailDao = new MailDao(mContext);
        if(mailDao.isExistMessage(id)){
            mailDao.updateisDelete(id);
        }
    }

    /**
     * 查询未读邮件
     * @param email
     * @return
     */
    public List<EmailMessage> queryUnReadMessage(Email email){
        MailDao mailDao = new MailDao(mContext);
        return mailDao.QueryUnReadMessage(email);
    }

    /**
     * 查询星标邮件
     * @param email
     * @return
     */
    public List<EmailMessage> queryStarMessage(Email email){
        MailDao mailDao = new MailDao(mContext);
        return mailDao.QueryStarMessage(email);
    }

}
