package cn.wzpmc;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;

import java.lang.instrument.Instrumentation;


public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) {
        new AgentBuilder.Default()
                .type(ElementMatchers.nameContains("$createSourceRepository"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(LogAdvice.class).on(ElementMatchers.named("execute")))
                )
                .installOn(inst);
    }

    public static class LogAdvice {
        @Advice.OnMethodExit
        public static void exit(@Advice.AllArguments Object[] arg) {
            if (arg[0] instanceof IvyArtifactRepository o) {
                String originalUrl = o.getUrl().toString();
                if (originalUrl.endsWith("distributions-snapshots")) {
                    System.out.println("[CHS-Agent] 检测到snapshot版本gradle，无法更换源");
                    return;
                }
                if (originalUrl.contains("https://services.gradle.org/distributions")) {
                    String srcBaseUrl = System.getenv("gradle.src.base");
                    if (srcBaseUrl == null) {
                        srcBaseUrl = "https://mirrors.cloud.tencent.com/gradle";
                    }
                    String targetUrl = originalUrl.replace("https://services.gradle.org/distributions", srcBaseUrl);
                    o.setUrl(targetUrl);
                    System.out.println("[CHS-Agent] 成功替换src下载源！");
                    System.out.println("[CHS-Agent] " + originalUrl + " ===>> " + targetUrl);
                }
            }
        }
    }
}