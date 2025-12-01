package cn.wzpmc;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ByteBuddy agent with additional entry/exit logging to diagnose why
 * CreateRepoReturnAdvice.exit() was not called.
 */
public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) {
        SimpleLogger.INSTANCE.info("CHS-Agent premain() starting");
        try {
            Class<?> module = Class.forName("java.lang.Module");
            Method getModule = Class.class.getDeclaredMethod("getModule");
            Object javaBase = getModule.invoke(Object.class);
            Object my = getModule.invoke(Agent.class);
            // 导出包给 agent 模块（如果只要访问类，使用 export；若需反射访问私有成员，使用 open）
            Map<String, Set<?>> extraExports = new HashMap<>();
            extraExports.put("jdk.internal.loader", Collections.singleton(my));
            Map<String, Set<?>> extraOpens = new HashMap<>();
            extraOpens.put("jdk.internal.loader", Collections.singleton(my));
            Method redefineModule = Instrumentation.class.getDeclaredMethod("redefineModule", module, Set.class, Map.class, Map.class, Set.class, Map.class);
            redefineModule.invoke(inst, javaBase, Collections.emptySet(), extraExports, extraOpens, Collections.emptySet(), Collections.emptyMap());
            SimpleLogger.INSTANCE.info("Exported java.base/jdk.internal.loader to " + my);
        } catch (Exception e) {
            SimpleLogger.INSTANCE.warn(e);
        }
        URL location = Agent.class.getProtectionDomain().getCodeSource().getLocation();
        new AgentBuilder.Default()
                .type(ElementMatchers.nameContains("SourceDistributionResolver")
                        .or(ElementMatchers.named("org.gradle.kotlin.dsl.resolver.SourceDistributionResolver")))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
                    boolean hasMethod = false;
                    try {
                        for (MethodDescription.InDefinedShape md : typeDescription.getDeclaredMethods()) {
                            if ("createSourceRepository".equals(md.getName()) && md.getParameters().isEmpty()) {
                                hasMethod = true;
                                break;
                            }
                        }
                    } catch (Throwable t) {
                        SimpleLogger.INSTANCE.error("[AgentTransform] error while inspecting methods of " + typeDescription.getName() + ": " + t.getMessage());
                    }

                    SimpleLogger.INSTANCE.info("[AgentTransform] Inspecting type: " + typeDescription.getName() + " hasCreateSourceRepository=" + hasMethod);

                    if (hasMethod) {
                        SimpleLogger.INSTANCE.info("[AgentTransform] Installing Advice on " + typeDescription.getName() + "#createSourceRepository()");
                        // 在 transform 时把 Helper 注入到目标 classloader（若非 null）
                        if (classLoader != null) {
                            File file = new File(System.getProperty("java.io.tmpdir"));
                            try {
                                ClassInjector injector = ClassInjector.UsingInstrumentation.of(
                                        file, // 暂存目录
                                        ClassInjector.UsingInstrumentation.Target.SYSTEM,
                                        inst
                                );
                                injectInto(injector, SimpleLogger.class);
                                injectInto(injector, Utils.class);
                            } catch (Exception e) {
                                SimpleLogger.INSTANCE.error(e.getMessage());
                            }
                        }
                        if (classLoader instanceof URLClassLoader) {
                            try {
                                Field ucp = URLClassLoader.class.getDeclaredField("ucp");
                                ucp.setAccessible(true);
                                Object ucpValue = ucp.get(classLoader);
                                Class<?> ucpClass = ucpValue.getClass();
                                Method addURL = ucpClass.getDeclaredMethod("addURL", URL.class);
                                addURL.invoke(ucpValue, location);
                            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                                     InvocationTargetException e) {
                                SimpleLogger.INSTANCE.error(e);
                            }
                        }
                        return builder.visit(Advice.to(AgentAdvice.class)
                                .on(ElementMatchers.named("createSourceRepository").and(ElementMatchers.takesArguments(0))));
                    } else {
                        return builder;
                    }
                })
                .installOn(inst);
        SimpleLogger.INSTANCE.info("CHS-Agent premain() installed");
    }
    public static void injectInto(ClassInjector injector, Class<?> clazz) throws IOException {
        String helperInternal = clazz.getName().replace('.', '/');
        InputStream is = Agent.class.getClassLoader().getResourceAsStream(helperInternal);
        if (is != null) {
            byte[] bytes = new byte[is.available()];
            is.read(bytes);

            TypeDescription td = TypeDescription.ForLoadedType.of(clazz);
            Map<TypeDescription, byte[]> map = new HashMap<>();
            map.put(td, bytes);
            injector.inject(map);
        }
    }
}