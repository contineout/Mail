package com.example.ttett.util.mailUtil;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.ttett.Dao.AttachmentDao;
import com.example.ttett.Entity.Attachment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import static android.content.ContentValues.TAG;

public class ShowMail {
    private MimeMessage mimeMessage = null;
    private String saveAttachPath = ""; // 附件下载后的存放目录
    private StringBuffer bodyText = new StringBuffer(); // 存放邮件内容的StringBuffer对象
    private String dateFormat = "yyyy-MM-dd HH:mm:ss"; // 默认的日前显示格式
    private Context mContext;
    private Attachment attachment;
    private int email_id;
    private int count = 0;

    /**
     * 构造函数,初始化一个MimeMessage对象
     */
    public ShowMail() {
    }

    public ShowMail(MimeMessage mimeMessage, Context context,int email_id) {
        this.mimeMessage = mimeMessage;
        this.mContext = context;
        this.email_id = email_id;
    }

    public void setMimeMessage(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;
    }

    /**
     * 　*　获得发件人的地址和姓名 　
     */
    public String getFrom() throws Exception {
        InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
        String from = address[0].getAddress();
        if (from == null) {
            from = "";
        }
        String personal = address[0].getPersonal();

        if (personal == null) {
            personal = "";
        }

        String fromAddr = null;
        if (personal != null || from != null) {
            fromAddr = personal + "<" + from + ">";
        } else {
        }
        return fromAddr;
    }

    /**
     * 　*　获得邮件的收件人，抄送，和密送的地址和姓名，根据所传递的参数的不同
     * 　*　"to"----收件人　"cc"---抄送人地址　"bcc"---密送人地址 　
     */
    public String getMailAddress(String type) throws Exception {
        String mailAddr = "";
        String addType = type.toUpperCase();

        InternetAddress[] address = null;
        if (addType.equals("TO") || addType.equals("CC")
                || addType.equals("BCC")) {

            if (addType.equals("TO")) {
                address = (InternetAddress[]) mimeMessage
                        .getRecipients(Message.RecipientType.TO);
            } else if (addType.equals("CC")) {
                address = (InternetAddress[]) mimeMessage
                        .getRecipients(Message.RecipientType.CC);
            } else {
                address = (InternetAddress[]) mimeMessage
                        .getRecipients(Message.RecipientType.BCC);
            }

            if (address != null) {
                for (int i = 0; i < address.length; i++) {
                    String emailAddr = address[i].getAddress();
                    if (emailAddr == null) {
                        emailAddr = "";
                    } else {
                        emailAddr = MimeUtility.decodeText(emailAddr);
                    }
                    String personal = address[i].getPersonal();
                    if (personal == null) {
                        personal = "";
                    } else {
                        personal = MimeUtility.decodeText(personal);
                    }
                    String compositeto = personal + "<" + emailAddr + ">";
                    mailAddr += "," + compositeto;
                }
                mailAddr = mailAddr.substring(1);
            }
        } else {
            throw new Exception("错误的电子邮件类型!");
        }
        return mailAddr;
    }

    /**
     * 　*　获得邮件主题 　
     */
    public String getSubject() throws MessagingException {
        String subject = "";
        try {
            subject = MimeUtility.decodeText(mimeMessage.getSubject());
            if (subject == null) {
                subject = "";
            }
        } catch (Exception exce) {
            exce.printStackTrace();
        }
        return subject;
    }

    /**
     * 　*　获得邮件发送日期 　
     */
    public String getSentDate() throws Exception {
        Date sentDate = mimeMessage.getSentDate();
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        String strSentDate = format.format(sentDate);
        return strSentDate;
    }
    public String getSaveDate() throws Exception {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        String strSaveDate = format.format(date);
        return strSaveDate;
    }

    /**
     * 　*　获得邮件正文内容 　
     */
    public String getBodyText() {
        return bodyText.toString();
    }

    /**
     * 　　*　解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件
     * 　　*　主要是根据MimeType类型的不同执行不同的操作，一步一步的解析 　　
     */

