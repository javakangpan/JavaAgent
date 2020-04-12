package demo;



import java.lang.instrument.Instrumentation;
public class AgentMain {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("pre....");
        System.out.println(agentArgs);
    }

}
