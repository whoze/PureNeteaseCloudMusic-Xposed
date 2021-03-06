package me.zjns.lovecloudmusic;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

final class CloudMusic {
    private String versionName;
    private boolean dontJump;
    private boolean autoSign;
    private boolean convertToPlay;
    private boolean removeAd;
    private boolean removeVideo;
    private boolean removeTopic;
    private boolean removeConcertInfo;
    private boolean zeroPointDLMV;
    private boolean enableVipFeature;
    private boolean removeHomeBannerAd;
    private boolean removeFuncDynamic;
    private boolean removeFuncVideo;
    private boolean removeFuncRadio;
    private boolean hideDot;
    private boolean hideItemVIP;
    private boolean hideItemShop;
    private boolean hideItemGame;
    private boolean hideItemFreeData;
    private boolean hideItemNearby;
    private boolean hideItemTicket;
    private Boolean mIsVipPro = null;
    private static ClassLoader loader;
    private static final boolean DEBUG = false;
    private static XSharedPreferences mSharedPrefs;
    private static CloudMusic mInstance;
    private WeakReference<View> viewRadio = null;

    private CloudMusic() {
    }

    static CloudMusic getInstance() {
        if (mInstance == null) {
            mInstance = new CloudMusic();
        }
        return mInstance;
    }

    void hookHandler(LoadPackageParam lpparam) throws Throwable {
        mSharedPrefs = new XSharedPreferences(HookInit.MODULE_PACKAGE_NAME);
        HookInfo.setClassLoader(lpparam.classLoader);
        loader = lpparam.classLoader;
        versionName = Utils.getPackageVersionName(HookInit.HOOK_PACKAGE_NAME);
        loadPrefs();
        if (!mSharedPrefs.getBoolean("enable_all_functions", true)) return;

        convertToPlayVersion();
        disableSignJumpToSmall();
        removeCommentAd();
        removeBannerAd();
        hideSideBarItems();
        hideUIFuncItems();
        hookVideoPoint();
        hookVIPTheme();
        hookLyricTemplate();

        HookInfo.saveHookInfo();
    }

    private void loadPrefs() {
        mSharedPrefs.reload();
        dontJump = mSharedPrefs.getBoolean("disable_sign_jump_to_small", false);
        autoSign = mSharedPrefs.getBoolean("auto_sign", false);
        convertToPlay = mSharedPrefs.getBoolean("convert_to_play", false);
        removeAd = mSharedPrefs.getBoolean("remove_comment_ad", false);
        removeVideo = mSharedPrefs.getBoolean("remove_comment_video", false);
        removeTopic = mSharedPrefs.getBoolean("remove_comment_topic", false);
        removeConcertInfo = mSharedPrefs.getBoolean("remove_comment_concert_info", false);
        zeroPointDLMV = mSharedPrefs.getBoolean("zero_point_video", false);
        enableVipFeature = mSharedPrefs.getBoolean("enable_vip_feature", false);
        removeHomeBannerAd = mSharedPrefs.getBoolean("remove_home_banner_ad", false);
        removeFuncDynamic = mSharedPrefs.getBoolean("remove_func_dynamic", false);
        removeFuncVideo = mSharedPrefs.getBoolean("remove_func_video", false);
        removeFuncRadio = mSharedPrefs.getBoolean("remove_func_radio", false);
        hideDot = mSharedPrefs.getBoolean("hide_dot", false);
        hideItemVIP = mSharedPrefs.getBoolean("hide_item_vip", false);
        hideItemShop = mSharedPrefs.getBoolean("hide_item_shop", false);
        hideItemGame = mSharedPrefs.getBoolean("hide_item_game", false);
        hideItemFreeData = mSharedPrefs.getBoolean("hide_item_free_data", false);
        hideItemNearby = mSharedPrefs.getBoolean("hide_item_nearby", false);
        hideItemTicket = mSharedPrefs.getBoolean("hide_item_ticket", false);
    }

    @SuppressWarnings("all")
    private String decryptString(String string) {
        try {
            Method c = findClass("a.auu.a", loader).getDeclaredMethod("c", String.class);
            c.setAccessible(true);
            return (String) c.invoke(null, string);
        } catch (Throwable t) {
            return null;
        }
    }

