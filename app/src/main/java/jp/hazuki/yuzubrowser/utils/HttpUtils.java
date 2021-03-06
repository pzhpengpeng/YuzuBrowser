/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.hazuki.yuzubrowser.settings.data.AppData;

public final class HttpUtils {

    private static final Pattern NAME_UTF_8 = Pattern.compile("filename\\*=UTF-8''(\\S+)");
    private static final Pattern NAME_NORMAL = Pattern.compile("filename=\"(.*)\"");

    public static File getFileName(String url, String defaultExt, Map<String, List<String>> header) {
        if (header.get("Content-Disposition") != null) {
            List<String> lines = header.get("Content-Disposition");
            for (String raw : lines) {
                if (raw != null) {
                    Matcher utf8 = NAME_UTF_8.matcher(raw);
                    if (utf8.find()) { /* RFC 6266 */
                        try {
                            return FileUtils.createUniqueFile(AppData.download_folder.get(), URLDecoder.decode(utf8.group(1), "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            throw new AssertionError("UTF-8 is unknown");
                        }
                    }
                    Matcher normal = NAME_NORMAL.matcher(raw);
                    if (normal.find()) {
                        try {
                            return FileUtils.createUniqueFile(AppData.download_folder.get(), URLDecoder.decode(normal.group(1), "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            throw new AssertionError("UTF-8 is unknown");
                        }
                    }
                }
            }
        }

        if (header.get("Content-Type") != null) {
            List<String> lines = header.get("Content-Type");
            if (lines.size() > 0) {
                String mineType = lines.get(0);
                int index = mineType.indexOf(';');
                if (index > -1) {
                    mineType = mineType.substring(0, index);
                }
                return WebDownloadUtils.guessDownloadFile(AppData.download_folder.get(), url, null, mineType, defaultExt);
            }
        }

        return WebDownloadUtils.guessDownloadFile(AppData.download_folder.get(), url, null, null, defaultExt);
    }
}
