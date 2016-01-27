package org.hsweb.web.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 浩 on 2016-01-27 0027.
 */
public class AccessUtils {
    private static String[] modules;
    private static Map<String, Integer> moduleAccessInfo = new HashMap<>();

    /**
     * 给角色注册模块权限
     *
     * @param role    角色标识
     * @param module  权限标识
     * @param modules 同时注册多个模块权限
     */
    public static final void registerAccessRole(String role, String module, String... modules) {
        Integer accessInfo = moduleAccessInfo.get(role);
        if (accessInfo == null) accessInfo = 0;
        int moduleIndex = indexOf(AccessUtils.modules, module);
        if (moduleIndex == -1) return;
        int access = (moduleIndex + 1) << 1;
        accessInfo = accessInfo | access;
        if (modules.length > 0) {
            for (int i = 0; i < modules.length; i++) {
                moduleIndex = indexOf(AccessUtils.modules, modules[i]);
                if (moduleIndex == -1) continue;
                access = (moduleIndex + 1) << 1;
                accessInfo = accessInfo | access;
            }
        }
        moduleAccessInfo.put(role, accessInfo);
    }

    /**
     * 取消某个角色的权限信息
     *
     * @param role    角色标识
     * @param module  权限标识
     * @param modules 同时取消多个模块权限
     */
    public static final void cancelAccessRole(String role, String module, String... modules) {
        Integer accessInfo = moduleAccessInfo.get(role);
        if (accessInfo == null) accessInfo = 0;
        int moduleIndex = indexOf(AccessUtils.modules, module);
        if (moduleIndex == -1) return;
        int access = (moduleIndex + 1) << 1;
        accessInfo = accessInfo & (~access);
        if (modules.length > 0) {
            for (int i = 0; i < modules.length; i++) {
                moduleIndex = indexOf(AccessUtils.modules, modules[i]);
                if (moduleIndex == -1) continue;
                access = (moduleIndex + 1) << 1;
                accessInfo = accessInfo & (~access);
            }
        }
        moduleAccessInfo.put(role, accessInfo);
    }

    /**
     * 判断某个角色是否持有某个模块的权限
     *
     * @param role   角色
     * @param module
     * @return
     */
    public static final boolean roleAccessModule(String role, String module) {
        Integer accessInfo = moduleAccessInfo.get(role);
        if (accessInfo == null) return false;
        int moduleIndex = indexOf(AccessUtils.modules, module);
        if (moduleIndex == -1) return false;
        int access = (moduleIndex + 1) << 1;
        return (accessInfo & access) == access;
    }

    public static final void initModules(String... modules) {
        AccessUtils.modules = modules;
    }

    private static final int indexOf(String[] arr, String str) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(str)) return i;
        }
        return -1;
    }

    public static void main(String[] args) {
        initModules("admin", "test", "test1", "test2", "test3");

        //给角色注册2个权限
        registerAccessRole("hehe", "admin", "test");

        System.out.println(roleAccessModule("hehe", "admin"));
        System.out.println(roleAccessModule("hehe", "test"));
        //注册test3权限
        registerAccessRole("hehe", "test3");
        System.out.println(roleAccessModule("hehe", "test3"));
        //取消test3权限
        cancelAccessRole("hehe", "test3");
        System.out.println(roleAccessModule("hehe", "test3"));
        //
        System.out.println(roleAccessModule("hehe", "test2"));


    }

}
