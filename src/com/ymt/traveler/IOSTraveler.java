package com.ymt.traveler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sunsheng on 2017/5/26.
 */
public class IOSTraveler extends Traveler {

    private static final Logger logger = LoggerFactory.getLogger(IOSTraveler.class);

    public IOSTraveler(){

        config.getIosCapability();

    }

    public static void main(String... args) {

    }

}
