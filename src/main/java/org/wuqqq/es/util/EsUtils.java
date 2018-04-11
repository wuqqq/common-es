package org.wuqqq.es.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.wuqqq.es.common.EsRuntimeException;
import io.searchbox.annotations.JestId;
import io.searchbox.annotations.JestVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.wuqqq.es.common.EsErrorEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 * @author wuqi 2018/2/2 0002.
 */
public class EsUtils {

    private static final Logger logger = LoggerFactory.getLogger(EsUtils.class);

    public static String buildDoc(Object obj) {
        if (obj == null)
            return null;
        return new DocBuilder(obj).build();
    }

    private static class DocBuilder {
        private Object obj;

        DocBuilder(Object obj) {
            this.obj = obj;
        }

        String build() {
            String script = null;
            try {
                String docStr = JSON.toJSONString(obj, (PropertyFilter) (object, name, value) -> {
                    Class<?> clazz = object.getClass();
                    Field f = null;
                    try {
                        f = clazz.getDeclaredField(name);
                    } catch (NoSuchFieldException e) {
                        try {
                            f = clazz.getField(name);
                        } catch (Exception ignored) {
                        }
                    }
                    if (f != null && (f.isAnnotationPresent(JestId.class) || f.isAnnotationPresent(JestVersion.class))) {
                        return false;
                    }
                    return true;
                }, SerializerFeature.WriteBigDecimalAsPlain);
                if (!docStr.isEmpty()) {
                    script = "{" + "\"doc\":" + docStr + "}";
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            return script;
        }
    }

    public static String loadJsonStringFromPath(String path) {
        BufferedReader br = null;
        try {
            StringBuilder json = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(new ClassPathResource(path).getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
            return json.toString();
        } catch (IOException e) {
            logger.error("load file {} failed: {}", path, e);
            throw new EsRuntimeException(EsErrorEnum.IO_EXCEPTION, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
