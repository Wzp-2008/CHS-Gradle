package cn.wzpmc;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.util.Arrays;

public class AgentAdvice {

    @Advice.OnMethodExit()
    public static void exit(@Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object returned) {
        SimpleLogger.INSTANCE.info("CreateRepoReturnAdvice.exit() called. returned=" + (returned == null ? "null" : returned.getClass().getName()) + " thread=" + Thread.currentThread().getName());

        if (returned == null) {
            SimpleLogger.INSTANCE.info("Returned repository is null, nothing to do.");
            return;
        }

        try {
            String retClassName = returned.getClass().getName();
            SimpleLogger.INSTANCE.info("CHS-Agent found repository instance: " + retClassName);
            java.lang.reflect.Method getUrl = Utils.findMethodNoArgs(returned.getClass(), "getUrl");
            if (getUrl == null) {
                SimpleLogger.INSTANCE.warn("CHS-Agent: getUrl() not found on returned repository: " + retClassName);
                return;
            }
            Object urlObj = getUrl.invoke(returned);
            if (urlObj == null) {
                SimpleLogger.INSTANCE.warn("CHS-Agent: repository.getUrl() returned null");
                return;
            }
            String originalUrl = urlObj.toString();
            SimpleLogger.INSTANCE.info("CHS-Agent original URL: " + originalUrl);

            if (originalUrl.endsWith(Utils.DISTRIBUTIONS_SNAPSHOTS_SUFFIX)) {
                SimpleLogger.INSTANCE.warn("CHS-Agent 检测到 snapshot 版本 gradle，无法更换源: " + originalUrl);
                return;
            }

            if (!originalUrl.contains(Utils.GRADLE_DISTRIBUTIONS_HOST)) {
                SimpleLogger.INSTANCE.info("CHS-Agent original URL does not point to services.gradle.org/distributions; skipping.");
                return;
            }

            String srcBase = System.getenv(Utils.SOURCE_BASE_ENV);
            if (srcBase == null || srcBase.trim().isEmpty()) {
                srcBase = Utils.DEFAULT_SRC_BASE;
            }
            String targetUrl = originalUrl.replace(Utils.GRADLE_DISTRIBUTIONS_HOST, srcBase);

            boolean setOk = Utils.trySetUrl(returned, targetUrl);

            if (setOk) {
                SimpleLogger.INSTANCE.info("CHS-Agent 成功替换 src 下载源！");
                SimpleLogger.INSTANCE.info("CHS-Agent " + originalUrl + " ===>> " + targetUrl);
            } else {
                SimpleLogger.INSTANCE.warn("CHS-Agent 未能找到合适的 setUrl 方法来替换 URL on " + retClassName);
            }

        } catch (Throwable t) {
            SimpleLogger.INSTANCE.error("CHS-Agent exception while modifying repository URL: " + t.getMessage());
            SimpleLogger.INSTANCE.error(Arrays.toString(t.getStackTrace()));
        }
    }
}