/*
 * ============================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2018-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.

 * Module Date: 8/5/2019
 * Module Auth: Fahy.F
 * Description:

 * Revision History:
 * Date                   Author                       Action
 * 8/5/2019              Fahy.F                       Create
 * ============================================================================
 */
package com.pax.poslink.util;

public class IOUtil {

    public static  <T extends java.io.Closeable> void close(T t) {
        try {
            if (t != null) {
                t.close();
            }
        } catch (Exception e) {
            LogStaticWrapper.getLog().exceptionLog(e);
        }
    }
}
