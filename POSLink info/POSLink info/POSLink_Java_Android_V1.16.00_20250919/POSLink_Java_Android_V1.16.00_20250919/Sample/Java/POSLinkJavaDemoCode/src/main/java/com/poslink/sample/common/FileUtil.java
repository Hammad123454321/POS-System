package com.poslink.sample.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class FileUtil {

    /**
     *
     * @param path
     * @return
     */
    public static String readByBytes(String path) {
        String content = null;

        try {
            InputStream inputStream = new FileInputStream(path);
            StringBuffer sb = new StringBuffer();
            int c = 0;
            byte[] bytes = new byte[1024];
            /*
             * InputStream.read(byte[] b)
             *
             * Reads some number of bytes from the input stream and stores them into the buffer array b.
             * The number of bytes actually read is returned as an integer.
             * This method blocks until input data is available, end of file is detected, or an exception is thrown.
             */
            while ((c = inputStream.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, c, "utf-8"));
            }

            content = sb.toString();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    /**
     *
     * @param path
     * @return
     */
    public static String readByLines(String path) {
        String content = null;


        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));

            StringBuffer sb = new StringBuffer();
            String temp = null;
            while ((temp = bufferedReader.readLine()) != null) {
                sb.append(temp);
            }

            content = sb.toString();
            bufferedReader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    /**
     *
     * @param path
     * @return
     */
    public static String readByChars(String path) {
        String content = null;

        try {

            Reader reader = new InputStreamReader(new FileInputStream(path), "utf-8");
            StringBuffer sb = new StringBuffer();


            char[] tempchars = new char[1024];
            while (reader.read(tempchars) != -1) {
                sb.append(tempchars);
            }

            content = sb.toString();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     *
     * @param content
     * @param path
     * @return
     */
    public static boolean saveAs(String content, String path) {

        FileWriter fw = null;

        try {
            /**
             * Constructs a FileWriter object given a File object.
             * If the second argument is true, then bytes will be written to the end of the file rather than the beginning.
             *
             *    Parameters:
             *        file,  a File object to write to
             *        append,  if true, then bytes will be written to the end of the file rather than the beginning
             *    Throws:
             *        IOException -
             *        if the file exists but is a directory rather than a regular file,
             *            does not exist but cannot be created,
             *            or cannot be opened for any other reason
             */
            fw = new FileWriter(new File(path), false);
            if (content != null) {
                fw.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fw != null) {
                try {
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static boolean createFile(String strFilePath, String strFileContent) throws IOException {
        boolean bFlag = false;
        File file = new File(strFilePath);
        bFlag = file.exists() || file.createNewFile();
        if (bFlag == Boolean.TRUE) {
            FileWriter fw = new FileWriter(file);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(strFileContent);
            pw.close();
        }
        return bFlag;
    }
}