    public void getMailContent(Part part) throws Exception {

        String contentType = part.getContentType();
        // 获得邮件的MimeType类型

        int nameIndex = contentType.indexOf("name");

        boolean conName = false;

        if (nameIndex != -1) {
            conName = true;
        }


        if (part.isMimeType("text/plain") && !conName) {
            // text/plain 类型
            bodyText.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && !conName) {
            // text/html 类型
            bodyText.append((String) part.getContent());
        } else if (part.isMimeType("multipart/*")) {
            // multipart/*
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                getMailContent(multipart.getBodyPart(i));
            }
        } else if (part.isMimeType("message/rfc822")) {
            // message/rfc822
            getMailContent((Part) part.getContent());
        }
    }

    /**
     * 　　*　判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false" 　
     */
    public boolean getReplySign() throws MessagingException {

        boolean replySign = false;

        String[] needReply = mimeMessage
                .getHeader("Disposition-Notification-To");

        if (needReply != null) {
            replySign = true;
        }
        if (replySign) {
        } else {
        }
        return replySign;
    }

    /**
     *　获得此邮件的Message-ID 　　
     */
    public String getMessageId() throws MessagingException {
        try{
            String messageID = mimeMessage.getMessageID();
            return messageID;
        }catch (Exception e){
//            return RandomNum();
        }
        return null;
    }

    public String RandomNum()
    {  int a = 0;
        for(int i=0;i<20;i++)		 //控制产生的随机数的个数
        {
            Random random=new Random();	 //使用Random函数产生随机数；
            a = random.nextInt(10)+11;	 //random.nextInt(n)为产生的随机数的范围
        }
        return String.valueOf(a);
    }

    /**
     * 判断此邮件是否已读，如果未读返回false,反之返回true
     */
    public boolean isNew() throws MessagingException {
        boolean isNew = false;
        Flags flags = ((Message) mimeMessage).getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
        for (int i = 0; i < flag.length; i++) {
            if (flag[i] == Flags.Flag.SEEN) {
                isNew = true;
                // break;
            }
        }
        return isNew;
    }

    /**
     * 判断此邮件是否包含附件
     */
    public boolean isContainAttach(Part part) throws Exception {
        boolean attachFlag = false;
        // String contentType = part.getContentType();
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mPart = mp.getBodyPart(i);
                String disposition = mPart.getDisposition();
                if ((disposition != null)
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition
                        .equals(Part.INLINE))))
                    attachFlag = true;
                else if (mPart.isMimeType("multipart/*")) {
                    attachFlag = isContainAttach((Part) mPart);
                } else {
                    String conType = mPart.getContentType();
                    if (conType.toLowerCase().contains("application"))
                        attachFlag = true;
                    if (conType.toLowerCase().contains("name"))
                        attachFlag = true;
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            attachFlag = isContainAttach((Part) part.getContent());
        }
        return attachFlag;
    }

    /**
     * 　*　保存附件 　
     */

    public void saveAttachMent(Part part,String id) throws Exception {
        String fileName = "";
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mPart = mp.getBodyPart(i);
                String disposition = mPart.getDisposition();
                if ((disposition != null)
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition
                        .equals(Part.INLINE)))) {
                    fileName = mPart.getFileName();
                    if (fileName.toLowerCase().contains("gb2312")||fileName.toLowerCase().contains("gb18030")) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    saveFile(fileName, mPart.getInputStream(),id);
                } else if (mPart.isMimeType("multipart/*")) {
                    saveAttachMent(mPart,id);
                } else {
                    fileName = mPart.getFileName();
                    if ((fileName != null)
                            && (fileName.toLowerCase().contains("GB2312")||fileName.toLowerCase().contains("gb18030"))) {
                        fileName = MimeUtility.decodeText(fileName);
                        saveFile(fileName, mPart.getInputStream(),id);
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachMent((Part) part.getContent(),id);
        }
    }

    /**
     *　设置附件存放路径
     */
    public void setAttachPath(String attachPath) {
        this.saveAttachPath = attachPath;
    }

    /**
     * 　*　设置日期显示格式 　
     */
    public void setDateFormat(String format) throws Exception {
        this.dateFormat = format;
    }

    /**
     * 　*　获得附件存放路径 　
     */
    public String getAttachPath() {
        return saveAttachPath;
    }

    /**
     * 　*　真正的保存附件到指定目录里 　
     */
    private void saveFile(String fileName, InputStream in,String id) throws Exception {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return ;
        File path = mContext.getExternalFilesDir("Email");
        File file = new File(path, fileName);

        try {
            attachment = new Attachment();
            // 确认Music目录存在
            path.mkdirs();
            String[] str = fileName.split("[.]");
            FileOutputStream out = new FileOutputStream(file);
            byte[] bytes = new byte[in.available()];
            attachment.setSize(String.valueOf(in.available()));
            attachment.setName(fileName);
            attachment.setSaveDate(getSaveDate());
            attachment.setType(str[1]);
            attachment.setMessage_id(id);
            attachment.setEmail_id(email_id);
            attachment.setPath(path.getPath()+"/"+fileName);
            in.read(bytes);
            out.write(bytes);
            in.close();
            out.close();
            count+=1;
            AttachmentDao attachmentDao = new AttachmentDao(mContext);
            attachmentDao.InsertAttachment(attachment);
            Log.d(TAG,"第"+id+"封"+"第"+count+"附件"+"path = " + path.toString() + "/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