    private void convertToPlayVersion() {
        Method getChannel = HookInfo.getMethodChannel();
        if (getChannel != null) {
            hookMethod(getChannel, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    loadPrefs();
                    if (convertToPlay) {
                        param.setResult("google");
                    }
                }
            });
        }
    }

    private void disableSignJumpToSmall() {
        if (versionName.compareTo("4.1.3") > 0 && versionName.compareTo("4.3.3") < 0) {
            findAndHookMethod("android.app.SharedPreferencesImpl$EditorImpl", loader, "putString",
                    String.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            loadPrefs();
                            if (!dontJump) return;
                            if (param.args[0].toString().equals("siginWebUrl")) {
                                param.args[1] = null;
                            }
                        }
                    });
        } else if (versionName.compareTo("4.3.3") >= 0 && versionName.compareTo("4.3.5") < 0) {
            Method j = Utils.findMethodByExactParameters(findClass("com.netease.cloudmusic.module.a.b", loader), "j", Boolean.TYPE);
            hookMethod(j, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    loadPrefs();
                    if (!dontJump) return;
                    param.setResult(true);
                }
            });
        } else if (versionName.compareTo("4.3.5") >= 0) {
            Method userGroup = HookInfo.getMethodUserGroup();
            if (userGroup != null) {
                hookMethod(userGroup, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        loadPrefs();
                        if (dontJump && param.args[0].toString().equals("yueqian")) {
                            param.setResult("t2");
                        }
                    }
                });
            }
            Method isWeekend = HookInfo.getMethodIsWeekend();
            if (isWeekend != null) {
                hookMethod(isWeekend, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        loadPrefs();
                        if (dontJump) {
                            param.setResult(false);
                        }
                    }
                });
            }
        }
    }

    private boolean shouldRemove(Object obj) {
        int type = (int) callMethod(obj, "getType");
        return type == 5 && removeConcertInfo ||
                type == 6 && removeTopic ||
                type == 9 && removeAd ||
                type == 11 && removeVideo;
    }

    private void removeCommentAd() {
        if (versionName.compareTo("4.2.1") <= 0) return;
        Method getComments = HookInfo.getInnerFragmentMethod("CommentListEntry");
        if (getComments == null) return;
        hookMethod(getComments, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                loadPrefs();
                ArrayList<?> origList = (ArrayList) param.getResult();
                if (origList == null || origList.isEmpty()) return;
                Iterator<?> iterator = origList.iterator();
                while (iterator.hasNext()) {
                    Object entry = iterator.next();
                    if (shouldRemove(entry)) {
                        iterator.remove();
                    }
                }
            }
        });
    }

    private void hookVideoPoint() {
        Class<?> metaMV = findClass("com.netease.cloudmusic.meta.MV", loader);
        findAndHookMethod(metaMV, "isDownloadNeedPoint", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                loadPrefs();
                if (zeroPointDLMV) {
                    param.setResult(false);
                }
            }
        });
        findAndHookMethod(metaMV, "isFreePointCurBitMvDownload", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                loadPrefs();
                if (zeroPointDLMV) {
                    param.setResult(true);
                }
            }
        });
    }

    private void hookVIPTheme() {
        String ThemeDetailActivity = "com.netease.cloudmusic.activity.ThemeDetailActivity";
        String ThemeDetailActivity$1 = ThemeDetailActivity + "$1";
        findAndHookConstructor(ThemeDetailActivity$1, loader,
                ThemeDetailActivity, Integer.TYPE, Boolean.TYPE, Boolean.TYPE, Integer.TYPE, Intent.class, String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        loadPrefs();
                        if (!enableVipFeature || isVipPro()) return;
                        param.args[2] = false;
                        param.args[3] = false;
                    }
                });
    }

    private void hookLyricTemplate() {
        if (versionName.compareTo("4.1.1") < 0) return;
        try {
            Method e = Utils.findMethodByExactParameters(
                    findClass("com.netease.cloudmusic.module.lyrictemplate.d", loader),
                    "e", Boolean.TYPE);
            hookMethod(e, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    loadPrefs();
                    if (enableVipFeature && !isVipPro()) {
                        param.setResult(false);
                    }
                }
            });
        } catch (Throwable t) {
            log(t);
        }
    }

    private void removeBannerAd() {
        Method getBanners = HookInfo.getInnerFragmentMethod("Banner");
        if (getBanners == null) return;
        hookMethod(getBanners, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                loadPrefs();
                if (!removeHomeBannerAd) return;
                List<?> list = (List) param.getResult();
                if (list == null || list.isEmpty()) return;
                Iterator<?> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Object banner = iterator.next();
                    if (DEBUG) log(banner.toString());
                    String typeTitle = callMethod(banner, "getTypeTitle").toString();
                    if ("广告".equals(typeTitle) || "商城".equals(typeTitle)) {
                        iterator.remove();
                    }
                }
            }
        });
    }

    private void hideSideBarItems() {
        findAndHookMethod(TextView.class, "setText", CharSequence.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) return;
                String content = param.args[0].toString();
                if ("优惠券".equals(content)) {
                    loadPrefs();
                    if (!hideItemTicket) return;
                    TextView textView = (TextView) param.thisObject;
                    Class<?> clazz = textView.getParent().getClass().getSuperclass();
                    if (clazz == FrameLayout.class) {
                        ((View) textView.getParent()).setVisibility(View.GONE);
                    }
                } else if ("电台".equals(content)) {
                    TextView textView = (TextView) param.thisObject;
                    Class<?> clazz = textView.getParent().getClass().getSuperclass();
                    if (clazz == LinearLayout.class) {
                        viewRadio = new WeakReference<>((View) textView.getParent());
                    }
                } else if ("VIP会员".equals(content)) {
                    loadPrefs();
                    if (!hideItemVIP) return;
                    TextView textView = (TextView) param.thisObject;
                    Class<?> clazz = textView.getParent().getClass().getSuperclass();
                    if (clazz == FrameLayout.class) {
                        ((View) textView.getParent()).setVisibility(View.GONE);
                    }
                } else if ("商城".equals(content)) {
                    loadPrefs();
                    if (!hideItemShop) return;
                    TextView textView = (TextView) param.thisObject;
                    Class<?> clazz = textView.getParent().getClass().getSuperclass();
                    if (clazz == FrameLayout.class) {
                        ((View) textView.getParent()).setVisibility(View.GONE);
                    }
                } else if (content.contains("游戏推荐")) {
                    loadPrefs();
                    if (!hideItemGame) return;
                    TextView textView = (TextView) param.thisObject;
                    Class<?> clazz = textView.getParent().getClass().getSuperclass();
                    if (clazz == LinearLayout.class) {
                        ViewGroup.LayoutParams params = ((LinearLayout) textView.getParent()).getLayoutParams();
                        params.height = 0;
                    }
                } else if ("在线听歌免流量".equals(content)) {
                    loadPrefs();
                    if (!hideItemFreeData) return;
                    TextView textView = (TextView) param.thisObject;
                    Class<?> clazz = textView.getParent().getClass().getSuperclass();
                    if (clazz == FrameLayout.class) {
                        ViewGroup.LayoutParams params = ((FrameLayout) textView.getParent()).getLayoutParams();
                        params.height = 0;
                    }
                } else if ("附近的人".equals(content)) {
                    loadPrefs();
                    if (!hideItemNearby) return;
                    TextView textView = (TextView) param.thisObject;
                    Class<?> clazz = textView.getParent().getClass().getSuperclass();
                    if (clazz == FrameLayout.class) {
                        ViewGroup.LayoutParams params = ((FrameLayout) textView.getParent()).getLayoutParams();
                        params.height = 0;
                    }
                } else if ("签到".equals(content)) {
                    loadPrefs();
                    if (!dontJump || !autoSign) return;
                    TextView textView = (TextView) param.thisObject;
                    if (textView.getParent().getClass() == LinearLayout.class) {
                        while (!textView.getText().toString().equals("已签到")) {
                            textView.performClick();
                        }
                    }
                }
            }
        });
    }

    private void hideUIFuncItems() {
        findAndHookMethod(View.class, "setContentDescription", CharSequence.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) return;
                String content = param.args[0].toString();
                List<String> items = new ArrayList<>();
                if (removeFuncDynamic) {
                    items.add("我的动态");
                }
                if (removeFuncVideo) {
                    items.add("我的视频");
                }
                if (items.contains(content)) {
                    View view = (View) param.thisObject;
                    view.setVisibility(View.GONE);
                }
            }
        });
        Class<?> MessageBubbleView = findClass("com.netease.cloudmusic.ui.MessageBubbleView", loader);
        findAndHookMethod(View.class, "setVisibility", Integer.TYPE, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (viewRadio != null
                        && param.thisObject == viewRadio.get()) {
                    loadPrefs();
                    if (removeFuncRadio) {
                        param.args[0] = View.GONE;
                    }
                } else if (param.thisObject.getClass() == MessageBubbleView) {
                    loadPrefs();
                    if (hideDot) {
                        param.args[0] = View.GONE;
                    }
                }
            }
        });
    }

    private boolean isVipPro() {
        if (mIsVipPro != null) return mIsVipPro;
        Object profile = HookInfo.getProfile();
        return mIsVipPro = profile != null && (boolean) callMethod(profile, "isVipPro");
    }
}
