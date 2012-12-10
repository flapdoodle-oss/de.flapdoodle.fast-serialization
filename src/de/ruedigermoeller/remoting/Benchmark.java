package de.ruedigermoeller.remoting;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 09.12.12
 * Time: 19:39
 * To change this template use File | Settings | File Templates.
 */
public class Benchmark {

    public int test = 13;

    public void setTest(int val) {
        test = val;
    }

    static class Benchmark_setTest {
        public void invoke( Benchmark bm, int v ) {
            bm.setTest(v);
        }
    }

    public static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe)f.get(null);
        } catch (Exception e) { /* ... */ }
        return null;
    }

    public static void main(String arg[]) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Unsafe unsafe = getUnsafe();


        Benchmark bm = new Benchmark();
        Field f = Benchmark.class.getField("test");
        Method m = Benchmark.class.getMethod("setTest",int.class);
        long foff = unsafe.objectFieldOffset(f);
        Benchmark_setTest mm = new Benchmark_setTest();
        long tim = System.currentTimeMillis();
        for ( int i=0; i < 1000000; i++) {
            bm.test = i;
//            bm.setTest(i);
        }
        long dur = System.currentTimeMillis()-tim;
        System.out.println("tim direct "+dur);

        tim = System.currentTimeMillis();
        for ( int i=0; i < 1000000; i++) {
            unsafe.putInt(bm,foff,i);
//            f.setInt(bm, i);
//            m.invoke(bm,i);
//            mm.invoke(bm,i);
        }
        dur = System.currentTimeMillis()-tim;
        System.out.println("tim reflect "+dur);
    }
}
