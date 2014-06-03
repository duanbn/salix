package com.salix.core.util;


/**
 * 和字符串相关的工具类.
 *
 * @author duanbn
 * @since 1.1
 */
public class StringUtil
{
    public static final boolean isNotBlank(String v)
    {
        if (v != null && !v.trim().equals("")) {
            return true;
        }

        return false;
    }

    public static final boolean isBlank(String v)
    {
        if (v == null || v.trim().equals("")) {
            return true;
        }

        return false;
    }

    public static final String lowcaseFirst(String str) {
        return str.substring(0,1).toLowerCase()+str.substring(1);
    }
}
