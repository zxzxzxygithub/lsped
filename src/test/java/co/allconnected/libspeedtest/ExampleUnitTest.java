package co.allconnected.libspeedtest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testFormat() throws Exception {
       float result= (float) (Math.round(-1.23444 * 100)) / 100;
        System.out.println(result+"_");
    }
}