package com.alibaba.gov.android.chinaoly.meeting.util;

import android.app.Application;

import java.lang.reflect.InvocationTargetException;

public class AppGlobals {

    private static Application application;

    public static Application get() {
        if (application == null) {
            try {
                application = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        return application;
    }

}
