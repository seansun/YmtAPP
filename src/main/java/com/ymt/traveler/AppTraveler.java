package com.ymt.traveler;

/**
 * Created by sunsheng on 2017/5/26.
 */
public class AppTraveler {


    public static void main(String... args) {

        Traveler traveler = null;

        try {
            if (args.length == 1) {
                String platForm = args[0].toLowerCase();


                if (platForm.equals("android")) {
                    traveler = new AndroidTraveler();

                } else if (platForm.equals("ios")) {
                    traveler = new IOSTraveler();
                }

            } else {
                System.err.println("参数类型错误,请指定系统: android/ios!");
            }

            boolean result = traveler.start();

            while (result) {

                result = traveler.start();

            }


        } catch (Exception e) {

            e.printStackTrace();

        }


    }

}
