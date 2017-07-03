package com.ymt.traveler;

import org.junit.Test;

import java.util.*;

/**
 * Created by sunsheng on 2017/5/26.
 */
public class TestTraveler {

    @Test
    public void testTraveler() {

        Traveler traveler = new AndroidTraveler();

        boolean result = traveler.start();


/*        while (result) {

            result = traveler.start();

        }*/


    }

    @Test
    public void test(){

        Map<String, String> tmp = new LinkedHashMap<String, String>();

        tmp.put("b", "bbb");
        tmp.put("a", "aaa");
        tmp.put("c", "ccc");
        tmp.put("d", "cdc");
        if (tmp.containsKey("b")){

                tmp.remove("b");
        }
        tmp.put("b", "eeeee");


        Iterator<String> iterator_2 = tmp.keySet().iterator();
        while (iterator_2.hasNext()) {
            Object key = iterator_2.next();
            System.out.println("tmp.get(key) is :" + tmp.get(key));
        }
    }
}
