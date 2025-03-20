package com.joonhuiwong.kavitawebview;

public class MainConstants {
    public static final String PREFS_NAME = "KavitaWebViewPrefs";
    public static final String[] PREF_KEYS = {
            "currentUrl", "volumeUpBinding", "volumeDownBinding", "swipeLeftBinding", "swipeRightBinding",
            "swipeUpBinding", "swipeDownBinding", "doubleTapTopBinding", "doubleTapBottomBinding",
            "doubleTapLeftBinding", "doubleTapRightBinding", "gestureDistanceThreshold", "gestureVelocityThreshold",
            "hideStatusBar", "hideNavigationBar" // Replaced fullscreenEnabled with two settings
    };
}