package com.example.sb3_2_saml.log;

import org.slf4j.Logger;
import org.owasp.encoder.Encode;

public class CleanLogger {

    private Logger logger;

    public CleanLogger(Logger logger) {
        this.logger = logger;
    }

    private String clean(String msg) {
        String cleanMsg = null;
        try {
            if (msg != null) {
                msg = msg.replace('\n', '_').replace('\r', '_')
                        .replace('\t', '_');
                cleanMsg = Encode.forHtml(msg);
                return msg;
            }
        } catch (Exception e) {
            this.logger.error("Failed to print log statement.  Could not clean");
        }
        return cleanMsg;
    }

    private void clean(Object... args) {
        for (int i = 0; i < args.length; i++) {
            if(args[i] instanceof String) {
                args[i] = clean(args[i].toString());
            } else if(args[i] instanceof Object[] objects) {
                for(int j = 0; j < objects.length; j++) {
                    objects[j] = clean(objects[j].toString());
                }
            }
        }
    }

    public void info(String msg) {
        this.logger.info(clean(msg));
    }

    public void info(String msg, Throwable t) {
        this.logger.info(clean(msg), t);
    }

    public void info(String msg, Object... args) {
        clean(args);
        this.logger.info(clean(msg), args);
    }


}
