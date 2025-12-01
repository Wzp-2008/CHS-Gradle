package cn.wzpmc;

public class Utils {
    public static final String SOURCE_BASE_ENV = "gradle.src.base";
    public static final String DEFAULT_SRC_BASE = "https://mirrors.cloud.tencent.com/gradle";
    public static final String GRADLE_DISTRIBUTIONS_HOST = "https://services.gradle.org/distributions";
    public static final String DISTRIBUTIONS_SNAPSHOTS_SUFFIX = "distributions-snapshots";
    public static java.lang.reflect.Method findMethodNoArgs(Class<?> cls, String name) {
        for (java.lang.reflect.Method m : cls.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 0) {
                return m;
            }
        }
        return null;
    }

    public static boolean trySetUrl(Object repo, String targetUrl) {
        Class<?> cls = repo.getClass();

        try {
            java.lang.reflect.Method setUri = cls.getMethod("setUrl", java.net.URI.class);
            setUri.invoke(repo, new java.net.URI(targetUrl));
            return true;
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable t) {
            SimpleLogger.INSTANCE.error("CHS-Agent error invoking setUrl(URI): " + t);
        }

        try {
            java.lang.reflect.Method setString = cls.getMethod("setUrl", String.class);
            setString.invoke(repo, targetUrl);
            return true;
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable t) {
            SimpleLogger.INSTANCE.error("CHS-Agent error invoking setUrl(String): " + t.getMessage());
        }

        try {
            java.lang.reflect.Method setObject = cls.getMethod("setUrl", Object.class);
            setObject.invoke(repo, new java.net.URI(targetUrl));
            return true;
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable t) {
            SimpleLogger.INSTANCE.error("CHS-Agent error invoking setUrl(Object): " + t.getMessage());
        }

        return false;
    }
}
