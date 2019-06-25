package util;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.midi.SoundbankResource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.Date;

public class UploadFileUtil {
    private static Logger LOG = Logger.getLogger(UploadFileUtil.class);

    public JSONObject uploadfile(HttpServletRequest req, HttpServletResponse resp, String realPath)
            throws IOException, JSONException {
        try {
            //��request���л�ȡ����Ϣ
            InputStream fileSource = req.getInputStream();
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");//������ʱ�ļ�
            File fileupload = new File(realPath);
            if (!fileupload.exists()) {
                fileupload.mkdir();
            }
            String tempFileName = realPath + "/tp" + uuid;
            //tempFileָ����ʱ�ļ�
            File tempFile = new File(tempFileName);
            //outputStram�ļ������ָ�������ʱ�ļ�
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte b[] = new byte[1024];
            int n;
            while ((n = fileSource.read(b)) != -1) {
                outputStream.write(b, 0, n);
            }
            //�ر��������������
            outputStream.close();
            fileSource.close();

            //��ȡ�ϴ��ļ�������
            RandomAccessFile randomFile = new RandomAccessFile(tempFile, "r");
            randomFile.readLine();
            String str = randomFile.readLine();
            //int beginIndex = str.lastIndexOf("=") + 2;
            int endIndex = str.lastIndexOf("\"");
            int spotIndex = str.lastIndexOf(".");
            //String filename = str.substring(beginIndex, endIndex);//ԭʼ�ļ���
            String fileSuffix=str.substring(spotIndex, endIndex);//�ļ���׺
            //��ʼ�����µ��ļ���

            String newuuid = UUID.randomUUID().toString().replaceAll("-", "");
            String filename=newuuid+fileSuffix;


            //System.out.println("filename:" + filename);
            //���¶�λ�ļ�ָ�뵽�ļ�ͷ
            randomFile.seek(0);
            long startPosition = 0;
            int i = 1;
            //��ȡ�ļ����� ��ʼλ��
            while ((n = randomFile.readByte()) != -1 && i <= 4) {
                if (n == '\n') {
                    startPosition = randomFile.getFilePointer();
                    i++;
                }
            }
            startPosition = randomFile.getFilePointer() - 1;
            //��ȡ�ļ����� ����λ��
            randomFile.seek(randomFile.length());
            long endPosition = randomFile.getFilePointer();
            int j = 1;
            while (endPosition >= 0 && j <= 2) {
                endPosition--;
                randomFile.seek(endPosition);
                if (randomFile.readByte() == '\n') {
                    j++;
                }
            }
            endPosition = endPosition - 1;

            //���ñ����ϴ��ļ���·��
            //String realPath = getServletContext().getRealPath("/") + "images";

            File saveFile = new File(realPath, filename);
            RandomAccessFile randomAccessFile = new RandomAccessFile(saveFile, "rw");
            //����ʱ�ļ����ж�ȡ�ļ����ݣ�������ֹλ�û�ȡ��
            randomFile.seek(startPosition);
            while (startPosition < endPosition) {
                randomAccessFile.write(randomFile.readByte());
                startPosition = randomFile.getFilePointer();
            }
            //�ر������������ɾ����ʱ�ļ�
            randomAccessFile.close();
            randomFile.close();
            tempFile.delete();

            JSONObject obj = new JSONObject();
            obj.put("code", 0);
            obj.put("filename", realPath.substring(realPath.lastIndexOf("/")+1)+"/"+filename);
            obj.put("msg", "success");
            return obj;
        }catch (Exception e){
            LOG.error("�ļ��ϴ�ʧ�ܣ�" + e);
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            obj.put("filename", "");
            obj.put("msg", e);
            return obj;
        }
    }
}
