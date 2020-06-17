package com.dong;

import com.dong.util.StringUtil;
import com.dong.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 * 从流中获取内容
 * @version 1.0 2020/6/16
 * @author dongliyang
 */
public class StreamReceiver extends  Thread {
    
    private final Logger logger = LoggerFactory.getLogger(StreamReceiver.class);

    private InputStream inputStream;
    private String streamName;
    private StringBuilder buffer;
    private String charsetName;
    private volatile boolean isStopped = false;

    public StreamReceiver(InputStream inputStream, String streamName,String charsetName) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream cannot be null");
        }
        if (StringUtil.isNullEmpty(streamName)) {
            throw new IllegalArgumentException("streamName cannot be null or empty");
        }
        if (StringUtil.isNullEmpty(charsetName)) {
            throw new IllegalArgumentException("charsetName cannot be null or empty");
        }
        this.inputStream = inputStream;
        this.streamName = streamName;
        this.charsetName = charsetName;

        this.buffer = new StringBuilder();
        this.isStopped = false;
    }

    @Override
    public void run() {
        logger.debug("streamReceiver thread is running,thread name:{}", Thread.currentThread().getName());

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charsetName));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                logger.debug("{} > {}",streamName,line);
                this.buffer.append(line);
            }
        } catch (Exception e) {
            logger.error("Failed to consume and display the input stream of {},exception message:{}",streamName,e.getMessage());
        } finally {
            this.isStopped = true;
            synchronized (this) {
                notify();
            }
        }

        logger.debug("exit streamReceiver thread,thread name:{}",Thread.currentThread().getName());
    }

    public String getContent() {
        if (!this.isStopped) {
            synchronized (this) {
                try {
                    wait();
                } catch (Exception e) {
                    logger.error("an exception has appeared when wait,exception message:{}", e.getMessage());
                }
            }
        }
        return this.buffer.toString();
    }
}
