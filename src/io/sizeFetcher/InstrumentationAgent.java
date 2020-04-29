package io.sizeFetcher;

import java.lang.instrument.Instrumentation;

public class InstrumentationAgent {
    private static volatile Instrumentation globalInstrumentation;
 
    public static void premain(final String agentArgs, final Instrumentation inst) {
        globalInstrumentation = inst;
    }
 
    public static long getObjectSize(final Object object) {
        if (globalInstrumentation == null) {
            throw new IllegalStateException("Agent not initialized.");
        }
        return globalInstrumentation.getObjectSize(object);
    }

    public static void main(String[] args) {
        System.out.println(getObjectSize(new Double(3)));
    }

    public static void printMemoryOverview(){
        int dataSize = 1024 * 1024;

        System.out.println("Used Memory   : " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/dataSize + " MB");
        System.out.println("Free Memory   : " + Runtime.getRuntime().freeMemory()/dataSize + " MB");
        System.out.println("Total Memory  : " + Runtime.getRuntime().totalMemory()/dataSize + " MB");
        System.out.println("Max Memory    : " + Runtime.getRuntime().maxMemory()/dataSize + " MB");
    }
}