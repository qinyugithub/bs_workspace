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
            //从request当中获取流信息
            InputStream fileSource = req.getInputStream();
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");//用于临时文件
            File fileupload = new File(realPath);
            if (!fileupload.exists()) {
                fileupload.mkdir();
            }
            String tempFileName = realPath + "/tp" + uuid;
            //tempFile指向临时文件
            File tempFile = new File(tempFileName);
            //outputStram文件输出流指向这个临时文件
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte b[] = new byte[1024];
            int n;
            while ((n = fileSource.read(b)) != -1) {
                outputStream.write(b, 0, n);
            }
            //关闭输出流、输入流
            outputStream.close();
            fileSource.close();

            //获取上传文件的名称
            RandomAccessFile randomFile = new RandomAccessFile(tempFile, "r");
            randomFile.readLine();
            String str = randomFile.readLine();
            //int beginIndex = str.lastIndexOf("=") + 2;
            int endIndex = str.lastIndexOf("\"");
            int spotIndex = str.lastIndexOf(".");
            //String filename = str.substring(beginIndex, endIndex);//原始文件名
            String fileSuffix=str.substring(spotIndex, endIndex);//文件后缀
            //开始生成新的文件名

            String newuuid = UUID.randomUUID().toString().replaceAll("-", "");
            String filename=newuuid+fileSuffix;


            //System.out.println("filename:" + filename);
            //重新定位文件指针到文件头
            randomFile.seek(0);
            long startPosition = 0;
            int i = 1;
            //获取文件内容 开始位置
            while ((n = randomFile.readByte()) != -1 && i <= 4) {
                if (n == '\n') {
                    startPosition = randomFile.getFilePointer();
                    i++;
                }
            }
            startPosition = randomFile.getFilePointer() - 1;
            //获取文件内容 结束位置
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

            //设置保存上传文件的路径
            //String realPath = getServletContext().getRealPath("/") + "images";

            File saveFile = new File(realPath, filename);
            RandomAccessFile randomAccessFile = new RandomAccessFile(saveFile, "rw");
            //从临时文件当中读取文件内容（根据起止位置获取）
            randomFile.seek(startPosition);
            while (startPosition < endPosition) {
                randomAccessFile.write(randomFile.readByte());
                startPosition = randomFile.getFilePointer();
            }
            //关闭输入输出流、删除临时文件
            randomAccessFile.close();
            randomFile.close();
            tempFile.delete();

            JSONObject obj = new JSONObject();
            obj.put("code", 0);
            obj.put("filename", realPath.substring(realPath.lastIndexOf("/")+1)+"/"+filename);
            obj.put("msg", "success");
            return obj;
        }catch (Exception e){
            LOG.error("文件上传失败：" + e);
            JSONObject obj = new JSONObject();
            obj.put("code", 1);
            obj.put("filename", "");
            obj.put("msg", e);
            return obj;
        }
    }
}
